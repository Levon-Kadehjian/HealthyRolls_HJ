import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './dash/routes/router'
import CartProvider from './dash/cart/CartProvider'
import { captureGlobalErrors } from './dash/console/consoleStore'
import './index.css'
import './dash/App.css'

captureGlobalErrors()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <CartProvider>
      <RouterProvider router={router} />
    </CartProvider>
  </StrictMode>,
)
