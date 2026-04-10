<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>题目管理 - 实验 #{{ $route.params.id }}</h2>
      <div>
        <el-button @click="$router.back()">返回实验列表</el-button>
        <el-button type="primary" @click="showAddDialog">新增题目</el-button>
      </div>
    </div>
    <el-table :data="items" border stripe>
      <el-table-column prop="experimentItemNo" label="题号" width="80" />
      <el-table-column prop="experimentItemName" label="题目名" />
      <el-table-column prop="experimentItemType" label="类型" width="80" />
      <el-table-column prop="experimentItemScore" label="分值" width="80" />
      <el-table-column prop="experimentItemAnswer" label="参考答案" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="setAnswer(row)">设置答案</el-button>
          <el-button size="small" @click="editItem(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.experimentItemId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题号">
          <el-input-number v-model="form.experimentItemNo" :min="1" />
        </el-form-item>
        <el-form-item label="题目名">
          <el-input v-model="form.experimentItemName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input-number v-model="form.experimentItemType" :min="1" />
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.experimentItemScore" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="form.experimentItemContent" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="answerDialogVisible" title="设置参考答案" width="400">
      <el-input v-model="answerForm" type="textarea" :rows="3" />
      <template #footer>
        <el-button @click="answerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnswer">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const expId = route.params.id
const items = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const answerDialogVisible = ref(false)
const answerForm = ref('')
const answerItemId = ref(null)

onMounted(loadData)

async function loadData() {
  const res = await api.listItems(expId)
  items.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { experimentItemNo: items.value.length + 1, experimentItemName: '', experimentItemType: 1, experimentItemScore: 10, experimentItemContent: '' }
  dialogVisible.value = true
}

function editItem(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

function setAnswer(row) {
  answerItemId.value = row.experimentItemId
  answerForm.value = row.experimentItemAnswer || ''
  answerDialogVisible.value = true
}

async function saveAnswer() {
  await api.setItemAnswer(answerItemId.value, answerForm.value)
  ElMessage.success('答案已保存')
  answerDialogVisible.value = false
  loadData()
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateItem(form.value.experimentItemId, form.value)
  } else {
    await api.addItem(expId, form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
  await api.deleteItem(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
