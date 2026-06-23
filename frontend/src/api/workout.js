import request from './request'

export function listWorkoutsApi(page = 1, size = 20, keyword = '', category = '') {
  return request.get('/workouts', { params: { page, size, keyword, category } })
}

export function getWorkoutApi(id) {
  return request.get(`/workouts/${id}`)
}

export function getRandomWorkoutApi() {
  return request.get('/workouts/random')
}
