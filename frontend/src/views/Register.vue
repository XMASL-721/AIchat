<template>
  <div class="auth-container">
    <!-- 动态背景粒子 -->
    <div class="bg-particles">
      <span v-for="i in 6" :key="i" class="particle" :class="'p' + i"></span>
    </div>

    <div class="auth-card">
      <div class="logo-area">
        <div class="logo-icon">🤖</div>
        <h2>创建账号</h2>
        <p class="subtitle">注册后即可与 AI 开始对话</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号"
            :prefix-icon="Iphone"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（至少6位）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            class="register-btn"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-link">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.register(form.phone, form.password)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 40%, #7c3aed 100%);
  position: relative;
  overflow: hidden;
}

.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  border-radius: 50%;
  animation: float 20s infinite ease-in-out;
}

.particle.p1 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.15), transparent 70%);
  top: -100px; right: -50px; animation-delay: 0s;
}
.particle.p2 {
  width: 200px; height: 200px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.12), transparent 70%);
  bottom: -60px; left: -60px; animation-delay: -5s;
}
.particle.p3 {
  width: 150px; height: 150px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.1), transparent 70%);
  top: 40%; left: 10%; animation-delay: -10s;
}
.particle.p4 {
  width: 250px; height: 250px;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.08), transparent 70%);
  bottom: 10%; right: 15%; animation-delay: -3s;
}
.particle.p5 {
  width: 100px; height: 100px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.12), transparent 70%);
  top: 20%; right: 30%; animation-delay: -8s;
}
.particle.p6 {
  width: 180px; height: 180px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.08), transparent 70%);
  top: 60%; left: 40%; animation-delay: -12s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -20px) scale(1.1); }
  50% { transform: translate(-20px, 30px) scale(0.95); }
  75% { transform: translate(20px, 20px) scale(1.05); }
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 44px 40px 36px;
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1), 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: cardSlideUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes cardSlideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.logo-area { text-align: center; margin-bottom: 32px; }

.logo-icon {
  width: 64px; height: 64px;
  background: linear-gradient(135deg, #7c3aed, #ec4899);
  border-radius: 18px;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 32px; margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.35);
  animation: logoPulse 3s infinite ease-in-out;
}

@keyframes logoPulse {
  0%, 100% { box-shadow: 0 8px 24px rgba(124, 58, 237, 0.35); }
  50% { box-shadow: 0 8px 32px rgba(124, 58, 237, 0.5); }
}

.logo-area h2 { font-size: 22px; font-weight: 700; color: #0f172a; margin-bottom: 4px; }
.subtitle { color: #64748b; font-size: 14px; margin: 0; }

.auth-card :deep(.el-form-item) { margin-bottom: 20px; }

.auth-card :deep(.el-input__wrapper) {
  border-radius: 12px; padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 !important; transition: all 0.25s;
}
.auth-card :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #94a3b8 !important; }
.auth-card :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px #7c3aed !important; }
.auth-card :deep(.el-input__inner) { height: 44px; font-size: 15px; }

.register-btn {
  width: 100%; height: 48px; font-size: 16px; font-weight: 600;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #7c3aed, #ec4899) !important;
  border: none !important; transition: all 0.3s; letter-spacing: 2px;
}
.register-btn:hover {
  background: linear-gradient(135deg, #6d28d9, #db2777) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.4) !important;
}

.auth-link { text-align: center; margin-top: 24px; font-size: 14px; color: #64748b; }
.auth-link a { color: #7c3aed; text-decoration: none; font-weight: 600; transition: color 0.2s; }
.auth-link a:hover { color: #6d28d9; text-decoration: underline; }

html.dark .auth-card { background: rgba(30, 41, 59, 0.97); }
html.dark .auth-card h2 { color: #f1f5f9; }
html.dark .subtitle { color: #94a3b8; }
html.dark .auth-link { color: #94a3b8; }

@media (max-width: 768px) {
  .auth-card { width: 92%; padding: 32px 24px; margin: 16px; }
}
</style>
