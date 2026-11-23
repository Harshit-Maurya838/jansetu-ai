import { Platform } from 'react-native';
import i18n from '../../i18n';
import { useAuthStore, type SessionUser } from '../../store/authStore';
import { api, endLocalSession, type AuthResponse } from '../api/client';
import { getDeviceId, hasPin, loadPrefs, loadSession, savePrefs, saveSession, saveUser, setPin } from './secureStore';

/** Calls for docs/modules/01-authentication.md#api-endpoints plus the local session lifecycle. */

export interface SessionInfo {
  id: string;
  deviceId: string;
  deviceName?: string | null;
  lastRefreshedAt: string;
  expiresAt: string;
  current: boolean;
}

const post = <T>(path: string, data?: unknown) => api<T>(path, { method: 'POST', data });
const deviceName = () => `${Platform.OS === 'ios' ? 'iOS' : 'Android'} ${Platform.Version}`;
const device = async () => ({ deviceId: await getDeviceId(), deviceName: deviceName() });

/** On app launch: restore language and session. A stored session opens behind the PIN, even offline. */
export async function bootstrap(): Promise<void> {
  const prefs = await loadPrefs();
  if (prefs.language) await i18n.changeLanguage(prefs.language);
  const s = await loadSession();
  const store = useAuthStore.getState();
  if (!s || s.refreshExpiresAt < Date.now()) {
    if (s) await endLocalSession();
    store.set({ status: 'signedOut', user: null, languageChosen: !!prefs.language });
    return;
  }
  store.set({ user: s.user, status: (await hasPin()) ? 'locked' : 'needsPin', languageChosen: true });
}

export async function chooseLanguage(language: 'en' | 'hi'): Promise<void> {
  await i18n.changeLanguage(language);
  await savePrefs({ language });
  useAuthStore.getState().set({ languageChosen: true });
}

async function startSession(res: AuthResponse): Promise<void> {
  await saveSession({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
  useAuthStore.getState().set({ user: res.user, status: (await hasPin()) ? 'signedIn' : 'needsPin' });
}

// ---- public flows ------------------------------------------------------------------------------

export const login = async (identifier: string, password: string) =>
  startSession(await post<AuthResponse>('/auth/login', { identifier, password, ...(await device()) }));

export const requestLoginOtp = (mobile: string) => post('/auth/login/otp/request', { mobile });

export const verifyLoginOtp = async (mobile: string, otp: string) =>
  startSession(await post<AuthResponse>('/auth/login/otp/verify', { mobile, otp, ...(await device()) }));

export const requestRegistrationOtp = (mobile: string) => post('/auth/register/request-otp', { mobile });

export const verifyRegistration = async (mobile: string, otp: string, fullName: string, password: string) =>
  startSession(
    await post<AuthResponse>('/auth/register/verify', {
      mobile,
      otp,
      fullName,
      password,
      preferredLanguage: i18n.language === 'en' ? 'en' : 'hi',
      ...(await device()),
    }),
  );

export const requestActivationOtp = (mobile: string) => post('/auth/activate/request-otp', { mobile });

export const activate = async (mobile: string, otp: string, newPassword: string) =>
  startSession(await post<AuthResponse>('/auth/activate', { mobile, otp, newPassword, ...(await device()) }));

export const forgotPassword = (identifier: string) => post('/auth/password/forgot', { identifier });

export const resetPassword = (identifier: string, otp: string, newPassword: string) =>
  post('/auth/password/reset', { identifier, otp, newPassword });

// ---- PIN ---------------------------------------------------------------------------------------

export async function createPin(pin: string): Promise<void> {
  await setPin(pin);
  useAuthStore.getState().set({ status: 'signedIn' });
}

/** After unlocking: refresh the cached profile (role / permissions may have changed). Ignored when offline. */
export async function syncProfile(): Promise<void> {
  try {
    const user = await api<SessionUser>('/users/me');
    await saveUser(user);
    useAuthStore.getState().set({ user });
  } catch {
    // offline or session ended (the client already handled a rejected refresh)
  }
}

// ---- signed-in ---------------------------------------------------------------------------------

export const getSessions = () => api<SessionInfo[]>('/auth/sessions');

export const revokeSession = (id: string) => api<void>(`/auth/sessions/${id}`, { method: 'DELETE' });

export async function updateProfile(changes: Partial<Pick<SessionUser, 'fullName' | 'email' | 'preferredLanguage'>>) {
  const user = await api<SessionUser>('/users/me', { method: 'PATCH', data: changes });
  await saveUser(user);
  useAuthStore.getState().set({ user });
  if (changes.preferredLanguage) await chooseLanguage(changes.preferredLanguage);
}

export const changePassword = (currentPassword: string, newPassword: string) =>
  post('/users/me/password', { currentPassword, newPassword });

/** This device: tells the server when reachable, and always clears the local session. */
export async function logout(): Promise<void> {
  try {
    await post('/auth/logout');
  } catch {
    // shortcut: offline logout only wipes this phone; the server session expires on its own (<= 30 days).
  }
  await endLocalSession();
}

/** Every device: needs the server, so an error (e.g. offline) is thrown and nothing is cleared. */
export async function logoutAll(): Promise<void> {
  await post('/auth/logout-all');
  await endLocalSession();
}
