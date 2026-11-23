import { create } from 'zustand';
import type { Role } from '../constants/roles';

/** Mirrors in.jansetu.auth.dto.UserDto. */
export interface SessionUser {
  id: string;
  fullName: string;
  mobile: string;
  email?: string | null;
  role: Role;
  status: 'PENDING_ACTIVATION' | 'ACTIVE' | 'LOCKED' | 'DISABLED';
  permissions: string[];
  preferredLanguage: 'en' | 'hi';
  centreId?: string | null;
  ngoId?: string | null;
}

/**
 * loading   – reading Keychain at launch
 * signedOut – login / signup screens
 * needsPin  – just logged in, must set the 4-digit app PIN
 * locked    – session exists (maybe offline), PIN required to open
 * signedIn  – home
 */
export type AuthStatus = 'loading' | 'signedOut' | 'needsPin' | 'locked' | 'signedIn';

interface AuthState {
  status: AuthStatus;
  user: SessionUser | null;
  languageChosen: boolean;
  set: (partial: Partial<Omit<AuthState, 'set'>>) => void;
}

/** In-memory UI state only. Tokens, profile and PIN hash live in the Keystore (services/auth/secureStore.ts). */
export const useAuthStore = create<AuthState>(set => ({
  status: 'loading',
  user: null,
  languageChosen: false,
  set: partial => set(partial),
}));
