const NAVS = {
  customer: [
    { key: 'home', label: '首页', icon: '⌂', url: '/pages/customer/home/index' },
    { key: 'cart', label: '购物篮', icon: '♡', url: '/pages/customer/cart/index' },
    { key: 'orders', label: '订单', icon: '≡', url: '/pages/customer/orders/index' },
    { key: 'profile', label: '我的', icon: '☺', url: '/pages/customer/profile/index' }
  ],
  merchant: [
    { key: 'orders', label: '订单', icon: '≡', url: '/pages/merchant/orders/index' },
    { key: 'products', label: '商品', icon: '◇', url: '/pages/merchant/products/index' },
    { key: 'categories', label: '分类', icon: '▦', url: '/pages/merchant/categories/index' },
    { key: 'settings', label: '设置', icon: '⚙', url: '/pages/merchant/settings/index' }
  ]
}

Component({
  properties: {
    role: {
      type: String,
      value: 'customer'
    },
    active: {
      type: String,
      value: ''
    },
    cartCount: {
      type: Number,
      value: 0
    }
  },

  data: {
    items: []
  },

  observers: {
    'role, active': function updateItems(role, active) {
      const items = (NAVS[role] || []).map((item) => ({
        ...item,
        active: item.key === active
      }))
      this.setData({ items })
    }
  },

  methods: {
    navigate(event) {
      const key = event.currentTarget.dataset.key
      if (key === this.data.active) return
      wx.reLaunch({ url: event.currentTarget.dataset.url })
    }
  }
})
