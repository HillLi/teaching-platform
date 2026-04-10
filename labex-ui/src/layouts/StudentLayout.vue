<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #545c64">
      <div style="padding: 20px; color: #fff; font-size: 18px; text-align: center; border-bottom: 1px solid #636e7b">
        教学平台 - 学生端
      </div>
      <el-menu :default-active="$route.path" router background-color="#545c64" text-color="#fff" active-text-color="#ffd04b">
        <el-menu-item index="/student">
          <el-icon><HomeFilled /></el-icon><span>我的实验</span>
        </el-menu-item>
        <el-menu-item index="/student/lectures">
          <el-icon><FolderOpened /></el-icon><span>讲义下载</span>
        </el-menu-item>
        <el-menu-item index="/student/password">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: flex-end; border-bottom: 1px solid #eee">
        <span style="margin-right: 16px">{{ userStore.user?.userName }} ({{ userStore.user?.account }})</span>
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
