<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>题库管理</h2>
      <div>
        <el-select v-model="filterType" placeholder="筛选题型" clearable @change="loadData" style="width: 150px; margin-right: 10px">
          <el-option v-for="t in types" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
        </el-select>
        <el-button type="primary" @click="showAddDialog">新增题目</el-button>
      </div>
    </div>
    <el-table :data="filteredQuestions" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="题目内容" min-width="200">
        <template #default="{ row }">
          <span v-html="row.question"></span>
        </template>
      </el-table-column>
      <el-table-column label="题型" width="120">
        <template #default="{ row }">{{ getTypeName(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="answer" label="答案" min-width="150" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editQuestion(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="600">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="t in types" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="form.question" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="3" />
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
import { ref, computed, onMounted } from 'vue'
import api from '../../api/question'
import { ElMessage, ElMessageBox } from 'element-plus'

const questions = ref([])
const types = ref([])
const filterType = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ type: null, question: '', answer: '' })

const filteredQuestions = computed(() => {
  if (!filterType.value) return questions.value
  return questions.value.filter(q => q.type === filterType.value)
})

onMounted(async () => {
  const [qRes, tRes] = await Promise.all([api.list(), api.listTypes()])
  questions.value = qRes.data
  types.value = tRes.data
})

function getTypeName(type) {
  const t = types.value.find(t => t.typeId === type)
  return t ? t.typeName : type
}

function showAddDialog() {
  isEdit.value = false
  form.value = { type: types.value[0]?.typeId || null, question: '', answer: '' }
  dialogVisible.value = true
}

function editQuestion(row) {
  isEdit.value = true
  form.value = {
    id: row.id,
    type: row.type,
    question: row.question,
    answer: row.answer || ''
  }
  dialogVisible.value = true
}

async function handleSave() {
  const data = { ...form.value }
  if (isEdit.value) {
    await api.update(form.value.id, data)
  } else {
    await api.add(data)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  const res = await api.list()
  questions.value = res.data
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await api.delete(id)
  ElMessage.success('删除成功')
  const res = await api.list()
  questions.value = res.data
}
</script>
