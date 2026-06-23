import request from './request'

export function listSupplementsApi(page = 1, size = 20, keyword = '', category = '') {
  return request.get('/supplements', { params: { page, size, keyword, category } })
}

export function getSupplementApi(id) {
  return request.get(`/supplements/${id}`)
}
