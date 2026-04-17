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
      <RichTextEditor v-else-if="itemType === 5 || itemType === 7" v-model="content" :height="350" :disabled="graded" />
      <!-- 填空/其他 -->
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
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
const graded = ref(false)
const lastSaveTime = ref('')
let autoSaveTimer = null

const itemType = computed(() => item.value?.experimentItemType)

const options = computed(() => {
  const raw = item.value?.experimentItemContent
  if (!raw || (itemType.value !== 2 && itemType.value !== 3)) return []
  return raw.includes('||') ? raw.split('||') : raw.split(',')
})

/** Serialize current answer state into a single string for saving */
function serializeAnswer() {
  if (itemType.value === 3) {
    return (multiAnswer.value || []).sort().join('')
  }
  if (itemType.value === 2 || itemType.value === 4) {
    return answer.value
  }
  return content.value
}

/** Restore saved content into the correct reactive variable */
function restoreAnswer(saved) {
  if (!saved) return
  if (itemType.value === 3) {
    multiAnswer.value = saved.split('')
  } else if (itemType.value === 2 || itemType.value === 4) {
    answer.value = saved
  } else {
    content.value = saved
  }
}

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
