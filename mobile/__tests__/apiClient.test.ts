import { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { store } from '../jest/keychainMock';
import { ApiError, api, http, refreshHttp } from '../src/services/api/client';
import { loadSession, saveSession } from '../src/services/auth/secureStore';
import { useAuthStore, type SessionUser } from '../src/store/authStore';


const user = { id: 'u1', fullName: 'Asha', role: 'ANGANWADI_WORKER', permissions: ['child'] } as unknown as SessionUser;

function respond(config: InternalAxiosRequestConfig, status: number, data: unknown): Promise<AxiosResponse> {
  const res = { data, status, statusText: '', headers: {}, config } as AxiosResponse;
  if (status >= 400) return Promise.reject(new AxiosError('fail', 'ERR_BAD_REQUEST', config, null, res));
  return Promise.resolve(res);
}

let refreshCalls = 0;

beforeEach(async () => {
  store.clear();
  refreshCalls = 0;
  await saveSession({ accessToken: 'expired', refreshToken: 'r1', user });
  useAuthStore.getState().set({ status: 'signedIn', user });
  // API: only the fresh token works
  http.defaults.adapter = config =>
    config.headers.Authorization === 'Bearer fresh'
      ? respond(config, 200, { success: true, data: config.url })
      : respond(config, 401, { success: false, error: { code: 'UNAUTHORIZED', message: '' } });
});

const refreshOk = (config: InternalAxiosRequestConfig) => {
  refreshCalls++;
  return new Promise<AxiosResponse>(resolve =>
    setTimeout(() => resolve(respond(config, 200, { success: true, data: { accessToken: 'fresh', refreshToken: 'r2', expiresIn: 900, user } })), 10),
  );
};

test('parallel 401s share one refresh, then retry with the new token', async () => {
  refreshHttp.defaults.adapter = refreshOk;
  const results = await Promise.all([api('/a'), api('/b'), api('/c')]);
  expect(results).toEqual(['/a', '/b', '/c']);
  expect(refreshCalls).toBe(1);
  expect((await loadSession())?.refreshToken).toBe('r2');
});

test('a rejected refresh ends the local session', async () => {
  refreshHttp.defaults.adapter = config =>
    respond(config, 401, { success: false, error: { code: 'AUTH_TOKEN_REUSED', message: '' } });
  await expect(api('/a')).rejects.toMatchObject({ status: 401, code: 'AUTH_TOKEN_REUSED' });
  expect(useAuthStore.getState().status).toBe('signedOut');
  expect(await loadSession()).toBeNull();
});

test('a refresh that fails offline keeps the session', async () => {
  refreshHttp.defaults.adapter = config => Promise.reject(new AxiosError('Network Error', 'ERR_NETWORK', config));
  await expect(api('/a')).rejects.toMatchObject({ code: 'NETWORK' });
  expect(useAuthStore.getState().status).toBe('signedIn');
  expect(await loadSession()).not.toBeNull();
});

test('401 from a public auth endpoint is returned as-is, without refreshing', async () => {
  refreshHttp.defaults.adapter = refreshOk;
  http.defaults.adapter = config =>
    respond(config, 401, { success: false, error: { code: 'AUTH_INVALID_CREDENTIALS', message: 'bad' } });
  const err = (await api('/auth/login', { method: 'POST' }).catch(e => e)) as ApiError;
  expect(err).toBeInstanceOf(ApiError);
  expect(err.code).toBe('AUTH_INVALID_CREDENTIALS');
  expect(refreshCalls).toBe(0);
});
