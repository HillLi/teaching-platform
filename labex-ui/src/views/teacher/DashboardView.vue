<template>
  <div>
    <h2>教师首页</h2>
    <p>欢迎回来，{{ userStore.user?.userName }}</p>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="班级数" :value="quickStats.classes" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="学生数" :value="quickStats.students" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="实验数" :value="quickStats.experiments" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="资料数" :value="quickStats.lectures" />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px" v-if="hasViewStats">
      <el-col :span="12">
        <el-card>
          <template #header><span>学生统计</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="学生总数">{{ viewStats.studentInfo?.count || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大ID">{{ viewStats.studentInfo?.maxId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近访问">{{ viewStats.studentInfo?.lastAccess || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>系统日志</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="日志总数">{{ viewStats.sysLogInfo?.count || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近登录">{{ viewStats.sysLogInfo?.lastAccess || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import api from '../../api/teacher'

const userStore = useUserStore()
const quickStats = ref({ classes: 0, students: 0, experiments: 0, lectures: 0 })
const viewStats = ref({})

const hasViewStats = computed(() => Object.keys(viewStats.value).length > 0)

onMounted(async () => {
  const [classes, students, experiments, lectures, stats] = await Promise.all([
    api.listClasses(),
    api.listStudents({ pageNum: 1, pageSize: 1 }),
    api.listExperiments(),
    api.listLectures(),
    api.dashboardStats().catch(() => null)
  ])
  quickStats.value.classes = classes.data.length
  quickStats.value.students = students.data.total
  quickStats.value.experiments = experiments.data.length
  quickStats.value.lectures = lectures.data.length
  if (stats) viewStats.value = stats.data
})
</script>
