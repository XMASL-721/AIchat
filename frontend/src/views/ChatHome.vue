<template>
  <div class="chat-layout">
    <!-- 侧边栏遮罩（移动端） -->
    <div
      v-if="!sidebarCollapsed && isMobile"
      class="sidebar-overlay"
      @click="sidebarCollapsed = true"
    />

    <!-- 左侧边栏 -->
    <aside :class="['sidebar', { collapsed: sidebarCollapsed }]">
      <div class="sidebar-header">
        <h3>💬 历史对话</h3>
        <el-button
          type="primary"
          :icon="Plus"
          size="small"
          @click="handleNewConversation"
        >
          新建
        </el-button>
      </div>

      <div class="sidebar-content" @scroll="handleSidebarScroll">
        <!-- 减脂餐入口 -->
        <div class="sidebar-nav">
          <div class="nav-item" @click="router.push('/meals')">
            <span class="nav-icon">🥗</span>
            <span class="nav-label">减脂餐食谱</span>
            <el-icon class="nav-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="nav-item" @click="router.push('/workouts')">
            <span class="nav-icon">🏋️</span>
            <span class="nav-label">健身训练</span>
            <el-icon class="nav-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="nav-item" @click="router.push('/articles')">
            <span class="nav-icon">📰</span>
            <span class="nav-label">健康资讯</span>
            <el-icon class="nav-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="nav-item" @click="router.push('/supplements')">
            <span class="nav-icon">💊</span>
            <span class="nav-label">补剂指南</span>
            <el-icon class="nav-arrow"><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="sidebar-divider" />

        <div
          v-for="conv in conversationStore.conversations"
          :key="conv.id"
          :class="['conv-item', { active: conv.id === conversationStore.currentConversationId }]"
          @click="handleSwitchConversation(conv.id)"
        >
          <div class="conv-icon">💬</div>
          <span class="conv-title">{{ conv.title }}</span>
          <el-icon
            class="conv-delete"
            size="14"
            @click.stop="handleDeleteConversation(conv.id)"
          >
            <Delete />
          </el-icon>
        </div>

        <div v-if="conversationStore.conversationHasMore" class="load-more">
          <el-button text :loading="loadingMore" @click="loadMoreConversations">
            加载更多
          </el-button>
        </div>

        <div v-if="conversationStore.conversations.length === 0" class="empty-sidebar">
          <p>暂无历史对话</p>
        </div>
      </div>

      <!-- 用户信息底部 -->
      <div class="sidebar-footer">
        <div class="footer-actions">
          <el-button
            :icon="userStore.darkMode ? Sunny : Moon"
            circle
            size="small"
            @click="userStore.toggleDarkMode"
            :title="userStore.darkMode ? '切换到亮色模式' : '切换到暗黑模式'"
          />
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="30" shape="square" style="border-radius: 8px;">
                {{ userStore.userInfo?.nickname?.charAt(0) || '用' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon> 我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="dashboard">
                  <el-icon><DataLine /></el-icon> 数据看板
                </el-dropdown-item>
                <el-dropdown-item command="changePassword">
                  <el-icon><Edit /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <main class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div style="display:flex;align-items:center;gap:8px;min-width:0;">
          <el-button
            class="toggle-btn"
            :icon="Expand"
            size="small"
            text
            @click="sidebarCollapsed = false"
          />
          <span class="header-title">{{ currentTitle }}</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messageListRef" class="message-list">
        <div v-if="conversationStore.messageHasMore" class="load-more">
          <el-button text :loading="loadingMoreMsg" @click="loadMoreMessages">
            加载更多消息
          </el-button>
        </div>

        <!-- 消息气泡 -->
        <div
          v-for="msg in conversationStore.messages"
          :key="msg.id"
          :class="['message', msg.role]"
        >
          <div v-if="msg.role === 'assistant'" class="message-avatar ai-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/><path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/><path d="M2 22c0-4 4-6 10-6s10 2 10 6"/></svg>
          </div>
          <div
            :class="['message-body']"
            :style="msg.role === 'user' ? { alignItems: 'flex-end' } : {}"
          >
            <div class="message-bubble" v-html="renderMarkdown(msg.content)"></div>
            <div class="message-time">
              {{ msg.role === 'user' ? '我' : '小V' }}
            </div>
          </div>
          <div v-if="msg.role === 'user'" class="message-avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || '我' }}
          </div>
        </div>

        <!-- 流式输出 -->
        <div v-if="conversationStore.isStreaming" class="message assistant">
          <div class="message-avatar ai-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/><path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/><path d="M2 22c0-4 4-6 10-6s10 2 10 6"/></svg>
          </div>
          <div class="message-body">
            <div
              v-if="conversationStore.streamingContent"
              class="message-bubble"
            ><span v-html="renderMarkdown(conversationStore.streamingContent)"></span><span class="cursor-blink">|</span></div>
            <div v-else class="typing-indicator">
              <span class="typing-dot" />
              <span class="typing-dot" />
              <span class="typing-dot" />
            </div>
            <div class="message-time">小V</div>
          </div>
        </div>

        <!-- 空状态 -->
        <div
          v-if="!conversationStore.currentConversationId"
          class="empty-state"
        >
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="width:36px;height:36px;"><path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/><path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/><path d="M2 22c0-4 4-6 10-6s10 2 10 6"/></svg>
          </div>
          <h3>智能问答助手</h3>
          <p>点击左侧「新建」按钮<br>或直接在下方输入消息开始对话吧</p>

          <!-- 随机推荐卡片 -->
          <div class="random-recommendations">
            <div class="recommend-card meal" @click="showRandomMeal" :class="{ loading: loadingRandom }">
              <div class="recommend-icon">🥗</div>
              <div class="recommend-content">
                <h4>今天吃什么？</h4>
                <p v-if="randomMeal">{{ randomMeal.name }}</p>
                <p v-else>点击随机推荐一道减脂餐</p>
              </div>
            </div>
            <div class="recommend-card workout" @click="showRandomWorkout" :class="{ loading: loadingRandom }">
              <div class="recommend-icon">🏋️</div>
              <div class="recommend-content">
                <h4>今天练什么？</h4>
                <p v-if="randomWorkout">{{ randomWorkout.name }}</p>
                <p v-else>点击随机推荐一套训练</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <!-- 快捷提问按钮 -->
        <div v-if="!conversationStore.isStreaming" class="quick-questions">
          <el-button
            v-for="q in quickQuestions"
            :key="q"
            size="small"
            plain
            @click="handleQuickQuestion(q)"
          >
            {{ q }}
          </el-button>
        </div>

        <div class="chat-input-wrapper">
          <el-input
            ref="inputRef"
            v-model="inputMessage"
            type="textarea"
            :disabled="conversationStore.isStreaming"
            placeholder="输入消息，Enter 发送，Shift+Enter 换行"
            :autosize="{ minRows: 1, maxRows: 6 }"
            @keydown.enter.exact.prevent="handleSendMessage"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="conversationStore.isStreaming"
            :disabled="!inputMessage.trim()"
            @click="handleSendMessage"
          />
        </div>
      </div>
    </main>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" destroy-on-close>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmNewPassword">
          <el-input v-model="passwordForm.confirmNewPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Edit, SwitchButton, Expand, Promotion, ArrowRight, User, Star, DataLine, Sunny, Moon } from '@element-plus/icons-vue'
import { renderMarkdown } from '../utils/markdown'
import { useUserStore } from '../stores/user'
import { useConversationStore } from '../stores/conversation'
import { changePasswordApi } from '../api/auth'
import { getRandomMealApi } from '../api/meal'
import { getRandomWorkoutApi } from '../api/workout'

const router = useRouter()
const userStore = useUserStore()
const conversationStore = useConversationStore()

const sidebarCollapsed = ref(window.innerWidth < 768)
const isMobile = ref(window.innerWidth < 768)
const loadingMore = ref(false)
const messageListRef = ref(null)
const inputRef = ref(null)
const loadingMoreMsg = ref(false)
const inputMessage = ref('')

// 快捷提问问题
const quickQuestions = [
  '推荐一份300千卡以内的午餐',
  '适合新手的20分钟腹肌训练',
  '减脂期间一天应该怎么吃',
  '帮我制定今天的训练计划'
]

// 随机推荐
const randomMeal = ref(null)
const randomWorkout = ref(null)
const loadingRandom = ref(false)

const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const changingPassword = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmNewPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (r, v, cb) => v === passwordForm.value.newPassword ? cb() : cb(new Error('两次密码输入不一致')), trigger: 'blur' }
  ]
}

const currentTitle = computed(() => {
  if (!conversationStore.currentConversationId) return '新对话'
  const conv = conversationStore.conversations.find(c => c.id === conversationStore.currentConversationId)
  return conv?.title || '新对话'
})

onMounted(async () => {
  await conversationStore.loadConversations()
  window.addEventListener('resize', handleResize)
})

watch(() => conversationStore.messages.length, () => nextTick(scrollToBottom))
watch(() => conversationStore.streamingContent, () => nextTick(scrollToBottom))
watch(() => conversationStore.currentConversationId, () => nextTick(() => inputRef.value?.focus()))

function handleResize() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) sidebarCollapsed.value = false
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleNewConversation() {
  await conversationStore.createConversation()
  if (isMobile.value) sidebarCollapsed.value = true
  nextTick(() => inputRef.value?.focus())
}

function handleSwitchConversation(id) {
  conversationStore.switchConversation(id)
  if (isMobile.value) sidebarCollapsed.value = true
}

async function handleDeleteConversation(id) {
  try {
    await ElMessageBox.confirm('确定删除该会话？会话下的消息也将一起删除。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await conversationStore.deleteConversation(id)
    ElMessage.success('会话已删除')
  } catch { /* 取消 */ }
}

async function handleSidebarScroll(e) {
  const el = e.target
  if (el.scrollHeight - el.scrollTop - el.clientHeight < 50) {
    if (conversationStore.conversationHasMore && !loadingMore.value) {
      loadingMore.value = true
      await conversationStore.loadConversations(true)
      loadingMore.value = false
    }
  }
}

async function loadMoreConversations() {
  loadingMore.value = true
  await conversationStore.loadConversations(true)
  loadingMore.value = false
}

async function loadMoreMessages() {
  loadingMoreMsg.value = true
  await conversationStore.loadMessages(true)
  loadingMoreMsg.value = false
}

async function handleSendMessage() {
  const content = inputMessage.value.trim()
  if (!content) return
  if (!conversationStore.currentConversationId) {
    const conv = await conversationStore.createConversation()
    if (!conv) {
      ElMessage.error('创建对话失败，请重试')
      return
    }
  }
  inputMessage.value = ''
  conversationStore.sendMessage(content)
}

async function handleQuickQuestion(question) {
  inputMessage.value = question
  await handleSendMessage()
}

async function showRandomMeal() {
  loadingRandom.value = true
  try {
    const res = await getRandomMealApi()
    randomMeal.value = res.data
    if (randomMeal.value) {
      router.push(`/meals/${randomMeal.value.id}`)
    }
  } catch (error) {
    ElMessage.error('获取推荐失败')
  } finally {
    loadingRandom.value = false
  }
}

async function showRandomWorkout() {
  loadingRandom.value = true
  try {
    const res = await getRandomWorkoutApi()
    randomWorkout.value = res.data
    if (randomWorkout.value) {
      router.push(`/workouts/${randomWorkout.value.id}`)
    }
  } catch (error) {
    ElMessage.error('获取推荐失败')
  } finally {
    loadingRandom.value = false
  }
}

function handleUserCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'favorites') {
    router.push('/favorites')
  } else if (command === 'dashboard') {
    router.push('/dashboard')
  } else if (command === 'changePassword') {
    passwordForm.value = { oldPassword: '', newPassword: '', confirmNewPassword: '' }
    passwordDialogVisible.value = true
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return
  try { await passwordFormRef.value.validate() } catch { return }
  changingPassword.value = true
  try {
    await changePasswordApi({ oldPassword: passwordForm.value.oldPassword, newPassword: passwordForm.value.newPassword })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch { /* handled */ } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
/* ===== AI 消息 Markdown 美化 ===== */
.message.assistant .message-bubble :deep(p) {
  margin: 0 0 8px;
  line-height: 1.7;
}
.message.assistant .message-bubble :deep(p:last-child) {
  margin-bottom: 0;
}
.message.assistant .message-bubble :deep(strong) {
  color: #0f172a;
  font-weight: 700;
}
.message.assistant .message-bubble :deep(code.md-inline-code) {
  background: #f1f5f9;
  color: #e11d48;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 13px;
  font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace;
}
.message.assistant .message-bubble :deep(.md-code-block) {
  background: #1e293b;
  border-radius: 12px;
  margin: 12px 0;
  overflow: hidden;
}
.message.assistant .message-bubble :deep(.md-code-lang) {
  background: #334155;
  padding: 6px 16px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #94a3b8;
}
.message.assistant .message-bubble :deep(.md-code-block code) {
  display: block;
  padding: 16px;
  font-size: 13px;
  line-height: 1.6;
  font-family: 'JetBrains Mono', 'Cascadia Code', 'Consolas', monospace;
  color: #e2e8f0;
  overflow-x: auto;
}
.message.assistant .message-bubble :deep(ul),
.message.assistant .message-bubble :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}
.message.assistant .message-bubble :deep(li) {
  margin-bottom: 4px;
  line-height: 1.6;
}
.message.assistant .message-bubble :deep(br) {
  display: block;
  content: '';
  margin: 4px 0;
}
.ai-avatar svg {
  width: 20px;
  height: 20px;
}
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 99;
  backdrop-filter: blur(4px);
}

.sidebar-nav {
  padding: 8px 8px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #e2e8f0;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.nav-icon {
  font-size: 18px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
  font-size: 13.5px;
  font-weight: 500;
}

.nav-arrow {
  color: #64748b;
  font-size: 14px;
}

.sidebar-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 8px 12px;
}

/* 光标闪烁动画 */
.cursor-blink {
  animation: blink 1s step-end infinite;
  color: #3b82f6;
  font-weight: 300;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 快捷提问按钮 */
.quick-questions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.quick-questions .el-button {
  font-size: 13px;
  border-radius: 16px;
}

/* 随机推荐卡片 */
.random-recommendations {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  flex-wrap: wrap;
  justify-content: center;
}

.recommend-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 240px;
  border: 2px solid transparent;
}

.recommend-card.meal {
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  border-color: #10b981;
}

.recommend-card.workout {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-color: #ef4444;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.recommend-card.loading {
  opacity: 0.6;
  pointer-events: none;
}

.recommend-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.recommend-content h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.recommend-content p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-actions .el-button {
  flex-shrink: 0;
}

.footer-actions .el-dropdown {
  flex: 1;
}
</style>
