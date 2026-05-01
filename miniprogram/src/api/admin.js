import request from '../utils/request.js'

export function getProjects() {
  return request({
    url: '/admin/projects',
    method: 'GET'
  })
}

export function getProjectDetail(id) {
  return request({
    url: `/admin/projects/${id}`,
    method: 'GET'
  })
}

export function scanQRCode(qrcodeData) {
  return request({
    url: '/admin/scan-qrcode',
    method: 'POST',
    data: { qrcodeData }
  })
}

export function deductCoins(data) {
  return request({
    url: '/admin/deduct-coins',
    method: 'POST',
    data
  })
}

export function getQRCodeValidity() {
  return request({
    url: '/admin/qrcode-validity',
    method: 'GET'
  })
}

export function updateQRCodeValidity(validityTime) {
  return request({
    url: '/admin/qrcode-validity',
    method: 'PUT',
    data: { validityTime }
  })
}
