import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { MODULE_DOC } from '../../constants/roles';
import type { RootStackParamList } from '../../navigation/RootNavigator';

type Props = NativeStackScreenProps<RootStackParamList, 'Module'>;

/** Stand-in until each module's real screens (src/screens/<module>/) are built in Phase II. */
export default function ModulePlaceholderScreen({ route }: Props) {
  const { t } = useTranslation();
  return (
    <View style={styles.container}>
      <Text style={styles.text}>{t('common.comingSoon')}</Text>
      <Text style={styles.doc}>docs/modules/{MODULE_DOC[route.params.module]}-*.md</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
  text: { fontSize: 18, textAlign: 'center' },
  doc: { marginTop: 8, color: '#777' },
});
