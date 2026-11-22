import { MODULE_ACCESS, MODULE_DOC, ROLES, canSee } from '../src/constants/roles';

test('every role has an access list of known modules', () => {
  for (const role of ROLES) {
    for (const m of MODULE_ACCESS[role]) expect(MODULE_DOC[m]).toBeDefined();
  }
});

test('parents cannot see elderly, admin or meal modules', () => {
  expect(canSee('PARENT_GUARDIAN', 'elderly')).toBe(false);
  expect(canSee('PARENT_GUARDIAN', 'admin')).toBe(false);
  expect(canSee('PARENT_GUARDIAN', 'meal')).toBe(false);
});

test('SOS is visible to NGO field staff', () => {
  expect(canSee('NGO_WORKER', 'sos')).toBe(true);
  expect(canSee('NGO_ADMIN', 'sos')).toBe(true);
});
