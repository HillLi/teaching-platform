<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>练习答题</h2>
      <el-button @click="$router.back()">返回练习列表</el-button>
    </div>
    <el-card v-for="(item, idx) in items" :key="item.excerciseItemId" style="margin-bottom: 16px">
      <h3 style="margin-top: 0">第 {{ idx + 1 }} 题</h3>
      <div v-html="item.question"></div>
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
      <!-- 填空/简答/编程/综合 -->
      <div v-else style="margin-top: 8px">
        <el-input v-model="answers[item.excerciseItemId]" type="textarea" :rows="3" placeholder="请输入答案..." />
      </div>
      <div style="margin-top: 8px">
        <el-button type="primary" size="small" @click="submitAnswer(item)">提交答案</el-button>
      </div>
    </el-card>
    <el-empty v-if="items.length === 0" description="暂无题目" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const exerciseId = route.params.id
const items = ref([])
const answers = reactive({})
const multiAnswers = reactive({})

function parseOptions(options) {
  if (!options) return []
  return options.includes('||') ? options.split('||') : options.split(',')
}

onMounted(async () => {
  const res = await api.getExerciseItems(exerciseId)
  items.value = res.data
  // 初始化多选答案数组
  items.value.forEach(item => {
    if (item.type === 3) {
      multiAnswers[item.excerciseItemId] = []
    }
  })
})

async function submitAnswer(item) {
  let answer
  if (item.type === 3) {
    answer = (multiAnswers[item.excerciseItemId] || []).sort().join('')
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
  ElMessage.success('答案已提交')
}
</script>
