import request from '../utils/request.js'

export function generateQRCode() {
  return request({
    url: '/qrcode/generate',
    method: 'POST'
  })
}

export function getQRCodeInfo() {
  return request({
    url: '/qrcode/info',
    method: 'GET'
  })
}

export function refreshQRCode() {
  return request({
    url: '/qrcode/refresh',
    method: 'POST'
  })
}
