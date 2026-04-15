<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>课程资料</h2>
      <el-button type="primary" @click="openUploadDialog">上传资料</el-button>
    </div>
    <el-dialog v-model="uploadDialogVisible" title="上传资料" width="400">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="资料名称">
          <el-input v-model="uploadForm.name" placeholder="请输入资料名称" />
        </el-form-item>
        <el-form-item label="资料类型">
          <el-select v-model="uploadForm.type" style="width: 100%">
            <el-option v-for="(name, val) in lectureTypes" :key="val" :label="name" :value="Number(val)" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx,.ppt,.pptx,.zip"
          >
            <el-button size="small" type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doUpload" :disabled="!pendingFile">保存</el-button>
      </template>
    </el-dialog>
    <el-table :data="lectures" border stripe>
      <el-table-column prop="lectureId" label="ID" width="80" />
      <el-table-column prop="lectureName" label="资料名称" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ lectureTypes[row.lectureType] || row.lectureType }}</template>
      </el-table-column>
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
const lectureTypes = { 1: '讲义', 2: '代码', 3: '软件', 4: '参考资料' }
const uploadDialogVisible = ref(false)
const uploadForm = ref({ name: '', type: 1 })
const uploadRef = ref(null)
const pendingFile = ref(null)

onMounted(loadData)

async function loadData() {
  const res = await api.listLectures()
  lectures.value = res.data
}

function openUploadDialog() {
  uploadForm.value = { name: '', type: 1 }
  pendingFile.value = null
  uploadRef.value?.clearFiles()
  uploadDialogVisible.value = true
}

function handleFileChange(file) {
  pendingFile.value = file.raw
  if (!uploadForm.value.name) {
    uploadForm.value.name = file.name.replace(/\.[^.]+$/, '')
  }
}

function handleFileRemove() {
  pendingFile.value = null
}

async function doUpload() {
  if (!pendingFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  const formData = new FormData()
  formData.append('file', pendingFile.value)
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
