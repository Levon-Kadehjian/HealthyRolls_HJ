export const BANDS = ['short', 'building', 'long']
export const BAND_LABELS = { short: 'Short', building: 'Building', long: 'Long' }

export function queueBand({ status, queuedCount = 0 }) {
  if (status === 'PAUSED' || queuedCount >= 6) return 'long'
  return queuedCount >= 3 ? 'building' : 'short'
}
