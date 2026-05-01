import request from '../utils/request.js'

export function getRechargeOptions() {
  return request({
    url: '/recharge/options',
    method: 'GET'
  })
}

export function createRechargeOrder(amount) {
  return request({
    url: '/recharge/order',
    method: 'POST',
    data: { amount }
  })
}

export function getRechargeRecords(params) {
  return request({
    url: '/recharge/records',
    method: 'GET',
    data: params
  })
}
