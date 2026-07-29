function money(value) {
  return Number(value || 0).toFixed(2)
}

function dateTime(value) {
  if (!value) return ''
  const date = new Date(value)
  const pad = (number) => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toastError(error) {
  wx.showToast({
    title: error && error.message ? error.message : '操作失败',
    icon: 'none',
    duration: 2600
  })
}

module.exports = { money, dateTime, toastError }
