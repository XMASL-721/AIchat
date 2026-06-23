import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login',  name: 'Login',    component: () => import('../views/Login.vue'),    meta: { requiresAuth: false } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { requiresAuth: false } },
  { path: '/',         name: 'ChatHome',  component: () => import('../views/ChatHome.vue'),  meta: { requiresAuth: true } },
  { path: '/meals',    name: 'MealList',  component: () => import('../views/MealList.vue'),  meta: { requiresAuth: false } },
  { path: '/meals/:id',name: 'MealDetail',component: () => import('../views/MealDetail.vue'), meta: { requiresAuth: false } },
  { path: '/workouts', name: 'WorkoutList', component: () => import('../views/WorkoutList.vue'), meta: { requiresAuth: false } },
  { path: '/workouts/:id', name: 'WorkoutDetail', component: () => import('../views/WorkoutDetail.vue'), meta: { requiresAuth: false } },
  { path: '/articles', name: 'ArticleList', component: () => import('../views/ArticleList.vue'), meta: { requiresAuth: false } },
  { path: '/articles/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue'), meta: { requiresAuth: false } },
  { path: '/supplements', name: 'SupplementList', component: () => import('../views/SupplementList.vue'), meta: { requiresAuth: false } },
  { path: '/supplements/:id', name: 'SupplementDetail', component: () => import('../views/SupplementDetail.vue'), meta: { requiresAuth: false } },
  { path: '/admin',    name: 'Admin',     component: () => import('../views/Admin.vue'),     meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/profile',  name: 'Profile',   component: () => import('../views/Profile.vue'),   meta: { requiresAuth: true } },
  { path: '/favorites', name: 'Favorites', component: () => import('../views/Favorites.vue'), meta: { requiresAuth: true } },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/plans/generate', name: 'PlanGenerator', component: () => import('../views/PlanGenerator.vue'), meta: { requiresAuth: true } },
  { path: '/plans', name: 'PlanHistory', component: () => import('../views/PlanHistory.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      userStore.restoreSession()
      next()
    } else {
      next('/login')
    }
    return
  }

  // 管理员页面 — 检查是否為 admin
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
    return
  }

  // 已登录访问登录/注册页 → 跳首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
