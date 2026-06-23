import request from './request'

export function getProfileApi() {
  return request.get('/user/profile')
}

export function updateProfileApi(data) {
  return request.put('/user/profile', data)
}
