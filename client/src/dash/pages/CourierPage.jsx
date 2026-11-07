import { useEffect } from 'react'
import { useFetcher, useLoaderData, useRevalidator } from 'react-router-dom'
import './CourierPage.css'

const REFRESH_MS = 5000

const STATUS_LABELS = {
  OFFLINE: 'Off duty',
  IDLE: 'On duty · waiting for an order',
  ASSIGNED: 'Order assigned',
  CARRYING: 'Delivering',
}

const shortId = (id) => String(id ?? '').slice(0, 8)

function describeStep(courier) {
  switch (courier.status) {
    case 'OFFLINE':
      return 'Go on duty to start receiving orders.'
    case 'IDLE':
      return 'You will be offered the next ready order near you.'
    case 'ASSIGNED':
      return 'Head to the restaurant and confirm pickup.'
    case 'CARRYING':
      return 'Bring the order to the customer and confirm delivery.'
    default:
      return ''
  }
}

function OfferActions({ offer }) {
  const fetcher = useFetcher()
  const busy = fetcher.state !== 'idle'
  const pendingIntent = busy ? fetcher.formData?.get('intent') : null
  const error = fetcher.data?.ok === false ? fetcher.data.error : null

  return (
    <fetcher.Form method="post">
      <input type="hidden" name="orderId" value={offer.orderId} />
      {error && (
        <p className="courier-action-error" role="alert">
          {error}
        </p>
      )}
      <div className="courier-actions">
        <button
          type="submit"
          name="intent"
          value="ACCEPT_OFFER"
          className="btn-primary courier-btn"
          disabled={busy}
        >
          {pendingIntent === 'ACCEPT_OFFER' ? 'Accepting…' : 'Accept offer'}
        </button>
        <button
          type="submit"
          name="intent"
          value="DECLINE_AND_GO_OFF_DUTY"
          className="btn-secondary courier-btn"
          disabled={busy}
        >
          {pendingIntent === 'DECLINE_AND_GO_OFF_DUTY' ? 'Declining…' : 'Decline offer & go off duty'}
        </button>
      </div>
    </fetcher.Form>
  )
}

function CourierActions({ courier }) {
  const fetcher = useFetcher()
  const busy = fetcher.state !== 'idle'
  const pendingIntent = busy ? fetcher.formData?.get('intent') : null
  const error = fetcher.data?.ok === false ? fetcher.data.error : null

  const holdsOrder = courier.currentOrderId != null
  const assigned = courier.status === 'ASSIGNED'

  const buttons = [
    { intent: 'GO_ON_DUTY', label: 'Go on duty', pending: 'Going on duty…', enabled: courier.status === 'OFFLINE' },
    { intent: 'GO_OFF_DUTY', label: 'Go off duty', pending: 'Going off duty…', enabled: courier.status === 'IDLE' && !holdsOrder },
    { intent: 'CONFIRM_PICKUP', label: 'Confirm pickup', pending: 'Confirming…', enabled: assigned && courier.offerAccepted },
    { intent: 'CONFIRM_DELIVERY', label: 'Confirm delivery', pending: 'Confirming…', enabled: courier.status === 'CARRYING' },
  ]

  return (
    <fetcher.Form method="post">
      {holdsOrder && <input type="hidden" name="orderId" value={courier.currentOrderId} />}
      {error && (
        <p className="courier-action-error" role="alert">
          {error}
        </p>
      )}
      <div className="courier-actions">
        {buttons.map((button) => (
          <button
            key={button.intent}
            type="submit"
            name="intent"
            value={button.intent}
            className={`${button.enabled ? 'btn-primary' : 'btn-secondary'} courier-btn`}
            disabled={busy || !button.enabled}
          >
            {pendingIntent === button.intent ? button.pending : button.label}
          </button>
        ))}
      </div>
    </fetcher.Form>
  )
}

export default function CourierPage() {
  const { courier, offer, failed } = useLoaderData()
  const revalidator = useRevalidator()
  const refreshing = revalidator.state === 'loading'

    useEffect(() => {
    if (failed) {
      return undefined
    }
    const timer = setInterval(() => {
      if (revalidator.state === 'idle') {
        revalidator.revalidate()
      }
    }, REFRESH_MS)
    return () => clearInterval(timer)
  }, [revalidator, failed])

  return (
    <div className="courier-page">
      <header className="page-header courier-header">
        <div>
          <h2>Deliveries{courier ? ` · ${courier.name}` : ''}</h2>
          <p>Go on duty, accept the order offered to you, then confirm pickup and delivery.</p>
        </div>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => revalidator.revalidate()}
          disabled={refreshing}
        >
          {refreshing ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      {failed && (
        <p className="courier-load-error" role="alert">
          Your courier profile could not be loaded. Check the output console for details.
        </p>
      )}

      {courier && (
        <section
          className={`courier-card courier-card-${courier.status.toLowerCase()}`}
          aria-labelledby="courier-status-title"
        >
          <div className="courier-card-header">
            <h3 id="courier-status-title">{STATUS_LABELS[courier.status] ?? courier.status}</h3>
            <span className="courier-badge">{courier.status}</span>
          </div>
          {offer ? (
            <>
              <p className="courier-card-note">A ready order has been offered to you. Accept it to start.</p>
              <p className="courier-order">Order #{shortId(offer.orderId)}</p>
              <OfferActions offer={offer} />
            </>
          ) : (
            <>
          <p className="courier-card-note">{describeStep(courier)}</p>
          {courier.currentOrderId && <p className="courier-order">Order #{shortId(courier.currentOrderId)}</p>}
          <CourierActions courier={courier} />
            </>
          )}
        </section>
      )}
    </div>
  )
}