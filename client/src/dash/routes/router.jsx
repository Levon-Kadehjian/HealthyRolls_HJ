import { createBrowserRouter, redirect } from 'react-router-dom'
import { getSession } from '../auth/session'
import { ensureSessionLocation } from '../api/auth'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'
import { buildOrderHistory, canCancelOrder, canRefundOrder } from '../orders/orderHistory'
import Root from '../layouts/Root'
import Dashboard from '../layouts/Dashboard'
import HomePage from '../pages/HomePage'
import AuthPage from '../pages/AuthPage'
import AboutPage from '../pages/AboutPage'
import AccountPage from '../pages/AccountPage'
import CartPage from '../pages/CartPage'
import CheckoutPage from '../pages/CheckoutPage'
import OrdersPage from '../pages/OrdersPage'
import CourierPage from '../pages/CourierPage'
import MapPage from '../pages/MapPage'
import RestaurantOrdersPage from '../pages/RestaurantOrdersPage'
import RestaurantsPage from '../pages/RestaurantsPage'
import RestaurantMenuPage from '../pages/RestaurantMenuPage'
import ConsolePage from '../pages/ConsolePage'
import ErrorPage from '../pages/ErrorPage'
import {
  addMenuItem,
  editMenuItem,
  changeMenuAvailability,
  pauseRestaurant,
  reopenRestaurant,
  getMapRestaurants,
  getRestaurant,
  getRestaurants,
} from '../api/restaurants'
import {
  applyOrderAction,
  getBillingHistory,
  getQueueCounts,
  listOrderEvents,
  listOrders,
  refundOrder,
} from '../api/orders'
import {
  acceptOffer,
  confirmDelivery,
  confirmPickup,
  declineOffer,
  getMyActiveOrder,
  getMyCourier,
  getPendingOffer,
  getStalledOrders,
  goOffDuty,
  goOnDuty,
  listCouriers,
  reassignStalledOrder,
} from '../api/couriers'

const ORDER_COMMANDS = {
  ACCEPT: 'ACCEPT_ORDER',
  REJECT: 'REJECT_ORDER',
  MARK_READY: 'MARK_ORDER_READY',
}

const KITCHEN_PATH = '/dashboard/kitchen'
const COURIER_HOME_PATH = '/courier'

const isStaffSession = (session) => session?.role === 'RESTAURANT_STAFF'
const isCourierSession = (session) => session?.role === 'COURIER'

const redirectStaffToKitchen = () => {
  if (isStaffSession(getSession())) {
    throw redirect(KITCHEN_PATH)
  }
}

const redirectCourierHome = () => {
  if (isCourierSession(getSession())) {
    throw redirect(COURIER_HOME_PATH)
  }
}

const requireLogin = () => {
  const session = getSession()
  if (!session) {
    throw redirect('/login')
  }
  return session
}

const requireSession = () => {
  if (!getSession()) {
    throw new Response('Log in to browse restaurants and menus.', { status: 401, statusText: 'Unauthorized' })
  }
}

const loginRequiredLoader = () => {
  requireLogin()
  return null
}

const customerOnlyLoader = () => {
  const session = requireLogin()
  redirectStaffToKitchen()
  redirectCourierHome()
  if (session.role !== 'CUSTOMER') {
    throw redirect('/dashboard')
  }
  return null
}

const homeLoader = () => {
  redirectStaffToKitchen()
  redirectCourierHome()
  return null
}

const dashboardLoader = () => ({ session: requireLogin() })

const dashboardIndexLoader = () => {
  const session = getSession()
  if (isStaffSession(session)) return redirect('kitchen')
  if (isCourierSession(session)) return redirect('/courier')
  return redirect('/orders')
}

const loadRestaurantCatalog = async (restaurantIds, session) => {
  const ids = [...new Set(restaurantIds)].filter(Boolean)

  if (session?.role === 'COURIER') {
    try {
      const mapRestaurants = await getMapRestaurants()
      const mapById = new Map(mapRestaurants.map((r) => [r.id, r]))
      return ids.map((id) => mapById.get(id)).filter(Boolean)
    } catch (error) {
      recordCommand('LOAD_MAP_RESTAURANTS', '', 'error', await describeError(error))
      return []
    }
  }

  const restaurants = await Promise.all(
    ids.map(async (id) => {
      try {
        return await getRestaurant(id)
      } catch (error) {
        recordCommand(
          'LOAD_RESTAURANT',
          `restaurant=${id}`,
          'error',
          await describeError(error),
        )
        return null
      }
    }),
  )

  return restaurants.filter(Boolean)
}

const loadOrderHistory = async (knownRestaurantIds = [], session = null) => {
  const [orders, events, ledger] = await Promise.all([
    listOrders(),
    listOrderEvents().catch(async (error) => {
      recordCommand(
        'LOAD_ORDER_EVENTS',
        '',
        'error',
        await describeError(error),
      )
      return []
    }),
    getBillingHistory().catch(async (error) => {
      recordCommand('LOAD_BILLING_HISTORY', '', 'error', await describeError(error))
      return []
    }),
  ])

  const restaurants = await loadRestaurantCatalog([
    ...knownRestaurantIds,
    ...orders.map((order) => order.restaurantId),
  ], session)

  return {
    orders: buildOrderHistory(orders, events, restaurants, ledger),
    restaurants,
  }
}

const customerOrdersLoader = async () => {
  const session = requireLogin()
  redirectStaffToKitchen()
  try {
    const { orders } = await runCommand(
      'LIST_ORDERS',
      `role=${session.role}`,
      () => loadOrderHistory([], session),
      (history) => `${history.orders.length} orders`,
    )
    if (session.role === 'OPERATOR') {
      return { orders, failed: false, stalled: await loadStalledOrders() }
    }
    return { orders, failed: false }
  } catch {
    return { orders: [], failed: true }
  }
}

const loadStalledOrders = async () => {
  try {
    return await runCommand('LIST_STALLED_ORDERS', '', () => getStalledOrders(), (stalled) => `${stalled.length} stalled`)
  } catch {
    return []
  }
}

const describeReassignment = (result) =>
  result.queued
    ? `released from ${result.previousCourierName}, no courier available, order placed in the dispatch queue`
    : `released from ${result.previousCourierName}, offered to ${result.offeredCourierName}`

const customerOrdersAction = async ({ request }) => {
  const session = requireLogin()
  const form = await request.formData()
  const orderId = form.get('orderId')
  const intent = form.get('intent')
  const status = form.get('status')
  const details = `order=${String(orderId).slice(0, 8)}`

  if (intent === 'REFUND') {
    if (!canRefundOrder(session.role, status)) {
      recordCommand('REFUND_ORDER', details, 'error', `order is ${status} or unauthorized`)
      return { ok: false, error: 'Only operators can refund delivered orders.' }
    }
    try {
      await runCommand('REFUND_ORDER', details, () => refundOrder(orderId), () => 'refunded')
      return { ok: true }
    } catch (error) {
      return { ok: false, error: await describeError(error) }
    }
  }

  if (intent === 'REASSIGN_STALLED_ORDER') {
    if (session.role !== 'OPERATOR') {
      recordCommand(intent, details, 'error', 'operators only')
      return { ok: false, error: 'Only operators can reassign orders.' }
    }
    try {
      const result = await runCommand(intent, details, () => reassignStalledOrder(orderId), describeReassignment)
      return { ok: true, reassignment: result }
    } catch (error) {
      return { ok: false, error: await describeError(error) }
    }
  }

  if (intent !== 'CANCEL') {
    recordCommand('ORDER_ACTION', details, 'error', `unknown action ${intent}`)
    return { ok: false, error: `Unknown action ${intent}` }
  }
  if (!canCancelOrder(session.role, status)) {
    recordCommand('CANCEL_ORDER', details, 'error', `order is ${status}`)
    return { ok: false, error: 'This order can no longer be cancelled.' }
  }

  try {
    await runCommand('CANCEL_ORDER', details, () => applyOrderAction(orderId, 'CANCEL'), (order) => `now ${order.status}`)
    return { ok: true }
  } catch (error) {
    return { ok: false, error: await describeError(error) }
  }
}

const restaurantOrdersLoader = async () => {
  const session = requireLogin()
  if (!isStaffSession(session)) {
    recordCommand('OPEN_KITCHEN', `role=${session?.role ?? 'none'}`, 'error', 'restaurant staff only')
    throw redirect('/dashboard')
  }
  try {
    const { orders, restaurants } = await runCommand(
      'LIST_ORDERS',
      `role=${session.role}`,
      () => loadOrderHistory([session.restaurantId], session),
      (history) => `${history.orders.length} orders`,
    )
    const ownOrders = session.restaurantId
      ? orders.filter((order) => order.restaurantId === session.restaurantId)
      : orders
    const restaurantName =
      restaurants.find((restaurant) => restaurant.id === session.restaurantId)?.name ?? ownOrders[0]?.restaurantName ?? null
    return { orders: ownOrders, restaurantName, failed: false }
  } catch {
    return { orders: [], restaurantName: null, failed: true }
  }
}

const restaurantOrdersAction = async ({ request }) => {
  const form = await request.formData()
  const orderId = form.get('orderId')
  const intent = form.get('intent')
  const reason = form.get('reason')?.trim() || null
  const command = ORDER_COMMANDS[intent]
  const details = `order=${String(orderId).slice(0, 8)}${reason ? ` reason="${reason}"` : ''}`

  if (!command) {
    recordCommand('ORDER_ACTION', details, 'error', `unknown action ${intent}`)
    return { ok: false, error: `Unknown action ${intent}` }
  }
  if (intent === 'REJECT' && !reason) {
    recordCommand(command, details, 'error', 'a rejection reason is required')
    return { ok: false, error: 'A rejection reason is required.' }
  }

  try {
    await runCommand(command, details, () => applyOrderAction(orderId, intent, reason), (order) => `now ${order.status}`)
    return { ok: true }
  } catch (error) {
    return { ok: false, error: await describeError(error) }
  }
}

const COURIER_COMMANDS = {
  GO_ON_DUTY: () => goOnDuty(),
  GO_OFF_DUTY: () => goOffDuty(),
  DECLINE_AND_GO_OFF_DUTY: (orderId) => declineOffer(orderId),
  ACCEPT_OFFER: (orderId) => acceptOffer(orderId),
  CONFIRM_PICKUP: (orderId) => confirmPickup(orderId),
  CONFIRM_DELIVERY: (orderId) => confirmDelivery(orderId),
}

const COURIER_RESULT_MESSAGES = {
  ACCEPT_OFFER: () => 'offer accepted',
  DECLINE_AND_GO_OFF_DUTY: (result) => `offer declined, now ${result.status}`,
}

const describeCourierResult = (intent, result) =>
  COURIER_RESULT_MESSAGES[intent]?.(result) ?? `now ${result.status}`

const courierLoader = async () => {
  const session = requireLogin()
  if (!isCourierSession(session)) {
    recordCommand('OPEN_DELIVERIES', `role=${session.role}`, 'error', 'couriers only')
    throw redirect('/orders')
  }
  try {
    return { courier: await getMyCourier(), failed: false }
  } catch (error) {
    recordCommand('LOAD_COURIER', '', 'error', await describeError(error))
    return { courier: null, failed: true }
  }
}

const courierPageLoader = async () => {
  const data = await courierLoader()

  if (data.failed) {
    return { ...data, offer: null, activeOrder: null, orderFailed: false }
  }

  let offer = null
  let activeOrder = null
  let orderFailed = false

  try {
    offer = await getPendingOffer()
  } catch (error) {
    recordCommand('LOAD_OFFER', '', 'error', await describeError(error))
  }

  try {
    activeOrder = await getMyActiveOrder()
  } catch (error) {
    orderFailed = true
    recordCommand('LOAD_COURIER_ORDER', '', 'error', await describeError(error))
  }

  return { ...data, offer, activeOrder, orderFailed }
}

const loadMapCouriers = async () => {
  try {
    return await runCommand('LIST_COURIERS', '', () => listCouriers(), (couriers) => `${couriers.length} couriers`)
  } catch {
    return []
  }
}

const courierAction = async ({ request }) => {
  const form = await request.formData()
  const intent = form.get('intent')
  const orderId = form.get('orderId')
  const command = COURIER_COMMANDS[intent]
  const details = orderId ? `order=${String(orderId).slice(0, 8)}` : ''

  if (!command) {
    recordCommand('COURIER_ACTION', details, 'error', `unknown action ${intent}`)
    return { ok: false, error: `Unknown action ${intent}` }
  }

  try {
    await runCommand(intent, details, () => command(orderId), (result) => describeCourierResult(intent, result))
    return { ok: true }
  } catch (error) {
    return { ok: false, error: await describeError(error) }
  }
}

const restaurantAction = async ({ request, params }) => {
  const form = await request.formData()
  const intent = form.get('intent')
  const itemId = form.get('itemId')
  const details = `restaurant=${params.restaurantId}${itemId ? ` item=${itemId}` : ''}`
  const session = getSession()
  if (intent === 'PAUSE_RESTAURANT' || intent === 'REOPEN_RESTAURANT') {
    const isOperator = session?.role === 'OPERATOR'
    const isOwnStaff = isStaffSession(session) && session.restaurantId === params.restaurantId
    if (!isOperator && !(intent === 'PAUSE_RESTAURANT' && isOwnStaff)) {
      recordCommand(intent, details, 'error', 'You are not allowed to perform this restaurant action')
      return { ok: false, error: 'You are not allowed to perform this restaurant action.' }
    }
    const message = intent === 'PAUSE_RESTAURANT' ? 'Restaurant paused. Existing orders can still be prepared.' : 'Restaurant reopened.'
    try {
      await runCommand(intent, details,
        () => intent === 'PAUSE_RESTAURANT' ? pauseRestaurant(params.restaurantId) : reopenRestaurant(params.restaurantId),
        () => message)
      return { ok: true, message }
    } catch (error) {
      return { ok: false, error: await describeError(error) }
    }
  }
  if (!isStaffSession(session) || session.restaurantId !== params.restaurantId) {
    recordCommand('MENU_ACTION', details, 'error', 'Only staff of this restaurant can edit its menu')
    return { ok: false, error: 'Only staff of this restaurant can edit its menu.' }
  }

  const item = { name: form.get('name'), price: form.get('price') }
  let action
  let message
  switch (intent) {
    case 'ADD_MENU_ITEM':
      action = () => addMenuItem(params.restaurantId, item)
      message = 'Menu item added.'
      break
    case 'EDIT_MENU_ITEM':
      action = () => editMenuItem(params.restaurantId, itemId, item)
      message = 'Menu item updated.'
      break
    case 'CHANGE_MENU_AVAILABILITY':
      action = () => changeMenuAvailability(params.restaurantId, itemId, form.get('status'))
      message = 'Item availability updated.'
      break
    default:
      recordCommand('MENU_ACTION', details, 'error', `unknown action ${intent}`)
      return { ok: false, error: `Unknown menu action ${intent}` }
  }

  try {
    await runCommand(intent, details, action, () => message)
    return { ok: true, intent, message }
  } catch (error) {
    return { ok: false, error: await describeError(error) }
  }
}

const loadRestaurantsWithQueues = async () => {
  requireSession()
  const [restaurants, queues] = await Promise.all([
    getRestaurants(),
    getQueueCounts().catch(async (error) => {
      recordCommand('LOAD_QUEUE_COUNTS', '', 'error', await describeError(error))
      return []
    }),
  ])
  const counts = new Map(queues.map((queue) => [queue.restaurantId, queue]))
  return restaurants.map((restaurant) => ({
    ...restaurant,
    queuedCount: counts.get(restaurant.id)?.queuedCount ?? 0,
    preparingCount: counts.get(restaurant.id)?.preparingCount ?? 0,
    readyCount: counts.get(restaurant.id)?.readyCount ?? 0,
  }))
}

const mapLoader = () => {
  const session = requireLogin()
  ensureSessionLocation(session)
  return runCommand(
    'VIEW_MAP',
    '',
    async () => {
      const isCourier = session.role === 'COURIER'
      const [restaurants, orders, couriers, courier, activeOrder] = await Promise.all([
        getMapRestaurants(),
        isCourier ? [] : listOrders().catch(async (error) => {
          recordCommand('LOAD_MAP_ORDERS', '', 'error', await describeError(error))
          return []
        }),
        loadMapCouriers(),
        isCourier ? getMyCourier().catch(async (error) => {
          recordCommand('LOAD_COURIER', '', 'error', await describeError(error))
          return null
        }) : null,
        isCourier ? getMyActiveOrder().catch(async (error) => {
          recordCommand('LOAD_COURIER_ORDER', '', 'error', await describeError(error))
          return null
        }) : null,
      ])
      return { restaurants, orders, couriers, courier, activeOrder: activeOrder ?? null }
    },
    ({ restaurants }) => `${restaurants.length} restaurants`,
  )
}

const MAP_ORDER_INTENTS = new Set(['ACCEPT', 'REJECT', 'MARK_READY'])

const mapAction = async ({ request }) => {
  const form = await request.clone().formData()
  const intent = form.get('intent')
  if (intent === 'CANCEL') return customerOrdersAction({ request })
  if (MAP_ORDER_INTENTS.has(intent)) return restaurantOrdersAction({ request })
  return restaurantAction({ request, params: { restaurantId: form.get('restaurantId') } })
}

const restaurantsLoader = () => {
  redirectStaffToKitchen()
  redirectCourierHome()
  ensureSessionLocation(getSession())
  return runCommand(
    'BROWSE_RESTAURANTS',
    '',
    loadRestaurantsWithQueues,
    (restaurants) => `${restaurants.length} restaurants`,
  )
}

const restaurantLoader = ({ params }) => {
  const session = getSession()
  if (isStaffSession(session) && session.restaurantId !== params.restaurantId) {
    throw redirect(KITCHEN_PATH)
  }
  redirectCourierHome()
  return runCommand(
    'BROWSE_MENU',
    `restaurant=${params.restaurantId}`,
    async () => {
      requireSession()
      const restaurant = await getRestaurant(params.restaurantId)
      if (!restaurant) {
        throw new Response('Restaurant not found', { status: 404, statusText: 'Not Found' })
      }
      return restaurant
    },
    (restaurant) => `${restaurant.name} (${restaurant.status}), ${restaurant.menu.length} items`,
  )
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        loader: homeLoader,
        element: <HomePage />,
      },
      {
        path: 'login',
        element: <AuthPage initialMode="login" />,
      },
      {
        path: 'signup',
        element: <AuthPage initialMode="signup" />,
      },
      {
        path: 'about',
        element: <AboutPage />,
      },
      {
        path: 'orders',
        loader: customerOrdersLoader,
        action: customerOrdersAction,
        element: <OrdersPage />,
      },
      {
        path: 'courier',
        element: <CourierPage />,
        loader: courierPageLoader,
        action: courierAction,
      },
      {
        path: 'account',
        loader: loginRequiredLoader,
        element: <AccountPage />,
      },
      {
        path: 'cart',
        loader: customerOnlyLoader,
        element: <CartPage />,
      },
      {
        path: 'checkout',
        loader: customerOnlyLoader,
        element: <CheckoutPage />,
      },
      {
        path: 'console',
        element: <ConsolePage />,
      },
      {
        path: 'restaurants',
        element: <RestaurantsPage />,
        loader: restaurantsLoader,
      },
      {
        path: 'map',
        element: <MapPage />,
        loader: mapLoader,
        action: mapAction,
      },
      {
        path: 'restaurants/:restaurantId',
        element: <RestaurantMenuPage />,
        loader: restaurantLoader,
        action: restaurantAction,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
        loader: dashboardLoader,
        children: [
          {
            index: true,
            loader: dashboardIndexLoader,
          },
          {
            path: 'orders',
            loader: () => redirect('/orders'),
          },
          {
            path: 'kitchen',
            element: <RestaurantOrdersPage />,
            loader: restaurantOrdersLoader,
            action: restaurantOrdersAction,
          },
          {
            path: 'courier',
            loader: () => redirect('/courier'),
          },
          {
            path: 'account',
            element: <AccountPage />,
          },
        ],
      },
      {
        path: '*',
        element: <ErrorPage />,
      },
    ],
  },
])
