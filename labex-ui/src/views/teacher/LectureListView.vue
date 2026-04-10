<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>讲义管理</h2>
      <el-upload :show-file-list="false" :before-upload="handleUpload" accept=".pdf,.doc,.docx,.ppt,.pptx,.zip">
        <el-button type="primary">上传讲义</el-button>
      </el-upload>
    </div>
    <el-dialog v-model="uploadDialogVisible" title="上传讲义" width="400">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="讲义名称">
          <el-input v-model="uploadForm.name" />
        </el-form-item>
        <el-form-item label="讲义类型">
          <el-input-number v-model="uploadForm.type" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doUpload">确认上传</el-button>
      </template>
    </el-dialog>
    <el-table :data="lectures" border stripe>
      <el-table-column prop="lectureId" label="ID" width="80" />
      <el-table-column prop="lectureName" label="讲义名称" />
      <el-table-column prop="lectureType" label="类型" width="100" />
      <el-table-column prop="lectureFiletype" label="文件类型" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.lectureId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const lectures = ref([])
const uploadDialogVisible = ref(false)
const uploadForm = ref({ name: '', type: 1 })
let pendingFile = null

onMounted(loadData)

async function loadData() {
  const res = await api.listLectures()
  lectures.value = res.data
}

function handleUpload(file) {
  pendingFile = file
  uploadForm.value.name = file.name.replace(/\.[^.]+$/, '')
  uploadDialogVisible.value = true
  return false
}

async function doUpload() {
  const formData = new FormData()
  formData.append('file', pendingFile)
  formData.append('name', uploadForm.value.name)
  formData.append('type', uploadForm.value.type)
  await api.uploadLecture(formData)
  ElMessage.success('上传成功')
  uploadDialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await api.deleteLecture(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
