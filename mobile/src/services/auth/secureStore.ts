import * as Keychain from 'react-native-keychain';
import type { SessionUser } from '../../store/authStore';
import { sha256Hex } from '../../utils/sha256';
import { uuidv4 } from '../../utils/uuid';

/** Everything secret lives in the Android Keystore / iOS Keychain via react-native-keychain. Never AsyncStorage. */
const SESSION = 'in.jansetu.session';
const DEVICE = 'in.jansetu.device';
const PIN = 'in.jansetu.pin';
const PREFS = 'in.jansetu.prefs';

export const REFRESH_TTL_MS = 30 * 24 * 60 * 60 * 1000; // server refresh-token lifetime (JWT_REFRESH_TTL)
export const MAX_PIN_ATTEMPTS = 5;

export interface StoredSession {
  accessToken: string;
  refreshToken: string;
  /** Local estimate; the server is the authority. Past this the app needs an online login. */
  refreshExpiresAt: number;
  user: SessionUser;
}

async function read<T>(service: string): Promise<T | null> {
  const creds = await Keychain.getGenericPassword({ service });
  return creds ? (JSON.parse(creds.password) as T) : null;
}

const write = (service: string, value: unknown) =>
  Keychain.setGenericPassword('jansetu', JSON.stringify(value), { service });

// ---- session -----------------------------------------------------------------------------------

let cached: StoredSession | null | undefined; // undefined = not loaded yet

export async function loadSession(): Promise<StoredSession | null> {
  if (cached === undefined) cached = await read<StoredSession>(SESSION);
  return cached;
}

export async function saveSession(s: Omit<StoredSession, 'refreshExpiresAt'>): Promise<StoredSession> {
  cached = { ...s, refreshExpiresAt: Date.now() + REFRESH_TTL_MS };
  await write(SESSION, cached);
  return cached;
}

export async function saveUser(user: SessionUser): Promise<void> {
  const s = await loadSession();
  if (!s) return;
  cached = { ...s, user };
  await write(SESSION, cached);
}

/** Removes tokens, profile and PIN. The device id and language stay. */
export async function wipeSession(): Promise<void> {
  cached = null;
  await Keychain.resetGenericPassword({ service: SESSION });
  await Keychain.resetGenericPassword({ service: PIN });
}

// ---- device id ---------------------------------------------------------------------------------

let deviceId: string | null = null;

/** Stable per install: generated on first launch, kept in the Keystore. */
export async function getDeviceId(): Promise<string> {
  if (deviceId) return deviceId;
  deviceId = await read<string>(DEVICE);
  if (!deviceId) {
    deviceId = uuidv4();
    await write(DEVICE, deviceId);
  }
  return deviceId;
}

// ---- app PIN -----------------------------------------------------------------------------------

interface StoredPin {
  salt: string;
  hash: string;
  failed: number;
}

const hashPin = (salt: string, pin: string) => sha256Hex(`${salt}:${pin}`);

export async function hasPin(): Promise<boolean> {
  return (await read<StoredPin>(PIN)) !== null;
}

export async function setPin(pin: string): Promise<void> {
  const salt = uuidv4();
  await write(PIN, { salt, hash: hashPin(salt, pin), failed: 0 } satisfies StoredPin);
}

/** 'ok', the attempts left, or 'wiped' after the 5th wrong PIN (session removed; online login required). */
export async function checkPin(pin: string): Promise<'ok' | 'wiped' | number> {
  const stored = await read<StoredPin>(PIN);
  if (!stored) return 'wiped';
  if (hashPin(stored.salt, pin) === stored.hash) {
    if (stored.failed) await write(PIN, { ...stored, failed: 0 });
    return 'ok';
  }
  const failed = stored.failed + 1;
  if (failed >= MAX_PIN_ATTEMPTS) {
    await wipeSession();
    return 'wiped';
  }
  await write(PIN, { ...stored, failed }); // persisted, so restarting the app does not reset the count
  return MAX_PIN_ATTEMPTS - failed;
}

// ---- preferences (not secret, but avoids adding AsyncStorage) ----------------------------------

export interface Prefs {
  language?: 'en' | 'hi';
}

export const loadPrefs = async (): Promise<Prefs> => (await read<Prefs>(PREFS)) ?? {};

export const savePrefs = (p: Prefs) => write(PREFS, p);
