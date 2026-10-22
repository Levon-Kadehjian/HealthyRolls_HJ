import { createContext, useContext } from 'react'

export const CartContext = createContext(null)

export function useCart() {
  const cart = useContext(CartContext)
  if (!cart) {
    throw new Error('useCart must be used inside a CartProvider')
  }
  return cart
}
