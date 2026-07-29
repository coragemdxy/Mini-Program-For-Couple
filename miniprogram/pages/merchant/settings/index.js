const api = require('../../../services/api')
const env = require('../../../config/env')
const { dateTime, toastError } = require('../../../utils/format')

Page({
  data: {
    localMode: env.mode === 'local',
    invitation: null,
    notificationSettings: []
  },

  onShow() {
    this.loadSettings()
  },

  async loadSettings() {
    try {
      this.setData({
        notificationSettings: await api.request({ url: '/api/notifications/settings' })
      })
    } catch (error) {
      toastError(error)
    }
  },

  async createInvitation() {
    try {
      const invitation = await api.request({
        url: '/api/merchant/invitations',
        method: 'POST',
        data: { maxUses: 1, validDays: 30 }
      })
      invitation.displayExpiry = dateTime(invitation.expiresAt)
      this.setData({ invitation })
    } catch (error) {
      toastError(error)
    }
  },

  copyInvitation() {
    wx.setClipboardData({ data: this.data.invitation.code })
  },

  async subscribeNewOrder() {
    try {
      let result = 'accept'
      if (!this.data.localMode) {
        if (!env.newOrderTemplateId) {
          throw new Error('请先在 config/env.js 填写新订单模板 ID')
        }
        const response = await wx.requestSubscribeMessage({
          tmplIds: [env.newOrderTemplateId]
        })
        result = response[env.newOrderTemplateId] || 'reject'
      }
      await api.request({
        url: '/api/notifications/consents',
        method: 'POST',
        data: { type: 'NEW_ORDER', result }
      })
      wx.showToast({
        title: result === 'accept' ? '已允许新单提醒' : '未允许提醒',
        icon: 'none'
      })
      this.loadSettings()
    } catch (error) {
      toastError(error)
    }
  },

  switchToCustomer() {
    wx.setStorageSync('debugOpenid', 'local-customer')
    wx.reLaunch({ url: '/pages/launch/index' })
  }
})
