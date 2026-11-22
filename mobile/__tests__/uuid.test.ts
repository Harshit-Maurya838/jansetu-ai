jest.mock('react-native-get-random-values', () => {
  const { webcrypto } = require('crypto');
  (globalThis as any).crypto = webcrypto;
});
import { uuidv4 } from '../src/utils/uuid';

test('uuidv4 is RFC 4122 v4 and unique', () => {
  const ids = new Set(Array.from({ length: 1000 }, uuidv4));
  expect(ids.size).toBe(1000);
  for (const id of ids) expect(id).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/);
});
