import { createBrowserRouter, redirect } from 'react-router-dom'
import { getSession } from '../auth/session'
import { recordCommand } from '../console/consoleStore'
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
import RestaurantOrdersPage from '../pages/RestaurantOrdersPage'
import RestaurantsPage from '../pages/RestaurantsPage'
import RestaurantMenuPage from '../pages/RestaurantMenuPage'
import ConsolePage from '../pages/ConsolePage'
import ErrorPage from '../pages/ErrorPage'
import { addMenuItem, editMenuItem, changeMenuAvailability, pauseRestaurant, reopenRestaurant, getRestaurant, getRestaurants } from '../api/restaurants'
import { applyOrderAction, getBillingHistory, listOrderEvents, listOrders, refundOrder } from '../api/orders'

const ORDER_COMMANDS = {
  ACCEPT: 'ACCEPT_ORDER',
  REJECT: 'REJECT_ORDER',
  MARK_READY: 'MARK_ORDER_READY',
}

const KITCHEN_PATH = '/dashboard/kitchen'
const COURIER_PATH = '/dashboard/orders'

const isStaffSession = (session) => session?.role === 'RESTAURANT_STAFF'
const isCourierSession = (session) => session?.role === 'COURIER'

const redirectStaffToKitchen = () => {
  if (isStaffSession(getSession())) {
    throw redirect(KITCHEN_PATH)
  }
}

const redirectCourierToOrders = () => {
  if (isCourierSession(getSession())) {
    throw redirect(COURIER_PATH)
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
  redirectCourierToOrders()
  if (session.role !== 'CUSTOMER') {
    throw redirect('/dashboard')
  }
  return null
}

const homeLoader = () => {
  redirectStaffToKitchen()
  redirectCourierToOrders()
  return null
}

const dashboardLoader = () => ({ session: requireLogin() })

const dashboardIndexLoader = () => {
  const session = getSession()
  return redirect(isStaffSession(session) ? 'kitchen' : 'orders')
}

const loadRestaurantCatalog = async (restaurantIds) => {
  const ids = [...new Set(restaurantIds)].filter(Boolean)
  const restaurants = await Promise.all(ids.map((id) => getRestaurant(id).catch(() => null)))
  return restaurants.filter(Boolean)
}

const loadOrderHistory = async (knownRestaurantIds = []) => {
  const [orders, events, ledger] = await Promise.all([
    listOrders(),
    listOrderEvents().catch(() => []),
    getBillingHistory(),
  ])
  const restaurants = await loadRestaurantCatalog([...knownRestaurantIds, ...orders.map((order) => order.restaurantId)])
  return { orders: buildOrderHistory(orders, events, restaurants, ledger), restaurants }
}

const customerOrdersLoader = async () => {
  const session = requireLogin()
  redirectStaffToKitchen()
  try {
    const { orders } = await runCommand(
      'LIST_ORDERS',
      `role=${session.role}`,
      () => loadOrderHistory(),
      (history) => `${history.orders.length} orders`,
    )
    return { orders, failed: false }
  } catch {
    return { orders: [], failed: true }
  }
}

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
      () => loadOrderHistory([session.restaurantId]),
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

const restaurantsLoader = () => {
  redirectStaffToKitchen()
  redirectCourierToOrders()
  return runCommand(
    'BROWSE_RESTAURANTS',
    '',
    async () => {
      requireSession()
      return getRestaurants()
    },
    (restaurants) => `${restaurants.length} restaurants`,
  )
}

const restaurantLoader = ({ params }) => {
  const session = getSession()
  if (isStaffSession(session) && session.restaurantId !== params.restaurantId) {
    throw redirect(KITCHEN_PATH)
  }
  redirectCourierToOrders()
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
            loader: customerOrdersLoader,
            action: customerOrdersAction,
            element: <OrdersPage />,
          },
          {
            path: 'kitchen',
            element: <RestaurantOrdersPage />,
            loader: restaurantOrdersLoader,
            action: restaurantOrdersAction,
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

async function runCommand(name, details, command, describeSuccess = () => 'ok') {
  recordCommand(name, details, 'running', 'in progress')
  try {
    const result = await command()
    recordCommand(name, details, 'ok', describeSuccess(result))
    return result
  } catch (error) {
    const message = await describeError(error)
    recordCommand(name, details, 'error', message)
    throw error
  }
}

async function describeError(error) {
  if (error && typeof error === 'object' && 'text' in error && typeof error.text === 'function') {
    try {
      const body = await error.text()
      try {
        const json = JSON.parse(body)
        return json.detail || json.message || json.title || body
      } catch {
        return body
      }
    } catch {
      return error.statusText || 'Request failed'
    }
  }
  return error?.message || 'Operation failed'
}
