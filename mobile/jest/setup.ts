// Native modules are not available under Jest.
jest.mock('react-native-keychain', () => require('./keychainMock'));
jest.mock('react-native-get-random-values', () => {
  const { webcrypto } = require('crypto');
  (globalThis as any).crypto = webcrypto;
});
