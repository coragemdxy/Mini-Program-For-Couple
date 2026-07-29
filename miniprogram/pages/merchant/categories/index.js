const api = require('../../../services/api')
const { toastError } = require('../../../utils/format')

Page({
  data: {
    categories: [],
    editingId: null,
    name: '',
    sortOrder: '10',
    enabled: true
  },

  onShow() {
    this.load()
  },

  async load() {
    try {
      this.setData({ categories: await api.request({ url: '/api/merchant/categories' }) })
    } catch (error) {
      toastError(error)
    }
  },

  edit(event) {
    const category = this.data.categories.find((item) => item.id === Number(event.currentTarget.dataset.id))
    this.setData({
      editingId: category.id,
      name: category.name,
      sortOrder: String(category.sortOrder),
      enabled: category.enabled
    })
  },

  clear() {
    this.setData({ editingId: null, name: '', sortOrder: '10', enabled: true })
  },

  onName(event) {
    this.setData({ name: event.detail.value })
  },

  onSort(event) {
    this.setData({ sortOrder: event.detail.value })
  },

  onEnabled(event) {
    this.setData({ enabled: event.detail.value })
  },

  async save() {
    try {
      const editing = Boolean(this.data.editingId)
      await api.request({
        url: editing
          ? `/api/merchant/categories/${this.data.editingId}`
          : '/api/merchant/categories',
        method: editing ? 'PUT' : 'POST',
        data: {
          name: this.data.name.trim(),
          iconUrl: null,
          sortOrder: Number(this.data.sortOrder || 0),
          enabled: this.data.enabled
        }
      })
      wx.showToast({ title: editing ? '分类已更新' : '分类已创建', icon: 'success' })
      this.clear()
      this.load()
    } catch (error) {
      toastError(error)
    }
  },

  async remove(event) {
    const id = event.currentTarget.dataset.id
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: '删除分类？',
        content: '分类下还有商品时，后端会阻止删除。',
        success: (result) => resolve(result.confirm)
      })
    })
    if (!confirmed) return
    try {
      await api.request({ url: `/api/merchant/categories/${id}`, method: 'DELETE' })
      this.load()
    } catch (error) {
      toastError(error)
    }
  }
})
