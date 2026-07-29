const api = require('../../../services/api')
const { money, dateTime, toastError } = require('../../../utils/format')

function actionsFor(status) {
  if (status === 'SUBMITTED') {
    return [
      { status: 'ACCEPTED', label: '接受订单', style: 'primary' },
      { status: 'REJECTED', label: '无法完成', style: 'danger' }
    ]
  }
  if (status === 'ACCEPTED') {
    return [
      { status: 'PREPARING', label: '开始准备', style: 'primary' },
      { status: 'CANCELLED', label: '取消订单', style: 'danger' }
    ]
  }
  if (status === 'PREPARING') {
    return [
      { status: 'COMPLETED', label: '已经完成', style: 'primary' },
      { status: 'CANCELLED', label: '取消订单', style: 'danger' }
    ]
  }
  return []
}

Page({
  data: {
    order: null,
    actions: [],
    message: ''
  },

  onLoad(options) {
    this.orderId = options.id
  },

  onShow() {
    this.load()
  },

  async load() {
    try {
      const order = await api.request({ url: `/api/merchant/orders/${this.orderId}` })
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
      this.setData({ order, actions: actionsFor(order.status) })
    } catch (error) {
      toastError(error)
    }
  },

  onMessage(event) {
    this.setData({ message: event.detail.value })
  },

  async changeStatus(event) {
    const status = event.currentTarget.dataset.status
    if (status === 'CANCELLED' || status === 'REJECTED') {
      const confirmed = await new Promise((resolve) => {
        wx.showModal({
          title: '确认取消订单？',
          content: '取消后不能恢复。',
          success: (result) => resolve(result.confirm)
        })
      })
      if (!confirmed) return
    }
    try {
      await api.request({
        url: `/api/merchant/orders/${this.orderId}/status`,
        method: 'PATCH',
        data: { status, message: this.data.message.trim() }
      })
      wx.showToast({ title: '状态已更新', icon: 'success' })
      this.setData({ message: '' })
      this.load()
    } catch (error) {
      toastError(error)
    }
  }
})
