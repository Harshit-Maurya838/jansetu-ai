import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { BigButton, CodeInput, Message, Screen, Title } from '../../components/ui';
import { createPin } from '../../services/auth/authApi';

/** After the first successful login: a 4-digit PIN that unlocks the app (also offline) on shared phones. */
export default function SetPinScreen() {
  const { t } = useTranslation();
  const [pin, setPin] = useState('');
  const [confirm, setConfirm] = useState('');
  const [step, setStep] = useState<'enter' | 'confirm'>('enter');
  const [error, setError] = useState<string | null>(null);

  const next = () => {
    if (step === 'enter') {
      setStep('confirm');
      return;
    }
    if (confirm !== pin) {
      setError(t('pin.mismatch'));
      setPin('');
      setConfirm('');
      setStep('enter');
      return;
    }
    createPin(pin);
  };

  return (
    <Screen>
      <Title>{t('pin.setTitle')}</Title>
      <Message tone="info" text={t(step === 'enter' ? 'pin.setIntro' : 'pin.confirmIntro')} />
      {step === 'enter' ? (
        <CodeInput key="enter" value={pin} onChange={setPin} length={4} secure label={t('pin.label')} />
      ) : (
        <CodeInput key="confirm" value={confirm} onChange={setConfirm} length={4} secure label={t('pin.confirmLabel')} />
      )}
      <Message text={error} />
      <BigButton title={t('pin.next')} onPress={next} disabled={(step === 'enter' ? pin : confirm).length !== 4} />
    </Screen>
  );
}
