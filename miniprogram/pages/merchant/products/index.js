const api = require('../../../services/api')
const { money, toastError } = require('../../../utils/format')

Page({
  data: {
    products: [],
    query: '',
    loading: true
  },

  onShow() {
    this.load()
  },

  async load() {
    this.setData({ loading: true })
    try {
      let url = '/api/merchant/products?size=100'
      if (this.data.query.trim()) url += `&query=${encodeURIComponent(this.data.query.trim())}`
      const result = await api.request({ url })
      this.setData({
        products: result.content.map((product) => ({
          ...product,
          displayPrice: money(product.virtualPrice),
          displayCover: api.imageUrl(product.coverUrl)
        }))
      })
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ loading: false })
    }
  },

  onQuery(event) {
    this.setData({ query: event.detail.value })
  },

  add() {
    wx.navigateTo({ url: '/pages/merchant/product-edit/index' })
  },

  edit(event) {
    wx.navigateTo({ url: `/pages/merchant/product-edit/index?id=${event.currentTarget.dataset.id}` })
  },

  async toggle(event) {
    const id = event.currentTarget.dataset.id
    const enabled = event.detail.value
    try {
      await api.request({
        url: `/api/merchant/products/${id}/availability`,
        method: 'PATCH',
        data: { enabled }
      })
      this.load()
    } catch (error) {
      toastError(error)
    }
  }
})
