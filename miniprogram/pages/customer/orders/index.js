const api = require('../../../services/api')
const cart = require('../../../services/cart')
const { money, dateTime, toastError } = require('../../../utils/format')

Page({
  data: {
    orders: [],
    loading: true,
    cartCount: 0
  },

  onShow() {
    this.setData({ cartCount: cart.count() })
    this.load()
  },

  onPullDownRefresh() {
    this.load()
  },

  async load() {
    this.setData({ loading: true })
    try {
      const result = await api.request({ url: '/api/orders/my?size=100' })
      this.setData({
        orders: result.content.map((order) => ({
          ...order,
          displayTotal: money(order.totalVirtualPrice),
          displayTime: dateTime(order.createdAt),
          summary: order.items.map((item) => `${item.productName}×${item.quantity}`).join('、')
        }))
      })
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  open(event) {
    wx.navigateTo({ url: `/pages/customer/order-detail/index?id=${event.currentTarget.dataset.id}` })
  }
})
