import React, { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import NetInfo from '@react-native-community/netinfo';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { MODULE_ACCESS } from '../../constants/roles';
import type { RootStackParamList } from '../../navigation/RootNavigator';
import { clearSession, useAuthStore } from '../../store/authStore';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

/** Role-gated module grid. SOS tile (Module 10) is always first for roles that have it. */
export default function HomeScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const user = useAuthStore(s => s.user)!;
  const [online, setOnline] = useState(true);

  useEffect(() => NetInfo.addEventListener(s => setOnline(!!s.isConnected)), []);

  const modules = [...MODULE_ACCESS[user.role]].sort((a, b) => (a === 'sos' ? -1 : b === 'sos' ? 1 : 0));

  return (
    <View style={styles.container}>
      <Text style={styles.greeting}>{t('home.greeting', { name: user.fullName })}</Text>
      {!online && <Text style={styles.offline}>{t('home.offline')}</Text>}
      {/* TODO(team): show t('home.pendingSync', {count}) from sync_queue */}
      <FlatList
        data={modules}
        numColumns={2}
        keyExtractor={m => m}
        renderItem={({ item }) => (
          <Pressable
            style={[styles.tile, item === 'sos' && styles.sos]}
            onPress={() => navigation.navigate('Module', { module: item })}
            accessibilityRole="button">
            <Text style={[styles.tileText, item === 'sos' && styles.sosText]}>{t(`modules.${item}`)}</Text>
          </Pressable>
        )}
      />
      <Pressable onPress={clearSession} accessibilityRole="button">
        <Text style={styles.logout}>{t('common.logout')}</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 12, backgroundColor: '#f6f7f5' },
  greeting: { fontSize: 22, fontWeight: '600', margin: 8 },
  offline: { backgroundColor: '#fff3cd', color: '#6b4e00', padding: 10, borderRadius: 6, margin: 8, fontSize: 16 },
  tile: {
    flex: 1,
    margin: 8,
    minHeight: 96,
    borderRadius: 12,
    backgroundColor: '#fff',
    justifyContent: 'center',
    padding: 12,
    elevation: 1,
  },
  tileText: { fontSize: 18, textAlign: 'center', color: '#0b5d3b', fontWeight: '600' },
  sos: { backgroundColor: '#c62828' },
  sosText: { color: '#fff' },
  logout: { textAlign: 'center', fontSize: 16, padding: 16, color: '#555' },
});
