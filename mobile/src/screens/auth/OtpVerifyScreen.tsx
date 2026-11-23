import React, { useState } from 'react';
import { Text } from 'react-native';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, CodeInput, LinkButton, Message, OfflineNotice, Screen, Title } from '../../components/ui';
import { useCountdown } from '../../hooks/useCountdown';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { requestLoginOtp, requestRegistrationOtp, verifyLoginOtp, verifyRegistration } from '../../services/auth/authApi';
import { otpError } from '../../utils/validation';

type Props = NativeStackScreenProps<AuthStackParamList, 'OtpVerify'>;

export const RESEND_SECONDS = 60; // server cooldown

/** 6-box OTP entry for OTP login and parent signup. A successful verify switches the app to the signed-in flow. */
export default function OtpVerifyScreen({ route }: Props) {
  const { t } = useTranslation();
  const p = route.params;
  const [otp, setOtp] = useState('');
  const { left, restart } = useCountdown(RESEND_SECONDS);
  const { busy, error, setError, run } = useSubmit();
  const [resent, setResent] = useState(false);

  const verify = () => {
    if (otpError(otp)) return setError(t('validation.otp'));
    run(() =>
      p.mode === 'login' ? verifyLoginOtp(p.mobile, otp) : verifyRegistration(p.mobile, otp, p.fullName, p.password),
    );
  };

  const resend = () =>
    run(() => (p.mode === 'login' ? requestLoginOtp(p.mobile) : requestRegistrationOtp(p.mobile))).then(ok => {
      if (ok) {
        restart();
        setResent(true);
      }
    });

  return (
    <Screen>
      <Title>{t('otp.title')}</Title>
      <OfflineNotice />
      <Text>{t('otp.sentTo', { mobile: `+91 ${p.mobile}` })}</Text>
      <CodeInput value={otp} onChange={setOtp} label={t('login.otp')} />
      <Message text={error} />
      {resent && !error ? <Message tone="success" text={t('otp.resent')} /> : null}
      <BigButton title={t('login.verify')} onPress={verify} busy={busy} disabled={otp.length !== 6} />
      {left > 0 ? (
        <Message tone="info" text={t('otp.resendIn', { seconds: left })} />
      ) : (
        <LinkButton title={t('otp.resend')} onPress={resend} />
      )}
    </Screen>
  );
}
