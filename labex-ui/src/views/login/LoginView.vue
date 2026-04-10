<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <el-card style="width: 400px; border-radius: 12px">
      <template #header>
        <h2 style="text-align: center; margin: 0">教学一体化平台</h2>
      </template>
      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="账号">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">教师</el-radio>
            <el-radio :value="1">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'

const form = ref({ account: '', password: '', type: 0 })
const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()

async function handleLogin() {
  if (!form.value.account || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const user = await userStore.login(form.value.account, form.value.password, form.value.type)
    ElMessage.success('登录成功')
    router.push(user.userType === 0 ? '/teacher' : '/student')
  } catch {
    // Error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
