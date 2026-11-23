import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { BigButton, Message } from '../../components/ui';
import { colors } from '../../components/theme';
import { useSubmit } from '../../hooks/useSubmit';
import { getSessions, revokeSession, type SessionInfo } from '../../services/auth/authApi';

/** Devices signed in to this account; end any other one (e.g. a lost phone). */
export default function SessionsScreen() {
  const { t, i18n } = useTranslation();
  const [sessions, setSessions] = useState<SessionInfo[]>([]);
  const { busy, error, run } = useSubmit();

  const load = useCallback(() => run(async () => setSessions(await getSessions())), [run]);
  useEffect(() => {
    load();
  }, [load]);

  const fmt = (iso: string) => new Date(iso).toLocaleString(i18n.language === 'hi' ? 'hi-IN' : 'en-IN');

  return (
    <FlatList
      style={styles.list}
      contentContainerStyle={styles.content}
      data={sessions}
      keyExtractor={s => s.id}
      refreshControl={<RefreshControl refreshing={busy} onRefresh={() => {
            load();
          }} />}
      ListHeaderComponent={<Message text={error} />}
      ListEmptyComponent={busy ? undefined : <Text style={styles.empty}>{t('sessions.empty')}</Text>}
      renderItem={({ item }) => (
        <View style={styles.card}>
          <Text style={styles.name}>
            {item.deviceName ?? t('sessions.unknownDevice')}
            {item.current ? ` · ${t('sessions.thisDevice')}` : ''}
          </Text>
          <Text style={styles.meta}>{t('sessions.lastActive', { time: fmt(item.lastRefreshedAt) })}</Text>
          {!item.current && (
            <BigButton title={t('sessions.revoke')} variant="danger" onPress={() => run(() => revokeSession(item.id)).then(load)} />
          )}
        </View>
      )}
    />
  );
}

const styles = StyleSheet.create({
  list: { flex: 1, backgroundColor: colors.surface },
  content: { padding: 12 },
  card: { backgroundColor: '#fff', borderRadius: 8, padding: 12, marginBottom: 12, elevation: 1 },
  name: { fontSize: 18, fontWeight: '600', color: colors.text },
  meta: { fontSize: 15, color: colors.muted, marginVertical: 4 },
  empty: { textAlign: 'center', fontSize: 16, color: colors.muted, padding: 24 },
});
