<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px; align-items: center">
      <h2>考试答题</h2>
      <div style="display: flex; align-items: center; gap: 16px">
        <el-tag type="danger" size="large" v-if="remaining >= 0">剩余时间：{{ formatCountdown(remaining) }}</el-tag>
        <el-button @click="$router.back()">退出考试</el-button>
      </div>
    </div>

    <el-card v-for="(item, idx) in items" :key="item.id" style="margin-bottom: 16px">
      <h3 style="margin-top: 0">
        第 {{ idx + 1 }} 题
        <el-tag size="small" style="margin-left: 8px">{{ typeMap[item.type] || '未知' }}</el-tag>
        <span style="margin-left: 8px; color: #999; font-size: 12px">（{{ item.score }}分）</span>
      </h3>
      <div v-html="item.content"></div>
      <!-- 单选 type=2 -->
      <div v-if="item.type === 2 && item.options" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.id]">
          <el-radio v-for="(opt, oi) in parseOptions(item.options)" :key="oi"
            :value="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-radio>
        </el-radio-group>
      </div>
      <!-- 多选 type=3 -->
      <div v-else-if="item.type === 3 && item.options" style="margin-top: 8px">
        <el-checkbox-group v-model="multiAnswers[item.id]">
          <el-checkbox v-for="(opt, oi) in parseOptions(item.options)" :key="oi"
            :label="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <!-- 判断 type=4 -->
      <div v-else-if="item.type === 4" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.id]">
          <el-radio value="T" style="margin-right: 20px">正确</el-radio>
          <el-radio value="F">错误</el-radio>
        </el-radio-group>
      </div>
      <!-- 编程 type=6 -->
      <div v-else-if="item.type === 6" style="margin-top: 8px">
        <CodeEditor v-model="answers[item.id]" language="java" :height="250" />
      </div>
      <!-- 简答 type=5 / 综合 type=7 -->
      <div v-else-if="item.type === 5 || item.type === 7" style="margin-top: 8px">
        <RichTextEditor v-model="answers[item.id]" :height="200" />
      </div>
      <!-- 填空 type=1 -->
      <div v-else-if="item.type === 1" style="margin-top: 8px; line-height: 2.2; font-size: 15px">
        <template v-for="(segment, si) in getQuestionSegments(item)" :key="si">
          <span>{{ segment }}</span>
          <input v-if="si < getBlankCount(item)"
            v-model="getFillBlanks(item.id)[si]"
            class="inline-blank"
            :placeholder="'第' + (si+1) + '空'"
          />
        </template>
      </div>
      <!-- 其他 -->
      <div v-else style="margin-top: 8px">
        <el-input v-model="answers[item.id]" type="textarea" :rows="3" placeholder="请输入答案..." />
      </div>
    </el-card>

    <div style="text-align: center; margin-top: 24px">
      <el-button type="primary" size="large" @click="submitExam" :disabled="submitted">提交试卷</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import CodeEditor from '../../components/CodeEditor.vue'
import RichTextEditor from '../../components/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()
const examId = route.params.id
const items = ref([])
const answers = reactive({})
const multiAnswers = reactive({})
const fillAnswers = reactive({})
const submitted = ref(false)
const remaining = ref(0)
const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }
let timer = null

function parseOptions(options) {
  if (!options) return []
  return options.includes('||') ? options.split('||') : options.split(',')
}

function getFillBlanks(itemId) {
  if (!fillAnswers[itemId]) fillAnswers[itemId] = ['']
  return fillAnswers[itemId]
}

function getQuestionSegments(item) {
  const text = item.content || ''
  if (!text || item.type !== 1) return [text || '']
  return text.split(/_{2,}/)
}

function getBlankCount(item) {
  const segs = getQuestionSegments(item)
  const count = Math.max(segs.length - 1, 1)
  const blanks = getFillBlanks(item.id)
  while (blanks.length < count) blanks.push('')
  return count
}

function formatCountdown(seconds) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

onMounted(async () => {
  try {
    const res = await api.getExamItems(examId)
    items.value = res.data
    // Start countdown from duration
    const exam = items.value.length > 0 ? null : null
    // Try to start exam to get submission info
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载考试失败')
    router.back()
  }

  // Use exam info for countdown - get from list
  try {
    const examsRes = await api.listExams()
    const exam = examsRes.data.find(e => e.id === Number(examId))
    if (exam && exam.duration) {
      remaining.value = exam.duration * 60
      timer = setInterval(() => {
        remaining.value--
        if (remaining.value <= 0) {
          clearInterval(timer)
          ElMessage.warning('考试时间到，自动提交')
          submitExam()
        }
      }, 1000)
    }
  } catch (e) { /* ignore */ }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function submitExam() {
  if (submitted.value) return
  try {
    await ElMessageBox.confirm('确定提交试卷？提交后不可修改。', '确认提交', { type: 'warning' })
  } catch { return }

  const answersList = items.value.map(item => {
    let answer = ''
    if (item.type === 3) {
      answer = (multiAnswers[item.id] || []).sort().join('')
    } else if (item.type === 1) {
      answer = (fillAnswers[item.id] || []).join('|')
    } else {
      answer = answers[item.id] || ''
    }
    return { examItemId: item.id, answer }
  })

  try {
    await api.submitExam(examId, { answers: answersList })
    submitted.value = true
    ElMessage.success('试卷已提交')
    router.push(`/student/exams/${examId}/score`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  }
}
</script>

<style scoped>
.inline-blank {
  border: none;
  border-bottom: 2px solid #409eff;
  outline: none;
  width: 120px;
  padding: 0 4px;
  font-size: 15px;
  text-align: center;
  background: transparent;
  color: #303133;
  transition: border-color 0.3s;
}
.inline-blank:focus {
  border-bottom-color: #66b1ff;
}
.inline-blank::placeholder {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
