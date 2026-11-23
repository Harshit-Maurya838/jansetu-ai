import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, CodeInput, Field, LinkButton, Message, OfflineNotice, Screen, Title } from '../../components/ui';
import { useCountdown } from '../../hooks/useCountdown';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { activate, requestActivationOtp } from '../../services/auth/authApi';
import { mobileError, normalizeMobile, otpError, passwordError } from '../../utils/validation';
import { RESEND_SECONDS } from './OtpVerifyScreen';

type Props = NativeStackScreenProps<AuthStackParamList, 'Activate'>;

/** Staff created by an admin: OTP to their mobile, then they choose their own password. */
export default function ActivateScreen(_: Props) {
  const { t } = useTranslation();
  const [step, setStep] = useState<'mobile' | 'code'>('mobile');
  const [mobile, setMobile] = useState('');
  const [otp, setOtp] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { left, restart } = useCountdown(RESEND_SECONDS);
  const { busy, error, run } = useSubmit();

  const m = normalizeMobile(mobile);
  const sendOtp = () => {
    setSubmitted(true);
    if (mobileError(mobile)) return;
    run(() => requestActivationOtp(m)).then(ok => {
      if (ok) {
        setStep('code');
        setSubmitted(false);
        restart();
      }
    });
  };

  const errors = {
    otp: otpError(otp),
    password: passwordError(password),
    confirm: confirm === password ? null : 'validation.passwordMismatch',
  };
  const show = (k: keyof typeof errors) => (submitted && errors[k] ? t(errors[k]!) : null);
  const submit = () => {
    setSubmitted(true);
    if (Object.values(errors).some(Boolean)) return;
    run(() => activate(m, otp, password));
  };

  return (
    <Screen>
      <Title>{t('activate.title')}</Title>
      <OfflineNotice />
      {step === 'mobile' ? (
        <>
          <Message tone="info" text={t('activate.intro')} />
          <Field
            label={t('login.mobile')}
            keyboardType="phone-pad"
            maxLength={14}
            value={mobile}
            onChangeText={setMobile}
            error={submitted && mobileError(mobile) ? t('validation.mobile') : null}
          />
          <Message text={error} />
          <BigButton title={t('login.sendOtp')} onPress={sendOtp} busy={busy} />
        </>
      ) : (
        <>
          <CodeInput value={otp} onChange={setOtp} label={t('login.otp')} />
          {show('otp') ? <Message text={show('otp')} /> : null}
          <Field label={t('password.new')} secureTextEntry value={password} onChangeText={setPassword} error={show('password')} placeholder={t('validation.passwordPolicy')} />
          <Field label={t('password.confirm')} secureTextEntry value={confirm} onChangeText={setConfirm} error={show('confirm')} />
          <Message text={error} />
          <BigButton title={t('activate.submit')} onPress={submit} busy={busy} />
          {left > 0 ? <Message tone="info" text={t('otp.resendIn', { seconds: left })} /> : <LinkButton title={t('otp.resend')} onPress={sendOtp} />}
        </>
      )}
    </Screen>
  );
}
