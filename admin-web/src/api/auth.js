import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/wx-login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/auth/user-info',
    method: 'put',
    data
  })
}
