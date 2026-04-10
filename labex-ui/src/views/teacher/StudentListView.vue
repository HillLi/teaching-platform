<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>学生管理</h2>
      <div>
        <el-select v-model="clazzNo" placeholder="筛选班级" clearable @change="loadData" style="width: 150px; margin-right: 10px">
          <el-option v-for="c in classes" :key="c.no" :label="c.no" :value="c.no" />
        </el-select>
        <el-button type="primary" @click="showAddDialog">新增学生</el-button>
      </div>
    </div>
    <el-table :data="students.records" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column prop="clazzNo" label="班级" width="120" />
      <el-table-column prop="state" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === 1 ? 'success' : 'danger'">{{ row.state === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="最后IP" width="150" />
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="editStudent(row)">编辑</el-button>
          <el-button size="small" @click="viewLogs(row)">日志</el-button>
          <el-button size="small" type="warning" @click="handleResetPassword(row.studentId)">重置密码</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.studentId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="students.total > 10" :total="students.total" :page-size="10" @current-change="loadData"
      style="margin-top: 16px" layout="prev, pager, next" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学生' : '新增学生'" width="400">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.studentName" />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.clazzNo" style="width: 100%">
            <el-option v-for="c in classes" :key="c.no" :label="c.no" :value="c.no" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logsDialogVisible" title="学生活动日志" width="700">
      <el-table :data="studentLogs" border size="small" v-loading="logsLoading">
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column prop="info" label="信息" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="time" label="时间" width="170" />
      </el-table>
      <el-empty v-if="!logsLoading && studentLogs.length === 0" description="暂无日志" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const classes = ref([])
const students = ref({ records: [], total: 0 })
const clazzNo = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ studentNo: '', studentName: '', clazzNo: '' })
let pageNum = 1

onMounted(async () => {
  const res = await api.listClasses()
  classes.value = res.data
  loadData()
})

async function loadData(page = 1) {
  pageNum = page
  const res = await api.listStudents({ pageNum, pageSize: 10, clazzNo: clazzNo.value || undefined })
  students.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { studentNo: '', studentName: '', clazzNo: '' }
  dialogVisible.value = true
}

function editStudent(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateStudent(form.value.studentId, form.value)
  } else {
    await api.addStudent(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该学生？', '提示', { type: 'warning' })
  await api.deleteStudent(id)
  ElMessage.success('删除成功')
  loadData()
}

const logsDialogVisible = ref(false)
const studentLogs = ref([])
const logsLoading = ref(false)

async function viewLogs(row) {
  logsDialogVisible.value = true
  logsLoading.value = true
  try {
    const res = await api.getStudentLogs(row.studentId)
    studentLogs.value = res.data
  } catch (e) {
    studentLogs.value = []
  } finally {
    logsLoading.value = false
  }
}

async function handleResetPassword(id) {
  await ElMessageBox.confirm('确定将该学生密码重置为 123456？', '提示', { type: 'warning' })
  await api.resetPassword(id)
  ElMessage.success('密码已重置为 123456')
}
</script>
