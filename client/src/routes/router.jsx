import { createBrowserRouter } from 'react-router-dom'
import Root from '../layouts/Root'
import ErrorPage from '../pages/ErrorPage'
import HomePage from '../pages/HomePage'
import AboutPage from '../pages/AboutPage'
import OrdersPage from '../pages/OrdersPage'
import AccountPage from '../pages/AccountPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'about',
        element: <AboutPage />,
      },
      {
        path: 'orders',
        element: <OrdersPage />,
      },
      {
        path: 'account',
        element: <AccountPage />,
      },
      {
        path: '*',
        element: <ErrorPage />,
      },
    ],
  },
])
