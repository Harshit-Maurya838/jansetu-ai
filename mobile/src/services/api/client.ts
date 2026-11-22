import { getAccessToken } from '../../store/authStore';

// TODO(team): read from build config (react-native-config or similar). 10.0.2.2 = host machine from the Android emulator.
export const API_BASE_URL = 'http://10.0.2.2:8080/api/v1';

/** Mirrors in.jansetu.common.response.ApiResponse – see docs/api-overview.md */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string; fieldErrors?: { field: string; message: string }[] };
  timestamp: string;
}

export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string) {
    super(message);
  }
}

const TIMEOUT_MS = 20_000; // rural 2G/3G links are slow; fail and fall back to the sync queue rather than hang

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  const token = await getAccessToken();
  try {
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...init.headers,
      },
    });
    const body = (await res.json().catch(() => null)) as ApiResponse<T> | null;
    if (!res.ok || !body?.success) {
      // TODO(team): on 401 try /auth/refresh once, then force re-login.
      throw new ApiError(res.status, body?.error?.code ?? 'HTTP_ERROR', body?.error?.message ?? res.statusText);
    }
    return body.data as T;
  } finally {
    clearTimeout(timer);
  }
}
