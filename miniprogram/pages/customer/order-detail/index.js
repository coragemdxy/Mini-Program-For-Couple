const api = require('../../../services/api')
const { money, dateTime, toastError } = require('../../../utils/format')

Page({
  data: {
    order: null,
    created: false
  },

  onLoad(options) {
    this.orderId = options.id
    this.setData({ created: options.created === '1' })
  },

  onShow() {
    this.load()
  },

  async load() {
    try {
      const order = await api.request({ url: `/api/orders/${this.orderId}` })
      order.displayTotal = money(order.totalVirtualPrice)
      order.displayTime = dateTime(order.createdAt)
      order.items = order.items.map((item) => ({
        ...item,
        displayTotal: money(item.lineTotal)
      }))
      order.events = order.events.map((event) => ({
        ...event,
        displayTime: dateTime(event.createdAt)
      }))
      this.setData({ order })
    } catch (error) {
      toastError(error)
    }
  },

  async cancel() {
    const answer = await new Promise((resolve) => {
      wx.showModal({
        title: '取消订单？',
        content: '老板还没接单，现在可以取消。',
        success: (result) => resolve(result.confirm)
      })
    })
    if (!answer) return
    try {
      await api.request({ url: `/api/orders/${this.orderId}/cancel`, method: 'POST' })
      wx.showToast({ title: '订单已取消' })
      this.load()
    } catch (error) {
      toastError(error)
    }
  }
})
