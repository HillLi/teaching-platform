<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>学生报告批改</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-table :data="report" border stripe>
      <el-table-column prop="experiment_item_no" label="题号" width="80" />
      <el-table-column prop="experiment_item_name" label="题目名" width="200" />
      <el-table-column prop="experiment_item_type" label="类型" width="80" />
      <el-table-column prop="student_answer" label="学生答案">
        <template #default="{ row }">
          <div v-html="row.student_answer || '(未作答)'"></div>
        </template>
      </el-table-column>
      <el-table-column prop="experiment_item_score" label="满分" width="80" />
      <el-table-column label="得分" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.score" :min="0" :max="row.experiment_item_score" size="small" v-if="row.student_item_id" />
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px; text-align: right">
      <el-button type="primary" @click="saveAllScores">提交全部评分</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage } from 'element-plus'

const route = useRoute()
const report = ref([])

onMounted(async () => {
  const res = await api.getStudentReport(route.params.studentId, route.params.expId)
  report.value = res.data
})

async function saveAllScores() {
  for (const item of report.value) {
    if (item.student_item_id && item.score !== undefined) {
      await api.submitScore({ studentItemId: item.student_item_id, score: item.score })
    }
  }
  ElMessage.success('评分已提交')
}
</script>
