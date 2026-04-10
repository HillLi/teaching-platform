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
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'

const classes = ref([])
const experiments = ref([])
const clazzNo = ref('')
const experimentId = ref(null)
const scores = ref([])

onMounted(async () => {
  const [classRes, expRes] = await Promise.all([api.listClasses(), api.listExperiments()])
  classes.value = classRes.data
  experiments.value = expRes.data
})

async function loadData() {
  const res = await api.getClassScore(clazzNo.value, experimentId.value)
  scores.value = res.data
}
</script>
