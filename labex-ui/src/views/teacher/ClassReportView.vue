<template>
  <div>
    <h2>班级成绩汇总</h2>
    <div style="margin-bottom: 16px; display: flex; gap: 10px">
      <el-select v-model="clazzNo" placeholder="选择班级">
        <el-option v-for="c in classes" :key="c.no" :label="c.no" :value="c.no" />
      </el-select>
      <el-select v-model="experimentId" placeholder="选择实验">
        <el-option v-for="e in experiments" :key="e.experimentId" :label="e.experimentName" :value="e.experimentId" />
      </el-select>
      <el-button type="primary" @click="loadData" :disabled="!clazzNo || !experimentId">查询</el-button>
    </div>

    <!-- 统计分析区域 -->
    <div v-if="scores.length" style="display: flex; gap: 16px; margin-bottom: 16px;">
      <!-- 左侧：统计卡片 -->
      <div style="flex: 0 0 280px; display: flex; flex-direction: column; gap: 12px;">
        <div style="padding: 16px; background: #e8f5e9; border-radius: 4px; border-left: 4px solid #4caf50;">
          <div style="font-size: 12px; color: #888;">平均分</div>
          <div style="font-size: 28px; font-weight: bold; color: #333;">{{ stats.avg }}</div>
        </div>
        <div style="padding: 16px; background: #e3f2fd; border-radius: 4px; border-left: 4px solid #2196f3;">
          <div style="font-size: 12px; color: #888;">最高分</div>
          <div style="font-size: 28px; font-weight: bold; color: #333;">{{ stats.max }}</div>
        </div>
        <div style="padding: 16px; background: #fff3e0; border-radius: 4px; border-left: 4px solid #ff9800;">
          <div style="font-size: 12px; color: #888;">最低分</div>
          <div style="font-size: 28px; font-weight: bold; color: #333;">{{ stats.min }}</div>
        </div>
        <div style="padding: 16px; background: #fce4ec; border-radius: 4px; border-left: 4px solid #e91e63;">
          <div style="font-size: 12px; color: #888;">及格率</div>
          <div style="font-size: 28px; font-weight: bold; color: #333;">{{ stats.passRate }}%</div>
        </div>
      </div>
      <!-- 右侧：图表 -->
      <div style="flex: 1; display: flex; flex-direction: column; gap: 12px;">
        <div ref="barChartRef" style="height: 220px; border: 1px solid #ebeef5; border-radius: 4px;"></div>
        <div ref="pieChartRef" style="height: 220px; border: 1px solid #ebeef5; border-radius: 4px;"></div>
      </div>
    </div>

    <!-- 成绩明细表格 -->
    <el-table :data="scores" border stripe v-if="scores.length">
      <el-table-column prop="student_no" label="学号" width="120" />
      <el-table-column prop="student_name" label="姓名" width="120" />
      <el-table-column prop="clazz_no" label="班级" width="120" />
      <el-table-column prop="score" label="总分" width="100">
        <template #default="{ row }">
          {{ row.score ?? '-' }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import api from '../../api/teacher'

const classes = ref([])
const experiments = ref([])
const clazzNo = ref('')
const experimentId = ref(null)
const scores = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)

let barChart = null
let pieChart = null

const SCORE_SEGMENTS = [
  { label: '0-59', min: 0, max: 59, color: '#f44336' },
  { label: '60-69', min: 60, max: 69, color: '#ff9800' },
  { label: '70-79', min: 70, max: 79, color: '#ffc107' },
  { label: '80-89', min: 80, max: 89, color: '#8bc34a' },
  { label: '90-100', min: 90, max: 100, color: '#4caf50' }
]

const stats = ref({ avg: 0, max: 0, min: 0, passRate: 0 })

onMounted(async () => {
  const [classRes, expRes] = await Promise.all([api.listClasses(), api.listExperiments()])
  classes.value = classRes.data
  experiments.value = expRes.data
})

onBeforeUnmount(() => {
  barChart?.dispose()
  pieChart?.dispose()
})

function calcStats(data) {
  const validScores = data.map(d => Number(d.score)).filter(s => !isNaN(s))
  if (!validScores.length) return { avg: 0, max: 0, min: 0, passRate: 0 }
  const avg = (validScores.reduce((a, b) => a + b, 0) / validScores.length).toFixed(1)
  const max = Math.max(...validScores)
  const min = Math.min(...validScores)
  const passCount = validScores.filter(s => s >= 60).length
  const passRate = ((passCount / validScores.length) * 100).toFixed(1)
  return { avg, max, min, passRate }
}

function calcDistribution(data) {
  const validScores = data.map(d => Number(d.score)).filter(s => !isNaN(s))
  return SCORE_SEGMENTS.map(seg => ({
    name: seg.label,
    value: validScores.filter(s => s >= seg.min && s <= seg.max).length,
    itemStyle: { color: seg.color }
  }))
}

function renderCharts() {
  const dist = calcDistribution(scores.value)

  // 柱状图
  if (!barChart && barChartRef.value) barChart = echarts.init(barChartRef.value)
  barChart?.setOption({
    title: { text: '成绩分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dist.map(d => d.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data: dist.map(d => ({ value: d.value, itemStyle: d.itemStyle })) }]
  })

  // 饼图
  if (!pieChart && pieChartRef.value) pieChart = echarts.init(pieChartRef.value)
  pieChart?.setOption({
    title: { text: '分数段占比', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['35%', '60%'],
      data: dist.filter(d => d.value > 0),
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

async function loadData() {
  const res = await api.getClassScore(clazzNo.value, experimentId.value)
  scores.value = res.data
  stats.value = calcStats(scores.value)
  await nextTick()
  renderCharts()
}
</script>
