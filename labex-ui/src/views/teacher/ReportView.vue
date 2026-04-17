<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>学生报告批改</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-table :data="report" border stripe>
      <el-table-column prop="experiment_item_no" label="题号" width="80" />
      <el-table-column prop="experiment_item_name" label="题目名" width="200" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          {{ typeName(row.experiment_item_type) }}
        </template>
      </el-table-column>
      <el-table-column label="学生答案">
        <template #default="{ row }">
          <template v-if="!row.student_answer">(未作答)</template>
          <template v-else-if="isShortAnswer(row.student_answer)">
            <div v-html="row.student_answer"></div>
          </template>
          <template v-else>
            <div style="max-height: 60px; overflow: hidden; position: relative">
              <div v-html="row.student_answer"></div>
            </div>
            <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
          </template>
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

    <el-dialog v-model="detailVisible" title="学生答案详情" width="700">
      <div v-html="detailContent" style="max-height: 500px; overflow-y: auto"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const report = ref([])
const detailVisible = ref(false)
const detailContent = ref('')

const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }
function typeName(type) {
  return typeMap[type] || type
}

/** Strip HTML tags and check if the plain text is short enough to display inline */
function isShortAnswer(html) {
  const text = html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
  return text.length <= 80
}

function openDetail(row) {
  detailContent.value = row.student_answer
  detailVisible.value = true
}

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
  router.push('/teacher/grading')
}
</script>
