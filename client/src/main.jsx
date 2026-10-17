import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './routes/router'
import CartProvider from './cart/CartProvider'
import { captureGlobalErrors } from './console/consoleStore'
import './index.css'
import './App.css'

captureGlobalErrors()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <CartProvider>
      <RouterProvider router={router} />
    </CartProvider>
  </StrictMode>,
)
