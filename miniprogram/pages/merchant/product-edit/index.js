const api = require('../../../services/api')
const { money, toastError } = require('../../../utils/format')

let editorKey = 0

function nextKey(prefix) {
  editorKey += 1
  return `${prefix}-${Date.now()}-${editorKey}`
}

function optionDraft(option = {}) {
  return {
    _key: nextKey('option'),
    name: option.name || '',
    extraPrice: String(option.extraPrice ?? '0'),
    enabled: option.enabled !== false
  }
}

function groupDraft(group = {}) {
  const maxSelect = Number(group.maxSelect || 1)
  return {
    _key: nextKey('group'),
    name: group.name || '',
    required: group.required !== false,
    mode: maxSelect > 1 ? 'multiple' : 'single',
    minSelect: String(group.minSelect ?? (group.required === false ? 0 : 1)),
    maxSelect: String(maxSelect),
    options: (group.options || [optionDraft()]).map((option) =>
      option._key ? option : optionDraft(option)
    )
  }
}

Page({
  data: {
    editing: false,
    categories: [],
    categoryIndex: 0,
    name: '',
    description: '',
    coverUrl: '',
    displayCover: '',
    virtualPrice: '0',
    stock: '',
    enabled: true,
    recommended: false,
    sortOrder: '10',
    searchKeywords: '',
    optionGroups: [],
    saving: false
  },

  onLoad(options) {
    this.productId = options.id
    this.setData({ editing: Boolean(options.id) })
    this.load()
  },

  async load() {
    try {
      const categories = await api.request({ url: '/api/merchant/categories' })
      this.setData({ categories })
      if (!this.productId) return
      const product = await api.request({ url: `/api/merchant/products/${this.productId}` })
      const categoryIndex = Math.max(0, categories.findIndex((entry) => entry.id === product.categoryId))
      const optionGroups = (product.optionGroups || []).map(groupDraft)
      this.setData({
        categoryIndex,
        name: product.name,
        description: product.description || '',
        coverUrl: product.coverUrl || '',
        displayCover: api.imageUrl(product.coverUrl),
        virtualPrice: money(product.virtualPrice),
        stock: product.stock === null ? '' : String(product.stock),
        enabled: product.enabled,
        recommended: product.recommended,
        sortOrder: String(product.sortOrder),
        searchKeywords: product.searchKeywords || '',
        optionGroups
      })
    } catch (error) {
      toastError(error)
    }
  },

  field(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value })
  },

  boolField(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value })
  },

  category(event) {
    this.setData({ categoryIndex: Number(event.detail.value) })
  },

  addGroup() {
    const optionGroups = this.data.optionGroups.concat(groupDraft({
      name: '',
      required: true,
      minSelect: 1,
      maxSelect: 1,
      options: [optionDraft()]
    }))
    this.setData({ optionGroups })
  },

  removeGroup(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    wx.showModal({
      title: '删除这个规格组？',
      content: '组内的规格选项也会一起删除。',
      success: (result) => {
        if (!result.confirm) return
        const optionGroups = this.data.optionGroups.slice()
        optionGroups.splice(groupIndex, 1)
        this.setData({ optionGroups })
      }
    })
  },

  updateGroupField(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const field = event.currentTarget.dataset.field
    this.setData({
      [`optionGroups[${groupIndex}].${field}`]: event.detail.value
    })
  },

  toggleGroupRequired(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionGroups = this.data.optionGroups.slice()
    const group = optionGroups[groupIndex]
    group.required = event.detail.value
    group.minSelect = group.required ? '1' : '0'
    this.setData({ optionGroups })
  },

  chooseGroupMode(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const mode = event.currentTarget.dataset.mode
    const optionGroups = this.data.optionGroups.slice()
    const group = optionGroups[groupIndex]
    group.mode = mode
    if (mode === 'single') {
      group.maxSelect = '1'
      group.minSelect = group.required ? '1' : '0'
    } else {
      group.maxSelect = String(Math.max(2, Math.min(group.options.length, Number(group.maxSelect) || 2)))
      group.minSelect = group.required ? '1' : '0'
    }
    this.setData({ optionGroups })
  },

  moveGroup(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const delta = Number(event.currentTarget.dataset.delta)
    const targetIndex = groupIndex + delta
    const optionGroups = this.data.optionGroups.slice()
    if (targetIndex < 0 || targetIndex >= optionGroups.length) return
    ;[optionGroups[groupIndex], optionGroups[targetIndex]] =
      [optionGroups[targetIndex], optionGroups[groupIndex]]
    this.setData({ optionGroups })
  },

  addOption(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionGroups = this.data.optionGroups.slice()
    optionGroups[groupIndex].options.push(optionDraft())
    this.setData({ optionGroups })
  },

  removeOption(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionIndex = Number(event.currentTarget.dataset.optionIndex)
    const optionGroups = this.data.optionGroups.slice()
    const options = optionGroups[groupIndex].options
    if (options.length <= 1) {
      wx.showToast({ title: '每个规格组至少保留一个选项', icon: 'none' })
      return
    }
    options.splice(optionIndex, 1)
    const group = optionGroups[groupIndex]
    group.maxSelect = String(Math.min(Number(group.maxSelect) || 1, options.length))
    if (Number(group.minSelect) > Number(group.maxSelect)) {
      group.minSelect = group.maxSelect
    }
    this.setData({ optionGroups })
  },

  updateOptionField(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionIndex = Number(event.currentTarget.dataset.optionIndex)
    const field = event.currentTarget.dataset.field
    this.setData({
      [`optionGroups[${groupIndex}].options[${optionIndex}].${field}`]: event.detail.value
    })
  },

  toggleOptionEnabled(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionIndex = Number(event.currentTarget.dataset.optionIndex)
    this.setData({
      [`optionGroups[${groupIndex}].options[${optionIndex}].enabled`]: event.detail.value
    })
  },

  moveOption(event) {
    const groupIndex = Number(event.currentTarget.dataset.groupIndex)
    const optionIndex = Number(event.currentTarget.dataset.optionIndex)
    const delta = Number(event.currentTarget.dataset.delta)
    const targetIndex = optionIndex + delta
    const optionGroups = this.data.optionGroups.slice()
    const options = optionGroups[groupIndex].options
    if (targetIndex < 0 || targetIndex >= options.length) return
    ;[options[optionIndex], options[targetIndex]] =
      [options[targetIndex], options[optionIndex]]
    this.setData({ optionGroups })
  },

  async chooseImage() {
    try {
      const selected = await wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sizeType: ['compressed']
      })
      wx.showLoading({ title: '上传中' })
      const coverUrl = await api.uploadImage(selected.tempFiles[0].tempFilePath)
      this.setData({ coverUrl, displayCover: api.imageUrl(coverUrl) })
    } catch (error) {
      if (!String(error.errMsg || '').includes('cancel')) toastError(error)
    } finally {
      wx.hideLoading()
    }
  },

  async save() {
    if (!this.data.categories.length) {
      wx.showToast({ title: '请先创建分类', icon: 'none' })
      return
    }
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请填写商品名称', icon: 'none' })
      return
    }
    const virtualPrice = Number(this.data.virtualPrice)
    if (!Number.isFinite(virtualPrice) || virtualPrice < 0) {
      wx.showToast({ title: '商品价格必须是大于等于 0 的数字', icon: 'none' })
      return
    }
    const stock = this.data.stock === '' ? null : Number(this.data.stock)
    if (stock !== null && (!Number.isInteger(stock) || stock < 0)) {
      wx.showToast({ title: '库存必须是大于等于 0 的整数', icon: 'none' })
      return
    }
    let optionGroups
    try {
      optionGroups = this.buildOptionGroups()
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none', duration: 3200 })
      return
    }
    const body = {
      categoryId: this.data.categories[this.data.categoryIndex].id,
      name: this.data.name.trim(),
      description: this.data.description.trim(),
      coverUrl: this.data.coverUrl || null,
      virtualPrice,
      stock,
      enabled: this.data.enabled,
      recommended: this.data.recommended,
      sortOrder: Number(this.data.sortOrder || 0),
      searchKeywords: this.data.searchKeywords.trim(),
      optionGroups
    }
    this.setData({ saving: true })
    try {
      const url = this.productId
        ? `/api/merchant/products/${this.productId}`
        : '/api/merchant/products'
      await api.request({
        url,
        method: this.productId ? 'PUT' : 'POST',
        data: body
      })
      wx.showToast({ title: '商品已保存', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 450)
    } catch (error) {
      toastError(error)
    } finally {
      this.setData({ saving: false })
    }
  },

  buildOptionGroups() {
    const seenGroupNames = new Set()
    return this.data.optionGroups.map((group, groupIndex) => {
      const groupName = group.name.trim()
      if (!groupName) {
        throw new Error(`请填写第 ${groupIndex + 1} 个规格组的名称`)
      }
      if (seenGroupNames.has(groupName)) {
        throw new Error(`规格组名称“${groupName}”重复了`)
      }
      seenGroupNames.add(groupName)
      const seenOptionNames = new Set()
      const options = group.options.map((option, optionIndex) => {
        const optionName = option.name.trim()
        const extraPrice = Number(option.extraPrice || 0)
        if (!optionName) {
          throw new Error(`请填写“${groupName}”第 ${optionIndex + 1} 个选项的名称`)
        }
        if (!Number.isFinite(extraPrice) || extraPrice < 0) {
          throw new Error(`“${optionName}”的加价必须大于等于 0`)
        }
        if (seenOptionNames.has(optionName)) {
          throw new Error(`“${groupName}”中的选项“${optionName}”重复了`)
        }
        seenOptionNames.add(optionName)
        return {
          name: optionName,
          extraPrice,
          enabled: option.enabled,
          sortOrder: (optionIndex + 1) * 10
        }
      })
      const maxSelect = group.mode === 'single' ? 1 : Number(group.maxSelect)
      const minSelect = group.required
        ? (group.mode === 'single' ? 1 : Number(group.minSelect))
        : 0
      if (!Number.isInteger(minSelect) || minSelect < 0) {
        throw new Error(`“${groupName}”的最少选择数量不正确`)
      }
      if (!Number.isInteger(maxSelect) || maxSelect < 1 || maxSelect > options.length) {
        throw new Error(`“${groupName}”的最多选择数量应为 1～${options.length}`)
      }
      if (minSelect > maxSelect) {
        throw new Error(`“${groupName}”的最少选择数量不能大于最多数量`)
      }
      const enabledCount = options.filter((option) => option.enabled).length
      if (enabledCount < minSelect) {
        throw new Error(`“${groupName}”至少需要 ${minSelect} 个可选择的选项`)
      }
      return {
        name: groupName,
        required: group.required,
        minSelect,
        maxSelect,
        sortOrder: (groupIndex + 1) * 10,
        options
      }
    })
  },

  async remove() {
    const confirmed = await new Promise((resolve) => {
      wx.showModal({
        title: '删除商品？',
        content: '历史订单不受影响，但商品无法恢复。',
        success: (result) => resolve(result.confirm)
      })
    })
    if (!confirmed) return
    try {
      await api.request({
        url: `/api/merchant/products/${this.productId}`,
        method: 'DELETE'
      })
      wx.navigateBack()
    } catch (error) {
      toastError(error)
    }
  }
})
