/** In-memory react-native-keychain for tests (wired in jest/setup.ts). */
export const store = new Map<string, string>();

export const setGenericPassword = async (username: string, password: string, opts: { service: string }) => {
  store.set(opts.service, password);
  return { service: opts.service, storage: 'mock' };
};

export const getGenericPassword = async (opts: { service: string }) =>
  store.has(opts.service) ? { username: 'jansetu', password: store.get(opts.service)!, service: opts.service } : false;

export const resetGenericPassword = async (opts: { service: string }) => store.delete(opts.service);
