import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, Field, LinkButton, Message, OfflineNotice, Screen, Tabs, Title } from '../../components/ui';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { login, requestLoginOtp } from '../../services/auth/authApi';
import { identifierError, mobileError, normalizeMobile, toIdentifier } from '../../utils/validation';

type Props = NativeStackScreenProps<AuthStackParamList, 'Login'>;

/** Password or OTP login. OTP is the default: many workers have no email and forget passwords. */
export default function LoginScreen({ navigation, route }: Props) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<'otp' | 'password'>('otp');
  const [identifier, setIdentifier] = useState('');
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { busy, error, run } = useSubmit();

  const idErr = tab === 'password' ? identifierError(identifier) : mobileError(mobile);
  const pwErr = tab === 'password' && !password ? 'validation.required' : null;

  const submit = () => {
    setSubmitted(true);
    if (idErr || pwErr) return;
    if (tab === 'password') {
      run(() => login(toIdentifier(identifier), password));
    } else {
      const m = normalizeMobile(mobile);
      run(() => requestLoginOtp(m)).then(ok => ok && navigation.navigate('OtpVerify', { mode: 'login', mobile: m }));
    }
  };

  return (
    <Screen>
      <Title>{t('login.title')}</Title>
      <OfflineNotice />
      {route.params?.notice === 'passwordReset' && <Message tone="success" text={t('reset.done')} />}
      <Tabs
        value={tab}
        onChange={setTab}
        options={[
          { key: 'otp', label: t('login.tabOtp') },
          { key: 'password', label: t('login.tabPassword') },
        ]}
      />
      {tab === 'otp' ? (
        <Field
          label={t('login.mobile')}
          keyboardType="phone-pad"
          autoComplete="tel"
          maxLength={14}
          value={mobile}
          onChangeText={setMobile}
          error={submitted && idErr ? t(idErr) : null}
        />
      ) : (
        <>
          <Field
            label={t('login.identifier')}
            autoCapitalize="none"
            autoComplete="username"
            value={identifier}
            onChangeText={setIdentifier}
            error={submitted && idErr ? t(idErr) : null}
          />
          <Field
            label={t('login.password')}
            secureTextEntry
            autoComplete="password"
            value={password}
            onChangeText={setPassword}
            error={submitted && pwErr ? t(pwErr) : null}
          />
        </>
      )}
      <Message text={error} />
      <BigButton title={tab === 'otp' ? t('login.sendOtp') : t('login.submit')} onPress={submit} busy={busy} />
      {tab === 'password' && <LinkButton title={t('login.forgotPassword')} onPress={() => navigation.navigate('ForgotPassword')} />}
      <LinkButton title={t('login.signUpParent')} onPress={() => navigation.navigate('SignUp')} />
      <LinkButton title={t('login.activate')} onPress={() => navigation.navigate('Activate')} />
    </Screen>
  );
}
