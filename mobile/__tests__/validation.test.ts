import { identifierError, isValidMobile, normalizeMobile, passwordError, toIdentifier } from '../src/utils/validation';

test('normalises Indian mobile numbers', () => {
  expect(normalizeMobile('+91 98765 43210')).toBe('9876543210');
  expect(normalizeMobile('09876543210')).toBe('9876543210');
  expect(isValidMobile('9876543210')).toBe(true);
  expect(isValidMobile('5876543210')).toBe(false); // must start with 6-9
  expect(isValidMobile('98765')).toBe(false);
});

test('password policy matches the server', () => {
  expect(passwordError('abcdefg1')).toBeNull();
  expect(passwordError('पासवर्डabc1')).toBeNull();
  expect(passwordError('abcdefgh')).toBe('validation.passwordPolicy');
  expect(passwordError('12345678')).toBe('validation.passwordPolicy');
  expect(passwordError('ab1')).toBe('validation.passwordPolicy');
  expect(passwordError('a1' + 'x'.repeat(71))).toBe('validation.passwordTooLong');
});

test('login identifier is a mobile or an email', () => {
  expect(identifierError('asha@example.org')).toBeNull();
  expect(identifierError('9876543210')).toBeNull();
  expect(identifierError('asha')).toBe('validation.identifier');
  expect(toIdentifier('+91 98765-43210')).toBe('9876543210');
  expect(toIdentifier(' Asha@Example.org ')).toBe('Asha@Example.org');
});
