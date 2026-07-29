const api = require('../../services/api')
const env = require('../../config/env')
const { toastError } = require('../../utils/format')

Page({
  data: {
    loading: true,
    localMode: env.mode === 'local',
    currentIdentity: api.debugOpenid()
  },

  onShow() {
    this.bootstrap()
  },

  async bootstrap() {
    this.setData({ loading: true })
    try {
      const session = await api.request({ url: '/api/session' })
      getApp().globalData.session = session
      if (session.state === 'BLOCKED') {
        this.setData({ loading: false })
        wx.showModal({
          title: '暂时无法使用',
          content: '当前账号已被停用，请联系小店老板。',
          showCancel: false
        })
        return
      }
      if (session.state !== 'ACTIVE') {
        wx.redirectTo({ url: '/pages/invite/index' })
        return
      }
      const target = session.role === 'MERCHANT'
        ? '/pages/merchant/orders/index'
        : '/pages/customer/home/index'
      wx.reLaunch({ url: target })
    } catch (error) {
      this.setData({ loading: false })
      toastError(error)
    }
  },

  useCustomer() {
    wx.setStorageSync('debugOpenid', 'local-customer')
    this.setData({ currentIdentity: 'local-customer' })
    this.bootstrap()
  },

  useMerchant() {
    wx.setStorageSync('debugOpenid', 'local-merchant')
    this.setData({ currentIdentity: 'local-merchant' })
    this.bootstrap()
  }
})
