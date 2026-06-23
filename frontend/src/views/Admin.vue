<template>
  <div class="admin-root">
    <!-- 顶部导航 -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <span class="brand-icon">⚙️</span>
          <span class="brand-text">后台管理</span>
        </div>
        <div class="topbar-nav">
          <el-button text @click="router.push('/meals')" class="nav-link">🥗 食谱</el-button>
          <el-button text @click="router.push('/workouts')" class="nav-link">🏋️ 训练</el-button>
          <el-button text @click="router.push('/articles')" class="nav-link">📰 资讯</el-button>
          <el-button text @click="router.push('/supplements')" class="nav-link">💊 补剂</el-button>
          <el-button text @click="router.push('/')" class="nav-link">💬 小V</el-button>
          <el-divider direction="vertical" />
          <el-tag type="danger" size="small" effect="dark">超级管理员</el-tag>
          <el-button text @click="handleLogout" class="nav-link logout">退出登录</el-button>
        </div>
      </div>
    </header>

    <div class="admin-body">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card meal">
          <div class="stat-icon">🥗</div>
          <div class="stat-info">
            <div class="stat-num">{{ meals.length }}</div>
            <div class="stat-label">减脂餐食谱</div>
          </div>
        </div>
        <div class="stat-card workout">
          <div class="stat-icon">🏋️</div>
          <div class="stat-info">
            <div class="stat-num">{{ workouts.length }}</div>
            <div class="stat-label">健身训练计划</div>
          </div>
        </div>
        <div class="stat-card article">
          <div class="stat-icon">📰</div>
          <div class="stat-info">
            <div class="stat-num">{{ articles.length }}</div>
            <div class="stat-label">健康资讯</div>
          </div>
        </div>
        <div class="stat-card supplement">
          <div class="stat-icon">💊</div>
          <div class="stat-info">
            <div class="stat-num">{{ supplements.length }}</div>
            <div class="stat-label">补剂指南</div>
          </div>
        </div>
      </div>

      <!-- 标签页 -->
      <div class="tab-card">
        <div class="tab-header">
          <div :class="['tab-btn', { active: tab === 'meals' }]" @click="tab = 'meals'">
            <span>🥗</span> 减脂餐
          </div>
          <div :class="['tab-btn', { active: tab === 'workouts' }]" @click="tab = 'workouts'">
            <span>🏋️</span> 训练
          </div>
          <div :class="['tab-btn', { active: tab === 'articles' }]" @click="tab = 'articles'">
            <span>📰</span> 资讯
          </div>
          <div :class="['tab-btn', { active: tab === 'supplements' }]" @click="tab = 'supplements'">
            <span>💊</span> 补剂
          </div>
        </div>

        <!-- ========== 减脂餐 ========== -->
        <div v-show="tab === 'meals'" class="tab-body">
          <div class="toolbar">
            <el-input v-model="mealKeyword" placeholder="搜索食谱..." :prefix-icon="Search" clearable style="width:280px" />
            <el-button type="primary" @click="openMealDialog(null)"><el-icon><Plus /></el-icon> 添加食谱</el-button>
          </div>
          <el-table :data="filteredMeals" stripe style="width:100%" :header-cell-style="tableHeaderStyle">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="名称" min-width="150">
              <template #default="{row}"><span class="meal-name">{{ row.name }}</span></template>
            </el-table-column>
            <el-table-column prop="calories" label="kcal" width="80">
              <template #default="{row}"><span class="cal-tag">🔥 {{ row.calories }}</span></template>
            </el-table-column>
            <el-table-column label="食材" min-width="240" show-overflow-tooltip>
              <template #default="{row}">{{ row.ingredients?.length > 50 ? row.ingredients.slice(0, 50) + '…' : (row.ingredients || '') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="primary" plain @click="openMealDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="delMeal(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-foot">{{ filteredMeals.length }} 道食谱</div>
        </div>

        <!-- ========== 健身训练 ========== -->
        <div v-show="tab === 'workouts'" class="tab-body">
          <div class="toolbar">
            <el-select v-model="workCategory" placeholder="类别" clearable style="width:120px">
              <el-option v-for="c in cats" :key="c" :label="c" :value="c" />
            </el-select>
            <el-select v-model="workLevel" placeholder="难度" clearable style="width:120px">
              <el-option v-for="l in ['初级','中级','高级']" :key="l" :label="l" :value="l" />
            </el-select>
            <el-input v-model="workKeyword" placeholder="搜索..." :prefix-icon="Search" clearable style="width:200px" />
            <el-button type="primary" @click="openWorkoutDialog(null)"><el-icon><Plus /></el-icon> 添加训练</el-button>
          </div>
          <el-table :data="filteredWorkouts" stripe style="width:100%" :header-cell-style="tableHeaderStyle">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="名称" min-width="140">
              <template #default="{row}"><span class="work-name">{{ row.name }}</span></template>
            </el-table-column>
            <el-table-column prop="category" label="类别" width="80">
              <template #default="{row}"><span class="cat-chip">{{ row.category }}</span></template>
            </el-table-column>
            <el-table-column prop="level" label="难度" width="70">
              <template #default="{row}"><span :class="['lv-chip', 'lv-' + (row.level || '初级')]">{{ row.level }}</span></template>
            </el-table-column>
            <el-table-column prop="duration" label="时长" width="70">
              <template #default="{row}">⏱ {{ row.duration }}分</template>
            </el-table-column>
            <el-table-column prop="calories" label="kcal" width="70">
              <template #default="{row}">🔥 {{ row.calories }}</template>
            </el-table-column>
            <el-table-column label="首行动作" min-width="180" show-overflow-tooltip>
              <template #default="{row}">{{ row.exercises?.split('\n')[0] }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="primary" plain @click="openWorkoutDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="delWorkout(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-foot">{{ filteredWorkouts.length }} 套训练</div>
        </div>

        <!-- ========== 健康资讯 ========== -->
        <div v-show="tab === 'articles'" class="tab-body">
          <div class="toolbar">
            <el-select v-model="artCategory" placeholder="分类" clearable style="width:140px">
              <el-option v-for="c in articleCats" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
            <el-input v-model="artKeyword" placeholder="搜索文章..." :prefix-icon="Search" clearable style="width:240px" />
            <el-button type="primary" @click="openArticleDialog(null)"><el-icon><Plus /></el-icon> 添加文章</el-button>
          </div>
          <el-table :data="filteredArticles" stripe style="width:100%" :header-cell-style="tableHeaderStyle">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="coverEmoji" label="" width="50">
              <template #default="{row}"><span style="font-size:20px">{{ row.coverEmoji || '📰' }}</span></template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}"><span class="art-name">{{ row.title }}</span></template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{row}"><span :class="['acat-chip', 'acat-' + row.category]">{{ articleCatLabel(row.category) }}</span></template>
            </el-table-column>
            <el-table-column prop="viewCount" label="阅读" width="70">
              <template #default="{row}">👁 {{ row.viewCount || 0 }}</template>
            </el-table-column>
            <el-table-column label="摘要" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.summary }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="primary" plain @click="openArticleDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="delArticle(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-foot">{{ filteredArticles.length }} 篇文章</div>
        </div>

        <!-- ========== 补剂指南 ========== -->
        <div v-show="tab === 'supplements'" class="tab-body">
          <div class="toolbar">
            <el-select v-model="supCategory" placeholder="分类" clearable style="width:140px">
              <el-option v-for="c in supCats" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
            <el-input v-model="supKeyword" placeholder="搜索补剂..." :prefix-icon="Search" clearable style="width:240px" />
            <el-button type="primary" @click="openSupDialog(null)"><el-icon><Plus /></el-icon> 添加补剂</el-button>
          </div>
          <el-table :data="filteredSupplements" stripe style="width:100%" :header-cell-style="tableHeaderStyle">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="emoji" label="" width="50">
              <template #default="{row}"><span style="font-size:20px">{{ row.emoji || '💊' }}</span></template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="140">
              <template #default="{row}"><span class="sup-name">{{ row.name }}</span></template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{row}"><span :class="['scat-chip', 'scat-' + row.category]">{{ supCatLabel(row.category) }}</span></template>
            </el-table-column>
            <el-table-column prop="dosage" label="剂量" width="140" show-overflow-tooltip />
            <el-table-column label="简介" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.description?.slice(0, 60) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="primary" plain @click="openSupDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" plain @click="delSup(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-foot">{{ filteredSupplements.length }} 种补剂</div>
        </div>
      </div>
    </div>

    <!-- ===== 食谱弹窗 ===== -->
    <el-dialog v-model="mealDialog" :title="editingMeal?.id ? '✏️ 编辑食谱' : '➕ 添加食谱'" width="620px" destroy-on-close @closed="editingMeal=null">
      <el-form :model="mealForm" label-position="top">
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="餐品名称"><el-input v-model="mealForm.name" placeholder="例如：香煎鸡胸肉沙拉" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="卡路里 (kcal)"><el-input v-model.number="mealForm.calories" type="number" placeholder="280" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="食材清单"><el-input v-model="mealForm.ingredients" type="textarea" :rows="3" placeholder="鸡胸肉 200g、生菜 100g…" /></el-form-item>
        <el-form-item label="做法步骤"><el-input v-model="mealForm.steps" type="textarea" :rows="5" placeholder="1. 鸡胸肉腌制15分钟&#10;2. 生菜洗净撕碎…" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mealDialog=false">取消</el-button>
        <el-button type="primary" @click="saveMeal">💾 保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 训练弹窗 ===== -->
    <el-dialog v-model="workDialog" :title="editingWork?.id ? '✏️ 编辑训练' : '➕ 添加训练'" width="620px" destroy-on-close @closed="editingWork=null">
      <el-form :model="workForm" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="训练名称"><el-input v-model="workForm.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类别"><el-select v-model="workForm.category" style="width:100%"><el-option v-for="c in cats" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="难度"><el-select v-model="workForm.level" style="width:100%"><el-option v-for="l in ['初级','中级','高级']" :key="l" :label="l" :value="l" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="时长(分钟)"><el-input v-model.number="workForm.duration" type="number" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="消耗(kcal)"><el-input v-model.number="workForm.calories" type="number" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="训练动作清单"><el-input v-model="workForm.exercises" type="textarea" :rows="5" placeholder="每行一个动作" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workDialog=false">取消</el-button>
        <el-button type="primary" @click="saveWorkout">💾 保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 文章弹窗 ===== -->
    <el-dialog v-model="artDialog" :title="editingArt?.id ? '✏️ 编辑文章' : '➕ 添加文章'" width="720px" destroy-on-close @closed="editingArt=null">
      <el-form :model="artForm" label-position="top">
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="标题"><el-input v-model="artForm.title" placeholder="文章标题" /></el-form-item></el-col>
          <el-col :span="4"><el-form-item label="封面Emoji"><el-input v-model="artForm.coverEmoji" placeholder="📰" /></el-form-item></el-col>
          <el-col :span="4"><el-form-item label="分类"><el-select v-model="artForm.category" style="width:100%"><el-option v-for="c in articleCats" :key="c.value" :label="c.label" :value="c.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="摘要"><el-input v-model="artForm.summary" type="textarea" :rows="2" placeholder="文章摘要，显示在列表中" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="artForm.tags" placeholder="标签1,标签2,标签3" /></el-form-item>
        <el-form-item label="正文内容 (Markdown)"><el-input v-model="artForm.content" type="textarea" :rows="10" placeholder="支持 Markdown 格式" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="artDialog=false">取消</el-button>
        <el-button type="primary" @click="saveArticle">💾 保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 补剂弹窗 ===== -->
    <el-dialog v-model="supDialog" :title="editingSup?.id ? '✏️ 编辑补剂' : '➕ 添加补剂'" width="720px" destroy-on-close @closed="editingSup=null">
      <el-form :model="supForm" label-position="top">
        <el-row :gutter="16">
          <el-col :span="10"><el-form-item label="名称"><el-input v-model="supForm.name" placeholder="补剂名称" /></el-form-item></el-col>
          <el-col :span="4"><el-form-item label="Emoji"><el-input v-model="supForm.emoji" placeholder="💊" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="分类"><el-select v-model="supForm.category" style="width:100%"><el-option v-for="c in supCats" :key="c.value" :label="c.label" :value="c.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="简介"><el-input v-model="supForm.description" type="textarea" :rows="3" placeholder="补剂简介" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="主要功效（每行一条）"><el-input v-model="supForm.benefits" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用人群（每行一条）"><el-input v-model="supForm.suitableFor" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="推荐剂量"><el-input v-model="supForm.dosage" placeholder="每日 20-40g" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="服用时机"><el-input v-model="supForm.timing" placeholder="训练后 30 分钟内" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="注意事项/副作用"><el-input v-model="supForm.sideEffects" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="supDialog=false">取消</el-button>
        <el-button type="primary" @click="saveSupplement">💾 保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { listMealsApi } from '../api/meal'
import { listWorkoutsApi } from '../api/workout'
import { listArticlesApi } from '../api/article'
import { listSupplementsApi } from '../api/supplement'
import {
  addMealApi, updateMealApi, deleteMealApi,
  addWorkoutApi, updateWorkoutApi, deleteWorkoutApi,
  addArticleApi, updateArticleApi, deleteArticleApi,
  addSupplementApi, updateSupplementApi, deleteSupplementApi
} from '../api/admin'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const tab = ref('meals')
const tableHeaderStyle = { background: '#f8fafc', color: '#475569', fontWeight: 600, fontSize: '13px' }

// ===== 分类选项 =====
const cats = ['腹肌', '胸肌', '背部', '腿部', '手臂', '全身', '有氧']
const articleCats = [
  { value: 'nutrition', label: '🥗 营养知识' },
  { value: 'training', label: '🏋️ 训练技巧' },
  { value: 'myths', label: '❌ 减脂误区' },
  { value: 'mental', label: '🧠 心理健康' }
]
const supCats = [
  { value: 'protein', label: '🥛 蛋白质' },
  { value: 'creatine', label: '⚡ 肌酸' },
  { value: 'vitamin', label: '☀️ 维生素/矿物质' },
  { value: 'amino', label: '🔗 氨基酸' },
  { value: 'other', label: '📦 其他' }
]

function articleCatLabel(v) { return { nutrition: '营养知识', training: '训练技巧', myths: '减脂误区', mental: '心理健康' }[v] || v }
function supCatLabel(v) { return { protein: '蛋白质', creatine: '肌酸', vitamin: '维生素', amino: '氨基酸', other: '其他' }[v] || v }

// ===== 数据 =====
const meals = ref([]), mealKeyword = ref('')
const workouts = ref([]), workKeyword = ref(''), workCategory = ref(''), workLevel = ref('')
const articles = ref([]), artKeyword = ref(''), artCategory = ref('')
const supplements = ref([]), supKeyword = ref(''), supCategory = ref('')

// ===== 筛选 =====
const filteredMeals = computed(() => {
  if (!mealKeyword.value) return meals.value
  const kw = mealKeyword.value.toLowerCase()
  return meals.value.filter(m => m.name?.toLowerCase().includes(kw) || m.ingredients?.toLowerCase().includes(kw))
})

const filteredWorkouts = computed(() => {
  let list = workouts.value
  if (workCategory.value) list = list.filter(w => w.category === workCategory.value)
  if (workLevel.value) list = list.filter(w => w.level === workLevel.value)
  if (workKeyword.value) {
    const kw = workKeyword.value.toLowerCase()
    list = list.filter(w => w.name?.toLowerCase().includes(kw) || w.exercises?.toLowerCase().includes(kw))
  }
  return list
})

const filteredArticles = computed(() => {
  let list = articles.value
  if (artCategory.value) list = list.filter(a => a.category === artCategory.value)
  if (artKeyword.value) {
    const kw = artKeyword.value.toLowerCase()
    list = list.filter(a => a.title?.toLowerCase().includes(kw) || a.summary?.toLowerCase().includes(kw))
  }
  return list
})

const filteredSupplements = computed(() => {
  let list = supplements.value
  if (supCategory.value) list = list.filter(s => s.category === supCategory.value)
  if (supKeyword.value) {
    const kw = supKeyword.value.toLowerCase()
    list = list.filter(s => s.name?.toLowerCase().includes(kw) || s.description?.toLowerCase().includes(kw))
  }
  return list
})

// ===== 弹窗状态 =====
const mealDialog = ref(false), editingMeal = ref(null), mealForm = ref({})
const workDialog = ref(false), editingWork = ref(null), workForm = ref({})
const artDialog = ref(false), editingArt = ref(null), artForm = ref({})
const supDialog = ref(false), editingSup = ref(null), supForm = ref({})

// ===== 加载数据 =====
async function load() {
  const results = await Promise.allSettled([
    listMealsApi(1, 200),
    listWorkoutsApi(1, 200),
    listArticlesApi(1, 200),
    listSupplementsApi(1, 200)
  ])
  meals.value = results[0].status === 'fulfilled' ? (results[0].value.data?.list || []) : []
  workouts.value = results[1].status === 'fulfilled' ? (results[1].value.data?.list || []) : []
  articles.value = results[2].status === 'fulfilled' ? (results[2].value.data?.list || []) : []
  supplements.value = results[3].status === 'fulfilled' ? (results[3].value.data?.list || []) : []
}

// ===== 食谱 CRUD =====
function openMealDialog(row) {
  editingMeal.value = row
  mealForm.value = row ? { ...row } : { name: '', ingredients: '', steps: '', calories: 0 }
  mealDialog.value = true
}
async function saveMeal() {
  if (!mealForm.value.name?.trim()) return ElMessage.warning('请输入餐品名称')
  try {
    if (editingMeal.value?.id) {
      await updateMealApi(editingMeal.value.id, mealForm.value)
      ElMessage.success('✅ 食谱已更新')
    } else {
      await addMealApi(mealForm.value)
      ElMessage.success('✅ 食谱已添加')
    }
    mealDialog.value = false; load()
  } catch (e) { console.error(e) }
}
async function delMeal(id) {
  try {
    await ElMessageBox.confirm('确定要删除这份食谱吗？', '确认删除', { type: 'warning' })
    await deleteMealApi(id); ElMessage.success('🗑️ 已删除'); load()
  } catch (e) { if (e !== 'cancel' && e?.toString() !== 'cancel') console.error(e) }
}

// ===== 训练 CRUD =====
function openWorkoutDialog(row) {
  editingWork.value = row
  workForm.value = row ? { ...row } : { name: '', category: '腹肌', level: '初级', duration: 30, calories: 200, exercises: '' }
  workDialog.value = true
}
async function saveWorkout() {
  if (!workForm.value.name?.trim()) return ElMessage.warning('请输入训练名称')
  try {
    if (editingWork.value?.id) {
      await updateWorkoutApi(editingWork.value.id, workForm.value)
      ElMessage.success('✅ 训练已更新')
    } else {
      await addWorkoutApi(workForm.value)
      ElMessage.success('✅ 训练已添加')
    }
    workDialog.value = false; load()
  } catch (e) { console.error(e) }
}
async function delWorkout(id) {
  try {
    await ElMessageBox.confirm('确定要删除这套训练吗？', '确认删除', { type: 'warning' })
    await deleteWorkoutApi(id); ElMessage.success('🗑️ 已删除'); load()
  } catch (e) { if (e !== 'cancel' && e?.toString() !== 'cancel') console.error(e) }
}

// ===== 文章 CRUD =====
function openArticleDialog(row) {
  editingArt.value = row
  artForm.value = row ? { ...row } : {
    title: '', summary: '', content: '', coverEmoji: '📰', category: 'nutrition', tags: '', viewCount: 0
  }
  artDialog.value = true
}
async function saveArticle() {
  if (!artForm.value.title?.trim()) return ElMessage.warning('请输入文章标题')
  if (!artForm.value.content?.trim()) return ElMessage.warning('请输入文章正文')
  try {
    if (editingArt.value?.id) {
      await updateArticleApi(editingArt.value.id, artForm.value)
      ElMessage.success('✅ 文章已更新')
    } else {
      await addArticleApi(artForm.value)
      ElMessage.success('✅ 文章已添加')
    }
    artDialog.value = false; load()
  } catch (e) { console.error(e) }
}
async function delArticle(id) {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '确认删除', { type: 'warning' })
    await deleteArticleApi(id); ElMessage.success('🗑️ 已删除'); load()
  } catch (e) { if (e !== 'cancel' && e?.toString() !== 'cancel') console.error(e) }
}

// ===== 补剂 CRUD =====
function openSupDialog(row) {
  editingSup.value = row
  supForm.value = row ? { ...row } : {
    name: '', emoji: '💊', category: 'protein', description: '', benefits: '', dosage: '', timing: '', sideEffects: '', suitableFor: ''
  }
  supDialog.value = true
}
async function saveSupplement() {
  if (!supForm.value.name?.trim()) return ElMessage.warning('请输入补剂名称')
  if (!supForm.value.description?.trim()) return ElMessage.warning('请输入补剂简介')
  try {
    if (editingSup.value?.id) {
      await updateSupplementApi(editingSup.value.id, supForm.value)
      ElMessage.success('✅ 补剂已更新')
    } else {
      await addSupplementApi(supForm.value)
      ElMessage.success('✅ 补剂已添加')
    }
    supDialog.value = false; load()
  } catch (e) { console.error(e) }
}
async function delSup(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个补剂吗？', '确认删除', { type: 'warning' })
    await deleteSupplementApi(id); ElMessage.success('🗑️ 已删除'); load()
  } catch (e) { if (e !== 'cancel' && e?.toString() !== 'cancel') console.error(e) }
}

function handleLogout() { userStore.logout(); router.push('/login') }
onMounted(load)
</script>

<style scoped>
.admin-root { min-height: 100vh; background: #f1f5f9; }

.topbar {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #fff; padding: 0 24px; position: sticky; top: 0; z-index: 100;
  box-shadow: 0 2px 16px rgba(0,0,0,.2);
}
.topbar-inner { max-width: 1400px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; height: 60px; }
.brand { display: flex; align-items: center; gap: 10px; }
.brand-icon { font-size: 22px; }
.brand-text { font-size: 17px; font-weight: 700; letter-spacing: .5px; }
.topbar-nav { display: flex; align-items: center; gap: 4px; }
.nav-link { color: #cbd5e1 !important; font-size: 13px; }
.nav-link:hover { color: #fff !important; background: rgba(255,255,255,.08) !important; }
.logout { color: #f87171 !important; }

.admin-body { max-width: 1400px; margin: 0 auto; padding: 24px 20px; }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card {
  background: #fff; border-radius: 16px; padding: 24px;
  display: flex; align-items: center; gap: 18px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04); border: 1px solid #e2e8f0; transition: all .25s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }
.stat-icon { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 26px; }
.stat-card.meal .stat-icon { background: #ecfdf5; }
.stat-card.workout .stat-icon { background: #fef2f2; }
.stat-card.article .stat-icon { background: #ecfeff; }
.stat-card.supplement .stat-icon { background: #faf5ff; }
.stat-num { font-size: 28px; font-weight: 800; color: #0f172a; line-height: 1.1; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 2px; }

/* 标签页 */
.tab-card { background: #fff; border-radius: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.04); overflow: hidden; border: 1px solid #e2e8f0; }
.tab-header { display: flex; border-bottom: 1px solid #e2e8f0; background: #fafbfc; }
.tab-btn {
  flex: 1; text-align: center; padding: 16px 20px; cursor: pointer;
  font-size: 14px; font-weight: 600; color: #64748b;
  transition: all .2s; border-bottom: 2px solid transparent;
}
.tab-btn:hover { color: #0f172a; background: rgba(0,0,0,.02); }
.tab-btn.active { color: #2563eb; border-bottom-color: #2563eb; background: #fff; }
.tab-btn span { margin-right: 6px; }
.tab-body { padding: 20px; }
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; flex-wrap: wrap; }
.table-foot { text-align: center; padding: 16px 0 4px; font-size: 13px; color: #94a3b8; }

/* 表格 */
.meal-name, .work-name, .art-name, .sup-name { font-weight: 600; color: #0f172a; font-size: 14px; }
.cal-tag { font-size: 13px; color: #ef4444; font-weight: 600; }
.cat-chip { font-size: 12px; color: #ea580c; background: #fff7ed; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
.lv-chip { font-size: 12px; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
.lv-初级 { background: #d1fae5; color: #059669; }
.lv-中级 { background: #fef3c7; color: #d97706; }
.lv-高级 { background: #fee2e2; color: #dc2626; }

.acat-chip { font-size: 12px; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
.acat-nutrition { background: #d1fae5; color: #059669; }
.acat-training { background: #fef3c7; color: #d97706; }
.acat-myths { background: #fee2e2; color: #dc2626; }
.acat-mental { background: #e0e7ff; color: #4f46e5; }

.scat-chip { font-size: 12px; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
.scat-protein { background: #dbeafe; color: #2563eb; }
.scat-creatine { background: #fef3c7; color: #d97706; }
.scat-vitamin { background: #d1fae5; color: #059669; }
.scat-amino { background: #fce7f3; color: #db2777; }
.scat-other { background: #f1f5f9; color: #64748b; }

@media (max-width: 768px) {
  .topbar-nav { display: none; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .tab-header { flex-wrap: wrap; }
  .tab-btn { flex: none; width: 50%; text-align: left; padding: 14px 20px; }
}
</style>
