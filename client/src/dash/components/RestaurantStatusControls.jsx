import { useEffect, useState } from 'react'
import { useFetcher } from 'react-router-dom'

export default function RestaurantStatusControls({ restaurant, session }) {
  const fetcher = useFetcher()
  const [dismissedSuccess, setDismissedSuccess] = useState(null)
  const busy = fetcher.state !== 'idle'
  useEffect(() => {
    if (busy || fetcher.data?.ok !== true) return undefined

    const timer = setTimeout(() => setDismissedSuccess(fetcher.data), 3500)
    return () => clearTimeout(timer)
  }, [busy, fetcher.data])

  const isOperator = session?.role === 'OPERATOR'
  const isOwnStaff = session?.role === 'RESTAURANT_STAFF' && session.restaurantId === restaurant.id
  if (!isOperator && !isOwnStaff) return null

  const canPause = restaurant.status === 'OPEN'
  const canReopen = isOperator && restaurant.status === 'PAUSED'
  const error = fetcher.data?.ok === false ? fetcher.data.error : null

  return (
    <div className="restaurant-status-controls">
      {(canPause || canReopen) && (
        <fetcher.Form method="post" className="restaurant-banner-actions">
          <button type="submit" className="btn-secondary" name="intent"
            value={canPause ? 'PAUSE_RESTAURANT' : 'REOPEN_RESTAURANT'} disabled={busy}>
            {busy ? 'Updating…' : canPause ? 'Pause restaurant' : 'Reopen restaurant'}
          </button>
        </fetcher.Form>
      )}
      {!isOperator && restaurant.status === 'PAUSED' && (
        <p>Only an operator can reopen this restaurant. Existing orders can still be prepared.</p>
      )}
      {!busy && fetcher.data && (error || fetcher.data !== dismissedSuccess) && (
        <p className={`restaurant-banner ${error ? 'restaurant-banner-warning' : 'restaurant-banner-success'}`}
          role={error ? 'alert' : 'status'}>{error || fetcher.data.message}</p>
      )}
    </div>
  )
}
