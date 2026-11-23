import { utf8Bytes } from './sha256';

/** Form rules shared by the auth screens. Return an i18n key under `validation.*`, or null when valid. */

/** Accepts "98765 43210", "+919876543210", "09876543210"; returns the 10 digits. */
export function normalizeMobile(input: string): string {
  const digits = input.replace(/\D/g, '');
  if (digits.length === 12 && digits.startsWith('91')) return digits.slice(2);
  if (digits.length === 11 && digits.startsWith('0')) return digits.slice(1);
  return digits;
}

export const isValidMobile = (input: string): boolean => /^[6-9]\d{9}$/.test(normalizeMobile(input));

export const isEmail = (input: string): boolean => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.trim());

/** Mirrors the server PasswordPolicy: 8+ chars, a letter and a digit, at most 72 bytes. */
export function passwordError(pw: string): string | null {
  if (pw.length < 8 || !/\p{L}/u.test(pw) || !/\d/.test(pw)) return 'validation.passwordPolicy';
  if (utf8Bytes(pw).length > 72) return 'validation.passwordTooLong';
  return null;
}

export const mobileError = (input: string): string | null => (isValidMobile(input) ? null : 'validation.mobile');

/** Login identifier: a mobile number or an email address. */
export const identifierError = (input: string): string | null =>
  isEmail(input) || isValidMobile(input) ? null : 'validation.identifier';

export const otpError = (otp: string): string | null => (/^\d{6}$/.test(otp) ? null : 'validation.otp');

export const pinError = (pin: string): string | null => (/^\d{4}$/.test(pin) ? null : 'validation.pin');

/** Server expects the identifier as typed for email, or a normalised mobile. */
export const toIdentifier = (input: string): string => (isEmail(input) ? input.trim() : normalizeMobile(input));
