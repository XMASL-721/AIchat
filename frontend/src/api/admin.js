import request from './request'

// 减脂餐管理
export const addMealApi = (data) => request.post('/admin/meals', data)
export const updateMealApi = (id, data) => request.put(`/admin/meals/${id}`, data)
export const deleteMealApi = (id) => request.delete(`/admin/meals/${id}`)

// 健身训练管理
export const addWorkoutApi = (data) => request.post('/admin/workouts', data)
export const updateWorkoutApi = (id, data) => request.put(`/admin/workouts/${id}`, data)
export const deleteWorkoutApi = (id) => request.delete(`/admin/workouts/${id}`)

// 健康资讯管理
export const addArticleApi = (data) => request.post('/admin/articles', data)
export const updateArticleApi = (id, data) => request.put(`/admin/articles/${id}`, data)
export const deleteArticleApi = (id) => request.delete(`/admin/articles/${id}`)

// 补剂指南管理
export const addSupplementApi = (data) => request.post('/admin/supplements', data)
export const updateSupplementApi = (id, data) => request.put(`/admin/supplements/${id}`, data)
export const deleteSupplementApi = (id) => request.delete(`/admin/supplements/${id}`)
