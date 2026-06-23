<template>
  <div class="plan-page">
    <!-- ===== 顶部 Header ===== -->
    <header class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
        <h1>🎯 AI 个性化计划生成</h1>
        <p>根据你的身体数据和目标，生成专属的减脂计划</p>
      </div>
    </header>

    <div class="page-body">
      <!-- ===== 配置表单 ===== -->
      <div v-if="!generating" class="form-card">
        <div class="card-head">
          <span>⚙️</span>
          <h2>配置你的计划</h2>
        </div>

        <!-- 类型选择 -->
        <div class="field">
          <label>计划类型</label>
          <div class="type-row">
            <div
              v-for="t in types"
              :key="t.key"
              :class="['type-box', { on: form.planType === t.key }]"
              @click="form.planType = t.key"
            >
              <span class="type-emoji">{{ t.icon }}</span>
              <span class="type-name">{{ t.label }}</span>
              <span v-if="form.planType === t.key" class="type-badge">✓</span>
            </div>
          </div>
        </div>

        <!-- 目标 + 周期 -->
        <div class="field-row">
          <div class="field half">
            <label>🎯 主要目标</label>
            <el-select v-model="form.inputData.goal">
              <el-option label="🔥 减脂瘦身" value="减脂瘦身" />
              <el-option label="💪 增肌塑形" value="增肌塑形" />
              <el-option label="⚖️ 保持体重" value="保持体重" />
              <el-option label="🏃 提升体能" value="提升体能" />
            </el-select>
          </div>
          <div class="field half">
            <label>📅 计划周期</label>
            <el-select v-model="form.inputData.duration">
              <el-option label="1 周" value="1周" />
              <el-option label="2 周" value="2周" />
              <el-option label="4 周" value="4周" />
            </el-select>
          </div>
        </div>

        <!-- 偏好 -->
        <div class="field">
          <label>💡 偏好与限制</label>
          <el-input v-model="form.inputData.preferences" type="textarea" :rows="3"
            placeholder="例如：不吃海鲜、膝盖有伤不能深蹲、每周只能训练3天等" />
        </div>

        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 20px;">
          <template #title>请确保在「个人中心」填写了身高、体重等信息，AI 会据此生成更精准的计划。</template>
        </el-alert>

        <el-button type="primary" size="large" round class="submit-btn" @click="startGenerate">
          🚀 开始生成计划
        </el-button>
      </div>

      <!-- ===== 生成结果 ===== -->
      <div v-else class="result-area">
        <!-- 状态条 -->
        <div :class="['status-bar', { done: done }]">
          <div class="status-left">
            <span :class="['status-dot', { spin: !done }]">{{ done ? '✅' : '⏳' }}</span>
            <div>
              <strong>{{ done ? '计划生成完成' : '正在生成你的专属计划...' }}</strong>
              <small>{{ typeLabel }} · {{ form.inputData.goal }} · {{ form.inputData.duration }}</small>
            </div>
          </div>
          <div class="status-right">
            <div v-if="!done" class="ring">
              <svg viewBox="0 0 36 36">
                <circle cx="18" cy="18" r="15" fill="none" stroke="#e2e8f0" stroke-width="3" />
                <circle cx="18" cy="18" r="15" fill="none" stroke="#8b5cf6" stroke-width="3"
                  :stroke-dasharray="94.2" :stroke-dashoffset="94.2 - (94.2 * progress / 100)"
                  stroke-linecap="round" transform="rotate(-90 18 18)" />
              </svg>
              <span>{{ progress }}%</span>
            </div>
            <el-button v-if="done" type="primary" round @click="reset">🔄 新计划</el-button>
          </div>
        </div>

        <!-- H1 英雄区 -->
        <div v-if="heroTitle" class="hero-card">
          <h1>{{ heroTitle }}</h1>
          <div class="hero-tags">
            <span>{{ typeIcon }} {{ typeLabel }}</span>
            <span>🎯 {{ form.inputData.goal }}</span>
            <span>📅 {{ form.inputData.duration }}</span>
          </div>
        </div>

        <!-- 板块卡片列表 -->
        <div class="sections-scroll" ref="scrollRef">
          <div v-for="(sec, i) in sections" :key="i" :class="['sec-card', 'color' + (i % 4)]">
            <div class="sec-head">
              <span class="sec-num">{{ String(i + 1).padStart(2, '0') }}</span>
              <h2>{{ sec.title }}</h2>
            </div>
            <div class="sec-body md" v-html="sec.html"></div>
          </div>

          <!-- 生成中光标 -->
          <div v-if="!done" class="typing-bar">
            <span class="blink">|</span>
            <span class="typing-label">AI 正在生成中...</span>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div v-if="done" class="bottom-actions">
          <el-button type="success" round size="large" @click="router.push('/plans')">📋 查看历史计划</el-button>
          <el-button round size="large" @click="reset">🔄 生成新计划</el-button>
        </div>
      </div>
    </div>

    <!-- FAB -->
    <div class="fab" @click="router.push('/')">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/><path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/><path d="M2 22c0-4 4-6 10-6s10 2 10 6"/></svg>
      <small>AI 咨询</small>
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

// 类型选项
const types = [
  { key: 'diet', label: '饮食计划', icon: '🥗' },
  { key: 'workout', label: '训练计划', icon: '🏋️' },
  { key: 'combined', label: '综合计划', icon: '🎯' }
]

// 表单数据
const form = ref({
  planType: 'combined',
  inputData: { goal: '减脂瘦身', duration: '1周', preferences: '' }
})

// 生成状态
const generating = ref(false)
const done = ref(false)
const raw = ref('')           // 原始 markdown 文本
const scrollRef = ref(null)
let abortCtrl = null

// 计算属性
const typeLabel = computed(() => types.find(t => t.key === form.value.planType)?.label || '')
const typeIcon = computed(() => types.find(t => t.key === form.value.planType)?.icon || '🎯')

const progress = computed(() => {
  if (done.value) return 100
  return Math.min(95, Math.round((raw.value.length / 3000) * 100))
})

// 提取 H1 标题
const heroTitle = computed(() => {
  const m = raw.value.match(/^#\s+(.+)$/m)
  return m ? m[1].trim() : ''
})

// 按 ## 拆分成板块
const sections = computed(() => {
  const text = raw.value
  // 去掉 H1
  const body = text.replace(/^#\s+.+$/m, '').trim()
  if (!body) return []

  // 用正则匹配所有 ## 标题及其内容
  const regex = /^##\s+(.+)$/gm
  const matches = [...body.matchAll(regex)]
  if (matches.length === 0) {
    // 没有 H2，整块作为一个板块
    return [{ title: '📋 计划详情', html: renderMarkdown(body) }]
  }

  const result = []
  for (let i = 0; i < matches.length; i++) {
    const title = matches[i][1].trim()
    const startIdx = matches[i].index + matches[i][0].length
    const endIdx = i + 1 < matches.length ? matches[i + 1].index : body.length
    const content = body.substring(startIdx, endIdx).trim()
    result.push({
      title,
      html: content ? renderMarkdown(content) : ''
    })
  }
  return result
})

// 离开页面时中止请求
onBeforeUnmount(() => {
  if (abortCtrl) abortCtrl.abort()
})

// 开始生成
async function startGenerate() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  generating.value = true
  done.value = false
  raw.value = ''

  const token = localStorage.getItem('token')
  abortCtrl = new AbortController()

  try {
    const resp = await fetch('/api/plans/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(form.value),
      signal: abortCtrl.signal
    })

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)

    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buf = ''

    while (true) {
      const { done: streamDone, value } = await reader.read()
      if (streamDone) break

      buf += decoder.decode(value, { stream: true })
      const lines = buf.split('\n')
      buf = lines.pop() || ''

      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const data = line.slice(5).trim()
        if (data === '[DONE]') {
          done.value = true
          return
        }
        if (data.startsWith('[ERROR]')) {
          throw new Error(data.slice(7))
        }
        raw.value += data
        await nextTick()
        if (scrollRef.value) {
          scrollRef.value.scrollTop = scrollRef.value.scrollHeight
        }
      }
    }
    done.value = true
  } catch (e) {
    if (e.name === 'AbortError') return
    ElMessage.error('生成失败：' + e.message)
    generating.value = false
    done.value = false
  }
}

function reset() {
  generating.value = false
  done.value = false
  raw.value = ''
}
</script>

<style scoped>
/* ===== 页面 ===== */
.plan-page { min-height: 100vh; background: var(--bg-primary, #f0f2f5); padding-bottom: 80px; }

/* ===== Header ===== */
.page-header {
  position: relative; overflow: hidden;
  padding: 24px 24px 40px; text-align: center; color: #fff;
}
.header-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #8b5cf6, #6366f1, #3b82f6);
}
.header-bg::after {
  content: ''; position: absolute;
  width: 300px; height: 300px; border-radius: 50%;
  background: rgba(255,255,255,0.08);
  top: -100px; right: -60px;
}
.header-content { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; }
.back-btn {
  float: left; margin-bottom: 16px;
  background: rgba(255,255,255,0.15) !important;
  border: 1px solid rgba(255,255,255,0.3) !important;
  color: #fff !important;
}
.back-btn:hover { background: rgba(255,255,255,0.25) !important; }
.header-content h1 { font-size: 28px; font-weight: 800; margin: 0 0 8px; }
.header-content p { font-size: 15px; opacity: 0.9; margin: 0; }

.page-body { max-width: 860px; margin: 0 auto; padding: 24px 16px; }

/* ===== 表单卡片 ===== */
.form-card {
  background: var(--bg-secondary, #fff); border-radius: 20px; padding: 36px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06); border: 1px solid var(--border-color, #e2e8f0);
}
.card-head {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 28px; padding-bottom: 20px;
  border-bottom: 2px solid var(--border-light, #f1f5f9);
}
.card-head span { font-size: 28px; }
.card-head h2 { font-size: 20px; font-weight: 700; margin: 0; color: var(--text-primary, #0f172a); }

.field { margin-bottom: 20px; }
.field label {
  display: block; font-size: 14px; font-weight: 600;
  color: var(--text-primary, #334155); margin-bottom: 8px;
}
.field :deep(.el-select) { width: 100%; }

.field-row { display: flex; gap: 16px; margin-bottom: 20px; }
.field.half { flex: 1; margin-bottom: 0; }

/* 类型选择 */
.type-row { display: flex; gap: 12px; }
.type-box {
  flex: 1; position: relative;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 22px 12px; border-radius: 16px;
  border: 2px solid var(--border-color, #e2e8f0);
  cursor: pointer; transition: all 0.25s; background: var(--bg-tertiary, #f8fafc);
}
.type-box:hover { border-color: #8b5cf6; background: #faf5ff; }
.type-box.on {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #faf5ff, #ede9fe);
  box-shadow: 0 4px 16px rgba(139,92,246,0.15);
}
.type-emoji { font-size: 32px; }
.type-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.type-box.on .type-name { color: #7c3aed; }
.type-badge {
  position: absolute; top: 8px; right: 10px;
  width: 22px; height: 22px; border-radius: 50%;
  background: #8b5cf6; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}

.submit-btn {
  width: 100%; height: 50px; font-size: 16px; font-weight: 700; letter-spacing: 1px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1) !important;
}
.submit-btn:hover {
  background: linear-gradient(135deg, #7c3aed, #4f46e5) !important;
  box-shadow: 0 8px 24px rgba(139,92,246,0.3) !important;
}

/* ===== 结果区域 ===== */
.result-area { display: flex; flex-direction: column; gap: 16px; }

/* 状态条 */
.status-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--bg-secondary, #fff); border-radius: 16px; padding: 18px 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--border-color, #e2e8f0); border-left: 4px solid #8b5cf6;
}
.status-bar.done { border-left-color: #10b981; }
.status-left { display: flex; align-items: center; gap: 14px; }
.status-dot { font-size: 26px; }
.status-dot.spin { animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.15); } }
.status-left strong { font-size: 16px; display: block; color: var(--text-primary); }
.status-left small { font-size: 13px; color: var(--text-secondary, #64748b); }

/* 进度环 */
.ring { position: relative; width: 42px; height: 42px; }
.ring svg { width: 42px; height: 42px; }
.ring circle:last-child { transition: stroke-dashoffset 0.5s ease; }
.ring span {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: #8b5cf6;
}

/* 英雄区 */
.hero-card {
  background: linear-gradient(135deg, #8b5cf6, #6366f1, #3b82f6);
  border-radius: 20px; padding: 28px 32px; color: #fff; text-align: center;
}
.hero-card h1 { font-size: 24px; font-weight: 800; margin: 0 0 14px; }
.hero-tags { display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.hero-tags span {
  background: rgba(255,255,255,0.2); padding: 5px 14px;
  border-radius: 20px; font-size: 13px; backdrop-filter: blur(4px);
}

/* 板块滚动区 */
.sections-scroll {
  display: flex; flex-direction: column; gap: 16px;
  max-height: 700px; overflow-y: auto; padding: 4px;
}

/* 板块卡片 */
.sec-card {
  background: var(--bg-secondary, #fff); border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid var(--border-color, #e2e8f0);
  overflow: hidden; transition: all 0.25s;
}
.sec-card:hover { box-shadow: 0 8px 28px rgba(0,0,0,0.08); transform: translateY(-2px); }

.sec-head {
  display: flex; align-items: center; gap: 14px; padding: 18px 24px;
  border-bottom: 1px solid var(--border-light, #f1f5f9);
}
.sec-num {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 800; color: #fff; flex-shrink: 0;
}
.sec-head h2 { font-size: 18px; font-weight: 700; margin: 0; color: var(--text-primary); }

/* 4色循环 */
.color0 .sec-head { background: linear-gradient(90deg, rgba(139,92,246,0.08), transparent); }
.color0 .sec-num { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.color1 .sec-head { background: linear-gradient(90deg, rgba(16,185,129,0.08), transparent); }
.color1 .sec-num { background: linear-gradient(135deg, #10b981, #059669); }
.color2 .sec-head { background: linear-gradient(90deg, rgba(245,158,11,0.08), transparent); }
.color2 .sec-num { background: linear-gradient(135deg, #f59e0b, #d97706); }
.color3 .sec-head { background: linear-gradient(90deg, rgba(59,130,246,0.08), transparent); }
.color3 .sec-num { background: linear-gradient(135deg, #3b82f6, #2563eb); }

.sec-body { padding: 22px 24px; line-height: 1.85; font-size: 15px; }

/* ===== Markdown 样式 ===== */
.md :deep(h3) {
  font-size: 16px; font-weight: 600; margin: 20px 0 10px;
  color: #6366f1; padding-left: 12px; border-left: 3px solid #c4b5fd;
}
.md :deep(h4) { font-size: 15px; font-weight: 600; margin: 16px 0 8px; color: #334155; }
.md :deep(p) { margin: 0 0 12px; line-height: 1.85; color: var(--text-primary, #334155); }
.md :deep(ul), .md :deep(ol) { padding-left: 8px; margin: 10px 0 14px; }
.md :deep(li) { margin: 6px 0; padding: 6px 12px; line-height: 1.7; border-radius: 8px; }
.md :deep(li:hover) { background: var(--bg-tertiary, #f8fafc); }
.md :deep(li::marker) { color: #8b5cf6; }
.md :deep(ol li::marker) { font-weight: 700; }
.md :deep(strong) {
  font-weight: 700;
  background: linear-gradient(transparent 60%, rgba(139,92,246,0.15) 60%);
  padding: 0 2px;
}
.md :deep(em) { color: #6366f1; font-style: normal; font-weight: 500; }

.md :deep(table) {
  width: 100%; border-collapse: separate; border-spacing: 0;
  margin: 14px 0 18px; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid var(--border-color, #e2e8f0);
}
.md :deep(thead th) {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: #fff; font-weight: 600; font-size: 13px;
  padding: 10px 14px; text-align: left; border: none;
}
.md :deep(tbody td) {
  padding: 10px 14px; font-size: 13px;
  border-bottom: 1px solid var(--border-light, #f1f5f9);
  border-right: 1px solid var(--border-light, #f1f5f9);
}
.md :deep(tbody tr:last-child td) { border-bottom: none; }
.md :deep(tbody tr:hover) { background: rgba(139,92,246,0.04); }

.md :deep(blockquote) {
  margin: 14px 0; padding: 14px 18px;
  border-left: 4px solid #8b5cf6; border-radius: 0 12px 12px 0;
  background: linear-gradient(90deg, rgba(139,92,246,0.06), transparent);
}
.md :deep(blockquote p) { margin: 0; }

.md :deep(hr) {
  border: none; height: 2px; margin: 24px 0;
  background: linear-gradient(90deg, transparent, #c4b5fd, transparent);
}

.md :deep(code) {
  background: rgba(139,92,246,0.1); color: #7c3aed;
  padding: 2px 8px; border-radius: 6px; font-size: 13px;
}

/* 生成指示器 */
.typing-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 16px 24px;
  background: var(--bg-secondary, #fff); border-radius: 12px;
  border: 1px dashed #c4b5fd;
}
.typing-label {
  font-size: 14px; color: #8b5cf6; font-weight: 500;
  animation: fade 2s ease-in-out infinite;
}
@keyframes fade { 0%,100% { opacity: 0.5; } 50% { opacity: 1; } }
.blink { animation: blink 1s step-end infinite; color: #8b5cf6; font-size: 20px; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0; } }

/* 底部按钮 */
.bottom-actions { display: flex; gap: 12px; justify-content: center; padding: 8px 0; }

/* FAB */
.fab {
  position: fixed; bottom: 32px; right: 32px;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 2px;
  box-shadow: 0 4px 20px rgba(5,150,105,0.4);
  cursor: pointer; transition: all 0.3s; z-index: 50;
}
.fab:hover { transform: scale(1.1); }
.fab svg { width: 22px; height: 22px; }
.fab small { font-size: 9px; font-weight: 600; }

@media (max-width: 768px) {
  .type-row { flex-direction: column; }
  .field-row { flex-direction: column; gap: 0; }
  .status-bar { flex-direction: column; gap: 12px; align-items: flex-start; }
  .sec-body { padding: 16px; }
  .form-card { padding: 24px; }
  .fab { bottom: 24px; right: 24px; width: 50px; height: 50px; }
  .fab small { display: none; }
}
</style>
