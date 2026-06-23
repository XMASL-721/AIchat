<template>
  <div class="favorites-page">
    <header class="favorites-header">
      <div class="header-inner">
        <div class="header-decoration">
          <span class="deco-circle c1"></span>
          <span class="deco-circle c2"></span>
          <span class="deco-circle c3"></span>
        </div>
        <div class="header-top">
          <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
        </div>
        <h1>❤️ 我的收藏</h1>
        <p>收藏的减脂餐和训练计划</p>
        <div class="stats-bar">
          <span class="stat-chip">🥗 {{ mealFavorites.length }}</span>
          <span class="stat-chip">🏋️ {{ workoutFavorites.length }}</span>
          <span class="stat-chip">📰 {{ articleFavorites.length }}</span>
          <span class="stat-chip">💊 {{ supplementFavorites.length }}</span>
        </div>
      </div>
    </header>

    <div class="favorites-container">
      <el-tabs v-model="activeTab" class="favorites-tabs">
        <el-tab-pane label="🥗 减脂餐" name="meal">
          <div v-if="mealLoading" class="loading-state">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="mealFavorites.length === 0" class="empty-state">
            <div class="empty-illustration">🥗</div>
            <p>还没有收藏减脂餐</p>
            <el-button type="primary" round @click="router.push('/meals')">去浏览减脂餐</el-button>
          </div>
          <div v-else class="favorites-grid">
            <div
              v-for="fav in mealFavorites"
              :key="fav.id"
              class="favorite-card meal-card"
              @click="goToDetail(fav)"
            >
              <div class="card-emoji-bg meal-bg">🥗</div>
              <div class="card-content">
                <div class="card-header">
                  <h3>{{ fav.targetName || '减脂餐' }}</h3>
                  <el-button
                    class="fav-btn"
                    circle
                    size="small"
                    @click.stop="removeFavorite(fav, 'meal')"
                    :loading="fav.removing"
                  >
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
                </div>
                <div class="card-meta">
                  <span v-if="fav.targetData?.calories" class="meta-tag calories">
                    🔥 {{ fav.targetData.calories }} kcal
                  </span>
                </div>
                <div class="card-footer">
                  <span class="card-link">查看详情 →</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="🏋️ 训练计划" name="workout">
          <div v-if="workoutLoading" class="loading-state">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="workoutFavorites.length === 0" class="empty-state">
            <div class="empty-illustration">🏋️</div>
            <p>还没有收藏训练计划</p>
            <el-button type="primary" round @click="router.push('/workouts')">去浏览训练</el-button>
          </div>
          <div v-else class="favorites-grid">
            <div
              v-for="fav in workoutFavorites"
              :key="fav.id"
              class="favorite-card workout-card"
              @click="goToDetail(fav)"
            >
              <div class="card-emoji-bg workout-bg">🏋️</div>
              <div class="card-content">
                <div class="card-header">
                  <h3>{{ fav.targetName || '训练计划' }}</h3>
                  <el-button
                    class="fav-btn"
                    circle
                    size="small"
                    @click.stop="removeFavorite(fav, 'workout')"
                    :loading="fav.removing"
                  >
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
                </div>
                <div class="card-meta">
                  <span v-if="fav.targetData?.duration" class="meta-tag duration">
                    ⏱ {{ fav.targetData.duration }}分钟
                  </span>
                  <span v-if="fav.targetData?.calories" class="meta-tag calories">
                    🔥 {{ fav.targetData.calories }} kcal
                  </span>
                  <span v-if="fav.targetData?.level" class="level-badge" :class="'lv-' + fav.targetData.level">
                    {{ fav.targetData.level }}
                  </span>
                </div>
                <div class="card-footer">
                  <span class="card-link">查看详情 →</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="📰 健康资讯" name="article">
          <div v-if="articleLoading" class="loading-state">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="articleFavorites.length === 0" class="empty-state">
            <div class="empty-illustration">📰</div>
            <p>还没有收藏文章</p>
            <el-button type="primary" round @click="router.push('/articles')">去浏览资讯</el-button>
          </div>
          <div v-else class="favorites-grid">
            <div v-for="fav in articleFavorites" :key="fav.id" class="favorite-card article-card" @click="goToDetail(fav)">
              <div class="card-emoji-bg article-bg">📰</div>
              <div class="card-content">
                <div class="card-header">
                  <h3>{{ fav.targetName || '文章' }}</h3>
                  <el-button class="fav-btn" circle size="small" @click.stop="removeFavorite(fav, 'article')" :loading="fav.removing">
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
                </div>
                <div class="card-footer"><span class="card-link article-link">阅读全文 →</span></div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="💊 补剂指南" name="supplement">
          <div v-if="supplementLoading" class="loading-state">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="supplementFavorites.length === 0" class="empty-state">
            <div class="empty-illustration">💊</div>
            <p>还没有收藏补剂</p>
            <el-button type="primary" round @click="router.push('/supplements')">去浏览补剂</el-button>
          </div>
          <div v-else class="favorites-grid">
            <div v-for="fav in supplementFavorites" :key="fav.id" class="favorite-card supplement-card" @click="goToDetail(fav)">
              <div class="card-emoji-bg supplement-bg">💊</div>
              <div class="card-content">
                <div class="card-header">
                  <h3>{{ fav.targetName || '补剂' }}</h3>
                  <el-button class="fav-btn" circle size="small" @click.stop="removeFavorite(fav, 'supplement')" :loading="fav.removing">
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
                </div>
                <div class="card-footer"><span class="card-link supplement-link">查看详情 →</span></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 浮动 AI 按钮 -->
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
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, StarFilled } from '@element-plus/icons-vue'
import { getUserFavoritesApi, removeFavoriteApi } from '../api/favorite'
import { getMealApi } from '../api/meal'
import { getWorkoutApi } from '../api/workout'
import { getArticleApi } from '../api/article'
import { getSupplementApi } from '../api/supplement'

const router = useRouter()
const activeTab = ref('meal')

// 分离四个 Tab 的数据，避免竞态条件
const mealFavorites = ref([])
const workoutFavorites = ref([])
const articleFavorites = ref([])
const supplementFavorites = ref([])
const mealLoading = ref(false)
const workoutLoading = ref(false)
const articleLoading = ref(false)
const supplementLoading = ref(false)

const loaders = {
  meal: loadMealFavorites,
  workout: loadWorkoutFavorites,
  article: loadArticleFavorites,
  supplement: loadSupplementFavorites
}
const dataRefs = { meal: mealFavorites, workout: workoutFavorites, article: articleFavorites, supplement: supplementFavorites }
const loadingRefs = { meal: mealLoading, workout: workoutLoading, article: articleLoading, supplement: supplementLoading }

watch(activeTab, () => {
  const tab = activeTab.value
  if (dataRefs[tab].value.length === 0 && !loadingRefs[tab].value) loaders[tab]()
})

async function loadMealFavorites() {
  mealLoading.value = true
  try {
    const res = await getUserFavoritesApi('meal', 1, 100)
    const list = res.data.list || []
    mealFavorites.value = await enrichFavorites(list, 'meal')
  } catch (error) {
    ElMessage.error('加载减脂餐收藏失败')
  } finally {
    mealLoading.value = false
  }
}

async function loadWorkoutFavorites() {
  workoutLoading.value = true
  try {
    const res = await getUserFavoritesApi('workout', 1, 100)
    const list = res.data.list || []
    workoutFavorites.value = await enrichFavorites(list, 'workout')
  } catch (error) {
    ElMessage.error('加载训练收藏失败')
  } finally {
    workoutLoading.value = false
  }
}

async function loadArticleFavorites() {
  articleLoading.value = true
  try {
    const res = await getUserFavoritesApi('article', 1, 100)
    articleFavorites.value = await enrichFavorites(res.data.list || [], 'article')
  } catch { ElMessage.error('加载文章收藏失败') }
  finally { articleLoading.value = false }
}

async function loadSupplementFavorites() {
  supplementLoading.value = true
  try {
    const res = await getUserFavoritesApi('supplement', 1, 100)
    supplementFavorites.value = await enrichFavorites(res.data.list || [], 'supplement')
  } catch { ElMessage.error('加载补剂收藏失败') }
  finally { supplementLoading.value = false }
}

async function enrichFavorites(favoriteList, type) {
  return await Promise.all(
    favoriteList.map(async (fav) => {
      try {
        let targetData = null, targetName = ''
        if (type === 'meal') {
          const res = await getMealApi(fav.targetId); targetData = res.data; targetName = targetData.name
        } else if (type === 'workout') {
          const res = await getWorkoutApi(fav.targetId); targetData = res.data; targetName = targetData.name
        } else if (type === 'article') {
          const res = await getArticleApi(fav.targetId); targetData = res.data; targetName = targetData.title
        } else if (type === 'supplement') {
          const res = await getSupplementApi(fav.targetId); targetData = res.data; targetName = targetData.name
        }
        return { ...fav, targetName, targetData, removing: false }
      } catch {
        return { ...fav, targetName: '已删除', targetData: null, removing: false }
      }
    })
  )
}

async function removeFavorite(fav, type) {
  fav.removing = true
  try {
    await removeFavoriteApi(fav.targetType, fav.targetId)
    ElMessage.success('已取消收藏')
    const refMap = { meal: mealFavorites, workout: workoutFavorites, article: articleFavorites, supplement: supplementFavorites }
    if (refMap[type]) {
      refMap[type].value = refMap[type].value.filter(f => f.id !== fav.id)
    }
  } catch { ElMessage.error('取消收藏失败') }
  finally { fav.removing = false }
}

function goToDetail(fav) {
  const routes = { meal: '/meals/', workout: '/workouts/', article: '/articles/', supplement: '/supplements/' }
  if (routes[fav.targetType]) router.push(routes[fav.targetType] + fav.targetId)
}

onMounted(() => {
  loadMealFavorites()
  loadWorkoutFavorites()
  loadArticleFavorites()
  loadSupplementFavorites()
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.favorites-header {
  background: linear-gradient(135deg, #dc2626, #ea580c, #f59e0b);
  color: #fff;
  padding: 48px 24px 40px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.header-inner {
  position: relative;
  z-index: 1;
}

.header-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.deco-circle.c1 {
  width: 200px;
  height: 200px;
  background: #fff;
  top: -60px;
  right: -40px;
}

.deco-circle.c2 {
  width: 120px;
  height: 120px;
  background: #fff;
  bottom: -30px;
  left: 10%;
}

.deco-circle.c3 {
  width: 80px;
  height: 80px;
  background: #fff;
  top: 20%;
  left: 5%;
}

.header-top {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-start;
  margin-bottom: 8px;
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

.header-inner > p {
  font-size: 15px;
  opacity: 0.9;
}

.stats-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.stat-chip {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.favorites-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}

.favorites-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.favorites-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.favorites-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
}

.favorites-tabs :deep(.el-tabs__content) {
  padding: 20px 0;
}

.favorites-tabs {
  background: var(--bg-secondary, #fff);
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-tertiary, #94a3b8);
}

.empty-illustration {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.loading-state p,
.empty-state p {
  margin: 12px 0 20px;
  font-size: 15px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.favorite-card {
  background: var(--bg-secondary, #fff);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--border-color, #e2e8f0);
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.meal-card:hover { border-color: #10b981; }
.workout-card:hover { border-color: #f59e0b; }
.article-card:hover { border-color: #0891b2; }
.supplement-card:hover { border-color: #7c3aed; }

.card-emoji-bg {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.meal-bg { background: linear-gradient(135deg, #ecfdf5, #d1fae5); }
.workout-bg { background: linear-gradient(135deg, #fef3c7, #fde68a); }
.article-bg { background: linear-gradient(135deg, #ecfeff, #cffafe); }
.supplement-bg { background: linear-gradient(135deg, #faf5ff, #ede9fe); }

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-btn {
  color: #f59e0b !important;
  background: #fef3c7 !important;
  border: none !important;
  flex-shrink: 0;
}

.fav-btn:hover {
  background: #fde68a !important;
}

.card-meta {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-tag {
  padding: 3px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.meta-tag.calories {
  background: #fef2f2;
  color: #ef4444;
}

.meta-tag.duration {
  background: #eff6ff;
  color: #3b82f6;
}

.level-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 8px;
  font-weight: 600;
}

.lv-初级 {
  background: #d1fae5;
  color: #059669;
}

.lv-中级 {
  background: #fef3c7;
  color: #d97706;
}

.lv-高级 {
  background: #fee2e2;
  color: #dc2626;
}

.card-footer {
  border-top: 1px solid var(--border-light, #f1f5f9);
  padding-top: 10px;
}

.card-link {
  font-size: 13px;
  color: #3b82f6;
  font-weight: 500;
}

.meal-card .card-link { color: #059669; }
.workout-card .card-link { color: #d97706; }
.article-card .card-link { color: #0891b2; }
.supplement-card .card-link { color: #7c3aed; }

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
  .fab-text {
    display: none;
  }
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  .stats-bar {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
