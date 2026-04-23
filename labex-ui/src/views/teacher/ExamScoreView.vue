<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>考试成绩汇总 - {{ examName }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-table :data="scores" border stripe>
      <el-table-column prop="studentId" label="学生ID" width="80" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'" size="small">
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总分" width="80">
        <template #default="{ row }">{{ row.totalScore != null ? row.totalScore : '-' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px" v-if="scores.length">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="参考人数">{{ scores.length }}</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ avgScore }}</el-descriptions-item>
        <el-descriptions-item label="最高分">{{ maxScore }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/teacher'

const route = useRoute()
const examId = route.params.id
const examName = ref('')
const scores = ref([])
const statusMap = { 0: '未提交', 1: '待批改', 2: '已批改' }

function formatTime(t) { return t ? t.replace('T', ' ') : '-' }

const gradedScores = computed(() => scores.value.filter(s => s.totalScore != null))
const avgScore = computed(() => {
  if (gradedScores.value.length === 0) return '-'
  return (gradedScores.value.reduce((sum, s) => sum + Number(s.totalScore), 0) / gradedScores.value.length).toFixed(1)
})
const maxScore = computed(() => {
  if (gradedScores.value.length === 0) return '-'
  return Math.max(...gradedScores.value.map(s => Number(s.totalScore)))
})

onMounted(async () => {
  const exams = (await api.listExams()).data
  const exam = exams.find(e => e.id === Number(examId))
  examName.value = exam ? exam.name : ''
  const res = await api.getExamScores(examId)
  scores.value = res.data
})
</script>
