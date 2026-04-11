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
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ getTypeName(row.type) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="addItemDialog" :title="editingItem ? '编辑题目' : '新增题目'" width="500">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="itemForm.type" style="width: 100%" @change="onItemTypeChange">
            <el-option v-for="t in questionTypes" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目"><el-input v-model="itemForm.question" type="textarea" :rows="2" /></el-form-item>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import api from '../../api/teacher'
import questionApi from '../../api/question'
import { ElMessage, ElMessageBox } from 'element-plus'

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

onMounted(async () => {
  const res = await api.listExercises()
  exercises.value = res.data
  const tRes = await questionApi.listTypes()
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
    // 单选：选项逗号拼接，答案为字母
    data.options = itemForm.value.optionList.filter(o => o.trim()).join(',')
    data.answer = itemForm.value.answer
  } else if (itemForm.value.type === 3) {
    // 多选：选项逗号拼接，答案为字母拼接
    data.options = itemForm.value.optionList.filter(o => o.trim()).join(',')
    data.answer = (itemForm.value.multiAnswer || []).sort().join('')
  } else if (itemForm.value.type === 4) {
    // 判断
    data.answer = itemForm.value.answer
  } else {
    // 填空/简答/编程/综合
    data.answer = itemForm.value.answer
  }
  await api.addExerciseItem(currentExId.value, data)
  ElMessage.success('题目已添加')
  addItemDialog.value = false
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
</script>
