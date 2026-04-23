<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>作业批改 - {{ exerciseName }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-table :data="submissions" border stripe>
      <el-table-column prop="studentId" label="学生ID" width="80" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="gradeStudent(row)">批改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批改对话框 -->
    <el-dialog v-model="gradingVisible" title="逐题批改" width="800">
      <el-table :data="detail" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题目" min-width="120">
          <template #default="{ row }">{{ truncate(row.question) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ typeMap[row.type] }}</template>
        </el-table-column>
        <el-table-column label="学生答案" min-width="120">
          <template #default="{ row }">{{ row.studentAnswer || '(未答)' }}</template>
        </el-table-column>
        <el-table-column label="自动评分" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.autoScored" type="success" size="small">{{ row.currentScore }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.currentScore" :min="0" :max="100" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="gradingVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGrading">提交评分</el-button>
      </template>
    </el-dialog>

    <div style="margin-top: 16px">
      <el-button type="success" @click="$router.push(`/teacher/exercise-scores/${exerciseId}`)">查看成绩汇总</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage } from 'element-plus'

const route = useRoute()
const exerciseId = route.params.id
const exerciseName = ref('')
const submissions = ref([])
const gradingVisible = ref(false)
const detail = ref([])
const currentStudentId = ref(null)
const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }

function truncate(text) { return !text ? '-' : text.length > 40 ? text.substring(0, 40) + '...' : text }

onMounted(async () => {
  const exercises = (await api.listExercises()).data
  const ex = exercises.find(e => e.id === Number(exerciseId))
  exerciseName.value = ex ? ex.name : ''
  loadData()
})

async function loadData() {
  const res = await api.getExerciseSubmissions(exerciseId)
  submissions.value = res.data
}

async function gradeStudent(row) {
  currentStudentId.value = row.studentId
  const res = await api.getExerciseSubmissionDetail(exerciseId, row.studentId)
  detail.value = res.data
  gradingVisible.value = true
}

async function saveGrading() {
  for (const item of detail.value) {
    if (item.studentExerciseId) {
      await api.submitExerciseScore({ studentExerciseId: item.studentExerciseId, score: item.currentScore })
    }
  }
  ElMessage.success('评分已提交')
  gradingVisible.value = false
  loadData()
}
</script>
