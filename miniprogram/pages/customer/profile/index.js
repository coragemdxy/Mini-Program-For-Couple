const api = require('../../../services/api')
const cart = require('../../../services/cart')
const env = require('../../../config/env')
const { toastError } = require('../../../utils/format')

Page({
  data: {
    session: null,
    notificationSettings: [],
    cartCount: 0,
    localMode: env.mode === 'local'
  },

  onShow() {
    this.setData({ cartCount: cart.count() })
    this.load()
  },

  async load() {
    try {
      const [session, notificationSettings] = await Promise.all([
        api.request({ url: '/api/session' }),
        api.request({ url: '/api/notifications/settings' })
      ])
      this.setData({ session, notificationSettings })
    } catch (error) {
      toastError(error)
    }
  },

  async subscribeOrderStatus() {
    try {
      let result = 'accept'
      if (!this.data.localMode) {
        if (!env.orderStatusTemplateId) {
          throw new Error('请先配置订单状态模板 ID')
        }
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
      wx.showToast({
        title: result === 'accept' ? '已允许订单提醒' : '未允许提醒',
        icon: 'none'
      })
      this.load()
    } catch (error) {
      toastError(error)
    }
  },

  switchToMerchant() {
    wx.setStorageSync('debugOpenid', 'local-merchant')
    wx.reLaunch({ url: '/pages/launch/index' })
  }
})
