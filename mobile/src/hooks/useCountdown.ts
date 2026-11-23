import { useCallback, useEffect, useState } from 'react';

/** Seconds left until a resend is allowed; restart() begins a new countdown. */
export function useCountdown(seconds: number) {
  const [left, setLeft] = useState(seconds);
  useEffect(() => {
    if (left <= 0) return;
    const id = setTimeout(() => setLeft(l => l - 1), 1000);
    return () => clearTimeout(id);
  }, [left]);
  const restart = useCallback(() => setLeft(seconds), [seconds]);
  return { left, restart };
}
