<template>
  <div class="detail-page">
    <div v-if="loading" class="detail-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template v-else-if="meal">
      <header class="detail-header">
        <div class="header-decoration">
          <span class="deco-circle c1"></span>
          <span class="deco-circle c2"></span>
        </div>
        <div class="header-inner">
          <el-button class="back-btn" @click="router.push('/meals')" round>
            ← 返回列表
          </el-button>
          <div style="display:flex;gap:12px;align-items:center;">
            <el-button
              :type="isFavorite ? 'danger' : 'default'"
              @click="toggleFavorite"
              :loading="favoriteLoading"
              round
              class="fav-toggle"
            >
              {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>
            <div class="header-badge" v-if="meal.calories">
              🔥 {{ meal.calories }} 千卡
            </div>
          </div>
        </div>
        <h1 class="detail-title">{{ meal.name }}</h1>
      </header>

      <div class="detail-body">
        <section class="detail-section">
          <h2>🛒 食材清单</h2>
          <ul class="ingredient-list">
            <li
              v-for="(item, i) in ingredientList"
              :key="i"
              class="ingredient-item"
            >
              <span class="ingredient-dot" />
              {{ item }}
            </li>
          </ul>
        </section>

        <section class="detail-section">
          <h2>👨‍🍳 做法步骤</h2>
          <div
            v-for="(step, i) in stepList"
            :key="i"
            class="step-item"
          >
            <div class="step-num">{{ i + 1 }}</div>
            <div class="step-text">{{ step }}</div>
          </div>
        </section>

        <section class="detail-section action-section">
          <el-button
            type="success"
            size="large"
            @click="recordMeal"
            :loading="recordLoading"
            style="width: 100%"
            round
          >
            📝 记录此餐
          </el-button>
        </section>
      </div>
    </template>

    <div v-else class="detail-loading">
      <p style="color:#94a3b8;">减脂餐不存在</p>
      <el-button @click="router.push('/meals')">返回列表</el-button>
    </div>

    <div class="fab" @click="goToChat">
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMealApi } from '../api/meal'
import { addFavoriteApi, removeFavoriteApi, checkFavoriteApi } from '../api/favorite'
import { addRecordApi } from '../api/dailyRecord'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const meal = ref(null)
const loading = ref(true)
const isFavorite = ref(false)
const favoriteLoading = ref(false)
const recordLoading = ref(false)

onMounted(async () => {
  await loadMeal()
  if (userStore.isLoggedIn && meal.value) {
    await checkFavoriteStatus()
  }
})

async function loadMeal() {
  try {
    const res = await getMealApi(route.params.id)
    meal.value = res.data
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function checkFavoriteStatus() {
  if (!localStorage.getItem('token')) return
  try {
    const res = await checkFavoriteApi('meal', meal.value.id)
    isFavorite.value = res.data
  } catch { /* 静默处理，不影响页面显示 */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavoriteApi('meal', meal.value.id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavoriteApi('meal', meal.value.id)
      isFavorite.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

async function recordMeal() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  recordLoading.value = true
  try {
    await addRecordApi('meal', meal.value.id, meal.value.name, meal.value.calories)
    ElMessage.success('记录成功！已加入今日饮食记录')
  } catch (error) {
    ElMessage.error('记录失败')
  } finally {
    recordLoading.value = false
  }
}

const ingredientList = computed(() => {
  return meal.value?.ingredients?.split(/、|，|,/)?.map(s => s.trim()).filter(Boolean) || []
})

const stepList = computed(() => {
  return meal.value?.steps?.split('\n')?.map(s => s.trim().replace(/^\d+[\.、]\s*/, '')).filter(Boolean) || []
})

function goToChat() {
  router.push('/')
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 12px;
  color: var(--text-tertiary, #94a3b8);
}

.detail-header {
  background: linear-gradient(135deg, #0d9488, #059669, #2563eb);
  color: #fff;
  padding: 24px 24px 36px;
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
  width: 200px; height: 200px;
  background: #fff;
  top: -60px; right: -40px;
}

.deco-circle.c2 {
  width: 140px; height: 140px;
  background: #fff;
  bottom: -30px; left: 5%;
}

.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 720px;
  margin: 0 auto 16px;
  position: relative;
  z-index: 1;
}

.back-btn {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: #fff !important;
  backdrop-filter: blur(8px);
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.25) !important;
}

.fav-toggle {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: #fff !important;
  backdrop-filter: blur(8px);
}

.header-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.detail-title {
  max-width: 720px;
  margin: 0 auto;
  font-size: 26px;
  font-weight: 700;
  padding: 0 4px;
  position: relative;
  z-index: 1;
}

.detail-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px;
}

.detail-section {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.detail-section h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border-light, #f1f5f9);
}

.ingredient-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-tertiary, #f8fafc);
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-primary, #334155);
  border: 1px solid var(--border-color, #e2e8f0);
  transition: all 0.2s;
}

.ingredient-item:hover {
  border-color: #0d9488;
  background: #ecfdf5;
}

.ingredient-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0d9488;
  flex-shrink: 0;
}

.step-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-light, #f1f5f9);
  transition: background 0.2s;
}

.step-item:hover {
  background: var(--bg-tertiary, #f8fafc);
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 12px;
}

.step-item:last-child {
  border-bottom: none;
}

.step-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(5, 150, 105, 0.3);
}

.step-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-primary, #334155);
  padding-top: 6px;
}

.action-section {
  text-align: center;
  padding: 24px 28px;
  margin-top: 16px;
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
}
</style>
