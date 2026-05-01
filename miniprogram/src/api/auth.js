import request from '../utils/request.js'

export function wxLogin(code) {
  return request({
    url: '/auth/wx-login',
    method: 'POST',
    data: { code }
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'GET'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/auth/user-info',
    method: 'PUT',
    data
  })
}
