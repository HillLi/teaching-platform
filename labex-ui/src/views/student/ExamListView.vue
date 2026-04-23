<template>
  <div>
    <h2>我的考试</h2>
    <el-table :data="exams" border stripe style="margin-top: 16px">
      <el-table-column prop="name" label="考试名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="时长" width="100">
        <template #default="{ row }">{{ row.duration }} 分钟</template>
      </el-table-column>
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="160">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.submitted">
            <el-tag type="success" size="small">已完成</el-tag>
            <el-button size="small" type="success" @click="viewScore(row)" style="margin-left: 8px">查看成绩</el-button>
          </template>
          <el-button size="small" type="primary" @click="startExam(row)" v-else-if="!row.started">开始考试</el-button>
          <el-button size="small" @click="goToExam(row)" v-else>继续答题</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()
const exams = ref([])

function formatTime(t) { return t ? t.replace('T', ' ') : '-' }

onMounted(async () => {
  const res = await api.listExams()
  exams.value = res.data
})

async function startExam(row) {
  try {
    await api.startExam(row.id)
    router.push(`/student/exams/${row.id}`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '无法开始考试')
  }
}

function goToExam(row) {
  router.push(`/student/exams/${row.id}`)
}

async function viewScore(row) {
  try {
    await api.getExamScore(row.id)
    router.push(`/student/exams/${row.id}/score`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '成绩尚未公布')
  }
}
</script>
