<template>
  <div class="workout-page">
    <header class="workout-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
      </div>
      <div class="header-inner">
        <h1>🏋️ 健身训练计划</h1>
        <p>科学训练，高效塑形 · 共 {{ total }} 套动作</p>
      </div>
    </header>

    <div class="toolbar">
      <div class="toolbar-inner">
        <el-input v-model="keyword" placeholder="搜索训练..." :prefix-icon="Search" clearable class="search" @input="onSearch" />
        <el-select v-model="category" placeholder="按类别筛选" clearable @change="onSearch" style="width:140px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button type="primary" round plain @click="router.push('/')">💬 AI 咨询</el-button>
      </div>
    </div>

    <div class="container">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      <div v-else-if="workouts.length === 0" class="empty-state">
        <div class="empty-icon">🏋️</div>
        <p>没有找到匹配的训练</p>
      </div>
      <div v-else class="grid">
        <div v-for="w in workouts" :key="w.id" class="card" @click="router.push(`/workouts/${w.id}`)">
          <div class="card-top">
            <span class="cat-badge">{{ w.category }}</span>
            <span class="level-badge" :class="levelClass(w.level)">{{ w.level }}</span>
          </div>
          <h3>{{ w.name }}</h3>
          <div class="meta">
            <span class="meta-item">
              <span class="meta-icon">⏱</span>
              {{ w.duration }}分钟
            </span>
            <span class="meta-item">
              <span class="meta-icon">🔥</span>
              {{ w.calories }} kcal
            </span>
          </div>
          <div class="card-foot">
            <span class="link">查看详情 →</span>
          </div>
        </div>
      </div>
      <div class="pagination" v-if="total > 0">
        <el-pagination background layout="prev,pager,next" :total="total" :page-size="20" :current-page="currentPage" @current-change="loadWorkouts($event)" />
        <span class="info">共 {{ total }} 套</span>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { listWorkoutsApi } from '../api/workout'

const router = useRouter()
const keyword = ref(''), category = ref('')
const workouts = ref([]), total = ref(0), currentPage = ref(1), loading = ref(true)
const categories = ['腹肌', '胸肌', '背部', '腿部', '手臂', '全身', '有氧']
let timer = null

async function loadWorkouts(page = 1) {
  loading.value = true
  currentPage.value = page
  try {
    const r = await listWorkoutsApi(page, 20, keyword.value, category.value)
    workouts.value = r.data.list || []
    total.value = r.data.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => loadWorkouts(1), 300)
}

function levelClass(l) {
  return { '初级': 'l1', '中级': 'l2', '高级': 'l3' }[l] || ''
}

onMounted(() => loadWorkouts(1))
</script>

<style scoped>
.workout-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.workout-header {
  background: linear-gradient(135deg, #dc2626, #ea580c, #f59e0b);
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
  opacity: 0.12;
}

.deco-circle.c1 {
  width: 240px; height: 240px;
  background: #fff;
  top: -80px; right: -40px;
}

.deco-circle.c2 {
  width: 160px; height: 160px;
  background: #fff;
  bottom: -40px; left: 10%;
}

.header-inner {
  position: relative;
  z-index: 1;
}

.header-top {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-start;
  max-width: 960px;
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

.header-inner h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-inner p {
  font-size: 15px;
  opacity: 0.9;
}

.toolbar {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 16px 0;
}

.toolbar-inner {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search {
  flex: 1;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 16px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-tertiary, #94a3b8);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.card {
  background: var(--bg-secondary, #fff);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border-color, #e2e8f0);
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(220, 38, 38, 0.12);
  border-color: #ea580c;
}

.card-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.cat-badge {
  font-size: 12px;
  color: #ea580c;
  background: #fff7ed;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.level-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.l1 { background: #d1fae5; color: #059669; }
.l2 { background: #fef3c7; color: #d97706; }
.l3 { background: #fee2e2; color: #dc2626; }

.card h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin-bottom: 10px;
}

.meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  font-size: 14px;
}

.card-foot {
  border-top: 1px solid var(--border-light, #f1f5f9);
  padding-top: 12px;
}

.link {
  font-size: 13px;
  color: #ea580c;
  font-weight: 500;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
}

.info {
  font-size: 13px;
  color: var(--text-tertiary, #94a3b8);
}

.fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #dc2626, #ea580c);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(220, 38, 38, 0.4);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 50;
  line-height: 1;
}

.fab:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 30px rgba(220, 38, 38, 0.5);
}

.fab-text {
  font-size: 9px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .fab {
    bottom: 24px;
    right: 24px;
    width: 50px;
    height: 50px;
  }
  .fab-text {
    display: none;
  }
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
