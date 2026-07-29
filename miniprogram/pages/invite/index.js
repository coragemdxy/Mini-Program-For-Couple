const api = require('../../services/api')
const env = require('../../config/env')
const { toastError } = require('../../utils/format')

Page({
  data: {
    code: '',
    nickname: '',
    submitting: false,
    localMode: env.mode === 'local',
    currentIdentity: api.debugOpenid()
  },

  onLoad() {
    if (env.mode !== 'local') return
    const merchant = api.debugOpenid() === 'local-merchant'
    this.setData({
      code: merchant ? 'LOVE-MERCHANT' : 'LOVE-CUSTOMER',
      nickname: merchant ? '小店老板' : '小可爱'
    })
  },

  onCode(event) {
    this.setData({ code: event.detail.value })
  },

  onNickname(event) {
    this.setData({ nickname: event.detail.value })
  },

  async submit() {
    if (!this.data.code.trim() || !this.data.nickname.trim()) {
      wx.showToast({ title: '请填写昵称和邀请码', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const session = await api.request({
        url: '/api/invitations/redeem',
        method: 'POST',
        data: {
          code: this.data.code.trim(),
          nickname: this.data.nickname.trim()
        }
      })
      getApp().globalData.session = session
      wx.showToast({ title: '欢迎光临', icon: 'success' })
      setTimeout(() => {
        const target = session.role === 'MERCHANT'
          ? '/pages/merchant/orders/index'
          : '/pages/customer/home/index'
        wx.reLaunch({ url: target })
      }, 450)
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ submitting: false })
    }
  },

  switchIdentity() {
    const next = api.debugOpenid() === 'local-merchant' ? 'local-customer' : 'local-merchant'
    wx.setStorageSync('debugOpenid', next)
    wx.reLaunch({ url: '/pages/launch/index' })
  }
})
