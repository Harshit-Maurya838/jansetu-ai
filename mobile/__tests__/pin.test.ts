import { store } from '../jest/keychainMock';
import { checkPin, hasPin, loadSession, saveSession, setPin } from '../src/services/auth/secureStore';
import type { SessionUser } from '../src/store/authStore';


const user = { id: 'u1', fullName: 'Asha', role: 'ANGANWADI_WORKER', permissions: [] } as unknown as SessionUser;

beforeEach(async () => {
  store.clear();
  await saveSession({ accessToken: 'a', refreshToken: 'r', user });
});

test('PIN is stored only as a salted hash', async () => {
  await setPin('1234');
  const raw = store.get('in.jansetu.pin')!;
  expect(raw).not.toContain('1234');
  expect(JSON.parse(raw)).toEqual(expect.objectContaining({ salt: expect.any(String), hash: expect.stringMatching(/^[0-9a-f]{64}$/) }));
  expect(await checkPin('1234')).toBe('ok');
});

test('five wrong PINs wipe the session; a correct PIN resets the count', async () => {
  await setPin('1234');
  expect(await checkPin('0000')).toBe(4);
  expect(await checkPin('1234')).toBe('ok');
  for (const left of [4, 3, 2, 1]) expect(await checkPin('0000')).toBe(left);
  expect(await checkPin('0000')).toBe('wiped');
  expect(await hasPin()).toBe(false);
  expect(await loadSession()).toBeNull();
  expect(store.has('in.jansetu.session')).toBe(false);
});
