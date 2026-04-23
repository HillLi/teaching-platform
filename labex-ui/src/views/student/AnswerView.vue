<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>答题</h2>
      <el-button @click="$router.back()">返回题目列表</el-button>
    </div>
    <el-card style="margin-bottom: 16px">
      <div v-html="item?.experimentItemContent || item?.experimentItemName || ''"></div>
    </el-card>
    <el-card>
      <h3 style="margin-top: 0">我的答案</h3>
      <el-alert v-if="graded" type="warning" :closable="false" style="margin-bottom: 12px">该题目已批改，不可修改</el-alert>
      <!-- 单选 type=2 -->
      <div v-if="itemType === 2 && options.length" style="margin-top: 8px">
        <el-radio-group v-model="answer" :disabled="graded">
          <el-radio v-for="(opt, oi) in options" :key="oi"
            :value="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-radio>
        </el-radio-group>
      </div>
      <!-- 多选 type=3 -->
      <div v-else-if="itemType === 3 && options.length" style="margin-top: 8px">
        <el-checkbox-group v-model="multiAnswer" :disabled="graded">
          <el-checkbox v-for="(opt, oi) in options" :key="oi"
            :label="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <!-- 判断 type=4 -->
      <div v-else-if="itemType === 4" style="margin-top: 8px">
        <el-radio-group v-model="answer" :disabled="graded">
          <el-radio value="T" style="margin-right: 20px">正确</el-radio>
          <el-radio value="F">错误</el-radio>
        </el-radio-group>
      </div>
      <!-- 编程 type=6 -->
      <CodeEditor v-else-if="itemType === 6" v-model="content" language="java" :height="350" :disabled="graded" />
      <!-- 简答 type=5 / 综合 type=7 -->
      <div v-else-if="itemType === 5 || itemType === 7" style="margin-top: 8px">
        <RichTextEditor v-model="content" :height="250" :disabled="graded" />
        <el-upload
          :action="uploadUrl"
          :limit="1"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :disabled="graded"
          style="margin-top: 12px"
        >
          <el-button size="small" type="primary" :disabled="graded">上传附件</el-button>
          <template #tip>
            <div class="el-upload__tip">支持上传文档作为答案</div>
          </template>
        </el-upload>
      </div>
      <!-- 填空 type=1 -->
      <div v-else-if="itemType === 1" style="margin-top: 8px">
        <div v-for="(_, idx) in fillBlanks" :key="idx" style="display: flex; align-items: center; margin-bottom: 8px">
          <span style="margin-right: 8px; white-space: nowrap">第 {{ idx + 1 }} 空：</span>
          <el-input v-model="fillBlanks[idx]" placeholder="请输入答案" :disabled="graded" style="width: 300px" />
        </div>
        <el-button size="small" @click="addBlank" :disabled="graded">增加空</el-button>
        <el-button size="small" type="danger" @click="removeBlank" :disabled="graded || fillBlanks.length <= 1">减少空</el-button>
      </div>
      <!-- 其他 -->
      <el-input v-else v-model="content" type="textarea" :rows="5" placeholder="请输入答案..." :disabled="graded" />
      <div style="margin-top: 16px; display: flex; justify-content: space-between; align-items: center">
        <span style="color: #999; font-size: 12px">上次保存: {{ lastSaveTime || '未保存' }}</span>
        <div>
          <el-button @click="saveAnswer" :disabled="graded">手动保存</el-button>
          <el-button type="primary" @click="saveAndBack" :disabled="graded">保存并返回</el-button>
          <el-button v-if="graded" @click="$router.back()">返回</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'
import CodeEditor from '../../components/CodeEditor.vue'
import RichTextEditor from '../../components/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()
const itemId = route.params.itemId
const item = ref(null)
const content = ref('')
const answer = ref('')
const multiAnswer = ref([])
const fillBlanks = reactive([''])
const graded = ref(false)
const lastSaveTime = ref('')
let autoSaveTimer = null

const itemType = computed(() => item.value?.experimentItemType)
const uploadUrl = `/api/student/items/${itemId}/upload`

const options = computed(() => {
  const raw = item.value?.experimentItemContent
  if (!raw || (itemType.value !== 2 && itemType.value !== 3)) return []
  return raw.includes('||') ? raw.split('||') : raw.split(',')
})

function addBlank() { fillBlanks.push('') }
function removeBlank() { if (fillBlanks.length > 1) fillBlanks.pop() }

function serializeAnswer() {
  if (itemType.value === 3) {
    return (multiAnswer.value || []).sort().join('')
  }
  if (itemType.value === 2 || itemType.value === 4) {
    return answer.value
  }
  if (itemType.value === 1) {
    return fillBlanks.join('|')
  }
  return content.value
}

function restoreAnswer(saved) {
  if (!saved) return
  if (itemType.value === 3) {
    multiAnswer.value = saved.split('')
  } else if (itemType.value === 2 || itemType.value === 4) {
    answer.value = saved
  } else if (itemType.value === 1) {
    const parts = saved.split('|')
    fillBlanks.length = 0
    parts.forEach(p => fillBlanks.push(p))
  } else {
    content.value = saved
  }
}

function onUploadSuccess() { ElMessage.success('文件上传成功') }
function onUploadError() { ElMessage.error('文件上传失败') }

onMounted(async () => {
  try {
    const res = await api.getItem(itemId)
    item.value = res.data.item
    const si = res.data.studentItem
    if (si && si.content) {
      restoreAnswer(si.content)
    }
    if (si && si.scoreFlag === 1) {
      graded.value = true
    }
  } catch (e) {
    ElMessage.error('加载题目失败')
  }

  autoSaveTimer = setInterval(() => {
    if (!graded.value && serializeAnswer()) {
      doSave()
    }
  }, 600000)
})

onUnmounted(() => {
  if (autoSaveTimer) clearInterval(autoSaveTimer)
})

async function doSave() {
  const val = serializeAnswer()
  if (val) {
    await api.saveAnswer(itemId, val)
    lastSaveTime.value = new Date().toLocaleTimeString()
  }
}

async function saveAnswer() {
  await doSave()
  ElMessage.success('已保存')
}

async function saveAndBack() {
  await doSave()
  ElMessage.success('已保存')
  router.back()
}
</script>
