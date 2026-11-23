import { MODULE_DOC, visibleModules } from '../src/constants/roles';

test('keeps only known module keys from the server, without duplicates', () => {
  expect(visibleModules(['child', 'future-module', 'child', 'growth'])).toEqual(['child', 'growth']);
  for (const m of visibleModules(Object.keys(MODULE_DOC))) expect(MODULE_DOC[m]).toBeDefined();
});

test('SOS tile is always first', () => {
  expect(visibleModules(['elderly', 'welfare', 'sos'])[0]).toBe('sos');
});

test('no permissions means no tiles', () => {
  expect(visibleModules([])).toEqual([]);
});
