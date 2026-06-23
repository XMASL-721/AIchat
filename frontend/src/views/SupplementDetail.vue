<template>
  <div class="detail-page">
    <div v-if="loading" class="detail-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template v-else-if="s">
      <header class="detail-header">
        <div class="header-decoration">
          <span class="deco-circle c1"></span>
          <span class="deco-circle c2"></span>
        </div>
        <div class="hdr-inner">
          <el-button class="back-btn" @click="router.push('/supplements')" round>← 返回列表</el-button>
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
            <span class="badge category" :class="'cat-' + s.category">{{ getCategoryLabel(s.category) }}</span>
          </div>
        </div>
        <div class="title-area">
          <span class="title-emoji">{{ s.emoji || '💊' }}</span>
          <h1>{{ s.name }}</h1>
        </div>
      </header>

      <div class="body">
        <!-- 简介 -->
        <section class="sec">
          <h2>📋 补剂简介</h2>
          <p class="desc-text">{{ s.description }}</p>
        </section>

        <!-- 主要功效 -->
        <section class="sec" v-if="s.benefits">
          <h2>✅ 主要功效</h2>
          <ul class="benefit-list">
            <li v-for="(b, i) in benefitList" :key="i">{{ b }}</li>
          </ul>
        </section>

        <!-- 推荐剂量 -->
        <section class="sec" v-if="s.dosage">
          <h2>📏 推荐剂量</h2>
          <div class="info-box dosage-box">{{ s.dosage }}</div>
        </section>

        <!-- 服用时机 -->
        <section class="sec" v-if="s.timing">
          <h2>⏰ 服用时机</h2>
          <div class="info-box timing-box">{{ s.timing }}</div>
        </section>

        <!-- 适用人群 -->
        <section class="sec" v-if="s.suitableFor">
          <h2>👥 适用人群</h2>
          <div class="tags-wrap">
            <span class="suitable-tag" v-for="(p, i) in suitableList" :key="i">✓ {{ p }}</span>
          </div>
        </section>

        <!-- 注意事项 -->
        <section class="sec warning" v-if="s.sideEffects">
          <h2>⚠️ 注意事项</h2>
          <ul class="warning-list">
            <li v-for="(w, i) in sideEffectList" :key="i">{{ w }}</li>
          </ul>
        </section>
      </div>
    </template>

    <div v-else class="detail-loading">
      <p style="color:#94a3b8;">补剂不存在</p>
      <el-button @click="router.push('/supplements')">返回列表</el-button>
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
import { getSupplementApi } from '../api/supplement'
import { addFavoriteApi, removeFavoriteApi, checkFavoriteApi } from '../api/favorite'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const s = ref(null)
const loading = ref(true)
const isFavorite = ref(false)
const favoriteLoading = ref(false)

function getCategoryLabel(cat) {
  return { protein: '蛋白质', creatine: '肌酸', vitamin: '维生素/矿物质', amino: '氨基酸', other: '其他' }[cat] || cat
}

const benefitList = computed(() => s.value?.benefits?.split('\n').filter(Boolean) || [])
const sideEffectList = computed(() => s.value?.sideEffects?.split('\n').filter(Boolean) || [])
const suitableList = computed(() => s.value?.suitableFor?.split('\n').filter(Boolean) || [])

onMounted(async () => {
  await loadData()
  if (userStore.isLoggedIn && s.value) await checkFavoriteStatus()
})

async function loadData() {
  try {
    const res = await getSupplementApi(route.params.id)
    s.value = res.data
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function checkFavoriteStatus() {
  if (!localStorage.getItem('token')) return
  try {
    const res = await checkFavoriteApi('supplement', s.value.id)
    isFavorite.value = res.data
  } catch { /* 静默处理 */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavoriteApi('supplement', s.value.id)
      isFavorite.value = false; ElMessage.success('已取消收藏')
    } else {
      await addFavoriteApi('supplement', s.value.id)
      isFavorite.value = true; ElMessage.success('收藏成功')
    }
  } catch { ElMessage.error('操作失败') }
  finally { favoriteLoading.value = false }
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--bg-primary, #f0f2f5); padding-bottom: 80px; }
.detail-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; gap: 12px; color: var(--text-tertiary); }

.detail-header {
  background: linear-gradient(135deg, #7c3aed, #6d28d9, #4f46e5);
  color: #fff; padding: 24px 24px 36px; position: relative; overflow: hidden;
}
.header-decoration { position: absolute; inset: 0; pointer-events: none; }
.deco-circle { position: absolute; border-radius: 50%; opacity: 0.1; }
.deco-circle.c1 { width: 200px; height: 200px; background: #fff; top: -60px; right: -40px; }
.deco-circle.c2 { width: 140px; height: 140px; background: #fff; bottom: -30px; left: 5%; }

.hdr-inner { display: flex; justify-content: space-between; align-items: center; max-width: 720px; margin: 0 auto 16px; position: relative; z-index: 1; }
.back-btn { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }
.back-btn:hover { background: rgba(255,255,255,0.25) !important; }
.fav-toggle { background: rgba(255,255,255,0.15) !important; border: 1px solid rgba(255,255,255,0.3) !important; color: #fff !important; }
.badge { font-size: 13px; background: rgba(255,255,255,0.2); padding: 5px 14px; border-radius: 20px; font-weight: 600; }

.title-area { text-align: center; position: relative; z-index: 1; }
.title-emoji { font-size: 48px; display: block; margin-bottom: 8px; }
.title-area h1 { font-size: 28px; font-weight: 700; }

.body { max-width: 720px; margin: 0 auto; padding: 24px 16px; }

.sec {
  background: var(--bg-secondary, #fff); border-radius: 20px; padding: 28px;
  margin-bottom: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}
.sec h2 {
  font-size: 18px; font-weight: 600; color: var(--text-primary, #0f172a);
  margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid var(--border-light, #f1f5f9);
}
.sec.warning { border-left: 4px solid #f59e0b; }

.desc-text { font-size: 15px; line-height: 1.8; color: var(--text-primary, #334155); }

.benefit-list { list-style: none; padding: 0; }
.benefit-list li {
  padding: 10px 0 10px 28px; position: relative; font-size: 15px;
  color: var(--text-primary, #334155); border-bottom: 1px solid var(--border-light, #f1f5f9);
}
.benefit-list li:last-child { border-bottom: none; }
.benefit-list li::before {
  content: '✅'; position: absolute; left: 0; top: 10px;
}

.warning-list { list-style: none; padding: 0; }
.warning-list li {
  padding: 10px 0 10px 28px; position: relative; font-size: 15px;
  color: var(--text-primary, #334155); border-bottom: 1px solid var(--border-light, #f1f5f9);
}
.warning-list li:last-child { border-bottom: none; }
.warning-list li::before {
  content: '⚠️'; position: absolute; left: 0; top: 10px;
}

.info-box {
  padding: 16px 20px; border-radius: 12px; font-size: 15px; line-height: 1.7;
  color: var(--text-primary, #334155);
}
.dosage-box { background: linear-gradient(135deg, #ecfdf5, #d1fae5); border: 1px solid #a7f3d0; }
.timing-box { background: linear-gradient(135deg, #eff6ff, #dbeafe); border: 1px solid #93c5fd; }

.tags-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.suitable-tag {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7); border: 1px solid #86efac;
  padding: 8px 16px; border-radius: 10px; font-size: 14px; color: #166534;
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
