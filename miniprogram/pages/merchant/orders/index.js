const api = require('../../../services/api')
const { money, dateTime, toastError } = require('../../../utils/format')

const FILTERS = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'SUBMITTED' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' }
]

Page({
  data: {
    filters: FILTERS,
    selectedFilter: '',
    orders: [],
    loading: true
  },

  onShow() {
    this.load()
  },

  async load() {
    this.setData({ loading: true })
    try {
      let url = '/api/merchant/orders?size=100'
      const selected = this.data.selectedFilter
      if (selected && selected !== 'ACTIVE') url += `&status=${selected}`
      const result = await api.request({ url })
      let orders = result.content
      if (selected === 'ACTIVE') {
        orders = orders.filter((order) => ['ACCEPTED', 'PREPARING'].includes(order.status))
      }
      this.setData({
        orders: orders.map((order) => ({
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

  onPullDownRefresh() {
    this.load()
  },

  chooseFilter(event) {
    this.setData({ selectedFilter: event.currentTarget.dataset.value })
    this.load()
  },

  open(event) {
    wx.navigateTo({ url: `/pages/merchant/order-detail/index?id=${event.currentTarget.dataset.id}` })
  }
})
