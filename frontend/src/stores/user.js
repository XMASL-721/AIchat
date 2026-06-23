import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi } from '../api/auth'

function safeParseUserInfo(str) {
  try {
    return JSON.parse(str || 'null')
  } catch {
    localStorage.removeItem('userInfo')
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(safeParseUserInfo(localStorage.getItem('userInfo')))
  const darkMode = ref(localStorage.getItem('darkMode') === 'true')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  function restoreSession() {
    token.value = localStorage.getItem('token') || ''
    userInfo.value = safeParseUserInfo(localStorage.getItem('userInfo'))
  }

  async function login(phone, password) {
    const res = await loginApi({ phone, password })
    const { token: newToken, userInfo: info } = res.data
    token.value = newToken
    userInfo.value = info
    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(info))
    // 返回 role，让调用方決定跳轉哪裡
    return { role: info.role }
  }

  async function register(phone, password) {
    return await registerApi({ phone, password })
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function toggleDarkMode() {
    darkMode.value = !darkMode.value
    localStorage.setItem('darkMode', darkMode.value)
    applyDarkMode()
  }

  function applyDarkMode() {
    if (darkMode.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 初始化时应用暗黑模式
  applyDarkMode()

  return { token, userInfo, isLoggedIn, isAdmin, darkMode, restoreSession, login, register, logout, toggleDarkMode }
})
