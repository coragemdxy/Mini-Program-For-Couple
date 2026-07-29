const KEY = 'takeawayCart'

function getCart() {
  return wx.getStorageSync(KEY) || []
}

function saveCart(items) {
  wx.setStorageSync(KEY, items)
  return items
}

function cartKey(item) {
  return `${item.productId}:${(item.optionIds || []).slice().sort((a, b) => a - b).join(',')}`
}

function addItem(item) {
  const cart = getCart()
  const key = cartKey(item)
  const found = cart.find((entry) => cartKey(entry) === key)
  if (found) {
    found.quantity += item.quantity
  } else {
    cart.push(item)
  }
  return saveCart(cart)
}

function clear() {
  saveCart([])
}

function count() {
  return getCart().reduce((sum, item) => sum + item.quantity, 0)
}

module.exports = { getCart, saveCart, addItem, clear, count }
