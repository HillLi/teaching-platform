<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>班级管理</h2>
      <el-button type="primary" @click="showAddDialog">新增班级</el-button>
    </div>
    <el-table :data="classes" border stripe>
      <el-table-column prop="no" label="班级编号" width="150" />
      <el-table-column prop="memo" label="备注/教师名" />
      <el-table-column prop="state" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === 1 ? 'success' : 'danger'">{{ row.state === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editClass(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.no)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '新增班级'" width="400">
      <el-form :model="form" label-width="80px">
        <el-form-item label="班级编号">
          <el-input v-model="form.no" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.memo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const classes = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ no: '', memo: '' })

onMounted(loadData)

async function loadData() {
  const res = await api.listClasses()
  classes.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { no: '', memo: '' }
  dialogVisible.value = true
}

function editClass(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateClass(form.value.no, form.value)
  } else {
    await api.addClass(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(no) {
  await ElMessageBox.confirm('确定删除该班级？', '提示', { type: 'warning' })
  await api.deleteClass(no)
  ElMessage.success('删除成功')
  loadData()
}
</script>
