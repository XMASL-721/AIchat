<template>
  <div class="meal-page">
    <header class="meal-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
      </div>
      <div class="header-inner">
        <h1>🥗 减脂餐食谱大全</h1>
        <p>健康饮食，从每一餐开始 · 共 {{ total }} 道食谱</p>
      </div>
    </header>

    <div class="meal-toolbar">
      <div class="toolbar-inner">
        <el-input
          v-model="keyword"
          placeholder="搜索减脂餐..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="onSearch"
        />
        <el-button type="primary" round plain @click="router.push('/')">
          💬 AI 咨询
        </el-button>
      </div>
    </div>

    <div class="meal-container">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="meals.length === 0" class="empty-meals">
        <div class="empty-icon">🥗</div>
        <p>没有找到匹配的减脂餐</p>
      </div>

      <template v-else>
        <div class="meal-grid">
          <div
            v-for="meal in meals"
            :key="meal.id"
            class="meal-card"
            @click="router.push(`/meals/${meal.id}`)"
          >
            <div class="card-top">
              <span class="card-emoji">{{ getMealEmoji(meal.name) }}</span>
              <span class="card-calories" v-if="meal.calories">
                🔥 {{ meal.calories }} kcal
              </span>
            </div>
            <h3 class="card-name">{{ meal.name }}</h3>
            <p class="card-ingredients">{{ meal.ingredients?.slice(0, 40) }}...</p>
            <div class="card-footer">
              <span class="card-link">查看详情 →</span>
            </div>
          </div>
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            v-if="total > 0"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="20"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
          <span class="page-info">共 {{ total }} 道食谱</span>
        </div>
      </template>
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
import { listMealsApi } from '../api/meal'

const router = useRouter()
const keyword = ref('')
const meals = ref([])
const total = ref(0)
const currentPage = ref(1)
const loading = ref(true)
let searchTimer = null

function getMealEmoji(name) {
  if (!name) return '🥘'
  if (name.includes('鸡') || name.includes('鸡肉')) return '🍗'
  if (name.includes('鱼') || name.includes('三文鱼') || name.includes('虾')) return '🐟'
  if (name.includes('牛') || name.includes('牛肉')) return '🥩'
  if (name.includes('蛋') || name.includes('鸡蛋')) return '🥚'
  if (name.includes('沙拉') || name.includes('拌')) return '🥗'
  if (name.includes('汤') || name.includes('粥')) return '🍲'
  if (name.includes('豆腐') || name.includes('豆')) return '🫘'
  if (name.includes('面') || name.includes('粉')) return '🍜'
  if (name.includes('饭') || name.includes('米')) return '🍚'
  if (name.includes('三明治') || name.includes('面包')) return '🥪'
  if (name.includes('酸奶') || name.includes('奶')) return '🥛'
  if (name.includes('果') || name.includes('莓')) return '🍓'
  return '🥘'
}

async function loadMeals(page = 1) {
  loading.value = true
  currentPage.value = page
  try {
    const res = await listMealsApi(page, 20, keyword.value)
    meals.value = res.data.list || []
    total.value = res.data.total || 0
  } catch {
    ElMessage.error('加载减脂餐失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  loadMeals(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadMeals(1)
  }, 300)
}

onMounted(() => loadMeals(1))
</script>

<style scoped>
.meal-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.meal-header {
  background: linear-gradient(135deg, #059669, #0d9488, #2563eb);
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
  width: 260px; height: 260px;
  background: #fff;
  top: -80px; right: -50px;
}

.deco-circle.c2 {
  width: 180px; height: 180px;
  background: #fff;
  bottom: -50px; left: 8%;
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

.header-inner h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-inner p {
  font-size: 15px;
  opacity: 0.9;
}

.meal-toolbar {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px 0;
}

.toolbar-inner {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.meal-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px;
}

.loading-state,
.empty-meals {
  text-align: center;
  padding: 60px 0;
  color: var(--text-tertiary, #94a3b8);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.meal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.meal-card {
  background: var(--bg-secondary, #fff);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border-color, #e2e8f0);
}

.meal-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(13, 148, 136, 0.12);
  border-color: #0d9488;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-emoji {
  font-size: 36px;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  border-radius: 14px;
}

.card-calories {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #ef4444;
  background: #fef2f2;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.card-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin-bottom: 8px;
}

.card-ingredients {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  line-height: 1.5;
  margin-bottom: 12px;
}

.card-footer {
  border-top: 1px solid var(--border-light, #f1f5f9);
  padding-top: 12px;
}

.card-link {
  font-size: 13px;
  color: #0d9488;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  padding: 16px 0;
}

.page-info {
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

.fab-icon {
  font-size: 22px;
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
  .fab-text {
    display: none;
  }
  .meal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
