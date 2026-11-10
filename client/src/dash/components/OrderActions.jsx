import { useState } from 'react'
import { useFetcher } from 'react-router-dom'

export default function OrderActions({ order }) {
  const fetcher = useFetcher()
  const [rejecting, setRejecting] = useState(false)
  const busy = fetcher.state !== 'idle'
  const pendingIntent = busy ? fetcher.formData?.get('intent') : null
  const error = fetcher.data?.ok === false ? fetcher.data.error : null

  if (order.status === 'PLACED' && rejecting) {
    return (
      <fetcher.Form method="post" className="kitchen-reject-form">
        <input type="hidden" name="orderId" value={order.orderId} />
        <label htmlFor={`reason-${order.orderId}`}>Reason for rejecting</label>
        <textarea
          id={`reason-${order.orderId}`}
          name="reason"
          rows={2}
          maxLength={200}
          required
          autoFocus
          placeholder="e.g. Out of salmon tonight"
        />
        {error && (
          <p className="kitchen-action-error" role="alert">
            {error}
          </p>
        )}
        <div className="kitchen-actions">
          <button type="submit" name="intent" value="REJECT" className="kitchen-btn kitchen-btn-danger" disabled={busy}>
            {pendingIntent === 'REJECT' ? 'Rejecting…' : 'Confirm reject'}
          </button>
          <button type="button" className="btn-secondary kitchen-btn" onClick={() => setRejecting(false)} disabled={busy}>
            Back
          </button>
        </div>
      </fetcher.Form>
    )
  }

  if (order.status === 'PLACED') {
    return (
      <fetcher.Form method="post">
        <input type="hidden" name="orderId" value={order.orderId} />
        {error && (
          <p className="kitchen-action-error" role="alert">
            {error}
          </p>
        )}
        <div className="kitchen-actions">
          <button type="submit" name="intent" value="ACCEPT" className="btn-primary kitchen-btn" disabled={busy}>
            {pendingIntent === 'ACCEPT' ? 'Accepting…' : 'Accept'}
          </button>
          <button type="button" className="btn-secondary kitchen-btn" onClick={() => setRejecting(true)} disabled={busy}>
            Reject
          </button>
        </div>
      </fetcher.Form>
    )
  }

  if (order.status === 'PREPARING') {
    return (
      <fetcher.Form method="post">
        <input type="hidden" name="orderId" value={order.orderId} />
        {error && (
          <p className="kitchen-action-error" role="alert">
            {error}
          </p>
        )}
        <div className="kitchen-actions">
          <button type="submit" name="intent" value="MARK_READY" className="btn-primary kitchen-btn" disabled={busy}>
            {pendingIntent === 'MARK_READY' ? 'Marking…' : 'Mark ready'}
          </button>
        </div>
      </fetcher.Form>
    )
  }

  return null
}
