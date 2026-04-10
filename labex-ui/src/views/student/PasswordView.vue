<template>
  <div>
    <h2>修改密码</h2>
    <el-card style="max-width: 400px; margin-top: 20px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChange">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function handleChange() {
  if (form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (form.value.newPassword.length < 4) {
    ElMessage.error('新密码至少4位')
    return
  }
  await api.changePassword(form.value.oldPassword, form.value.newPassword)
  ElMessage.success('密码修改成功')
  form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}
</script>
