<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>考试成绩</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-card v-if="score">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="总分">{{ score.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="score.status === 2 ? 'success' : 'warning'">
            {{ score.status === 2 ? '已批改' : '批改中' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-empty v-else description="暂无成绩信息" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const examId = route.params.id
const score = ref(null)

onMounted(async () => {
  try {
    const res = await api.getExamScore(examId)
    score.value = res.data
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '无法获取成绩')
  }
})
</script>
