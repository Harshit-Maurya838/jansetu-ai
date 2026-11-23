import { useCallback, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { TFunction } from 'i18next';
import { ApiError } from '../services/api/client';

/** Translates an API error code (errors.AUTH_OTP_EXPIRED, errors.NETWORK, …) with a generic fallback. */
export function errorMessage(t: TFunction, e: unknown): string {
  const code = e instanceof ApiError ? e.code : 'generic';
  return t(`errors.${code}`, { defaultValue: t('errors.generic') });
}

/** busy / error state around one async action. */
export function useSubmit() {
  const { t } = useTranslation();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const run = useCallback(
    async (action: () => Promise<unknown>): Promise<boolean> => {
      setBusy(true);
      setError(null);
      try {
        await action();
        return true;
      } catch (e) {
        setError(errorMessage(t, e));
        return false;
      } finally {
        setBusy(false);
      }
    },
    [t],
  );
  return { busy, error, setError, run };
}
