import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, CodeInput, Field, LinkButton, Message, OfflineNotice, Screen, Title } from '../../components/ui';
import { useCountdown } from '../../hooks/useCountdown';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { forgotPassword, resetPassword } from '../../services/auth/authApi';
import { otpError, passwordError } from '../../utils/validation';
import { RESEND_SECONDS } from './OtpVerifyScreen';

type Props = NativeStackScreenProps<AuthStackParamList, 'ResetPassword'>;

/** OTP + new password. The server ends every session of the account. */
export default function ResetPasswordScreen({ navigation, route }: Props) {
  const { t } = useTranslation();
  const { identifier } = route.params;
  const [otp, setOtp] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { left, restart } = useCountdown(RESEND_SECONDS);
  const { busy, error, run } = useSubmit();

  const errors = {
    otp: otpError(otp),
    password: passwordError(password),
    confirm: confirm === password ? null : 'validation.passwordMismatch',
  };
  const show = (k: keyof typeof errors) => (submitted && errors[k] ? t(errors[k]!) : null);

  const submit = () => {
    setSubmitted(true);
    if (Object.values(errors).some(Boolean)) return;
    run(() => resetPassword(identifier, otp, password)).then(
      ok => ok && navigation.reset({ index: 0, routes: [{ name: 'Login', params: { notice: 'passwordReset' } }] }),
    );
  };

  return (
    <Screen>
      <Title>{t('reset.title')}</Title>
      <OfflineNotice />
      <Message tone="info" text={t('otp.sentTo', { mobile: identifier })} />
      <CodeInput value={otp} onChange={setOtp} label={t('login.otp')} />
      {show('otp') ? <Message text={show('otp')} /> : null}
      <Field label={t('password.new')} secureTextEntry value={password} onChangeText={setPassword} error={show('password')} placeholder={t('validation.passwordPolicy')} />
      <Field label={t('password.confirm')} secureTextEntry value={confirm} onChangeText={setConfirm} error={show('confirm')} />
      <Message text={error} />
      <BigButton title={t('reset.submit')} onPress={submit} busy={busy} />
      {left > 0 ? (
        <Message tone="info" text={t('otp.resendIn', { seconds: left })} />
      ) : (
        <LinkButton title={t('otp.resend')} onPress={() => run(() => forgotPassword(identifier)).then(ok => ok && restart())} />
      )}
    </Screen>
  );
}
