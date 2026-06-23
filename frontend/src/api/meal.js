import request from './request'

export function listMealsApi(page = 1, size = 20, keyword = '') {
  return request.get('/meals', { params: { page, size, keyword } })
}

export function getMealApi(id) {
  return request.get(`/meals/${id}`)
}

export function getRandomMealApi() {
  return request.get('/meals/random')
}
