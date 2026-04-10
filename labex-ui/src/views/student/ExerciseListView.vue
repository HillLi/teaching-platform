<template>
  <div>
    <h2>练习中心</h2>
    <el-table :data="exercises" border stripe style="margin-top: 16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="练习名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="goToExercise(row.id)">开始练习</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/student'

const router = useRouter()
const exercises = ref([])

onMounted(async () => {
  const res = await api.listExercises()
  exercises.value = res.data
})

function goToExercise(id) {
  router.push(`/student/exercises/${id}`)
}
</script>
