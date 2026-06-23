import request from './request'

export function addFavoriteApi(targetType, targetId) {
  return request.post('/favorites', { targetType, targetId })
}

export function removeFavoriteApi(targetType, targetId) {
  return request.delete('/favorites', { params: { targetType, targetId } })
}

export function getUserFavoritesApi(targetType = null, page = 1, size = 20) {
  return request.get('/favorites', { params: { targetType, page, size } })
}

export function checkFavoriteApi(targetType, targetId) {
  return request.get('/favorites/check', { params: { targetType, targetId } })
}

export function getFavoriteStatsApi() {
  return request.get('/favorites/stats')
}
