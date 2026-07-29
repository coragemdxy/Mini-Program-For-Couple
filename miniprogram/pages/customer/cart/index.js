const api = require('../../../services/api')
const cart = require('../../../services/cart')
const env = require('../../../config/env')
const { money, toastError } = require('../../../utils/format')

Page({
  data: {
    items: [],
    total: '0.00',
    remark: '',
    submitting: false,
    cartCount: 0
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    const items = cart.getCart().map((item) => ({
      ...item,
      displayImage: api.imageUrl(item.image),
      displayPrice: money(item.unitPrice * item.quantity)
    }))
    const total = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    const cartCount = items.reduce((sum, item) => sum + item.quantity, 0)
    this.setData({ items, total: money(total), cartCount })
  },

  changeQuantity(event) {
    const index = Number(event.currentTarget.dataset.index)
    const delta = Number(event.currentTarget.dataset.delta)
    const items = cart.getCart()
    items[index].quantity += delta
    if (items[index].quantity <= 0) items.splice(index, 1)
    cart.saveCart(items)
    this.refresh()
  },

  onRemark(event) {
    this.setData({ remark: event.detail.value })
  },

  async submit() {
    if (!this.data.items.length || this.data.submitting) return
    this.setData({ submitting: true })
    try {
      await this.authorizeStatusNotice()
      const order = await api.request({
        url: '/api/orders',
        method: 'POST',
        data: {
          idempotencyKey: `wx-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`,
          remark: this.data.remark.trim(),
          items: this.data.items.map((item) => ({
            productId: item.productId,
            quantity: item.quantity,
            optionIds: item.optionIds || []
          }))
        }
      })
      cart.clear()
      wx.redirectTo({ url: `/pages/customer/order-detail/index?id=${order.id}&created=1` })
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ submitting: false })
    }
  },

  async authorizeStatusNotice() {
    try {
      let result = 'accept'
      if (env.mode === 'cloud' && env.orderStatusTemplateId) {
        const response = await wx.requestSubscribeMessage({
          tmplIds: [env.orderStatusTemplateId]
        })
        result = response[env.orderStatusTemplateId] || 'reject'
      }
      await api.request({
        url: '/api/notifications/consents',
        method: 'POST',
        data: { type: 'ORDER_STATUS', result }
      })
    } catch (_) {
      // 通知授权失败不应阻止下单。
    }
  }
})
