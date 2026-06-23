<template>
  <div class="detail-page">
    <div v-if="loading" class="detail-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template v-else-if="article">
      <header class="detail-header">
        <div class="header-decoration">
          <span class="deco-circle c1"></span>
          <span class="deco-circle c2"></span>
        </div>
        <div class="hdr-inner">
          <el-button class="back-btn" @click="router.push('/articles')" round>← 返回列表</el-button>
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
            <span class="badge views">👁 {{ article.viewCount }}</span>
            <span class="badge category" :class="'cat-' + article.category">{{ getCategoryLabel(article.category) }}</span>
          </div>
        </div>
        <h1>{{ article.title }}</h1>
        <p class="subtitle">{{ article.summary }}</p>
      </header>

      <div class="body">
        <div class="content-card">
          <div class="article-content" v-html="renderedContent"></div>
        </div>

        <div class="tags-section" v-if="article.tags">
          <span class="tag" v-for="tag in tagList" :key="tag"># {{ tag }}</span>
        </div>
      </div>
    </template>

    <div v-else class="detail-loading">
      <p style="color:#94a3b8;">文章不存在</p>
      <el-button @click="router.push('/articles')">返回列表</el-button>
    </div>

    <div class="fab" @click="router.push('/')">
      <span class="fab-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:22px;height:22px"><path d="M12 2a4 4 0 0 1 4 4c0 2-2 3-4 5-2-2-4-3-4-5a4 4 0 0 1 4-4z"/><path d="M6 16c0-2 2-3 4-5 2 2 4 3 4 5"/><path d="M2 22c0-4 4-6 10-6s10 2 10 6"/></svg></span>
      <span class="fab-text">AI 咨询</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getArticleApi } from '../api/article'
import { addFavoriteApi, removeFavoriteApi, checkFavoriteApi } from '../api/favorite'
import { useUserStore } from '../stores/user'
import { renderMarkdown } from '../utils/markdown'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const loading = ref(true)
const isFavorite = ref(false)
const favoriteLoading = ref(false)

const renderedContent = computed(() => renderMarkdown(article.value?.content || ''))
const tagList = computed(() => article.value?.tags?.split(',').map(t => t.trim()).filter(Boolean) || [])

function getCategoryLabel(cat) {
  return { nutrition: '营养知识', training: '训练技巧', myths: '减脂误区', mental: '心理健康' }[cat] || cat
}

onMounted(async () => {
  await loadArticle()
  if (userStore.isLoggedIn && article.value) await checkFavoriteStatus()
})

async function loadArticle() {
  try {
    const res = await getArticleApi(route.params.id)
    article.value = res.data
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function checkFavoriteStatus() {
  if (!localStorage.getItem('token')) return
  try {
    const res = await checkFavoriteApi('article', article.value.id)
    isFavorite.value = res.data
  } catch { /* 静默处理 */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavoriteApi('article', article.value.id)
      isFavorite.value = false; ElMessage.success('已取消收藏')
    } else {
      await addFavoriteApi('article', article.value.id)
      isFavorite.value = true; ElMessage.success('收藏成功')
    }
  } catch { ElMessage.error('操作失败') }
  finally { favoriteLoading.value = false }
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--bg-primary, #f0f2f5); padding-bottom: 80px; }
.detail-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; gap: 12px; color: var(--text-tertiary, #94a3b8); }

.detail-header {
  background: linear-gradient(135deg, #0891b2, #0e7490, #1e40af);
  color: #fff; padding: 24px 24px 36px; position: relative; overflow: hidden;
}
.header-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; opacity: 0.1; }
.deco-circle.c1 { width: 200px; height: 200px; background: #fff; top: -60px; right: -40px; }
.deco-circle.c2 { width: 140px; height: 140px; background: #fff; bottom: -30px; left: 5%; }

.hdr-inner { display: flex; justify-content: space-between; align-items: center; max-width: 800px; margin: 0 auto 16px; position: relative; z-index: 1; }
.back-btn { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }
.back-btn:hover { background: rgba(255,255,255,0.25) !important; }
.fav-toggle { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }

.badge { font-size: 13px; background: rgba(255,255,255,0.2); padding: 5px 14px; border-radius: 20px; }
.badge.category { font-weight: 600; }

.detail-header h1 { max-width: 800px; margin: 8px auto 12px; font-size: 28px; font-weight: 700; position: relative; z-index: 1; line-height: 1.4; }
.subtitle { max-width: 800px; margin: 0 auto; font-size: 15px; opacity: 0.85; position: relative; z-index: 1; line-height: 1.6; }

.body { max-width: 800px; margin: 0 auto; padding: 24px 16px; }

.content-card {
  background: var(--bg-secondary, #fff); border-radius: 20px; padding: 32px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06); border: 1px solid var(--border-color, #e2e8f0);
}

.article-content { line-height: 1.8; font-size: 15px; color: var(--text-primary, #1e293b); }
.article-content :deep(h2) { font-size: 22px; font-weight: 700; margin: 32px 0 16px; padding-bottom: 8px; border-bottom: 2px solid var(--border-light, #f1f5f9); }
.article-content :deep(h3) { font-size: 18px; font-weight: 600; margin: 24px 0 12px; }
.article-content :deep(p) { margin-bottom: 16px; }
.article-content :deep(strong) { color: var(--text-primary); font-weight: 700; }
.article-content :deep(ul), .article-content :deep(ol) { padding-left: 24px; margin: 12px 0; }
.article-content :deep(li) { margin: 8px 0; line-height: 1.7; }
.article-content :deep(table) { width: 100%; border-collapse: collapse; margin: 16px 0; border-radius: 8px; overflow: hidden; }
.article-content :deep(th), .article-content :deep(td) { border: 1px solid var(--border-color, #e2e8f0); padding: 12px; text-align: left; }
.article-content :deep(th) { background: var(--bg-tertiary, #f1f5f9); font-weight: 600; }

.tags-section { margin-top: 16px; display: flex; flex-wrap: wrap; gap: 8px; }
.tag {
  background: var(--bg-secondary, #fff); border: 1px solid var(--border-color, #e2e8f0);
  padding: 6px 14px; border-radius: 20px; font-size: 13px; color: var(--text-secondary, #64748b);
}

.fab {
  position: fixed; bottom: 32px; right: 32px;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #059669, #0d9488);
  color: #fff; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(5,150,105,0.4);
  cursor: pointer; transition: all 0.3s; z-index: 50; line-height: 1;
}
.fab:hover { transform: scale(1.1); }
.fab-text { font-size: 9px; font-weight: 600; }
@media (max-width: 768px) {
  .fab { bottom: 24px; right: 24px; width: 50px; height: 50px; }
  .fab-text { display: none; }
}
</style>
