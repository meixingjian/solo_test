import request from '@/utils/request'

export function getQRCodeValidity() {
  return request({
    url: '/admin/qrcode-validity',
    method: 'get'
  })
}

export function updateQRCodeValidity(data) {
  return request({
    url: '/admin/qrcode-validity',
    method: 'put',
    data
  })
}
