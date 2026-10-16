import { createBrowserRouter } from 'react-router-dom'
import Root from '../layouts/Root'
import ErrorPage from '../pages/ErrorPage'
import HomePage from '../pages/HomePage'
import MenuPage from '../pages/MenuPage'
import OrdersPage from '../pages/OrdersPage'
import { menuLoader } from '../loaders/menuLoader'

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
        path: 'menu',
        element: <MenuPage />,
        loader: menuLoader,
      },
      {
        path: 'orders',
        element: <OrdersPage />,
      },
      {
        path: '*',
        element: <ErrorPage />,
      },
    ],
  },
])
