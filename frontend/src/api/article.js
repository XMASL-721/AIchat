import request from './request'

export function listArticlesApi(page = 1, size = 20, keyword = '', category = '') {
  return request.get('/articles', { params: { page, size, keyword, category } })
}

export function getArticleApi(id) {
  return request.get(`/articles/${id}`)
}
