<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div style="padding: 20px; color: #fff; font-size: 18px; text-align: center; border-bottom: 1px solid #3a4a5b">
        教学平台 - 教师端
      </div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/teacher">
          <el-icon><HomeFilled /></el-icon><span>首页</span>
        </el-menu-item>
        <el-menu-item index="/teacher/classes">
          <el-icon><School /></el-icon><span>班级管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/students">
          <el-icon><User /></el-icon><span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/experiments">
          <el-icon><Document /></el-icon><span>实验管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/grading">
          <el-icon><Edit /></el-icon><span>批改中心</span>
        </el-menu-item>
        <el-menu-item index="/teacher/class-report">
          <el-icon><DataAnalysis /></el-icon><span>成绩汇总</span>
        </el-menu-item>
        <el-menu-item index="/teacher/lectures">
          <el-icon><FolderOpened /></el-icon><span>讲义管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/exercises">
          <el-icon><Notebook /></el-icon><span>练习管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/questions">
          <el-icon><Collection /></el-icon><span>题库管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: flex-end; border-bottom: 1px solid #eee">
        <span style="margin-right: 16px">{{ userStore.user?.userName }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>
