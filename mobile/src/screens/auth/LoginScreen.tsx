import React, { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { api } from '../../services/api/client';
import { saveTokens, useAuthStore, type SessionUser } from '../../store/authStore';

interface LoginResult {
  accessToken: string;
  refreshToken: string;
  user: SessionUser;
}

/** OTP-first login (docs/modules/01-authentication.md#screens). Password login & forgot-password: TODO(team). */
export default function LoginScreen() {
  const { t } = useTranslation();
  const setUser = useAuthStore(s => s.setUser);
  const [mobile, setMobile] = useState('');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const validMobile = /^[6-9]\d{9}$/.test(mobile);

  async function sendOtp() {
    setError(null);
    try {
      await api('/auth/otp/request', { method: 'POST', body: JSON.stringify({ mobile: `+91${mobile}`, purpose: 'LOGIN' }) });
      setOtpSent(true);
    } catch (e) {
      setError((e as Error).message);
    }
  }

  async function verify() {
    setError(null);
    try {
      const r = await api<LoginResult>('/auth/otp/verify', {
        method: 'POST',
        body: JSON.stringify({ mobile: `+91${mobile}`, otp, deviceId: 'TODO(team)-device-id' }),
      });
      await saveTokens(r.accessToken, r.refreshToken);
      setUser(r.user);
    } catch (e) {
      setError((e as Error).message);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{t('appName')}</Text>
      <Text style={styles.label}>{t('login.mobile')}</Text>
      <TextInput
        style={styles.input}
        keyboardType="phone-pad"
        maxLength={10}
        value={mobile}
        onChangeText={setMobile}
        accessibilityLabel={t('login.mobile')}
      />
      {otpSent && (
        <>
          <Text style={styles.label}>{t('login.otp')}</Text>
          <TextInput
            style={styles.input}
            keyboardType="number-pad"
            maxLength={6}
            value={otp}
            onChangeText={setOtp}
            accessibilityLabel={t('login.otp')}
          />
        </>
      )}
      {error && <Text style={styles.error}>{error}</Text>}
      <Pressable
        style={[styles.button, !validMobile && styles.disabled]}
        disabled={!validMobile}
        onPress={otpSent ? verify : sendOtp}
        accessibilityRole="button">
        <Text style={styles.buttonText}>{otpSent ? t('login.verify') : t('login.sendOtp')}</Text>
      </Pressable>
    </View>
  );
}

// Large touch targets and text: low-end phones, outdoor use, limited tech literacy.
const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: '#fff' },
  title: { fontSize: 32, fontWeight: '700', marginBottom: 32, textAlign: 'center', color: '#0b5d3b' },
  label: { fontSize: 18, marginBottom: 8, color: '#222' },
  input: { borderWidth: 1, borderColor: '#888', borderRadius: 8, fontSize: 20, padding: 12, marginBottom: 16 },
  button: { backgroundColor: '#0b5d3b', borderRadius: 8, minHeight: 56, justifyContent: 'center' },
  disabled: { opacity: 0.5 },
  buttonText: { color: '#fff', fontSize: 20, textAlign: 'center', fontWeight: '600' },
  error: { color: '#b00020', fontSize: 16, marginBottom: 12 },
});
