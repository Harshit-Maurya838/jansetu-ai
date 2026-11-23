import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, Field, Message, OfflineNotice, Screen, Title } from '../../components/ui';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { requestRegistrationOtp } from '../../services/auth/authApi';
import { mobileError, normalizeMobile, passwordError } from '../../utils/validation';

type Props = NativeStackScreenProps<AuthStackParamList, 'SignUp'>;

/** Parent / guardian self-registration. Staff accounts are created by an admin. */
export default function SignUpScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const [fullName, setFullName] = useState('');
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { busy, error, run } = useSubmit();

  const errors = {
    fullName: fullName.trim() ? null : 'validation.required',
    mobile: mobileError(mobile),
    password: passwordError(password),
    confirm: confirm === password ? null : 'validation.passwordMismatch',
  };
  const show = (k: keyof typeof errors) => (submitted && errors[k] ? t(errors[k]!) : null);

  const submit = () => {
    setSubmitted(true);
    if (Object.values(errors).some(Boolean)) return;
    const m = normalizeMobile(mobile);
    run(() => requestRegistrationOtp(m)).then(
      ok => ok && navigation.navigate('OtpVerify', { mode: 'register', mobile: m, fullName: fullName.trim(), password }),
    );
  };

  return (
    <Screen>
      <Title>{t('signUp.title')}</Title>
      <OfflineNotice />
      <Message tone="info" text={t('signUp.staffNote')} />
      <Field label={t('signUp.fullName')} value={fullName} onChangeText={setFullName} autoComplete="name" error={show('fullName')} />
      <Field
        label={t('login.mobile')}
        keyboardType="phone-pad"
        maxLength={14}
        value={mobile}
        onChangeText={setMobile}
        error={show('mobile')}
      />
      <Field
        label={t('login.password')}
        secureTextEntry
        value={password}
        onChangeText={setPassword}
        error={show('password')}
        placeholder={t('validation.passwordPolicy')}
      />
      <Field label={t('password.confirm')} secureTextEntry value={confirm} onChangeText={setConfirm} error={show('confirm')} />
      <Message text={error} />
      <BigButton title={t('login.sendOtp')} onPress={submit} busy={busy} />
    </Screen>
  );
}
