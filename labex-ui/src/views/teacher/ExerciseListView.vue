<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>练习管理</h2>
      <el-button type="primary" @click="showAddDialog">新增练习</el-button>
    </div>
    <el-table :data="exercises" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="练习名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="viewItems(row)">题目</el-button>
          <el-button size="small" @click="editExercise(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑练习' : '新增练习'" width="400">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input-number v-model="form.type" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" title="练习题目" width="600">
      <el-button size="small" type="primary" @click="addItemDialog = true" style="margin-bottom: 10px">新增题目</el-button>
      <el-table :data="items" border>
        <el-table-column prop="question" label="题目" />
        <el-table-column prop="options" label="选项" />
        <el-table-column prop="answer" label="答案" width="100" />
        <el-table-column prop="type" label="类型" width="80" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="addItemDialog" title="新增题目" width="400">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="题目"><el-input v-model="itemForm.question" /></el-form-item>
        <el-form-item label="选项"><el-input v-model="itemForm.options" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="答案"><el-input v-model="itemForm.answer" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="itemForm.type" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const exercises = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ name: '', description: '', type: 2 })
const itemDialogVisible = ref(false)
const addItemDialog = ref(false)
const items = ref([])
const currentExId = ref(null)
const itemForm = ref({ question: '', options: '', answer: '', type: 1 })

onMounted(loadData)

async function loadData() {
  const res = await api.listExercises()
  exercises.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { name: '', description: '', type: 2 }
  dialogVisible.value = true
}

function editExercise(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateExercise(form.value.id, form.value)
  } else {
    await api.addExercise(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function viewItems(row) {
  currentExId.value = row.id
  const res = await api.getExerciseItems(row.id)
  items.value = res.data
  itemDialogVisible.value = true
}

async function saveItem() {
  await api.addExerciseItem(currentExId.value, itemForm.value)
  ElMessage.success('题目已添加')
  addItemDialog.value = false
  itemForm.value = { question: '', options: '', answer: '', type: 1 }
  const res = await api.getExerciseItems(currentExId.value)
  items.value = res.data
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该练习？', '提示', { type: 'warning' })
  await api.deleteExercise(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
