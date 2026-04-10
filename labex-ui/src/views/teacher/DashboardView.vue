<template>
  <div>
    <h2>教师首页</h2>
    <p>欢迎回来，{{ userStore.user?.userName }}</p>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-statistic title="班级数" :value="stats.classes" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="学生数" :value="stats.students" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="实验数" :value="stats.experiments" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import api from '../../api/teacher'

const userStore = useUserStore()
const stats = ref({ classes: 0, students: 0, experiments: 0 })

onMounted(async () => {
  const [classes, students, experiments] = await Promise.all([
    api.listClasses(),
    api.listStudents({ pageNum: 1, pageSize: 1 }),
    api.listExperiments()
  ])
  stats.value.classes = classes.data.length
  stats.value.students = students.data.total
  stats.value.experiments = experiments.data.length
})
</script>
