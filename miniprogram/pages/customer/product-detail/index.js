const api = require('../../../services/api')
const cart = require('../../../services/cart')
const { money, toastError } = require('../../../utils/format')

Page({
  data: {
    product: null,
    quantity: 1,
    displayPrice: '0.00',
    loading: true
  },

  onLoad(options) {
    this.productId = options.id
    this.load()
  },

  async load() {
    try {
      const product = await api.request({ url: `/api/products/${this.productId}` })
      product.displayCover = api.imageUrl(product.coverUrl)
      product.optionGroups = (product.optionGroups || []).map((group) => ({
        ...group,
        options: group.options.map((option) => ({
          ...option,
          selected: false,
          displayExtra: money(option.extraPrice)
        }))
      }))
      this.setData({
        product,
        displayPrice: money(product.virtualPrice)
      })
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ loading: false })
    }
  },

  toggleOption(event) {
    const groupId = Number(event.currentTarget.dataset.group)
    const optionId = Number(event.currentTarget.dataset.option)
    const product = this.data.product
    const group = product.optionGroups.find((entry) => entry.id === groupId)
    const option = group.options.find((entry) => entry.id === optionId)
    if (group.maxSelect === 1) {
      group.options.forEach((entry) => { entry.selected = entry.id === optionId })
    } else if (option.selected) {
      option.selected = false
    } else {
      const count = group.options.filter((entry) => entry.selected).length
      if (count >= group.maxSelect) {
        wx.showToast({ title: `最多选择 ${group.maxSelect} 项`, icon: 'none' })
        return
      }
      option.selected = true
    }
    this.updatePrice(product)
  },

  updatePrice(product) {
    const extras = product.optionGroups
      .flatMap((group) => group.options)
      .filter((option) => option.selected)
      .reduce((sum, option) => sum + Number(option.extraPrice), 0)
    this.setData({
      product,
      displayPrice: money((Number(product.virtualPrice) + extras) * this.data.quantity)
    })
  },

  minus() {
    if (this.data.quantity <= 1) return
    this.setData({ quantity: this.data.quantity - 1 })
    this.updatePrice(this.data.product)
  },

  plus() {
    if (this.data.quantity >= 99) return
    this.setData({ quantity: this.data.quantity + 1 })
    this.updatePrice(this.data.product)
  },

  addToCart() {
    const product = this.data.product
    for (const group of product.optionGroups) {
      const count = group.options.filter((option) => option.selected).length
      if (count < group.minSelect || (group.required && count === 0)) {
        wx.showToast({ title: `请选择${group.name}`, icon: 'none' })
        return
      }
    }
    const selected = product.optionGroups
      .flatMap((group) => group.options)
      .filter((option) => option.selected)
    const unitPrice = Number(product.virtualPrice) +
      selected.reduce((sum, option) => sum + Number(option.extraPrice), 0)
    cart.addItem({
      productId: product.id,
      name: product.name,
      image: product.coverUrl,
      unitPrice,
      quantity: this.data.quantity,
      optionIds: selected.map((option) => option.id),
      optionSummary: selected.map((option) => option.name).join('、')
    })
    wx.showToast({ title: '已放进购物篮', icon: 'success' })
    setTimeout(() => wx.navigateBack(), 450)
  }
})
