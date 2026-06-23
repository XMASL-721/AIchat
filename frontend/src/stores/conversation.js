import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listConversationsApi,
  createConversationApi,
  deleteConversationApi,
  getMessagesApi
} from '../api/conversation'
import { streamChat } from '../api/chat'

export const useConversationStore = defineStore('conversation', () => {
  // ===== 状态 =====
  const conversations = ref([])
  const currentConversationId = ref(null)
  const messages = ref([])
  const isLoading = ref(false)
  const isStreaming = ref(false)
  const streamingContent = ref('')

  // 分页状态
  const conversationTotal = ref(0)
  const conversationPage = ref(1)
  const conversationHasMore = ref(false)

  const messageTotal = ref(0)
  const messagePage = ref(1)
  const messageHasMore = ref(false)

  // ===== 方法 =====

  /** 加载会话列表（第一页或加载更多） */
  async function loadConversations(loadMore = false) {
    if (loadMore && !conversationHasMore.value) return

    const page = loadMore ? conversationPage.value + 1 : 1
    try {
      const res = await listConversationsApi(page, 20)
      const { list, total, hasMore } = res.data

      if (loadMore) {
        conversations.value.push(...list)
      } else {
        conversations.value = list
      }
      conversationTotal.value = total
      conversationPage.value = page
      conversationHasMore.value = hasMore
    } catch (e) {
      // 错误已在拦截器中处理
    }
  }

  /** 新建会话 */
  async function createConversation() {
    try {
      const res = await createConversationApi()
      const conv = res.data
      conversations.value.unshift(conv)
      await switchConversation(conv.id)
      return conv
    } catch (e) {
      return null
    }
  }

  /** 删除会话 */
  async function deleteConversation(id) {
    try {
      await deleteConversationApi(id)
      conversations.value = conversations.value.filter(c => c.id !== id)
      if (currentConversationId.value === id) {
        currentConversationId.value = null
        messages.value = []
      }
    } catch (e) {
      // 错误已在拦截器中处理
    }
  }

  /** 切换当前会话 */
  async function switchConversation(id) {
    currentConversationId.value = id
    messages.value = []
    messagePage.value = 1
    messageHasMore.value = false
    streamingContent.value = ''
    await loadMessages()
  }

  /** 加载消息（第一页或加载更多） */
  async function loadMessages(loadMore = false) {
    if (!currentConversationId.value) return
    if (loadMore && !messageHasMore.value) return

    const page = loadMore ? messagePage.value + 1 : 1
    try {
      const res = await getMessagesApi(currentConversationId.value, page, 50)
      const { list, total, hasMore } = res.data

      if (loadMore) {
        messages.value = [...list, ...messages.value]
      } else {
        messages.value = list
      }
      messageTotal.value = total
      messagePage.value = page
      messageHasMore.value = hasMore
    } catch (e) {
      // 错误已在拦截器中处理
    }
  }

  /** 发送消息并流式获取回复 */
  function sendMessage(content) {
    if (!currentConversationId.value || !content.trim() || isStreaming.value) return

    const convId = currentConversationId.value

    // 添加用户消息到界面
    const userMsg = { role: 'user', content, id: Date.now() }
    messages.value.push(userMsg)

    // 开始流式请求
    isStreaming.value = true
    streamingContent.value = ''
    isLoading.value = false

    return streamChat(
      convId,
      content,
      // onToken
      (token) => {
        streamingContent.value += token
      },
      // onDone
      () => {
        // 将完整的 AI 回复加入消息列表
        if (streamingContent.value) {
          messages.value.push({
            role: 'assistant',
            content: streamingContent.value,
            id: Date.now() + 1
          })
        }
        streamingContent.value = ''
        isStreaming.value = false
        // 刷新会话列表（标题可能已更新）
        loadConversations()
      },
      // onError
      () => {
        isStreaming.value = false
        // 保留已生成的部分内容，不丢弃
        if (streamingContent.value) {
          messages.value.push({
            role: 'assistant',
            content: streamingContent.value + '\n\n⚠️ *（回复中断，请重试）*',
            id: Date.now() + 1
          })
        }
        streamingContent.value = ''
        ElMessage.error('AI 回复中断，请重新发送')
      }
    )
  }

  return {
    conversations,
    currentConversationId,
    messages,
    isLoading,
    isStreaming,
    streamingContent,
    conversationTotal,
    conversationHasMore,
    messageHasMore,
    loadConversations,
    createConversation,
    deleteConversation,
    switchConversation,
    loadMessages,
    sendMessage
  }
})
