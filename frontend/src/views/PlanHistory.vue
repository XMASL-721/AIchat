<template>
  <div class="plan-history-page">
    <header class="plan-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
      </div>
      <div class="header-inner">
        <h1>📋 我的计划历史</h1>
        <p>查看和管理你生成的所有计划</p>
      </div>
    </header>

    <div class="plan-container">
      <div class="toolbar">
        <el-button type="primary" round @click="router.push('/plans/generate')">
          <el-icon><Plus /></el-icon> 生成新计划
        </el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="plans.length === 0" class="empty-state">
        <div class="empty-illustration">📋</div>
        <p>还没有生成过计划</p>
        <el-button type="primary" round @click="router.push('/plans/generate')">去生成</el-button>
      </div>

      <div v-else class="plans-grid">
        <div
          v-for="plan in plans"
          :key="plan.id"
          class="plan-card"
          @click="viewPlan(plan)"
        >
          <div class="card-type-bar" :class="plan.planType"></div>
          <div class="card-body">
            <div class="card-header">
              <el-tag :type="getPlanTypeTag(plan.planType)" size="large" round>
                {{ getPlanTypeIcon(plan.planType) }} {{ getPlanTypeName(plan.planType) }}
              </el-tag>
              <el-button
                type="danger"
                text
                @click.stop="handleDelete(plan.id)"
                :loading="plan.deleting"
                size="small"
              >
                🗑️
              </el-button>
            </div>
            <div class="card-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(plan.createdAt) }}
              </span>
            </div>
            <div class="card-preview">
              {{ getPreview(plan.planContent) }}
            </div>
            <div class="card-footer">
              <span class="card-link">查看详情 →</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="10"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="currentPlan ? getPlanTypeName(currentPlan.planType) + ' 计划' : ''"
      width="800px"
      top="5vh"
    >
      <div class="plan-detail-content" v-if="currentPlan">
        <div v-html="renderedPlanContent"></div>
      </div>
      <template #footer>
        <el-button round @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading, Calendar } from '@element-plus/icons-vue'
import { getUserPlansApi, deletePlanApi } from '../api/plan'
import { renderMarkdown } from '../utils/markdown'

const router = useRouter()

const plans = ref([])
const total = ref(0)
const currentPage = ref(1)
const loading = ref(false)
const dialogVisible = ref(false)
const currentPlan = ref(null)

const renderedPlanContent = computed(() => {
  return currentPlan.value ? renderMarkdown(currentPlan.value.planContent) : ''
})

onMounted(() => { loadPlans() })

async function loadPlans() {
  loading.value = true
  try {
    const res = await getUserPlansApi(currentPage.value, 10)
    plans.value = (res.data.list || []).map(p => ({ ...p, deleting: false }))
    total.value = res.data.total || 0
  } catch {
    ElMessage.error('加载计划列表失败')
  } finally {
    loading.value = false
  }
}

function getPlanTypeName(type) {
  return { diet: '饮食计划', workout: '训练计划', combined: '综合计划' }[type] || '个性化计划'
}

function getPlanTypeIcon(type) {
  return { diet: '🥗', workout: '🏋️', combined: '🎯' }[type] || '📋'
}

function getPlanTypeTag(type) {
  return { diet: 'success', workout: 'warning', combined: '' }[type] || 'info'
}

function formatDate(dateStr) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getPreview(content) {
  if (!content) return ''
  const clean = content.replace(/[#*`_]/g, '').replace(/\n+/g, ' ')
  return clean.substring(0, 100) + (clean.length > 100 ? '...' : '')
}

function viewPlan(plan) {
  currentPlan.value = plan
  dialogVisible.value = true
}

async function handleDelete(planId) {
  try {
    await ElMessageBox.confirm('确定要删除这个计划吗？', '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    const plan = plans.value.find(p => p.id === planId)
    if (plan) plan.deleting = true
    await deletePlanApi(planId)
    ElMessage.success('已删除')
    loadPlans()
  } catch {
    const plan = plans.value.find(p => p.id === planId)
    if (plan) plan.deleting = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  loadPlans()
}
</script>

<style scoped>
.plan-history-page {
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

.header-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; opacity: 0.1; }
.deco-circle.c1 { width: 240px; height: 240px; background: #fff; top: -80px; right: -40px; }
.deco-circle.c2 { width: 140px; height: 140px; background: #fff; bottom: -30px; left: 8%; }

.header-top { position: relative; z-index: 1; display: flex; justify-content: flex-start; max-width: 1200px; margin: 0 auto 12px; }
.back-btn { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; backdrop-filter: blur(8px); }
.back-btn:hover { background: rgba(255,255,255,0.25) !important; }
.header-inner { position: relative; z-index: 1; }
.header-inner h1 { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.header-inner p { font-size: 15px; opacity: 0.9; }

.plan-container { max-width: 1200px; margin: 0 auto; padding: 24px 16px; }
.toolbar { display: flex; justify-content: flex-end; margin-bottom: 20px; }

.loading-state, .empty-state {
  text-align: center; padding: 60px 0; color: var(--text-tertiary, #94a3b8);
}
.empty-illustration { font-size: 64px; margin-bottom: 12px; opacity: 0.5; }
.loading-state p, .empty-state p { margin: 12px 0 20px; }

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}

.plan-card {
  background: var(--bg-secondary, #fff);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border-color, #e2e8f0);
  display: flex;
  overflow: hidden;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.12);
  border-color: #6366f1;
}

.card-type-bar { width: 6px; flex-shrink: 0; }
.card-type-bar.diet { background: linear-gradient(180deg, #10b981, #059669); }
.card-type-bar.workout { background: linear-gradient(180deg, #f59e0b, #d97706); }
.card-type-bar.combined { background: linear-gradient(180deg, #6366f1, #3b82f6); }

.card-body { flex: 1; padding: 20px; min-width: 0; }

.card-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
}

.card-meta {
  display: flex; gap: 16px; font-size: 13px;
  color: var(--text-secondary, #64748b); margin-bottom: 12px;
}

.meta-item { display: flex; align-items: center; gap: 4px; }

.card-preview {
  font-size: 14px; color: var(--text-secondary, #475569);
  line-height: 1.6; margin-bottom: 12px; min-height: 48px;
}

.card-footer { border-top: 1px solid var(--border-light, #f1f5f9); padding-top: 12px; }
.card-link { font-size: 13px; color: #6366f1; font-weight: 500; }

.pagination { display: flex; justify-content: center; margin-top: 32px; }

.plan-detail-content {
  max-height: 70vh; overflow-y: auto; padding: 28px;
  background: var(--bg-secondary, #fff); border-radius: 12px; line-height: 1.85; font-size: 15px;
}
.plan-detail-content :deep(> h1) {
  font-size: 24px; font-weight: 800; margin: 0 0 20px; padding: 14px 0;
  border-bottom: 3px solid #8b5cf6;
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.plan-detail-content :deep(> h2) {
  font-size: 19px; font-weight: 700; margin: 28px 0 14px; padding: 10px 16px;
  border-radius: 12px; border-left: 4px solid #8b5cf6;
  background: linear-gradient(90deg, rgba(139,92,246,0.08), transparent);
}
.plan-detail-content :deep(> h3) {
  font-size: 16px; font-weight: 600; margin: 22px 0 10px;
  color: #6366f1; padding-left: 12px; border-left: 3px solid #c4b5fd;
}
.plan-detail-content :deep(> p) { margin: 0 0 14px; line-height: 1.85; }
.plan-detail-content :deep(> ul > li),
.plan-detail-content :deep(> ol > li) {
  margin: 8px 0; padding: 8px 12px; border-radius: 8px; line-height: 1.7;
}
.plan-detail-content :deep(> ul > li::marker) { color: #8b5cf6; }
.plan-detail-content :deep(> ol > li::marker) { color: #8b5cf6; font-weight: 700; }
.plan-detail-content :deep(strong) {
  font-weight: 700;
  background: linear-gradient(transparent 60%, rgba(139,92,246,0.15) 60%);
  padding: 0 2px;
}
.plan-detail-content :deep(table) {
  width: 100%; border-collapse: separate; border-spacing: 0;
  margin: 16px 0 20px; border-radius: 12px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 1px solid var(--border-color, #e2e8f0);
}
.plan-detail-content :deep(thead th) {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: #fff; font-weight: 600; font-size: 14px; padding: 12px 16px; border: none;
}
.plan-detail-content :deep(tbody td) {
  padding: 11px 16px; font-size: 14px;
  border-bottom: 1px solid var(--border-light, #f1f5f9);
}
.plan-detail-content :deep(tbody tr:hover) { background: rgba(139,92,246,0.04); }
.plan-detail-content :deep(hr) {
  border: none; height: 2px; margin: 28px 0;
  background: linear-gradient(90deg, transparent, #c4b5fd, transparent);
}
.plan-detail-content :deep(blockquote) {
  margin: 16px 0; padding: 16px 20px;
  border-left: 4px solid #8b5cf6; border-radius: 0 12px 12px 0;
  background: linear-gradient(90deg, rgba(139,92,246,0.06), transparent);
}

.fab {
  position: fixed; bottom: 32px; right: 32px;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(5, 150, 105, 0.4);
  cursor: pointer; transition: all 0.3s; z-index: 50; line-height: 1;
}
.fab:hover { transform: scale(1.1); box-shadow: 0 8px 30px rgba(5, 150, 105, 0.5); }
.fab-text { font-size: 9px; font-weight: 600; letter-spacing: 0.5px; }

@media (max-width: 768px) {
  .fab { bottom: 24px; right: 24px; width: 50px; height: 50px; }
  .fab-text { display: none; }
  .plans-grid { grid-template-columns: 1fr; }
}
</style>
