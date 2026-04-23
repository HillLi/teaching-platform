<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>考试管理</h2>
      <el-button type="primary" @click="showAddDialog">新增考试</el-button>
    </div>
    <el-table :data="exams" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="考试名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="120" />
      <el-table-column label="时长(分钟)" width="100" prop="duration" />
      <el-table-column label="开始时间" width="160">
        <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="160">
        <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="viewItems(row)">题目</el-button>
          <el-button size="small" @click="editExam(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          <el-button size="small" type="warning" @click="viewGrading(row)">批改</el-button>
          <el-button size="small" type="success" @click="viewScores(row)">成绩</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑考试对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试' : '新增考试'" width="500">
      <el-form :model="form" label-width="100px">
        <el-form-item label="考试名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.duration" :min="1" :max="300" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 题目管理对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="'考试题目 - ' + currentExamName" width="700">
      <el-button size="small" type="primary" @click="showAddItemDialog" style="margin-bottom: 10px">新增题目</el-button>
      <el-table :data="examItems" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题目" min-width="150">
          <template #default="{ row }">
            {{ truncate(row.content) }}
            <el-button v-if="isLong(row.content)" type="primary" link size="small" @click="showDetail('题目', row.content)">详情</el-button>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ typeMap[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="70" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="editExamItem(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteExamItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑题目对话框 -->
    <el-dialog v-model="addItemDialog" :title="editingItem ? '编辑题目' : '新增题目'" width="500">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="itemForm.type" style="width: 100%">
            <el-option v-for="(name, id) in typeMap" :key="id" :label="name" :value="Number(id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目">
          <el-input v-model="itemForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项" v-if="itemForm.type === 2 || itemForm.type === 3">
          <div style="width: 100%">
            <div v-for="(opt, idx) in itemForm.optionList" :key="idx" style="display: flex; margin-bottom: 8px; align-items: center">
              <span style="width: 30px; flex-shrink: 0; font-weight: bold">{{ String.fromCharCode(65 + idx) }}</span>
              <el-input v-model="itemForm.optionList[idx]" style="flex: 1" />
              <el-button v-if="itemForm.optionList.length > 2" size="small" type="danger" @click="itemForm.optionList.splice(idx, 1)" style="margin-left: 8px" />
            </div>
            <el-button size="small" @click="itemForm.optionList.push('')" :disabled="itemForm.optionList.length >= 8">添加选项</el-button>
          </div>
        </el-form-item>
        <el-form-item label="正确答案" v-if="itemForm.type === 2">
          <el-radio-group v-model="itemForm.answer">
            <el-radio v-for="(_, idx) in itemForm.optionList" :key="idx" :value="String.fromCharCode(65 + idx)">
              {{ String.fromCharCode(65 + idx) }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正确答案" v-else-if="itemForm.type === 3">
          <el-checkbox-group v-model="itemForm.multiAnswer">
            <el-checkbox v-for="(_, idx) in itemForm.optionList" :key="idx" :label="String.fromCharCode(65 + idx)">
              {{ String.fromCharCode(65 + idx) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="正确答案" v-else-if="itemForm.type === 4">
          <el-radio-group v-model="itemForm.answer">
            <el-radio value="T">正确</el-radio>
            <el-radio value="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答案" v-else-if="itemForm.type">
          <el-input v-model="itemForm.answer" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="itemForm.score" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addItemDialog = false">取消</el-button>
        <el-button type="primary" @click="saveExamItem">保存</el-button>
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
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const exams = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ name: '', description: '', duration: 60, startTime: '', endTime: '' })
const itemDialogVisible = ref(false)
const addItemDialog = ref(false)
const editingItem = ref(null)
const examItems = ref([])
const currentExamId = ref(null)
const currentExamName = ref('')
const itemForm = ref({ content: '', type: 2, options: '', answer: '', score: 10, optionList: ['', ''], multiAnswer: [] })
const detailVisible = ref(false)
const detailTitle = ref('')
const detailContent = ref('')
const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }

function isLong(text) { return text && text.length > 30 }
function truncate(text) { return !text ? '-' : text.length > 30 ? text.substring(0, 30) + '...' : text }
function showDetail(title, content) { detailTitle.value = title; detailContent.value = content; detailVisible.value = true }
function formatTime(t) { return t ? t.replace('T', ' ') : '-' }

onMounted(() => { loadData() })

async function loadData() {
  const res = await api.listExams()
  exams.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { name: '', description: '', duration: 60, startTime: '', endTime: '' }
  dialogVisible.value = true
}

function editExam(row) {
  isEdit.value = true
  form.value = { ...row, startTime: row.startTime ? row.startTime.replace(' ', 'T') : '', endTime: row.endTime ? row.endTime.replace(' ', 'T') : '' }
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateExam(form.value.id, form.value)
  } else {
    await api.addExam(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该考试？', '提示', { type: 'warning' })
  await api.deleteExam(id)
  ElMessage.success('删除成功')
  loadData()
}

async function viewItems(row) {
  currentExamId.value = row.id
  currentExamName.value = row.name
  const res = await api.getExamItems(row.id)
  examItems.value = res.data
  itemDialogVisible.value = true
}

function showAddItemDialog() {
  editingItem.value = null
  itemForm.value = { content: '', type: 2, options: '', answer: '', score: 10, optionList: ['', ''], multiAnswer: [] }
  addItemDialog.value = true
}

function editExamItem(row) {
  editingItem.value = row
  const opts = row.options ? row.options.split(',').map(s => s.trim()) : ['', '']
  itemForm.value = {
    content: row.content || '',
    type: row.type,
    options: row.options || '',
    answer: row.answer || '',
    score: row.score || 10,
    optionList: opts,
    multiAnswer: row.type === 3 ? (row.answer || '').split('') : []
  }
  addItemDialog.value = true
}

async function deleteExamItem(row) {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await api.deleteExamItem(row.id)
  ElMessage.success('删除成功')
  const res = await api.getExamItems(currentExamId.value)
  examItems.value = res.data
}

async function saveExamItem() {
  const data = { content: itemForm.value.content, type: itemForm.value.type, score: itemForm.value.score }
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
    await api.updateExamItem(editingItem.value.id, data)
    ElMessage.success('题目已更新')
  } else {
    await api.addExamItem(currentExamId.value, data)
    ElMessage.success('题目已添加')
  }
  addItemDialog.value = false
  const res = await api.getExamItems(currentExamId.value)
  examItems.value = res.data
}

function viewGrading(row) { router.push(`/teacher/exam-grading/${row.id}`) }
function viewScores(row) { router.push(`/teacher/exam-scores/${row.id}`) }
</script>
