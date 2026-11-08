export const BANDS = ['short', 'building', 'long', 'closed']
export const BAND_LABELS = { short: 'Short', building: 'Building', long: 'Long', closed: 'Closed' }

export function queueBand({ status, queuedCount = 0 }) {
  if (status === 'CLOSED') return 'closed'
  if (status === 'PAUSED' || queuedCount >= 6) return 'long'
  return queuedCount >= 3 ? 'building' : 'short'
}
