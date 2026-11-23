import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore, type SessionUser } from '../../store/authStore';
import { getDeviceId, loadSession, saveSession, wipeSession } from '../auth/secureStore';

// TODO(team): read from build config (react-native-config or similar). 10.0.2.2 = host machine from the Android emulator.
export const API_BASE_URL = 'http://10.0.2.2:8080/api/v1';

/** Mirrors in.jansetu.common.response.ApiResponse – see docs/api-overview.md */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string; fieldErrors?: { field: string; message: string }[] };
  timestamp: string;
}

/** code is the server's error.code (e.g. AUTH_OTP_EXPIRED), or NETWORK when the server could not be reached. */
export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string) {
    super(message);
  }
}

/** Mirrors in.jansetu.auth.dto.AuthResponse. */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: SessionUser;
}

const TIMEOUT_MS = 20_000; // rural 2G/3G links are slow; fail and fall back to the sync queue rather than hang

export const http = axios.create({ baseURL: API_BASE_URL, timeout: TIMEOUT_MS });
/** Separate instance without interceptors, so a failing refresh can never trigger another refresh. */
export const refreshHttp = axios.create({ baseURL: API_BASE_URL, timeout: TIMEOUT_MS });

/** Public auth endpoints answer 401 for bad credentials; that must not trigger a token refresh. */
const PUBLIC_AUTH = /^\/auth\/(login|register|activate|password|token)/;

export function toApiError(e: unknown): ApiError {
  if (e instanceof ApiError) return e;
  const err = e as AxiosError<ApiResponse<unknown>>;
  if (err?.response) {
    const body = err.response.data;
    return new ApiError(err.response.status, body?.error?.code ?? 'HTTP_ERROR', body?.error?.message ?? err.message);
  }
  return new ApiError(0, 'NETWORK', err?.message ?? 'Network error');
}

/** Clears the local session and returns to the login screen. */
export async function endLocalSession(): Promise<void> {
  await wipeSession();
  useAuthStore.getState().set({ status: 'signedOut', user: null });
}

let refreshing: Promise<string> | null = null;

/**
 * Single-flight refresh: concurrent 401s share one /auth/token/refresh call and then retry with the new token.
 * A rejected refresh (401/403) ends the local session; a network failure does not (offline use keeps working).
 */
export function refreshAccessToken(): Promise<string> {
  refreshing ??= (async () => {
    const s = await loadSession();
    if (!s) throw new ApiError(401, 'AUTH_REFRESH_INVALID', 'No session');
    try {
      const res = await refreshHttp.post<ApiResponse<AuthResponse>>('/auth/token/refresh', {
        refreshToken: s.refreshToken,
        deviceId: await getDeviceId(),
      });
      const data = res.data.data!;
      await saveSession({ accessToken: data.accessToken, refreshToken: data.refreshToken, user: data.user });
      useAuthStore.getState().set({ user: data.user });
      return data.accessToken;
    } catch (e) {
      const err = toApiError(e);
      if (err.status === 401 || err.status === 403) await endLocalSession();
      throw err;
    }
  })().finally(() => {
    refreshing = null;
  });
  return refreshing;
}

http.interceptors.request.use(async config => {
  const s = await loadSession();
  if (s && !config.headers.Authorization) config.headers.Authorization = `Bearer ${s.accessToken}`;
  return config;
});

type RetryConfig = InternalAxiosRequestConfig & { _retried?: boolean };

http.interceptors.response.use(undefined, async (error: AxiosError) => {
  const original = error.config as RetryConfig | undefined;
  if (error.response?.status === 401 && original && !original._retried && !PUBLIC_AUTH.test(original.url ?? '')) {
    original._retried = true;
    const token = await refreshAccessToken();
    original.headers.Authorization = `Bearer ${token}`;
    return http(original);
  }
  throw toApiError(error);
});

/** Calls the API and unwraps ApiResponse.data; throws ApiError. */
export async function api<T>(path: string, config: AxiosRequestConfig = {}): Promise<T> {
  try {
    const res = await http.request<ApiResponse<T>>({ url: path, ...config });
    return res.data.data as T;
  } catch (e) {
    throw toApiError(e);
  }
}
