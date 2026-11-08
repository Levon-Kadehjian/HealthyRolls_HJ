export function restaurantActions(session, restaurant) {
  const role = session?.role
  const ownStaff = role === 'RESTAURANT_STAFF' && session.restaurantId === restaurant.id
  const isOperator = role === 'OPERATOR'

  return {
    viewMenu: role !== 'COURIER',
    placeOrder: role === 'CUSTOMER',
    placeOrderBlockedReason: restaurant.status === 'OPEN' ? null : `This restaurant is ${restaurant.status.toLowerCase()}.`,
    statusControls: isOperator || ownStaff,
    handleOrders: ownStaff,
    cancelOrders: isOperator,
    seeQueue: isOperator || ownStaff,
    seeOwnOrders: role === 'CUSTOMER',
  }
}
