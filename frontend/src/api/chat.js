import { ElMessage } from 'element-plus'

/**
 * 使用 fetch + ReadableStream 消费 SSE 流式接口
 * @param {number} conversationId - 会话 ID
 * @param {string} message - 用户消息
 * @param {function} onToken - 收到每个 token 的回调
 * @param {function} onDone - 流结束的回调
 * @param {function} onError - 出错的回调
 * @returns {AbortController} 用于取消请求的 controller
 */
export function streamChat(conversationId, message, onToken, onDone, onError) {
  const controller = new AbortController()
  const token = localStorage.getItem('token')

  // 前端超时：2 分钟（防无限挂起）
  const timeoutId = setTimeout(() => {
    controller.abort()
    ElMessage.error('AI 响应超时，请稍后重试')
    onError(new Error('timeout'))
  }, 120000)

  fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ conversationId, message }),
    signal: controller.signal
  }).then(async response => {
    clearTimeout(timeoutId)
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '[DONE]') {
            onDone()
            return
          }
          if (data.startsWith('[ERROR]')) {
            ElMessage.error(data.replace('[ERROR]', ''))
            onDone()
            return
          }
          onToken(data)
        }
      }
    }
    onDone()
  }).catch(err => {
    clearTimeout(timeoutId)
    if (err.name === 'AbortError') return
    onError(err)
  })

  return controller
}
