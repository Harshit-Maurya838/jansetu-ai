import React from 'react';
import { FlatList, Pressable, StyleSheet, Text } from 'react-native';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { visibleModules } from '../../constants/roles';
import { useOnline } from '../../hooks/useOnline';
import type { AppStackParamList } from '../../navigation/types';
import { useAuthStore } from '../../store/authStore';

type Props = NativeStackScreenProps<AppStackParamList, 'Home'>;

/** Module grid built from the server's `permissions` for this user. SOS tile (Module 10) is always first. */
export default function HomeScreen({ navigation }: Props) {
  const { t } = useTranslation();
  const user = useAuthStore(s => s.user)!;
  const online = useOnline();
  const modules = visibleModules(user.permissions);

  return (
    <FlatList
      style={styles.container}
      data={modules}
      numColumns={2}
      keyExtractor={m => m}
      ListHeaderComponent={
        <>
          <Text style={styles.greeting}>{t('home.greeting', { name: user.fullName })}</Text>
          {!online && <Text style={styles.offline}>{t('home.offline')}</Text>}
          {/* TODO(team): show t('home.pendingSync', {count}) from sync_queue */}
        </>
      }
      renderItem={({ item }) => (
        <Pressable
          style={[styles.tile, item === 'sos' && styles.sos]}
          onPress={() => navigation.navigate('Module', { module: item })}
          accessibilityRole="button">
          <Text style={[styles.tileText, item === 'sos' && styles.sosText]}>{t(`modules.${item}`)}</Text>
        </Pressable>
      )}
    />
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
});
