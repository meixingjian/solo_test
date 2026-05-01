import request from '../utils/request.js'

export function getConsumeRecords(params) {
  return request({
    url: '/records/consume',
    method: 'GET',
    data: params
  })
}

export function getRecordDetail(id) {
  return request({
    url: `/records/${id}`,
    method: 'GET'
  })
}
