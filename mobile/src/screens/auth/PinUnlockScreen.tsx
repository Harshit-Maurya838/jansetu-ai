import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { BigButton, CodeInput, LinkButton, Message, Screen, Title } from '../../components/ui';
import { endLocalSession } from '../../services/api/client';
import { syncProfile } from '../../services/auth/authApi';
import { checkPin } from '../../services/auth/secureStore';
import { useAuthStore } from '../../store/authStore';

/** Shown on every app open and after 5 min in the background. 5 wrong PINs wipe the local session. */
export default function PinUnlockScreen() {
  const { t } = useTranslation();
  const user = useAuthStore(s => s.user);
  const set = useAuthStore(s => s.set);
  const [pin, setPin] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const unlock = async () => {
    setBusy(true);
    const result = await checkPin(pin);
    setBusy(false);
    setPin('');
    if (result === 'ok') {
      set({ status: 'signedIn' });
      syncProfile(); // best effort, refreshes role / permissions when online
    } else if (result === 'wiped') {
      set({ status: 'signedOut', user: null });
    } else {
      setError(t('pin.wrong', { left: result }));
    }
  };

  return (
    <Screen>
      <Title>{t('pin.unlockTitle', { name: user?.fullName ?? '' })}</Title>
      <CodeInput value={pin} onChange={setPin} length={4} secure label={t('pin.label')} />
      <Message text={error} />
      <BigButton title={t('pin.unlock')} onPress={unlock} busy={busy} disabled={pin.length !== 4} />
      <LinkButton title={t('pin.forgot')} onPress={endLocalSession} />
    </Screen>
  );
}
