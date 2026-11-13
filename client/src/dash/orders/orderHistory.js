const currency = new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' })

export const STATUS_LABELS = {
  PLACED: 'Placed',
  ACCEPTED: 'Accepted',
  PREPARING: 'Preparing',
  READY: 'Ready for pickup',
  PICKED_UP: 'Out for delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  REJECTED: 'Rejected',
  REFUNDED: 'Refunded',
}

export const ACTIVE_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'PICKED_UP']

export const CUSTOMER_CANCELLABLE_STATUSES = ['PLACED']

export const OPERATOR_CANCELLABLE_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY']

export function canCancelOrder(role, status) {
  if (role === 'OPERATOR') return OPERATOR_CANCELLABLE_STATUSES.includes(status)
  if (role === 'CUSTOMER') return CUSTOMER_CANCELLABLE_STATUSES.includes(status)
  return false
}

export function canRefundOrder(role, status) {
  return role === 'OPERATOR' && status === 'DELIVERED'
}

export const statusLabel = (status) => STATUS_LABELS[status] ?? String(status ?? '').replace('_', ' ').toLowerCase()

export const formatMoney = (amount) => currency.format(amount)

export const formatDateTime = (value) =>
  value ? new Date(value).toLocaleString('en-CA', { dateStyle: 'medium', timeStyle: 'short' }) : null

function timelineByOrder(events) {
  const timeline = new Map()
  events.forEach(({ orderId, fromStatus, occurredAt }) => {
    const entry = timeline.get(orderId) ?? { placedAt: null, updatedAt: null }
    if (fromStatus === 'NONE') {
      entry.placedAt = occurredAt
    }
    if (!entry.updatedAt || new Date(occurredAt) > new Date(entry.updatedAt)) {
      entry.updatedAt = occurredAt
    }
    timeline.set(orderId, entry)
  })
  return timeline
}

export function buildOrderHistory(orders, events = [], restaurants = [], ledger = []) {
  const timeline = timelineByOrder(events)
  const restaurantsById = new Map(restaurants.map((restaurant) => [restaurant.id, restaurant]))
  const menuItemsById = new Map(
    restaurants.flatMap((restaurant) => (restaurant.menu ?? []).map((item) => [item.itemId, item])),
  )
  const chargesByOrderId = new Map()
  const refundsByOrderId = new Map()
  ledger.forEach((entry) => {
    if (entry.type === 'CHARGE') {
      chargesByOrderId.set(entry.orderId, entry)
    } else if (entry.type === 'REFUND') {
      refundsByOrderId.set(entry.orderId, entry)
    }
  })

  return orders.map((order) => {
    const lines = (order.lines ?? []).map((line) => {
      const item = menuItemsById.get(line.menuItemId)
      const name = line.name ?? item?.name ?? `Item ${String(line.menuItemId ?? '').slice(0, 8)}`
      const price = line.price != null ? Number(line.price) : (item ? Number(item.price) : null)
      return { ...line, name, price }
    })
    const priced = lines.every((line) => line.price !== null)
    const { placedAt = null, updatedAt = null } = timeline.get(order.orderId) ?? {}
    const charge = chargesByOrderId.get(order.orderId)
    const refund = refundsByOrderId.get(order.orderId)
    const billedTotal = charge?.amount != null ? Number(charge.amount) : null
    const subtotal = priced ? lines.reduce((sum, line) => sum + line.price * line.quantity, 0) : null
    const isCompleted = order.status === 'DELIVERED' || order.status === 'REFUNDED'
    const total = billedTotal ?? (isCompleted ? null : subtotal)

    return {
      ...order,
      lines,
      restaurantName: restaurantsById.get(order.restaurantId)?.name ?? null,
      itemCount: lines.reduce((sum, line) => sum + line.quantity, 0),
      subtotal,
      total,
      billedTotal,
      refunded: Boolean(refund || order.status === 'REFUNDED'),
      refundAmount: refund?.amount != null ? Math.abs(Number(refund.amount)) : null,
      placedAt,
      updatedAt: updatedAt ?? order.acceptedAt ?? null,
    }
  })
}

export function newestFirst(orders) {
  return [...orders].sort(
    (a, b) => new Date(b.placedAt ?? b.updatedAt ?? 0) - new Date(a.placedAt ?? a.updatedAt ?? 0),
  )
}
