import { useSyncExternalStore } from 'react'
import { getSession, getSessionSnapshot, subscribeSession } from './session'

export function useSession() {
  const snapshot = useSyncExternalStore(subscribeSession, getSessionSnapshot)
  return snapshot ? getSession() : null
}
