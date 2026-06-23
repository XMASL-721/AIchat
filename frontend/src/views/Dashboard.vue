<template>
  <div class="dashboard-page">
    <header class="dashboard-header">
      <div class="header-decoration">
        <span class="deco-circle c1"></span>
        <span class="deco-circle c2"></span>
      </div>
      <div class="header-top">
        <el-button class="back-btn" @click="$router.back()" round>← 返回</el-button>
      </div>
      <div class="header-inner">
        <h1>📊 数据看板</h1>
        <p>追踪你的减脂和训练进度</p>
      </div>
    </header>

    <div class="dashboard-container">
      <!-- 今日概览 -->
      <div class="summary-cards">
        <div class="summary-card meal">
          <div class="card-icon-wrap meal-icon">🍽️</div>
          <div class="card-info">
            <div class="card-value">{{ todaySummary.mealCalories || 0 }}</div>
            <div class="card-label">今日摄入 (kcal)</div>
          </div>
          <div class="card-accent meal-accent"></div>
        </div>
        <div class="summary-card workout">
          <div class="card-icon-wrap workout-icon">💪</div>
          <div class="card-info">
            <div class="card-value">{{ todaySummary.workoutCalories || 0 }}</div>
            <div class="card-label">今日消耗 (kcal)</div>
          </div>
          <div class="card-accent workout-accent"></div>
        </div>
        <div class="summary-card net" :class="netClass">
          <div class="card-icon-wrap net-icon">{{ netIcon }}</div>
          <div class="card-info">
            <div class="card-value">{{ todaySummary.netCalories || 0 }}</div>
            <div class="card-label">净卡路里</div>
          </div>
          <div class="card-accent" :class="netClass + '-accent'"></div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <div class="chart-card">
          <h3>📈 7天卡路里趋势</h3>
          <div ref="weeklyChartRef" class="chart-container"></div>
        </div>

        <div class="chart-card">
          <h3>🏋️ 训练类别分布</h3>
          <div ref="categoryChartRef" class="chart-container"></div>
        </div>
      </div>

      <!-- 详细记录 -->
      <div class="records-section">
        <h3>📝 最近记录</h3>
        <el-table :data="recentRecords" stripe style="width: 100%" :header-cell-style="{ background: 'var(--bg-tertiary, #f8fafc)' }">
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="recordType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.recordType === 'meal' ? 'success' : 'warning'" round>
                {{ row.recordType === 'meal' ? '🍽️ 饮食' : '🏋️ 训练' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetName" label="名称" />
          <el-table-column prop="calories" label="卡路里" width="120">
            <template #default="{ row }">
              <span :class="row.recordType === 'meal' ? 'calories-in' : 'calories-out'">
                {{ row.recordType === 'meal' ? '+' : '-' }}{{ row.calories || 0 }} kcal
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
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
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getTodaySummaryApi, getWeeklyStatsApi, getCategoryStatsApi, getUserRecordsApi } from '../api/dailyRecord'

const router = useRouter()

const todaySummary = ref({})
const recentRecords = ref([])
const weeklyChartRef = ref(null)
const categoryChartRef = ref(null)

let weeklyChart = null
let categoryChart = null

const netClass = computed(() => {
  const net = todaySummary.value.netCalories || 0
  if (net > 0) return 'positive'
  if (net < 0) return 'negative'
  return 'neutral'
})

const netIcon = computed(() => {
  const net = todaySummary.value.netCalories || 0
  if (net > 0) return '📈'
  if (net < 0) return '📉'
  return '➡️'
})

onMounted(async () => {
  await loadData()
  await nextTick()
  initCharts()
})

async function loadData() {
  try {
    const summaryRes = await getTodaySummaryApi()
    todaySummary.value = summaryRes.data

    const recordsRes = await getUserRecordsApi(null, null, null, 1, 10)
    recentRecords.value = recordsRes.data.list || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

async function initCharts() {
  try {
    const weeklyRes = await getWeeklyStatsApi()
    const weeklyData = weeklyRes.data

    if (weeklyChartRef.value) {
      weeklyChart = echarts.init(weeklyChartRef.value)
      const dates = weeklyData.map(d => d.date)
      const mealCalories = weeklyData.map(d => d.mealCalories || 0)
      const workoutCalories = weeklyData.map(d => d.workoutCalories || 0)

      weeklyChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(255,255,255,0.96)',
          borderColor: '#e2e8f0',
          textStyle: { color: '#1e293b' }
        },
        legend: {
          data: ['摄入', '消耗'],
          top: 10,
          textStyle: { color: '#64748b' }
        },
        grid: {
          left: '3%', right: '4%', bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            color: '#94a3b8',
            formatter: (value) => {
              const date = new Date(value)
              return `${date.getMonth() + 1}/${date.getDate()}`
            }
          },
          axisLine: { lineStyle: { color: '#e2e8f0' } }
        },
        yAxis: {
          type: 'value',
          name: 'kcal',
          nameTextStyle: { color: '#94a3b8' },
          axisLabel: { color: '#94a3b8' },
          splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
        },
        series: [
          {
            name: '摄入',
            type: 'bar',
            data: mealCalories,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#10b981' },
                { offset: 1, color: '#34d399' }
              ]),
              borderRadius: [6, 6, 0, 0]
            }
          },
          {
            name: '消耗',
            type: 'bar',
            data: workoutCalories,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#f59e0b' },
                { offset: 1, color: '#fbbf24' }
              ]),
              borderRadius: [6, 6, 0, 0]
            }
          }
        ]
      })
    }

    const categoryRes = await getCategoryStatsApi(null, null)
    const categoryData = categoryRes.data

    if (categoryChartRef.value && categoryData.length > 0) {
      categoryChart = echarts.init(categoryChartRef.value)
      const colors = ['#2563eb', '#7c3aed', '#ec4899', '#f59e0b', '#10b981', '#06b6d4', '#ef4444']
      categoryChart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} kcal ({d}%)',
          backgroundColor: 'rgba(255,255,255,0.96)',
          borderColor: '#e2e8f0',
          textStyle: { color: '#1e293b' }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle',
          textStyle: { color: '#64748b' }
        },
        color: colors,
        series: [
          {
            name: '训练消耗',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 3
            },
            label: { show: false, position: 'center' },
            emphasis: {
              label: { show: true, fontSize: 18, fontWeight: 'bold' }
            },
            labelLine: { show: false },
            data: categoryData
          }
        ]
      })
    } else if (categoryChartRef.value) {
      categoryChart = echarts.init(categoryChartRef.value)
      categoryChart.setOption({
        title: {
          text: '暂无训练数据',
          left: 'center',
          top: 'center',
          textStyle: { fontSize: 16, color: '#94a3b8' }
        }
      })
    }

    window.addEventListener('resize', () => {
      weeklyChart?.resize()
      categoryChart?.resize()
    })
  } catch (error) {
    console.error('初始化图表失败', error)
  }
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

.dashboard-header {
  background: linear-gradient(135deg, #8b5cf6, #6366f1, #3b82f6);
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
  opacity: 0.1;
}

.deco-circle.c1 {
  width: 280px; height: 280px;
  background: #fff;
  top: -100px; right: -50px;
}

.deco-circle.c2 {
  width: 160px; height: 160px;
  background: #fff;
  bottom: -40px; left: 8%;
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
  max-width: 1200px;
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

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.summary-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);
}

.card-accent {
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  border-radius: 0 0 0 80px;
  opacity: 0.1;
}

.meal-accent { background: #10b981; }
.workout-accent { background: #f59e0b; }
.positive-accent { background: #ef4444; }
.negative-accent { background: #10b981; }
.neutral-accent { background: #6b7280; }

.card-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.meal-icon { background: linear-gradient(135deg, #ecfdf5, #d1fae5); }
.workout-icon { background: linear-gradient(135deg, #fffbeb, #fef3c7); }
.net-icon { background: linear-gradient(135deg, #f8fafc, #f1f5f9); }

.card-info {
  flex: 1;
}

.card-value {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary, #0f172a);
  line-height: 1;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: var(--text-secondary, #64748b);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.chart-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin-bottom: 16px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.records-section {
  background: var(--bg-secondary, #fff);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e2e8f0);
}

.records-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
  margin-bottom: 16px;
}

.calories-in {
  color: #10b981;
  font-weight: 700;
  font-size: 14px;
}

.calories-out {
  color: #f59e0b;
  font-weight: 700;
  font-size: 14px;
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
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .summary-cards {
    grid-template-columns: 1fr;
  }
}
</style>
