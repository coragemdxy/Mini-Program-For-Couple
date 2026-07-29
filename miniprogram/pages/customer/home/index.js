const api = require('../../../services/api')
const cart = require('../../../services/cart')
const { money, toastError } = require('../../../utils/format')

Page({
  data: {
    categories: [],
    products: [],
    categoryId: null,
    query: '',
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
      const params = []
      if (this.data.categoryId) params.push(`categoryId=${this.data.categoryId}`)
      if (this.data.query.trim()) params.push(`query=${encodeURIComponent(this.data.query.trim())}`)
      params.push('size=100')
      const [categories, result] = await Promise.all([
        api.request({ url: '/api/categories' }),
        api.request({ url: `/api/products?${params.join('&')}` })
      ])
      const products = result.content.map((product) => ({
        ...product,
        displayPrice: money(product.virtualPrice),
        displayCover: api.imageUrl(product.coverUrl)
      }))
      this.setData({ categories, products })
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  chooseCategory(event) {
    const value = event.currentTarget.dataset.id
    this.setData({ categoryId: value === 'all' ? null : Number(value) })
    this.load()
  },

  onQuery(event) {
    this.setData({ query: event.detail.value })
  },

  search() {
    this.load()
  },

  openProduct(event) {
    wx.navigateTo({ url: `/pages/customer/product-detail/index?id=${event.currentTarget.dataset.id}` })
  }
})
