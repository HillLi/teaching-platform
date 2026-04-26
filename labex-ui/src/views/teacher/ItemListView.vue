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
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          {{ getTypeName(row.experimentItemType) }}
        </template>
      </el-table-column>
      <el-table-column prop="experimentItemScore" label="分值" width="80" />
      <el-table-column prop="experimentItemAnswer" label="参考答案" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editItem(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.experimentItemId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="550">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题号">
          <el-input-number v-model="form.experimentItemNo" :min="1" />
        </el-form-item>
        <el-form-item label="题目">
          <el-input v-model="form.experimentItemName" type="textarea" :rows="2" :placeholder="form.experimentItemType === 1 ? '填空题用 ____ 标记空白处' : ''" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.experimentItemType" style="width: 100%" @change="onTypeChange">
            <el-option v-for="t in questionTypes" :key="t.typeId" :label="t.typeName" :value="t.typeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.experimentItemScore" :min="1" :max="100" />
        </el-form-item>
        <!-- 单选/多选：动态选项列表 -->
        <template v-if="form.experimentItemType === 2 || form.experimentItemType === 3">
          <el-form-item label="选项">
            <div style="width: 100%">
              <div v-for="(opt, idx) in form.optionList" :key="idx" style="display: flex; margin-bottom: 8px; align-items: center">
                <span style="width: 30px; flex-shrink: 0; font-weight: bold">{{ String.fromCharCode(65 + idx) }}</span>
                <el-input v-model="form.optionList[idx]" style="flex: 1" :placeholder="'选项 ' + String.fromCharCode(65 + idx)" />
                <el-button v-if="form.optionList.length > 2" size="small" type="danger" :icon="Delete"
                  @click="form.optionList.splice(idx, 1)" style="margin-left: 8px" />
              </div>
              <el-button size="small" @click="form.optionList.push('')" :disabled="form.optionList.length >= 8">
                添加选项
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="正确答案">
            <div v-if="form.experimentItemType === 2">
              <el-radio-group v-model="form.answer">
                <el-radio v-for="(opt, idx) in form.optionList" :key="idx"
                  :value="String.fromCharCode(65 + idx)" style="margin-bottom: 8px">
                  {{ String.fromCharCode(65 + idx) }}. {{ opt || '(空)' }}
                </el-radio>
              </el-radio-group>
            </div>
            <div v-else>
              <el-checkbox-group v-model="form.multiAnswer">
                <el-checkbox v-for="(opt, idx) in form.optionList" :key="idx"
                  :label="String.fromCharCode(65 + idx)" style="margin-bottom: 8px">
                  {{ String.fromCharCode(65 + idx) }}. {{ opt || '(空)' }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </el-form-item>
        </template>
        <!-- 判断题 -->
        <el-form-item v-else-if="form.experimentItemType === 4" label="正确答案">
          <el-radio-group v-model="form.answer">
            <el-radio value="T">正确</el-radio>
            <el-radio value="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 填空 -->
        <template v-else-if="form.experimentItemType === 1">
          <el-form-item label="题目预览">
            <div style="line-height: 2.2; font-size: 15px; padding: 8px 0">
              <template v-for="(seg, si) in fillSegments" :key="si">
                <span>{{ seg }}</span>
                <span v-if="si < fillSegments.length - 1" style="border-bottom: 2px solid #409eff; padding: 0 20px; margin: 0 4px; color: #999; font-size: 12px">第{{ si + 1 }}空</span>
              </template>
            </div>
          </el-form-item>
          <el-form-item label="参考答案">
            <div style="width: 100%">
              <div v-for="(_, idx) in fillAnswers" :key="idx" style="display: flex; align-items: center; margin-bottom: 8px">
                <span style="width: 60px; flex-shrink: 0; color: #409eff">第 {{ idx + 1 }} 空：</span>
                <el-input v-model="fillAnswers[idx]" placeholder="输入该空的答案" style="flex: 1" />
                <el-button v-if="fillAnswers.length > 1" size="small" type="danger" @click="fillAnswers.splice(idx, 1)" style="margin-left: 8px">删除</el-button>
              </div>
              <el-button size="small" @click="fillAnswers.push('')">增加空</el-button>
            </div>
          </el-form-item>
        </template>
        <!-- 简答/编程/综合 -->
        <el-form-item v-else-if="form.experimentItemType" label="参考答案">
          <el-input v-model="form.answer" type="textarea" :rows="2" />
        </el-form-item>
        <!-- 附件上传 -->
        <el-form-item v-if="form.experimentItemType === 5 || form.experimentItemType === 7" label="题目附件">
          <el-upload
            :action="`/api/teacher/experiments/items/${form.experimentItemId || 0}/attachment`"
            :limit="1"
            :on-success="onAttachmentSuccess"
            :on-error="onAttachmentError"
          >
            <el-button size="small" type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">综合题可上传附件材料</div>
            </template>
          </el-upload>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const expId = route.params.id
const items = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const questionTypes = ref([])
const fillAnswers = reactive([''])

const fillSegments = computed(() => {
  const text = form.value.experimentItemName || ''
  if (!text) return ['']
  return text.split(/_+/)
})

onMounted(async () => {
  const res = await api.listQuestionTypes()
  questionTypes.value = res.data
  loadData()
})

function getTypeName(type) {
  const t = questionTypes.value.find(t => t.typeId === type)
  return t ? t.typeName : type
}

async function loadData() {
  const res = await api.listItems(expId)
  items.value = res.data
}

function parseOptions(optionsStr) {
  if (!optionsStr) return ['', '']
  return optionsStr.split(',').map(s => s.trim())
}

function parseAnswerForMulti(answerStr) {
  if (!answerStr) return []
  return answerStr.split('')
}

function showAddDialog() {
  isEdit.value = false
  form.value = {
    experimentItemNo: items.value.length + 1,
    experimentItemName: '',
    experimentItemType: 1,
    experimentItemScore: 10,
    experimentItemContent: '',
    optionList: ['', ''],
    answer: '',
    multiAnswer: []
  }
  fillAnswers.length = 0
  fillAnswers.push('')
  dialogVisible.value = true
}

function editItem(row) {
  isEdit.value = true
  const parsed = {
    ...row,
    optionList: parseOptions(row.experimentItemContent),
    answer: row.experimentItemAnswer || '',
    multiAnswer: (row.experimentItemType === 3) ? parseAnswerForMulti(row.experimentItemAnswer) : []
  }
  form.value = parsed
  fillAnswers.length = 0
  if (row.experimentItemType === 1 && row.experimentItemAnswer) {
    row.experimentItemAnswer.split('|').forEach(p => fillAnswers.push(p))
  } else {
    fillAnswers.push('')
  }
  dialogVisible.value = true
}

function onTypeChange(type) {
  if (type === 2 || type === 3) {
    if (!form.value.optionList || form.value.optionList.length < 2) {
      form.value.optionList = ['', '']
    }
    if (type === 3) {
      form.value.multiAnswer = []
    }
  }
  form.value.answer = ''
}

async function handleSave() {
  const data = {
    experimentItemNo: form.value.experimentItemNo,
    experimentItemName: form.value.experimentItemName,
    experimentItemType: form.value.experimentItemType,
    experimentItemScore: form.value.experimentItemScore
  }

  if (form.value.experimentItemType === 2) {
    data.experimentItemContent = form.value.optionList.filter(o => o.trim()).join(',')
    data.experimentItemAnswer = form.value.answer
  } else if (form.value.experimentItemType === 3) {
    data.experimentItemContent = form.value.optionList.filter(o => o.trim()).join(',')
    data.experimentItemAnswer = (form.value.multiAnswer || []).sort().join('')
  } else if (form.value.experimentItemType === 1) {
    data.experimentItemContent = form.value.experimentItemContent || ''
    data.experimentItemAnswer = fillAnswers.join('|')
  } else {
    data.experimentItemContent = form.value.experimentItemContent || ''
    data.experimentItemAnswer = form.value.answer || ''
  }

  if (isEdit.value) {
    data.experimentItemId = form.value.experimentItemId
    await api.updateItem(form.value.experimentItemId, data)
  } else {
    await api.addItem(expId, data)
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

function onAttachmentSuccess() { ElMessage.success('附件上传成功'); loadData() }
function onAttachmentError() { ElMessage.error('附件上传失败') }
</script>
