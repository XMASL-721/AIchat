<template>
  <div class="plan-generator-page">
    <header class="plan-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
      </div>
      <div class="header-inner">
        <h1>🎯 AI 个性化计划生成</h1>
        <p>根据你的身体数据和目标，生成专属的减脂计划</p>
      </div>
    </header>

    <div class="plan-container">
      <div class="form-section" v-if="!isGenerating">
        <el-form :model="formData" label-width="120px" class="plan-form">
          <el-form-item label="计划类型">
            <div class="type-cards">
              <div
                v-for="t in planTypes"
                :key="t.value"
                :class="['type-card', { active: formData.planType === t.value }]"
                @click="formData.planType = t.value"
              >
                <span class="type-icon">{{ t.icon }}</span>
                <span class="type-label">{{ t.label }}</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="主要目标">
            <el-select v-model="formData.inputData.goal" placeholder="选择你的目标" style="width: 100%">
              <el-option label="🔥 减脂瘦身" value="减脂瘦身" />
              <el-option label="💪 增肌塑形" value="增肌塑形" />
              <el-option label="⚖️ 保持体重" value="保持体重" />
              <el-option label="🏃 提升体能" value="提升体能" />
            </el-select>
          </el-form-item>

          <el-form-item label="计划周期">
            <el-select v-model="formData.inputData.duration" placeholder="选择计划周期" style="width: 100%">
              <el-option label="1周" value="1周" />
              <el-option label="2周" value="2周" />
              <el-option label="4周" value="4周" />
            </el-select>
          </el-form-item>

          <el-form-item label="偏好/限制">
            <el-input
              v-model="formData.inputData.preferences"
              type="textarea"
              :rows="3"
              placeholder="例如：不吃海鲜、膝盖有伤不能深蹲、每周只能训练3天等"
            />
          </el-form-item>

          <el-form-item>
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                <strong>提示：</strong>请确保在个人中心填写了身高、体重等信息，AI 会据此生成更精准的计划。
              </template>
            </el-alert>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" round @click="handleGenerate" class="generate-btn">
              🚀 生成计划
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="generating-section" v-else>
        <div class="generating-header">
          <h2>{{ isComplete ? '✅ 计划生成完成' : '⏳ 正在生成你的专属计划...' }}</h2>
          <el-button v-if="isComplete" type="primary" round @click="resetForm">
            生成新计划
          </el-button>
        </div>

        <div class="plan-content" ref="planContentRef">
          <div v-html="renderedPlan"></div>
          <span v-if="!isComplete" class="cursor-blink">|</span>
        </div>

        <div class="plan-actions" v-if="isComplete">
          <el-button type="success" round @click="router.push('/plans')">
            📋 查看历史计划
          </el-button>
          <el-button round @click="resetForm">
            🔄 生成新计划
          </el-button>
        </div>
      </div>
    </div>

    <div class="fab" @click="router.push('/')">
      <span class="fab-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:22px;height:22px">
          <path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/>
          <path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/>
          <path d="M2 22c0-4 4-6 10-6s10 2 10 6"/>
        </svg>
      </span>
      <span class="fab-text">AI 咨询</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '../utils/markdown'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const planTypes = [
  { value: 'diet', label: '饮食计划', icon: '🥗' },
  { value: 'workout', label: '训练计划', icon: '🏋️' },
  { value: 'combined', label: '综合计划', icon: '🎯' }
]

const formData = ref({
  planType: 'combined',
  inputData: {
    goal: '减脂瘦身',
    duration: '1周',
    preferences: ''
  }
})

const isGenerating = ref(false)
const isComplete = ref(false)
const planContent = ref('')
const planContentRef = ref(null)
let abortController = null

onBeforeUnmount(() => {
  if (abortController) abortController.abort()
})

const renderedPlan = computed(() => {
  return renderMarkdown(planContent.value)
})

async function handleGenerate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  isGenerating.value = true
  isComplete.value = false
  planContent.value = ''

  const token = localStorage.getItem('token')
  abortController = new AbortController()
  const controller = abortController

  try {
    const response = await fetch('/api/plans/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(formData.value),
      signal: controller.signal
    })

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
            isComplete.value = true
            return
          }
          planContent.value += data
          await nextTick()
          if (planContentRef.value) {
            planContentRef.value.scrollTop = planContentRef.value.scrollHeight
          }
        }
      }
    }

    isComplete.value = true
  } catch (error) {
    if (error.name === 'AbortError') return
    ElMessage.error('生成计划失败：' + error.message)
    isGenerating.value = false
    isComplete.value = false
  }
}

function resetForm() {
  isGenerating.value = false
  isComplete.value = false
  planContent.value = ''
}
</script>

<style scoped>
.plan-generator-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.plan-header {
  background: linear-gradient(135deg, #8b5cf6, #6366f1, #3b82f6);
  color: #fff;
  padding: 48px 24px 40px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.header-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.deco-circle.c1 {
  width: 260px; height: 260px;
  background: #fff;
  top: -80px; right: -40px;
}

.deco-circle.c2 {
  width: 160px; height: 160px;
  background: #fff;
  bottom: -40px; left: 8%;
}

.header-top {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-start;
  max-width: 900px;
  margin: 0 auto 12px;
}

.back-btn {
  background: rgba(255,255,255,0.15) !important;
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
  backdrop-filter: blur(8px);
}

.back-btn:hover {
  background: rgba(255,255,255,0.25) !important;
}

.header-inner {
  position: relative;
  z-index: 1;
}

.header-inner h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-inner p {
  font-size: 15px;
  opacity: 0.9;
}

.plan-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.form-section,
.generating-section {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.plan-form {
  max-width: 600px;
  margin: 0 auto;
}

/* 计划类型选择卡片 */
.type-cards {
  display: flex;
  gap: 12px;
  width: 100%;
}

.type-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  border-radius: 16px;
  border: 2px solid var(--border-color, #e2e8f0);
  cursor: pointer;
  transition: all 0.25s;
  background: var(--bg-tertiary, #f8fafc);
}

.type-card:hover {
  border-color: #8b5cf6;
  background: #faf5ff;
}

.type-card.active {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #faf5ff, #ede9fe);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.15);
}

.type-icon {
  font-size: 32px;
}

.type-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
}

.type-card.active .type-label {
  color: #7c3aed;
}

.generate-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.generating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-light, #f1f5f9);
}

.generating-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin: 0;
}

.plan-content {
  background: var(--bg-tertiary, #f8fafc);
  border-radius: 16px;
  padding: 24px;
  max-height: 600px;
  overflow-y: auto;
  line-height: 1.8;
  font-size: 15px;
  border: 1px solid var(--border-color, #e2e8f0);
}

.plan-content :deep(h1),
.plan-content :deep(h2),
.plan-content :deep(h3) {
  color: var(--text-primary, #1e293b);
  margin-top: 24px;
  margin-bottom: 12px;
}

.plan-content :deep(h1) {
  font-size: 24px;
  border-bottom: 2px solid var(--border-color, #e2e8f0);
  padding-bottom: 8px;
}

.plan-content :deep(h2) { font-size: 20px; }
.plan-content :deep(h3) { font-size: 16px; }

.plan-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  border-radius: 8px;
  overflow: hidden;
}

.plan-content :deep(th),
.plan-content :deep(td) {
  border: 1px solid var(--border-color, #e2e8f0);
  padding: 12px;
  text-align: left;
}

.plan-content :deep(th) {
  background: var(--bg-tertiary, #f1f5f9);
  font-weight: 600;
}

.plan-content :deep(ul),
.plan-content :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.plan-content :deep(li) { margin: 8px 0; }

.plan-content :deep(strong) {
  color: var(--text-primary, #0f172a);
  font-weight: 700;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
  color: #8b5cf6;
  font-weight: 300;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.plan-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(5, 150, 105, 0.4);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 50;
  line-height: 1;
}

.fab:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 30px rgba(5, 150, 105, 0.5);
}

.fab-text {
  font-size: 9px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .fab {
    bottom: 24px;
    right: 24px;
    width: 50px;
    height: 50px;
  }
  .fab-text { display: none; }
  .type-cards { flex-direction: column; }
  .generating-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
