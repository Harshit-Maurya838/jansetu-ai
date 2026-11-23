import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, Field, Message, OfflineNotice, Screen } from '../../components/ui';
import { useSubmit } from '../../hooks/useSubmit';
import type { AppStackParamList } from '../../navigation/types';
import { changePassword } from '../../services/auth/authApi';
import { passwordError } from '../../utils/validation';

type Props = NativeStackScreenProps<AppStackParamList, 'ChangePassword'>;

/** The server logs out the other devices; this one stays signed in. */
export default function ChangePasswordScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const [current, setCurrent] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const { busy, error, run } = useSubmit();

  const errors = {
    current: current ? null : 'validation.required',
    password: passwordError(password),
    confirm: confirm === password ? null : 'validation.passwordMismatch',
  };
  const show = (k: keyof typeof errors) => (submitted && errors[k] ? t(errors[k]!) : null);

  const submit = () => {
    setSubmitted(true);
    if (Object.values(errors).some(Boolean)) return;
    run(() => changePassword(current, password)).then(ok => ok && navigation.goBack());
  };

  return (
    <Screen>
      <OfflineNotice />
      <Field label={t('password.current')} secureTextEntry value={current} onChangeText={setCurrent} error={show('current')} />
      <Field label={t('password.new')} secureTextEntry value={password} onChangeText={setPassword} error={show('password')} placeholder={t('validation.passwordPolicy')} />
      <Field label={t('password.confirm')} secureTextEntry value={confirm} onChangeText={setConfirm} error={show('confirm')} />
      <Message tone="info" text={t('password.othersLoggedOut')} />
      <Message text={error} />
      <BigButton title={t('common.save')} onPress={submit} busy={busy} />
    </Screen>
  );
}
