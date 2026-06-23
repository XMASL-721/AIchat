<template>
  <div class="profile-page">
    <!-- 顶部渐变 Header -->
    <header class="profile-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <div class="avatar-area">
        <div class="avatar-wrapper">
          <el-avatar :size="100" :src="form.avatar || undefined" class="main-avatar">
            {{ form.nickname?.charAt(0) || '用' }}
          </el-avatar>
          <el-upload
            :show-file-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/*"
            class="avatar-upload-overlay"
          >
            <div class="upload-trigger">
              <el-icon><Camera /></el-icon>
            </div>
          </el-upload>
        </div>
        <h1>{{ form.nickname || '用户' }}</h1>
        <p class="phone-display">📱 {{ form.phone || '未设置' }}</p>
      </div>
    </header>

    <div class="profile-container">
      <!-- BMI 卡片 -->
      <div class="bmi-card" v-if="form.height && form.weight">
        <div class="bmi-info">
          <div class="bmi-value" :style="{ color: bmiColor }">{{ bmiValue }}</div>
          <div class="bmi-label">BMI 指数</div>
          <div class="bmi-status" :style="{ color: bmiColor }">{{ bmiStatus }}</div>
        </div>
        <div class="bmi-bar">
          <div class="bmi-track">
            <div class="bmi-fill" :style="{ width: bmiPercent + '%', background: bmiColor }"></div>
            <div class="bmi-marker" :style="{ left: bmiPercent + '%' }">
              <span class="marker-dot"></span>
            </div>
          </div>
          <div class="bmi-scale">
            <span>偏瘦</span>
            <span>正常</span>
            <span>偏胖</span>
            <span>肥胖</span>
          </div>
        </div>
      </div>

      <!-- 个人信息表单 -->
      <div class="form-card">
        <div class="card-title">
          <el-icon><User /></el-icon>
          <h2>个人信息</h2>
        </div>

        <el-form :model="form" label-width="100px" class="profile-form">
          <div class="form-section">
            <h3 class="section-label">基本信息</h3>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="form.phone" disabled />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio-button :label="1">♂ 男</el-radio-button>
                <el-radio-button :label="0">♀ 女</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <div class="form-section">
            <h3 class="section-label">身体数据</h3>
            <el-form-item label="身高(cm)">
              <el-input-number
                v-model="form.height"
                :min="100" :max="250" :precision="1"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="体重(kg)">
              <el-input-number
                v-model="form.weight"
                :min="30" :max="200" :precision="1"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="目标体重(kg)">
              <el-input-number
                v-model="form.targetWeight"
                :min="30" :max="200" :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="handleUpdate" :loading="updating" round size="large">
              💾 保存修改
            </el-button>
            <el-button @click="handleReset" round size="large">重置</el-button>
          </div>
        </el-form>
      </div>

      <!-- 数据统计 -->
      <div class="stats-card">
        <div class="card-title">
          <el-icon><DataLine /></el-icon>
          <h2>数据统计</h2>
        </div>
        <div class="stats-grid">
          <div class="stat-item conversations">
            <div class="stat-icon">💬</div>
            <div class="stat-value">{{ stats.conversations }}</div>
            <div class="stat-label">对话数量</div>
          </div>
          <div class="stat-item favorites">
            <div class="stat-icon">❤️</div>
            <div class="stat-value">{{ stats.favorites }}</div>
            <div class="stat-label">收藏数量</div>
          </div>
          <div class="stat-item plans">
            <div class="stat-icon">📋</div>
            <div class="stat-value">{{ stats.plans }}</div>
            <div class="stat-label">AI 计划</div>
          </div>
          <div class="stat-item days">
            <div class="stat-icon">📅</div>
            <div class="stat-value">{{ stats.days }}</div>
            <div class="stat-label">注册天数</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Camera, User, DataLine } from '@element-plus/icons-vue'
import { getProfileApi, updateProfileApi } from '../api/user'
import { listConversationsApi } from '../api/conversation'
import { getFavoriteStatsApi } from '../api/favorite'

const form = ref({
  nickname: '',
  phone: '',
  avatar: '',
  gender: null,
  birthday: null,
  height: null,
  weight: null,
  targetWeight: null
})

const originalForm = ref({})
const updating = ref(false)
const stats = ref({
  conversations: 0,
  favorites: 0,
  plans: 0,
  days: 0
})

// BMI 计算
const bmiValue = computed(() => {
  if (!form.value.height || !form.value.weight) return 0
  const h = form.value.height / 100
  return (form.value.weight / (h * h)).toFixed(1)
})

const bmiStatus = computed(() => {
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '偏胖'
  return '肥胖'
})

const bmiColor = computed(() => {
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return '#3b82f6'
  if (bmi < 24) return '#10b981'
  if (bmi < 28) return '#f59e0b'
  return '#ef4444'
})

const bmiPercent = computed(() => {
  const bmi = parseFloat(bmiValue.value)
  // BMI range 15-35 mapped to 0-100%
  return Math.max(0, Math.min(100, ((bmi - 15) / 20) * 100))
})

onMounted(async () => {
  await loadProfile()
  await loadStats()
})

async function loadProfile() {
  try {
    const res = await getProfileApi()
    form.value = res.data
    originalForm.value = { ...res.data }
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

async function loadStats() {
  try {
    const convRes = await listConversationsApi(1, 1)
    stats.value.conversations = convRes.data.total

    if (form.value.createdAt) {
      const created = new Date(form.value.createdAt)
      const now = new Date()
      stats.value.days = Math.floor((now - created) / (1000 * 60 * 60 * 24))
    }

    // 获取收藏统计
    try {
      const favRes = await getFavoriteStatsApi()
      stats.value.favorites = favRes.data.totalCount || 0
    } catch { /* ignore */ }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

function handleAvatarUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

async function handleUpdate() {
  updating.value = true
  try {
    await updateProfileApi({
      nickname: form.value.nickname,
      avatar: form.value.avatar,
      gender: form.value.gender,
      birthday: form.value.birthday,
      height: form.value.height,
      weight: form.value.weight,
      targetWeight: form.value.targetWeight
    })
    ElMessage.success('更新成功')
    originalForm.value = { ...form.value }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
}

function handleReset() {
  form.value = { ...originalForm.value }
  ElMessage.info('已重置')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 40px;
}

.profile-header {
  background: linear-gradient(135deg, #2563eb, #7c3aed, #ec4899);
  color: #fff;
  padding: 20px 24px 40px;
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
  width: 300px; height: 300px;
  background: #fff;
  top: -100px; right: -60px;
}

.deco-circle.c2 {
  width: 200px; height: 200px;
  background: #fff;
  bottom: -60px; left: -40px;
}

.header-top {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: flex-start;
  margin-bottom: 16px;
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

.avatar-area {
  position: relative;
  z-index: 1;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.main-avatar {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.avatar-upload-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
}

.upload-trigger {
  width: 32px;
  height: 32px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  color: #2563eb;
  transition: transform 0.2s;
}

.upload-trigger:hover {
  transform: scale(1.1);
}

.avatar-area h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
}

.phone-display {
  font-size: 14px;
  opacity: 0.85;
  margin: 0;
}

.profile-container {
  max-width: 800px;
  margin: -20px auto 0;
  padding: 0 16px;
  position: relative;
  z-index: 2;
}

/* BMI 卡片 */
.bmi-card {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color, #e2e8f0);
  display: flex;
  align-items: center;
  gap: 32px;
}

.bmi-info {
  text-align: center;
  min-width: 120px;
}

.bmi-value {
  font-size: 42px;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 4px;
}

.bmi-label {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
  margin-bottom: 4px;
}

.bmi-status {
  font-size: 14px;
  font-weight: 600;
}

.bmi-bar {
  flex: 1;
}

.bmi-track {
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  position: relative;
  margin-bottom: 8px;
}

.bmi-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.bmi-marker {
  position: absolute;
  top: -4px;
  transform: translateX(-50%);
  transition: left 0.5s ease;
}

.marker-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid currentColor;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: block;
}

.bmi-scale {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-tertiary, #94a3b8);
}

/* 表单卡片 */
.form-card {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color, #e2e8f0);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light, #f1f5f9);
}

.card-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin: 0;
}

.card-title .el-icon {
  color: #2563eb;
  font-size: 20px;
}

.form-section {
  margin-bottom: 24px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary, #64748b);
  margin-bottom: 16px;
  padding-left: 100px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.profile-form {
  max-width: 520px;
  margin: 0 auto;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 8px;
}

.form-actions .el-button {
  min-width: 140px;
}

/* 统计卡片 */
.stats-card {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color, #e2e8f0);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 20px 12px;
  border-radius: 16px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-4px);
}

.stat-item.conversations { background: linear-gradient(135deg, #eff6ff, #dbeafe); }
.stat-item.favorites { background: linear-gradient(135deg, #fef2f2, #fecaca); }
.stat-item.plans { background: linear-gradient(135deg, #f0fdf4, #dcfce7); }
.stat-item.days { background: linear-gradient(135deg, #faf5ff, #e9d5ff); }

.stat-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 4px;
}

.stat-item.conversations .stat-value { color: #2563eb; }
.stat-item.favorites .stat-value { color: #ef4444; }
.stat-item.plans .stat-value { color: #10b981; }
.stat-item.days .stat-value { color: #8b5cf6; }

.stat-label {
  font-size: 13px;
  color: var(--text-secondary, #64748b);
}

@media (max-width: 768px) {
  .bmi-card {
    flex-direction: column;
    gap: 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .section-label {
    padding-left: 0;
  }

  .profile-form {
    max-width: 100%;
  }
}
</style>
