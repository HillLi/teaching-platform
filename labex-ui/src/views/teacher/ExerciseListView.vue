<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>作业管理</h2>
      <el-button type="primary" @click="showAddDialog">新增练习</el-button>
    </div>
    <el-table :data="exercises" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="练习名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="viewItems(row)">题目</el-button>
          <el-button size="small" @click="editExercise(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          <el-button size="small" type="warning" @click="viewSubmissions(row)">批改</el-button>
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 题目管理对话框 -->
    <el-dialog v-model="itemDialogVisible" title="练习题目" width="750">
      <el-button size="small" type="primary" @click="showAddItemDialog" style="margin-bottom: 10px">新增题目</el-button>
      <el-table :data="items" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题目" min-width="120">
          <template #default="{ row }">
            {{ truncate(row.question) }}
            <el-button v-if="isLong(row.question)" type="primary" link size="small" @click="showDetail('题目', row.question)">详情</el-button>
          </template>
        </el-table-column>
        <el-table-column label="选项" width="120">
          <template #default="{ row }">
            <template v-if="row.options">
              {{ truncate(row.options) }}
              <el-button v-if="isLong(row.options)" type="primary" link size="small" @click="showDetail('选项', row.options)">详情</el-button>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="答案" width="120">
          <template #default="{ row }">
            {{ truncate(row.answer) }}
            <el-button v-if="isLong(row.answer)" type="primary" link size="small" @click="showDetail('答案', row.answer)">详情</el-button>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ getTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="editItem(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑题目对话框 -->
    <el-dialog v-model="addItemDialog" :title="editingItem ? '编辑题目' : '新增题目'" width="500">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="itemForm.type" style="width: 100%" @change="onItemTypeChange">
            <el-option v-for="t in questionTypes" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目"><el-input v-model="itemForm.question" type="textarea" :rows="2" /></el-form-item>
        <!-- 填空题提示 -->
        <el-alert v-if="itemForm.type === 1" type="info" :closable="false" style="margin-bottom: 12px">
          多个空用 | 分隔，如：答案1|答案2|答案3
        </el-alert>
        <!-- 单选/多选：动态选项列表 -->
        <template v-if="itemForm.type === 2 || itemForm.type === 3">
          <el-form-item label="选项">
            <div style="width: 100%">
              <div v-for="(opt, idx) in itemForm.optionList" :key="idx" style="display: flex; margin-bottom: 8px; align-items: center">
                <span style="width: 30px; flex-shrink: 0; font-weight: bold">{{ String.fromCharCode(65 + idx) }}</span>
                <el-input v-model="itemForm.optionList[idx]" style="flex: 1" :placeholder="'选项 ' + String.fromCharCode(65 + idx)" />
                <el-button v-if="itemForm.optionList.length > 2" size="small" type="danger" :icon="Delete"
                  @click="itemForm.optionList.splice(idx, 1)" style="margin-left: 8px" />
              </div>
              <el-button size="small" @click="itemForm.optionList.push('')" :disabled="itemForm.optionList.length >= 8">
                添加选项
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="正确答案">
            <div v-if="itemForm.type === 2">
              <el-radio-group v-model="itemForm.answer">
                <el-radio v-for="(opt, idx) in itemForm.optionList" :key="idx"
                  :value="String.fromCharCode(65 + idx)" style="margin-bottom: 8px">
                  {{ String.fromCharCode(65 + idx) }}. {{ opt || '(空)' }}
                </el-radio>
              </el-radio-group>
            </div>
            <div v-else>
              <el-checkbox-group v-model="itemForm.multiAnswer">
                <el-checkbox v-for="(opt, idx) in itemForm.optionList" :key="idx"
                  :label="String.fromCharCode(65 + idx)" style="margin-bottom: 8px">
                  {{ String.fromCharCode(65 + idx) }}. {{ opt || '(空)' }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </el-form-item>
        </template>
        <!-- 判断题 -->
        <el-form-item v-else-if="itemForm.type === 4" label="正确答案">
          <el-radio-group v-model="itemForm.answer">
            <el-radio value="T">正确</el-radio>
            <el-radio value="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 填空/简答/编程/综合 -->
        <el-form-item v-else label="答案">
          <el-input v-model="itemForm.answer" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="detailTitle" width="600">
      <div style="max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all">{{ detailContent }}</div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const exercises = ref([])
const questionTypes = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ name: '', description: '' })
const itemDialogVisible = ref(false)
const addItemDialog = ref(false)
const editingItem = ref(null)
const items = ref([])
const currentExId = ref(null)
const itemForm = ref({ question: '', optionList: ['', ''], answer: '', multiAnswer: [], type: 2 })
const detailVisible = ref(false)
const detailTitle = ref('')
const detailContent = ref('')

function isLong(text) {
  return text && text.length > 30
}
function truncate(text) {
  if (!text) return '-'
  return text.length > 30 ? text.substring(0, 30) + '...' : text
}
function showDetail(title, content) {
  detailTitle.value = title
  detailContent.value = content
  detailVisible.value = true
}

onMounted(async () => {
  const res = await api.listExercises()
  exercises.value = res.data
  const tRes = await api.listQuestionTypes()
  questionTypes.value = tRes.data
})

function getTypeName(type) {
  const t = questionTypes.value.find(t => t.typeId === type)
  return t ? t.typeName : type
}

async function loadData() {
  const res = await api.listExercises()
  exercises.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { name: '', description: '' }
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

function parseOptions(optionsStr) {
  if (!optionsStr) return ['', '']
  return optionsStr.split(',').map(s => s.trim())
}

function showAddItemDialog() {
  editingItem.value = null
  itemForm.value = { question: '', optionList: ['', ''], answer: '', multiAnswer: [], type: 2 }
  addItemDialog.value = true
}

function editItem(row) {
  editingItem.value = row
  const parsed = {
    question: row.question || '',
    type: row.type,
    answer: row.answer || '',
    optionList: parseOptions(row.options),
    multiAnswer: row.type === 3 ? (row.answer || '').split('') : []
  }
  itemForm.value = parsed
  addItemDialog.value = true
}

async function handleDeleteItem(row) {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await api.deleteExerciseItem(currentExId.value, row.excerciseItemId)
  ElMessage.success('删除成功')
  const res = await api.getExerciseItems(currentExId.value)
  items.value = res.data
}

function onItemTypeChange(type) {
  if (type === 2 || type === 3) {
    if (!itemForm.value.optionList || itemForm.value.optionList.length < 2) {
      itemForm.value.optionList = ['', '']
    }
    if (type === 3) {
      itemForm.value.multiAnswer = []
    }
  }
}

async function saveItem() {
  const data = {
    question: itemForm.value.question,
    type: itemForm.value.type
  }
  if (itemForm.value.type === 2) {
    data.options = itemForm.value.optionList.filter(o => o.trim()).join(',')
    data.answer = itemForm.value.answer
  } else if (itemForm.value.type === 3) {
    data.options = itemForm.value.optionList.filter(o => o.trim()).join(',')
    data.answer = (itemForm.value.multiAnswer || []).sort().join('')
  } else {
    data.answer = itemForm.value.answer
  }

  if (editingItem.value) {
    await api.updateExerciseItem(currentExId.value, editingItem.value.excerciseItemId, data)
    ElMessage.success('题目已更新')
  } else {
    await api.addExerciseItem(currentExId.value, data)
    ElMessage.success('题目已添加')
  }
  addItemDialog.value = false
  editingItem.value = null
  resetItemForm()
  const res = await api.getExerciseItems(currentExId.value)
  items.value = res.data
}

function resetItemForm() {
  itemForm.value = { question: '', optionList: ['', ''], answer: '', multiAnswer: [], type: 2 }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该练习？', '提示', { type: 'warning' })
  await api.deleteExercise(id)
  ElMessage.success('删除成功')
  loadData()
}

function viewSubmissions(row) {
  router.push(`/teacher/exercise-grading/${row.id}`)
}
</script>
