import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, Field, Message, OfflineNotice, Screen, Title } from '../../components/ui';
import { useSubmit } from '../../hooks/useSubmit';
import type { AuthStackParamList } from '../../navigation/types';
import { forgotPassword } from '../../services/auth/authApi';
import { identifierError, toIdentifier } from '../../utils/validation';

type Props = NativeStackScreenProps<AuthStackParamList, 'ForgotPassword'>;

export default function ForgotPasswordScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const [identifier, setIdentifier] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { busy, error, run } = useSubmit();
  const idErr = identifierError(identifier);

  const submit = () => {
    setSubmitted(true);
    if (idErr) return;
    const id = toIdentifier(identifier);
    run(() => forgotPassword(id)).then(ok => ok && navigation.navigate('ResetPassword', { identifier: id }));
  };

  return (
    <Screen>
      <Title>{t('forgot.title')}</Title>
      <OfflineNotice />
      <Message tone="info" text={t('forgot.intro')} />
      <Field
        label={t('login.identifier')}
        autoCapitalize="none"
        value={identifier}
        onChangeText={setIdentifier}
        error={submitted && idErr ? t(idErr) : null}
      />
      <Message text={error} />
      <BigButton title={t('login.sendOtp')} onPress={submit} busy={busy} />
    </Screen>
  );
}
