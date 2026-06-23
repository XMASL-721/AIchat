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
      <!-- ===== 表单区域 ===== -->
      <div class="form-card" v-if="!isGenerating">
        <div class="form-title">
          <span class="form-title-icon">⚙️</span>
          <h2>配置你的计划</h2>
        </div>

        <el-form :model="formData" label-position="top" class="plan-form">
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
                <span class="type-check" v-if="formData.planType === t.value">✓</span>
              </div>
            </div>
          </el-form-item>

          <div class="form-row">
            <el-form-item label="🎯 主要目标" class="form-half">
              <el-select v-model="formData.inputData.goal" placeholder="选择你的目标">
                <el-option label="🔥 减脂瘦身" value="减脂瘦身" />
                <el-option label="💪 增肌塑形" value="增肌塑形" />
                <el-option label="⚖️ 保持体重" value="保持体重" />
                <el-option label="🏃 提升体能" value="提升体能" />
              </el-select>
            </el-form-item>

            <el-form-item label="📅 计划周期" class="form-half">
              <el-select v-model="formData.inputData.duration" placeholder="选择计划周期">
                <el-option label="1 周" value="1周" />
                <el-option label="2 周" value="2周" />
                <el-option label="4 周" value="4周" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="💡 偏好与限制">
            <el-input
              v-model="formData.inputData.preferences"
              type="textarea"
              :rows="3"
              placeholder="例如：不吃海鲜、膝盖有伤不能深蹲、每周只能训练3天等"
            />
          </el-form-item>

          <el-alert type="info" :closable="false" show-icon class="form-tip">
            <template #title>
              请确保在「个人中心」填写了身高、体重等信息，AI 会据此生成更精准的计划。
            </template>
          </el-alert>

          <el-button type="primary" size="large" round @click="handleGenerate" class="generate-btn">
            🚀 开始生成计划
          </el-button>
        </el-form>
      </div>

      <!-- ===== 生成中 / 结果展示 ===== -->
      <div class="result-area" v-else>
        <!-- 顶部状态栏 -->
        <div class="status-bar" :class="{ complete: isComplete }">
          <div class="status-left">
            <div class="status-icon" :class="{ spinning: !isComplete }">
              {{ isComplete ? '✅' : '⏳' }}
            </div>
            <div class="status-text">
              <h2>{{ isComplete ? '计划生成完成' : '正在生成你的专属计划...' }}</h2>
              <p>{{ planTypeLabel }} · {{ formData.inputData.goal }} · {{ formData.inputData.duration }}</p>
            </div>
          </div>
          <div class="status-right">
            <div class="progress-ring" v-if="!isComplete">
              <svg viewBox="0 0 36 36">
                <circle cx="18" cy="18" r="15.5" fill="none" stroke="#e2e8f0" stroke-width="3"/>
                <circle cx="18" cy="18" r="15.5" fill="none" stroke="#8b5cf6" stroke-width="3"
                  stroke-dasharray="100" :stroke-dashoffset="100 - progressPercent"
                  stroke-linecap="round" transform="rotate(-90 18 18)"/>
              </svg>
              <span class="progress-num">{{ progressPercent }}%</span>
            </div>
            <el-button v-if="isComplete" type="primary" round @click="resetForm">
              🔄 新计划
            </el-button>
          </div>
        </div>

        <!-- 计划内容 -->
        <div class="plan-content-card">
          <div class="plan-content" ref="planContentRef">
            <div v-html="renderedPlan" class="markdown-body"></div>
            <span v-if="!isComplete" class="cursor-blink">|</span>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="plan-actions" v-if="isComplete">
          <el-button type="success" round size="large" @click="router.push('/plans')">
            📋 查看历史计划
          </el-button>
          <el-button round size="large" @click="resetForm">
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

const planTypeLabel = computed(() => {
  return planTypes.find(t => t.value === formData.value.planType)?.label || ''
})

// 模拟进度（基于内容长度估算）
const progressPercent = computed(() => {
  if (isComplete.value) return 100
  const len = planContent.value.length
  // 假设平均计划约 3000 字符
  return Math.min(95, Math.round((len / 3000) * 100))
})

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

/* ===== Header ===== */
.plan-header {
  background: linear-gradient(135deg, #8b5cf6, #6366f1, #3b82f6);
  color: #fff; padding: 48px 24px 40px; text-align: center;
  position: relative; overflow: hidden;
}
.header-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; opacity: 0.1; }
.deco-circle.c1 { width: 260px; height: 260px; background: #fff; top: -80px; right: -40px; }
.deco-circle.c2 { width: 160px; height: 160px; background: #fff; bottom: -40px; left: 8%; }
.header-top { position: relative; z-index: 1; display: flex; justify-content: flex-start; max-width: 900px; margin: 0 auto 12px; }
.back-btn { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }
.back-btn:hover { background: rgba(255,255,255,0.25) !important; }
.header-inner { position: relative; z-index: 1; }
.header-inner h1 { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.header-inner p { font-size: 15px; opacity: 0.9; }

.plan-container { max-width: 900px; margin: 0 auto; padding: 24px 16px; }

/* ===== 表单卡片 ===== */
.form-card {
  background: var(--bg-secondary, #fff); border-radius: 20px; padding: 36px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06); border: 1px solid var(--border-color, #e2e8f0);
}
.form-title {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 28px; padding-bottom: 20px;
  border-bottom: 2px solid var(--border-light, #f1f5f9);
}
.form-title-icon { font-size: 28px; }
.form-title h2 { font-size: 20px; font-weight: 700; color: var(--text-primary, #0f172a); margin: 0; }

.plan-form { max-width: 640px; margin: 0 auto; }

.form-row { display: flex; gap: 16px; }
.form-half { flex: 1; }

/* 类型选择卡片 */
.type-cards { display: flex; gap: 12px; width: 100%; }
.type-card {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 22px 12px; border-radius: 16px;
  border: 2px solid var(--border-color, #e2e8f0);
  cursor: pointer; transition: all 0.25s; background: var(--bg-tertiary, #f8fafc);
  position: relative;
}
.type-card:hover { border-color: #8b5cf6; background: #faf5ff; }
.type-card.active {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #faf5ff, #ede9fe);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.15);
}
.type-icon { font-size: 32px; }
.type-label { font-size: 14px; font-weight: 600; color: var(--text-primary, #0f172a); }
.type-card.active .type-label { color: #7c3aed; }
.type-check {
  position: absolute; top: 8px; right: 10px;
  width: 22px; height: 22px; border-radius: 50%;
  background: #8b5cf6; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}

.form-tip { margin-bottom: 20px; }

.generate-btn {
  width: 100%; height: 50px; font-size: 16px; font-weight: 700; letter-spacing: 1px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
}
.generate-btn:hover {
  background: linear-gradient(135deg, #7c3aed, #4f46e5) !important;
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.3) !important;
}

/* ===== 生成结果区域 ===== */
.result-area { display: flex; flex-direction: column; gap: 16px; }

/* 状态栏 */
.status-bar {
  background: var(--bg-secondary, #fff); border-radius: 16px; padding: 20px 24px;
  display: flex; justify-content: space-between; align-items: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid var(--border-color, #e2e8f0);
  border-left: 4px solid #8b5cf6;
}
.status-bar.complete { border-left-color: #10b981; }
.status-left { display: flex; align-items: center; gap: 14px; }
.status-icon { font-size: 28px; }
.status-icon.spinning { animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.15); } }
.status-text h2 { font-size: 17px; font-weight: 700; color: var(--text-primary, #0f172a); margin: 0 0 2px; }
.status-text p { font-size: 13px; color: var(--text-secondary, #64748b); margin: 0; }

/* 进度环 */
.progress-ring { position: relative; width: 44px; height: 44px; }
.progress-ring svg { width: 44px; height: 44px; }
.progress-ring circle:last-child { transition: stroke-dashoffset 0.5s ease; }
.progress-num {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #8b5cf6;
}

/* 内容卡片 */
.plan-content-card {
  background: var(--bg-secondary, #fff); border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06); border: 1px solid var(--border-color, #e2e8f0);
  overflow: hidden;
}
.plan-content {
  padding: 32px; max-height: 680px; overflow-y: auto;
  line-height: 1.85; font-size: 15px;
}

/* ===== Markdown 深度美化 ===== */
.markdown-body :deep(> h1) {
  font-size: 26px; font-weight: 800; margin: 0 0 20px; padding: 16px 0;
  border-bottom: 3px solid #8b5cf6;
  color: var(--text-primary, #0f172a);
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}

.markdown-body :deep(> h2) {
  font-size: 20px; font-weight: 700; margin: 32px 0 16px; padding: 12px 16px;
  border-radius: 12px; border-left: 4px solid #8b5cf6;
  background: linear-gradient(90deg, rgba(139,92,246,0.08), transparent);
  color: var(--text-primary, #0f172a);
}

.markdown-body :deep(> h3) {
  font-size: 17px; font-weight: 600; margin: 24px 0 12px;
  color: #6366f1; padding-left: 12px;
  border-left: 3px solid #c4b5fd;
}

.markdown-body :deep(> h4) {
  font-size: 15px; font-weight: 600; margin: 20px 0 8px;
  color: var(--text-primary, #334155);
}

.markdown-body :deep(> p) {
  margin: 0 0 14px; color: var(--text-primary, #334155); line-height: 1.85;
}

.markdown-body :deep(> ul),
.markdown-body :deep(> ol) {
  padding-left: 8px; margin: 12px 0 16px;
}

.markdown-body :deep(> ul > li),
.markdown-body :deep(> ol > li) {
  margin: 8px 0; padding: 8px 12px; line-height: 1.7;
  border-radius: 8px; transition: background 0.2s;
}

.markdown-body :deep(> ul > li:hover),
.markdown-body :deep(> ol > li:hover) {
  background: var(--bg-tertiary, #f8fafc);
}

.markdown-body :deep(> ul > li::marker) { color: #8b5cf6; }
.markdown-body :deep(> ol > li::marker) { color: #8b5cf6; font-weight: 700; }

.markdown-body :deep(strong) {
  color: var(--text-primary, #0f172a); font-weight: 700;
  background: linear-gradient(transparent 60%, rgba(139,92,246,0.15) 60%);
  padding: 0 2px;
}

.markdown-body :deep(em) { color: #6366f1; font-style: normal; font-weight: 500; }

/* 表格美化 */
.markdown-body :deep(table) {
  width: 100%; border-collapse: separate; border-spacing: 0;
  margin: 16px 0 20px; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.markdown-body :deep(thead th) {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: #fff; font-weight: 600; font-size: 14px;
  padding: 12px 16px; text-align: left;
  border: none;
}

.markdown-body :deep(tbody td) {
  padding: 11px 16px; font-size: 14px; border-bottom: 1px solid var(--border-light, #f1f5f9);
  border-right: 1px solid var(--border-light, #f1f5f9);
  color: var(--text-primary, #334155);
}

.markdown-body :deep(tbody tr:last-child td) { border-bottom: none; }
.markdown-body :deep(tbody tr:hover) { background: rgba(139,92,246,0.04); }

/* 引用块 */
.markdown-body :deep(blockquote) {
  margin: 16px 0; padding: 16px 20px;
  border-left: 4px solid #8b5cf6; border-radius: 0 12px 12px 0;
  background: linear-gradient(90deg, rgba(139,92,246,0.06), transparent);
  color: var(--text-secondary, #475569);
}

.markdown-body :deep(blockquote p) { margin: 0; }

/* 分隔线 */
.markdown-body :deep(hr) {
  border: none; height: 2px; margin: 28px 0;
  background: linear-gradient(90deg, transparent, #c4b5fd, transparent);
}

/* 代码 */
.markdown-body :deep(code) {
  background: rgba(139,92,246,0.1); color: #7c3aed;
  padding: 2px 8px; border-radius: 6px; font-size: 13px;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

/* 光标 */
.cursor-blink {
  animation: blink 1s step-end infinite;
  color: #8b5cf6; font-weight: 300; font-size: 18px;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* 底部操作 */
.plan-actions {
  display: flex; gap: 12px; justify-content: center; padding: 8px 0;
}

/* FAB */
.fab {
  position: fixed; bottom: 32px; right: 32px;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(5,150,105,0.4);
  cursor: pointer; transition: all 0.3s; z-index: 50; line-height: 1;
}
.fab:hover { transform: scale(1.1); box-shadow: 0 8px 30px rgba(5,150,105,0.5); }
.fab-text { font-size: 9px; font-weight: 600; letter-spacing: 0.5px; }

@media (max-width: 768px) {
  .fab { bottom: 24px; right: 24px; width: 50px; height: 50px; }
  .fab-text { display: none; }
  .type-cards { flex-direction: column; }
  .form-row { flex-direction: column; gap: 0; }
  .status-bar { flex-direction: column; gap: 12px; align-items: flex-start; }
  .plan-content { padding: 20px; }
  .form-card { padding: 24px; }
}
</style>
