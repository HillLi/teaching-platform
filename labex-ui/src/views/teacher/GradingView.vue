<template>
  <div>
    <h2>批改中心</h2>
    <div style="margin-bottom: 16px; display: flex; gap: 10px">
      <el-select v-model="experimentId" placeholder="选择实验" @change="loadStudents">
        <el-option v-for="e in experiments" :key="e.experimentId" :label="e.experimentName" :value="e.experimentId" />
      </el-select>
      <el-select v-model="clazzNo" placeholder="筛选班级" clearable @change="loadStudents">
        <el-option v-for="c in classes" :key="c.no" :label="c.no" :value="c.no" />
      </el-select>
    </div>

    <el-table :data="students.records" border stripe v-if="experimentId">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="clazzNo" label="班级" width="120" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewReport(row.studentId)">批改</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/teacher'

const router = useRouter()
const experiments = ref([])
const classes = ref([])
const experimentId = ref(null)
const clazzNo = ref('')
const students = ref({ records: [], total: 0 })

onMounted(async () => {
  const [expRes, classRes] = await Promise.all([api.listExperiments(), api.listClasses()])
  experiments.value = expRes.data
  classes.value = classRes.data
})

async function loadStudents() {
  if (!experimentId.value) return
  const res = await api.listSubmittedStudents({
    experimentId: experimentId.value,
    clazzNo: clazzNo.value || undefined,
    pageNum: 1, pageSize: 100
  })
  students.value = res.data
}

function viewReport(studentId) {
  router.push(`/teacher/grading/${studentId}/${experimentId.value}`)
}
</script>
