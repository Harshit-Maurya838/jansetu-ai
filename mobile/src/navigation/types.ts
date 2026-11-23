import type { ModuleKey } from '../constants/roles';

/** Screens before login. Mobile numbers are passed as 10 digits. */
export type AuthStackParamList = {
  Welcome: undefined;
  Login: { notice?: 'passwordReset' } | undefined;
  SignUp: undefined;
  OtpVerify: { mode: 'login'; mobile: string } | { mode: 'register'; mobile: string; fullName: string; password: string };
  ForgotPassword: undefined;
  ResetPassword: { identifier: string };
  Activate: undefined;
};

export type AppStackParamList = {
  Home: undefined;
  Module: { module: ModuleKey };
  Profile: undefined;
  ChangePassword: undefined;
  Sessions: undefined;
};
