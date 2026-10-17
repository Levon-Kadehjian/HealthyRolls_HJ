import { useEffect, useState } from 'react'
import { CartContext } from './useCart'
import { recordCommand } from '../console/consoleStore'

const STORAGE_KEY = 'healthyrolls.cart'
const MAX_QUANTITY = 99
const EMPTY_CART = { restaurant: null, lines: [] }

function loadCart() {
  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY))
    return Array.isArray(stored?.lines) ? stored : EMPTY_CART
  } catch {
    return EMPTY_CART
  }
}

function clampQuantity(quantity) {
  return Math.min(Math.max(Math.floor(Number(quantity)) || 1, 1), MAX_QUANTITY)
}

export default function CartProvider({ children }) {
  const [cart, setCart] = useState(loadCart)
  const { restaurant, lines } = cart
  const itemName = (itemId) => lines.find((line) => line.item.itemId === itemId)?.item.name ?? String(itemId)

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(cart))
    } catch {
      return
    }
  }, [cart])

  function addItem(target, item, quantity = 1) {
    const error =
      item.status === 'SOLD_OUT'
        ? `${item.name} is sold out`
        : restaurant && restaurant.id !== target.id && lines.length > 0
          ? `cart already holds items from ${restaurant.name}; an order can only come from one restaurant`
          : null
    recordCommand('ADD_TO_CART', `${clampQuantity(quantity)}x ${item.name}`, error ? 'error' : 'ok', error ?? `added from ${target.name}`)
    if (error) {
      return false
    }
    setCart((current) => {
      const exists = current.lines.some((line) => line.item.itemId === item.itemId)
      return {
        restaurant: target,
        lines: exists
          ? current.lines.map((line) =>
              line.item.itemId === item.itemId ? { ...line, quantity: clampQuantity(line.quantity + quantity) } : line,
            )
          : [...current.lines, { item, quantity: clampQuantity(quantity) }],
      }
    })
    return true
  }

  function replaceCart(target, item) {
    const soldOut = item.status === 'SOLD_OUT'
    recordCommand('REPLACE_CART', `1x ${item.name}`, soldOut ? 'error' : 'ok', soldOut ? `${item.name} is sold out` : `cart now from ${target.name}`)
    if (!soldOut) {
      setCart({ restaurant: target, lines: [{ item, quantity: 1 }] })
    }
    return !soldOut
  }

  function updateQuantity(itemId, quantity) {
    const next = clampQuantity(quantity)
    setCart((current) => ({
      ...current,
      lines: current.lines.map((line) => (line.item.itemId === itemId ? { ...line, quantity: next } : line)),
    }))
    recordCommand('UPDATE_QUANTITY', `${itemName(itemId)} → ${next}`, 'ok', 'OK')
  }

  function removeItem(itemId) {
    setCart((current) => {
      const remaining = current.lines.filter((line) => line.item.itemId !== itemId)
      return remaining.length === 0 ? EMPTY_CART : { ...current, lines: remaining }
    })
    recordCommand('REMOVE_FROM_CART', itemName(itemId), 'ok', 'OK')
  }

  function clearCart() {
    setCart(EMPTY_CART)
    recordCommand('CLEAR_CART', '', 'ok', 'cart emptied')
  }

  const value = {
    restaurant,
    lines,
    itemCount: lines.reduce((sum, line) => sum + line.quantity, 0),
    subtotal: lines.reduce((sum, line) => sum + Number(line.item.price) * line.quantity, 0),
    hasSoldOutItems: lines.some((line) => line.item.status === 'SOLD_OUT'),
    maxQuantity: MAX_QUANTITY,
    addItem,
    replaceCart,
    updateQuantity,
    removeItem,
    clearCart,
  }

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
