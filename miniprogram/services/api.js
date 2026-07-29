const env = require('../config/env')

function debugOpenid() {
  return wx.getStorageSync('debugOpenid') || 'local-customer'
}

function normalizeError(response) {
  const payload = response && response.data
  const message = payload && payload.message
    ? payload.message
    : `请求失败（${response.statusCode || '网络异常'}）`
  const error = new Error(message)
  error.code = payload && payload.code
  error.statusCode = response && response.statusCode
  return error
}

function request(options) {
  if (env.mode === 'cloud') {
    return wx.cloud.callContainer({
      config: { env: env.cloudEnv },
      path: options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'X-WX-SERVICE': env.cloudService,
        'content-type': 'application/json'
      }
    }).then((response) => {
      if (response.statusCode >= 200 && response.statusCode < 300) {
        return response.data
      }
      throw normalizeError(response)
    })
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: env.localBaseUrl + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'X-Debug-Openid': debugOpenid(),
        'content-type': 'application/json'
      },
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data)
        } else {
          reject(normalizeError(response))
        }
      },
      fail(error) {
        reject(new Error(error.errMsg || '无法连接 Java 后端'))
      }
    })
  })
}

function uploadImage(filePath) {
  if (env.mode === 'cloud') {
    const suffix = filePath.includes('.') ? filePath.slice(filePath.lastIndexOf('.')) : '.jpg'
    const cloudPath = `product-images/${Date.now()}-${Math.random().toString(16).slice(2)}${suffix}`
    return wx.cloud.uploadFile({ cloudPath, filePath }).then((result) => result.fileID)
  }

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: env.localBaseUrl + '/api/merchant/media',
      filePath,
      name: 'file',
      header: { 'X-Debug-Openid': debugOpenid() },
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(JSON.parse(response.data).url)
        } else {
          let body = {}
          try {
            body = JSON.parse(response.data)
          } catch (_) {
            body.message = '图片上传失败'
          }
          reject(new Error(body.message || '图片上传失败'))
        }
      },
      fail: reject
    })
  })
}

function imageUrl(value) {
  if (!value || value.startsWith('cloud://') || value.startsWith('http')) {
    return value
  }
  return env.mode === 'local' ? env.localBaseUrl + value : value
}

module.exports = { request, uploadImage, imageUrl, debugOpenid }
