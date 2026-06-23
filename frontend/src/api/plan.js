import request from './request'

export function getUserPlansApi(page = 1, size = 10) {
  return request.get('/plans', { params: { page, size } })
}

export function getPlanApi(id) {
  return request.get(`/plans/${id}`)
}

export function deletePlanApi(id) {
  return request.delete(`/plans/${id}`)
}

// 流式生成计划需要特殊处理，在组件中直接实现
export function generatePlanStream(planType, inputData) {
  return { planType, inputData }
}
