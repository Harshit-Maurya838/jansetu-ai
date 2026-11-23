import React, { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton, Field, Message, Screen, Tabs } from '../../components/ui';
import { colors } from '../../components/theme';
import { useSubmit } from '../../hooks/useSubmit';
import type { AppStackParamList } from '../../navigation/types';
import { logout, logoutAll, updateProfile } from '../../services/auth/authApi';
import { useAuthStore } from '../../store/authStore';
import { isEmail } from '../../utils/validation';

type Props = NativeStackScreenProps<AppStackParamList, 'Profile'>;

export default function ProfileScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const user = useAuthStore(s => s.user)!;
  const [fullName, setFullName] = useState(user.fullName);
  const [email, setEmail] = useState(user.email ?? '');
  const [saved, setSaved] = useState(false);
  const { busy, error, setError, run } = useSubmit();

  const emailErr = email && !isEmail(email) ? t('validation.email') : null;
  const save = () => {
    if (!fullName.trim() || emailErr) return;
    setSaved(false);
    const changes = { fullName: fullName.trim(), ...(email && email !== user.email ? { email: email.trim() } : {}) };
    run(() => updateProfile(changes)).then(setSaved);
  };

  return (
    <Screen>
      <View style={styles.card}>
        <Row label={t('profile.mobile')} value={user.mobile} />
        <Row label={t('profile.role')} value={t(`roles.${user.role}`)} />
        {user.centreId ? <Row label={t('profile.centre')} value={user.centreId} /> : null}
        {user.ngoId ? <Row label={t('profile.ngo')} value={user.ngoId} /> : null}
      </View>

      <Text style={styles.section}>{t('profile.language')}</Text>
      <Tabs
        value={user.preferredLanguage}
        onChange={lang => run(() => updateProfile({ preferredLanguage: lang }))}
        options={[
          { key: 'hi', label: 'हिन्दी' },
          { key: 'en', label: 'English' },
        ]}
      />

      <Field label={t('signUp.fullName')} value={fullName} onChangeText={setFullName} />
      <Field label={t('profile.email')} value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" error={emailErr} />
      {saved ? <Message tone="success" text={t('profile.saved')} /> : null}
      <Message text={error} />
      <BigButton title={t('common.save')} onPress={save} busy={busy} />

      <BigButton title={t('password.changeTitle')} variant="secondary" onPress={() => navigation.navigate('ChangePassword')} />
      <BigButton title={t('sessions.title')} variant="secondary" onPress={() => navigation.navigate('Sessions')} />
      <BigButton title={t('common.logout')} variant="secondary" onPress={logout} />
      <BigButton title={t('profile.logoutAll')} variant="danger" onPress={() => logoutAll().catch(() => setError(t('errors.NETWORK')))} />
    </Screen>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Text style={styles.rowValue} selectable>
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: colors.surface, borderRadius: 8, padding: 12, marginBottom: 16 },
  row: { paddingVertical: 6 },
  rowLabel: { fontSize: 14, color: colors.muted },
  rowValue: { fontSize: 18, color: colors.text },
  section: { fontSize: 18, marginBottom: 6, color: colors.text },
});
