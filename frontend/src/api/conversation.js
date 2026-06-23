import request from './request'

export function listConversationsApi(page = 1, size = 20) {
  return request.get('/conversations', { params: { page, size } })
}

export function createConversationApi() {
  return request.post('/conversations')
}

export function deleteConversationApi(id) {
  return request.delete(`/conversations/${id}`)
}

export function getMessagesApi(conversationId, page = 1, size = 50) {
  return request.get(`/conversations/${conversationId}/messages`, { params: { page, size } })
}
