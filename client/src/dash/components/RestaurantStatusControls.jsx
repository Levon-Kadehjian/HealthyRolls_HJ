import { useEffect, useRef, useState } from 'react'
import { useFetcher } from 'react-router-dom'
import './RestaurantStatusControls.css'

export default function RestaurantStatusControls({ restaurant, session }) {
  const fetcher = useFetcher()
  const dialogRef = useRef(null)
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
          <input type="hidden" name="restaurantId" value={restaurant.id} />
          {canPause ? (
            <>
              <button type="button" className="btn-secondary btn-pause btn-status" disabled={busy}
                onClick={() => dialogRef.current?.showModal()}>
                {busy ? 'Updating…' : 'Pause restaurant'}
              </button>
              <dialog ref={dialogRef} className="confirm-dialog" aria-labelledby="pause-title">
                <h4 id="pause-title">Pause {restaurant.name}?</h4>
                <p>New orders won&apos;t be accepted until an operator reopens it. Orders already in progress can still be prepared.</p>
                <div className="confirm-dialog-actions">
                  <button type="button" className="btn-secondary" onClick={() => dialogRef.current?.close()}>Cancel</button>
                  <button type="submit" className="btn-secondary btn-pause" name="intent" value="PAUSE_RESTAURANT"
                    onClick={() => dialogRef.current?.close()}>
                    Yes, pause
                  </button>
                </div>
              </dialog>
            </>
          ) : (
            <button type="submit" className="btn-secondary btn-status" name="intent" value="REOPEN_RESTAURANT" disabled={busy}>
              {busy ? 'Updating…' : 'Reopen restaurant'}
            </button>
          )}
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
