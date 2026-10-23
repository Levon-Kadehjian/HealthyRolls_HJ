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
}

export const ACTIVE_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY', 'PICKED_UP']

export const CUSTOMER_CANCELLABLE_STATUSES = ['PLACED']

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

export function buildOrderHistory(orders, events = [], restaurants = []) {
  const timeline = timelineByOrder(events)
  const restaurantsById = new Map(restaurants.map((restaurant) => [restaurant.id, restaurant]))
  const menuItemsById = new Map(
    restaurants.flatMap((restaurant) => (restaurant.menu ?? []).map((item) => [item.itemId, item])),
  )
  return orders.map((order) => {
    const lines = (order.lines ?? []).map((line) => {
      const item = menuItemsById.get(line.menuItemId)
      return { ...line, name: item?.name ?? line.name, price: item ? Number(item.price) : null }
    })
    const priced = lines.every((line) => line.price !== null)
    const { placedAt = null, updatedAt = null } = timeline.get(order.orderId) ?? {}
    return {
      ...order,
      lines,
      restaurantName: restaurantsById.get(order.restaurantId)?.name ?? null,
      itemCount: lines.reduce((sum, line) => sum + line.quantity, 0),
      total: priced ? lines.reduce((sum, line) => sum + line.price * line.quantity, 0) : null,
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
