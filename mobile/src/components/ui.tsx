import React, { useRef } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type TextInputProps,
} from 'react-native';
import { useTranslation } from 'react-i18next';
import { useOnline } from '../hooks/useOnline';
import { colors, MIN_TAP } from './theme';

export function Screen({ children }: { children: React.ReactNode }) {
  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.screenContent} keyboardShouldPersistTaps="handled">
      {children}
    </ScrollView>
  );
}

export function Title({ children }: { children: React.ReactNode }) {
  return (
    <Text style={styles.title} accessibilityRole="header">
      {children}
    </Text>
  );
}

export function Field({ label, error, ...input }: TextInputProps & { label: string; error?: string | null }) {
  return (
    <View style={styles.field}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={[styles.input, error ? styles.inputError : null]}
        accessibilityLabel={label}
        placeholderTextColor={colors.muted}
        {...input}
      />
      {error ? <Text style={styles.fieldError}>{error}</Text> : null}
    </View>
  );
}

type Variant = 'primary' | 'secondary' | 'danger';

export function BigButton(props: { title: string; onPress: () => void; disabled?: boolean; busy?: boolean; variant?: Variant }) {
  const { title, onPress, disabled, busy, variant = 'primary' } = props;
  const off = disabled || busy;
  return (
    <Pressable
      style={[styles.button, styles[variant], off && styles.disabled]}
      onPress={onPress}
      disabled={off}
      accessibilityRole="button"
      accessibilityState={{ disabled: !!off, busy: !!busy }}>
      {busy ? (
        <ActivityIndicator color={variant === 'secondary' ? colors.primary : '#fff'} />
      ) : (
        <Text style={[styles.buttonText, variant === 'secondary' && styles.secondaryText]}>{title}</Text>
      )}
    </Pressable>
  );
}

export function LinkButton({ title, onPress }: { title: string; onPress: () => void }) {
  return (
    <Pressable style={styles.link} onPress={onPress} accessibilityRole="link">
      <Text style={styles.linkText}>{title}</Text>
    </Pressable>
  );
}

export function Message({ text, tone = 'error' }: { text?: string | null; tone?: 'error' | 'info' | 'success' }) {
  if (!text) return null;
  return (
    <Text style={[styles.message, styles[tone]]} accessibilityLiveRegion="polite">
      {text}
    </Text>
  );
}

/** Shown on screens that need the server (login etc.). */
export function OfflineNotice() {
  const { t } = useTranslation();
  return useOnline() ? null : <Message tone="info" text={t('errors.offlineLogin')} />;
}

/** Two big tabs, e.g. Password | OTP. */
export function Tabs<K extends string>(props: { value: K; options: { key: K; label: string }[]; onChange: (k: K) => void }) {
  return (
    <View style={styles.tabs} accessibilityRole="tablist">
      {props.options.map(o => (
        <Pressable
          key={o.key}
          style={[styles.tab, props.value === o.key && styles.tabActive]}
          onPress={() => props.onChange(o.key)}
          accessibilityRole="tab"
          accessibilityState={{ selected: props.value === o.key }}>
          <Text style={[styles.tabText, props.value === o.key && styles.tabTextActive]}>{o.label}</Text>
        </Pressable>
      ))}
    </View>
  );
}

/**
 * N separate digit boxes backed by one hidden input, so paste and Android SMS autofill (sms-otp) work.
 * Used for the 6-digit OTP and the 4-digit PIN (secure = dots).
 */
export function CodeInput(props: { value: string; onChange: (v: string) => void; length?: number; secure?: boolean; label: string }) {
  const { value, onChange, length = 6, secure, label } = props;
  const ref = useRef<React.ComponentRef<typeof TextInput>>(null);
  return (
    <Pressable onPress={() => ref.current?.focus()} accessibilityLabel={label} style={styles.codeRow}>
      {Array.from({ length }, (_, i) => (
        <View key={i} style={[styles.codeBox, i === value.length && styles.codeBoxActive]}>
          <Text style={styles.codeText}>{value[i] ? (secure ? '●' : value[i]) : ''}</Text>
        </View>
      ))}
      <TextInput
        ref={ref}
        value={value}
        onChangeText={v => onChange(v.replace(/\D/g, '').slice(0, length))}
        keyboardType="number-pad"
        maxLength={length}
        autoFocus
        autoComplete={secure ? 'off' : 'sms-otp'}
        textContentType={secure ? 'none' : 'oneTimeCode'}
        secureTextEntry={secure}
        style={styles.hiddenInput}
        accessibilityLabel={label}
      />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.background },
  screenContent: { padding: 20, paddingBottom: 40 },
  title: { fontSize: 26, fontWeight: '700', color: colors.primary, marginBottom: 20 },
  field: { marginBottom: 16 },
  label: { fontSize: 18, marginBottom: 6, color: colors.text },
  input: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    fontSize: 20,
    paddingHorizontal: 12,
    minHeight: MIN_TAP,
    color: colors.text,
  },
  inputError: { borderColor: colors.danger, borderWidth: 2 },
  fieldError: { color: colors.danger, fontSize: 16, marginTop: 4 },
  button: { borderRadius: 8, minHeight: MIN_TAP, justifyContent: 'center', paddingHorizontal: 16, marginVertical: 6 },
  primary: { backgroundColor: colors.primary },
  secondary: { backgroundColor: '#fff', borderWidth: 2, borderColor: colors.primary },
  danger: { backgroundColor: colors.danger },
  disabled: { opacity: 0.5 },
  buttonText: { color: '#fff', fontSize: 20, textAlign: 'center', fontWeight: '600' },
  secondaryText: { color: colors.primary },
  link: { minHeight: 48, justifyContent: 'center' },
  linkText: { color: colors.primary, fontSize: 18, textDecorationLine: 'underline', textAlign: 'center' },
  message: { fontSize: 16, padding: 12, borderRadius: 6, marginBottom: 12 },
  error: { color: colors.danger, backgroundColor: '#fdecee' },
  info: { color: colors.warningText, backgroundColor: colors.warningBg },
  success: { color: colors.primary, backgroundColor: colors.successBg },
  tabs: { flexDirection: 'row', marginBottom: 20, borderRadius: 8, borderWidth: 2, borderColor: colors.primary, overflow: 'hidden' },
  tab: { flex: 1, minHeight: MIN_TAP, justifyContent: 'center' },
  tabActive: { backgroundColor: colors.primary },
  tabText: { fontSize: 18, textAlign: 'center', color: colors.primary, fontWeight: '600' },
  tabTextActive: { color: '#fff' },
  codeRow: { flexDirection: 'row', justifyContent: 'center', gap: 8, marginVertical: 16 },
  codeBox: {
    width: 44,
    height: 56,
    borderWidth: 2,
    borderColor: colors.border,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  codeBoxActive: { borderColor: colors.primary },
  codeText: { fontSize: 26, fontWeight: '700', color: colors.text },
  hiddenInput: { position: 'absolute', opacity: 0, width: 1, height: 1 },
});
