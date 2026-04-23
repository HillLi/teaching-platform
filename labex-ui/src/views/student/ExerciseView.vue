<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>答题</h2>
      <el-button @click="$router.back()">返回作业列表</el-button>
    </div>
    <el-card v-for="(item, idx) in items" :key="item.excerciseItemId" style="margin-bottom: 16px">
      <h3 style="margin-top: 0">
        第 {{ idx + 1 }} 题
        <el-tag size="small" style="margin-left: 8px">{{ typeMap[item.type] || '未知' }}</el-tag>
        <el-tag v-if="answeredMap[item.excerciseItemId]" type="success" size="small" style="margin-left: 8px">已答</el-tag>
        <el-tag v-else type="info" size="small" style="margin-left: 8px">未答</el-tag>
      </h3>
      <div v-if="item.type === 1" style="line-height: 2.2; font-size: 15px">
        <template v-for="(segment, si) in getQuestionSegments(item)" :key="si">
          <span>{{ segment }}</span>
          <input v-if="si < getBlankCount(item)"
            v-model="getFillBlanks(item.excerciseItemId)[si]"
            class="inline-blank"
            :placeholder="'第' + (si+1) + '空'"
          />
        </template>
      </div>
      <div v-else v-html="item.question"></div>
      <!-- 单选 type=2 -->
      <div v-if="item.type === 2 && item.options" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.excerciseItemId]">
          <el-radio v-for="(opt, oi) in parseOptions(item.options)" :key="oi"
            :value="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-radio>
        </el-radio-group>
      </div>
      <!-- 多选 type=3 -->
      <div v-else-if="item.type === 3 && item.options" style="margin-top: 8px">
        <el-checkbox-group v-model="multiAnswers[item.excerciseItemId]">
          <el-checkbox v-for="(opt, oi) in parseOptions(item.options)" :key="oi"
            :label="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <!-- 判断 type=4 -->
      <div v-else-if="item.type === 4" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.excerciseItemId]">
          <el-radio value="T" style="margin-right: 20px">正确</el-radio>
          <el-radio value="F">错误</el-radio>
        </el-radio-group>
      </div>
      <!-- 编程 type=6 -->
      <div v-else-if="item.type === 6" style="margin-top: 8px">
        <CodeEditor v-model="answers[item.excerciseItemId]" language="java" :height="250" />
      </div>
      <!-- 简答 type=5 / 综合 type=7 -->
      <div v-else-if="item.type === 5 || item.type === 7" style="margin-top: 8px">
        <RichTextEditor v-model="answers[item.excerciseItemId]" :height="250" />
      </div>
      <!-- 填空 type=1: 答案已在题目区内联输入，无需额外输入框 -->
      <div v-else-if="item.type !== 1" style="margin-top: 8px">
        <el-input v-model="answers[item.excerciseItemId]" type="textarea" :rows="3" placeholder="请输入答案..." />
      </div>
      <div style="margin-top: 8px">
        <el-button type="primary" size="small" @click="submitAnswer(item)">提交答案</el-button>
        <el-button v-if="answeredMap[item.excerciseItemId]" type="success" size="small" @click="toggleAnswer(item.excerciseItemId)">
          {{ showAnswerMap[item.excerciseItemId] ? '隐藏答案' : '查看答案' }}
        </el-button>
      </div>
      <el-alert v-if="showAnswerMap[item.excerciseItemId]" type="success" :closable="false" style="margin-top: 8px">
        <template #title>
          <span>正确答案：{{ formatCorrectAnswer(item) }}</span>
        </template>
      </el-alert>
    </el-card>
    <el-empty v-if="items.length === 0" description="暂无题目" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'
import CodeEditor from '../../components/CodeEditor.vue'
import RichTextEditor from '../../components/RichTextEditor.vue'

const route = useRoute()
const exerciseId = route.params.id
const items = ref([])
const answers = reactive({})
const multiAnswers = reactive({})
const fillAnswers = reactive({})
const answeredMap = reactive({})
const showAnswerMap = reactive({})
const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }

function parseOptions(options) {
  if (!options) return []
  return options.includes('||') ? options.split('||') : options.split(',')
}

function getFillBlanks(itemId) {
  if (!fillAnswers[itemId]) {
    fillAnswers[itemId] = ['']
  }
  return fillAnswers[itemId]
}

function getQuestionSegments(item) {
  const text = item.question || ''
  if (!text || item.type !== 1) return [text || '']
  return text.split(/_{2,}/)
}

function getBlankCount(item) {
  const segs = getQuestionSegments(item)
  const count = Math.max(segs.length - 1, 1)
  const blanks = getFillBlanks(item.excerciseItemId)
  while (blanks.length < count) blanks.push('')
  return count
}

function toggleAnswer(id) {
  showAnswerMap[id] = !showAnswerMap[id]
}

function formatCorrectAnswer(item) {
  const ans = item.answer
  if (!ans) return '暂无'
  if (item.type === 4) return ans === 'T' ? '正确' : '错误'
  return ans
}

onMounted(async () => {
  const res = await api.getExerciseItems(exerciseId)
  items.value = res.data
  items.value.forEach(item => {
    const sa = item.studentAnswer
    answeredMap[item.excerciseItemId] = item.answered || false
    if (item.type === 3) {
      multiAnswers[item.excerciseItemId] = sa ? sa.split('') : []
    } else if (item.type === 1 && sa) {
      fillAnswers[item.excerciseItemId] = sa.split('|')
    } else if (sa) {
      answers[item.excerciseItemId] = sa
    }
  })
})

async function submitAnswer(item) {
  let answer
  if (item.type === 3) {
    answer = (multiAnswers[item.excerciseItemId] || []).sort().join('')
  } else if (item.type === 1) {
    answer = (fillAnswers[item.excerciseItemId] || []).join('|')
  } else {
    answer = answers[item.excerciseItemId]
  }
  if (!answer) {
    ElMessage.warning('请先输入答案')
    return
  }
  await api.submitExerciseAnswer({
    itemId: item.excerciseItemId,
    type: item.type,
    answer: answer
  })
  answeredMap[item.excerciseItemId] = true
  ElMessage.success('答案已提交')
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
