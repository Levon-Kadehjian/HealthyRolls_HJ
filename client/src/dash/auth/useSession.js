import { useMemo, useSyncExternalStore } from 'react'
import { getSession, getSessionSnapshot, subscribeSession } from './session'

export function useSession() {
  const snapshot = useSyncExternalStore(subscribeSession, getSessionSnapshot)
  return useMemo(() => (snapshot ? getSession() : null), [snapshot])
}
