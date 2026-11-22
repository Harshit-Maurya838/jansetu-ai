import * as Keychain from 'react-native-keychain';
import { create } from 'zustand';
import type { Role } from '../constants/roles';

const TOKEN_SERVICE = 'in.jansetu.tokens';

export interface SessionUser {
  id: string;
  fullName: string;
  role: Role;
  centreId?: string;
  ngoId?: string;
  preferredLanguage: 'en' | 'hi';
}

interface AuthState {
  user: SessionUser | null;
  setUser: (u: SessionUser | null) => void;
}

/** Only non-secret profile data lives in memory/state; tokens live in the Android Keystore via Keychain. */
export const useAuthStore = create<AuthState>(set => ({
  user: null,
  setUser: user => set({ user }),
}));

export async function saveTokens(accessToken: string, refreshToken: string): Promise<void> {
  await Keychain.setGenericPassword('jansetu', JSON.stringify({ accessToken, refreshToken }), {
    service: TOKEN_SERVICE,
  });
}

export async function getAccessToken(): Promise<string | null> {
  const creds = await Keychain.getGenericPassword({ service: TOKEN_SERVICE });
  return creds ? (JSON.parse(creds.password).accessToken as string) : null;
}

export async function clearSession(): Promise<void> {
  await Keychain.resetGenericPassword({ service: TOKEN_SERVICE });
  useAuthStore.getState().setUser(null);
  // TODO(team): decide whether logout wipes the local SQLite DB (shared phones) – see docs/modules/01-authentication.md
}
