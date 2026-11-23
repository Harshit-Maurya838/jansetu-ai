import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { BigButton } from '../../components/ui';
import { colors } from '../../components/theme';
import type { AuthStackParamList } from '../../navigation/types';
import { chooseLanguage } from '../../services/auth/authApi';

type Props = NativeStackScreenProps<AuthStackParamList, 'Welcome'>;

/** First launch: pick the app language. Labels are shown in both languages on purpose. */
export default function WelcomeScreen({ navigation }: Props) {
  const pick = async (lang: 'en' | 'hi') => {
    await chooseLanguage(lang);
    navigation.replace('Login');
  };
  return (
    <View style={styles.container}>
      <Text style={styles.title}>जनसेतु · JanSetu</Text>
      <Text style={styles.subtitle}>भाषा चुनें · Choose language</Text>
      <BigButton title="हिन्दी" onPress={() => pick('hi')} />
      <BigButton title="English" variant="secondary" onPress={() => pick('en')} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: colors.background },
  title: { fontSize: 32, fontWeight: '700', textAlign: 'center', color: colors.primary, marginBottom: 12 },
  subtitle: { fontSize: 20, textAlign: 'center', color: colors.text, marginBottom: 32 },
});
