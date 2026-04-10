<template>
  <div>
    <h2>我的实验</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-table :data="experiments" border stripe>
          <el-table-column prop="experimentNo" label="编号" width="80" />
          <el-table-column prop="experimentName" label="实验名称" />
          <el-table-column prop="experimentType" label="类型" width="100" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="goToExperiment(row.experimentId)">进入实验</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="8">
        <h3>我的成绩</h3>
        <el-table :data="scores" border size="small">
          <el-table-column prop="experiment_name" label="实验" />
          <el-table-column prop="score" label="成绩" width="80">
            <template #default="{ row }">{{ row.score ?? '-' }}</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/student'

const router = useRouter()
const experiments = ref([])
const scores = ref([])

onMounted(async () => {
  const [expRes, scoreRes] = await Promise.all([api.listExperiments(), api.getMyScores()])
  experiments.value = expRes.data
  scores.value = scoreRes.data
})

function goToExperiment(id) {
  router.push(`/student/experiments/${id}`)
}
</script>
