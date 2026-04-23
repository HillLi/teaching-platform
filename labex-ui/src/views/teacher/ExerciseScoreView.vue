<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>作业成绩汇总 - {{ exerciseName }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-table :data="scores" border stripe>
      <el-table-column prop="studentId" label="学生ID" width="80" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="总分" width="80">
        <template #default="{ row }">{{ row.totalScore != null ? row.totalScore : '-' }}</template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px" v-if="scores.length">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="提交人数">{{ scores.length }}</el-descriptions-item>
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
const exerciseId = route.params.id
const exerciseName = ref('')
const scores = ref([])

const scoredList = computed(() => scores.value.filter(s => s.totalScore != null))
const avgScore = computed(() => {
  if (scoredList.value.length === 0) return '-'
  return (scoredList.value.reduce((sum, s) => sum + Number(s.totalScore), 0) / scoredList.value.length).toFixed(1)
})
const maxScore = computed(() => {
  if (scoredList.value.length === 0) return '-'
  return Math.max(...scoredList.value.map(s => Number(s.totalScore)))
})

onMounted(async () => {
  const exercises = (await api.listExercises()).data
  const ex = exercises.find(e => e.id === Number(exerciseId))
  exerciseName.value = ex ? ex.name : ''
  const res = await api.getExerciseScores(exerciseId)
  scores.value = res.data
})
</script>
