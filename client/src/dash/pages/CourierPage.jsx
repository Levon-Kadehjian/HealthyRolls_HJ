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
      return courier.currentOrderId != null
        ? 'You are off duty with an accepted order awaiting pickup. Go on duty to resume it, unless an operator reassigns it.'
        : 'Go on duty to start receiving orders.'
    case 'IDLE':
      return 'You will be offered the next ready order near you.'
    case 'ASSIGNED':
      return courier.offerAccepted
        ? 'Head to the restaurant and confirm pickup.'
        : 'A ready order has been offered to you. Accept it to start.'
    case 'CARRYING':
      return 'Bring the order to the customer and confirm delivery.'
    default:
      return ''
  }
}

function CourierActions({ courier, offer }) {
  const fetcher = useFetcher()
  const busy = fetcher.state !== 'idle'
  const pendingIntent = busy ? fetcher.formData?.get('intent') : null
  const error = fetcher.data?.ok === false ? fetcher.data.error : null

  const holdsOrder = courier.currentOrderId != null

  // An offer may exist before the courier becomes ASSIGNED.
  const hasPendingOffer = courier.status === 'IDLE' && offer?.orderId != null && courier.offerAccepted !== true

  const orderId = hasPendingOffer ? offer.orderId : courier.currentOrderId

  let buttons = []

switch (courier.status) {
  case 'OFFLINE':
    buttons = [
      {
        intent: 'GO_ON_DUTY',
        label: 'Go on duty',
        pending: 'Going on duty…',
        enabled: true,
      },
    ]
    break

  case 'IDLE':
    buttons = hasPendingOffer
      ? [
          {
            intent: 'ACCEPT_OFFER',
            label: 'Accept offer',
            pending: 'Accepting…',
            enabled: true,
          },
          {
            intent: 'DECLINE_AND_GO_OFF_DUTY',
            label: 'Decline & go off duty',
            pending: 'Declining…',
            enabled: true,
            secondary: true,
          },
        ]
      : [
          {
            intent: 'GO_OFF_DUTY',
            label: 'Go off duty',
            pending: 'Going off duty…',
            enabled: true,
          },
        ]
    break

  case 'ASSIGNED':
    buttons = [
      {
        intent: 'CONFIRM_PICKUP',
        label: 'Confirm pickup',
        pending: 'Confirming…',
        enabled: holdsOrder && courier.offerAccepted === true,
      },
      {
        intent: 'GO_OFF_DUTY',
        label: 'Go off duty',
        pending: 'Going off duty…',
        enabled: true,
        secondary: true,
      },
    ]
    break

  case 'CARRYING':
    buttons = [
      {
        intent: 'CONFIRM_DELIVERY',
        label: 'Confirm delivery',
        pending: 'Confirming…',
        enabled: holdsOrder,
      },
    ]
    break
}

  return (
    <fetcher.Form method="post">
      {orderId != null && (
        <input type="hidden" name="orderId" value={orderId} />
      )}

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
            className={`${
              button.enabled && !button.secondary
                ? 'btn-primary'
                : 'btn-secondary'
            } courier-btn`}
            disabled={busy || !button.enabled}
          >
            {pendingIntent === button.intent
              ? button.pending
              : button.label}
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

  const hasPendingOffer = courier?.status === 'IDLE' && offer?.orderId != null &&courier.offerAccepted !== true

const displayedOrderId = hasPendingOffer ? offer.orderId : courier?.currentOrderId

  return (
    <div className="courier-page">
      <header className="page-header courier-header">
        <div>
          <h2>Deliveries{courier ? ` · ${courier.name}` : ''}</h2>
          <p>
            Go on duty, accept the order offered to you,
            then confirm pickup and delivery.
          </p>
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
          Your courier profile could not be loaded.
          Check the output console for details.
        </p>
      )}

      {courier && (
        <section
          className={`courier-card courier-card-${courier.status.toLowerCase()}`}
          aria-labelledby="courier-status-title"
        >
          <div className="courier-card-header">
            <h3 id="courier-status-title">
              {STATUS_LABELS[courier.status] ?? courier.status}
            </h3>
            <span className="courier-badge">{courier.status}</span>
          </div>

          <p className="courier-card-note">
            {hasPendingOffer
              ? 'A ready order has been offered to you. Accept it to start.'
              : describeStep(courier)}
          </p>

          {displayedOrderId != null && (
            <p className="courier-order">
              Order #{shortId(displayedOrderId)}
            </p>
          )}

          <CourierActions courier={courier} offer={offer} />
        </section>
      )}
    </div>
  )
}
