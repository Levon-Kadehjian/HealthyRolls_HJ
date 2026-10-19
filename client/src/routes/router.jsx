import { createBrowserRouter, redirect } from 'react-router-dom'
import Root from '../layouts/Root'
import Dashboard from '../layouts/Dashboard'
import ErrorPage from '../pages/ErrorPage'
import HomePage from '../pages/HomePage'
import AboutPage from '../pages/AboutPage'
import OrdersPage from '../pages/OrdersPage'
import AccountPage from '../pages/AccountPage'
import CartPage from '../pages/CartPage'
import CheckoutPage from '../pages/CheckoutPage'
import RestaurantMenuPage from '../pages/RestaurantMenuPage'
import RestaurantsPage from '../pages/RestaurantsPage'
import RestaurantOrdersPage from '../pages/RestaurantOrdersPage'
import ConsolePage from '../pages/ConsolePage'
import AuthPage from '../pages/AuthPage'
import { getRestaurant, getRestaurants } from '../api/restaurants'
import { applyOrderAction, listOrders } from '../api/orders'
import { getSession } from '../auth/session'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'


const ORDER_COMMANDS = {
  ACCEPT: 'ACCEPT_ORDER',
  REJECT: 'REJECT_ORDER',
  MARK_READY: 'MARK_ORDER_READY',
}

const KITCHEN_PATH = '/dashboard/kitchen'

const isStaffSession = (session) => session?.role === 'RESTAURANT_STAFF'

const redirectStaffToKitchen = () => {
  if (isStaffSession(getSession())) {
    throw redirect(KITCHEN_PATH)
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
  requireLogin()
  redirectStaffToKitchen()
  return null
}

const homeLoader = () => {
  redirectStaffToKitchen()
  return null
}

const dashboardLoader = () => ({ session: requireLogin() })

const dashboardIndexLoader = () => {
  const session = getSession()
  return redirect(isStaffSession(session) ? 'kitchen' : 'orders')
}

const restaurantOrdersLoader = async () => {
  const session = requireLogin()
  if (!isStaffSession(session)) {
    recordCommand('OPEN_KITCHEN', `role=${session?.role ?? 'none'}`, 'error', 'restaurant staff only')
    throw redirect('/dashboard')
  }
  try {
    const orders = await runCommand('LIST_ORDERS', `role=${session.role}`, listOrders, (list) => `${list.length} orders`)
    return { orders, failed: false }
  } catch {
    return { orders: [], failed: true }
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

const restaurantsLoader = () => {
  redirectStaffToKitchen()
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
  redirectStaffToKitchen()
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
        loader: customerOnlyLoader,
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
            loader: customerOnlyLoader,
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
