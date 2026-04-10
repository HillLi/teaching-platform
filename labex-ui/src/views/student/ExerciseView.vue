<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>练习答题</h2>
      <el-button @click="$router.back()">返回练习列表</el-button>
    </div>
    <el-card v-for="(item, idx) in items" :key="item.id" style="margin-bottom: 16px">
      <h3 style="margin-top: 0">第 {{ idx + 1 }} 题</h3>
      <div v-html="item.question"></div>
      <div v-if="item.options" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.id]" v-if="item.type === 1">
          <el-radio v-for="(opt, oi) in item.options.split('||')" :key="oi" :value="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-radio>
        </el-radio-group>
      </div>
      <div v-if="item.type !== 1" style="margin-top: 8px">
        <el-input v-model="answers[item.id]" type="textarea" :rows="3" placeholder="请输入答案..." />
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

onMounted(async () => {
  const res = await api.getExerciseItems(exerciseId)
  items.value = res.data
})

async function submitAnswer(item) {
  const answer = answers[item.id]
  if (!answer) {
    ElMessage.warning('请先输入答案')
    return
  }
  await api.submitExerciseAnswer({
    itemId: item.id,
    type: item.type,
    answer: answer
  })
  ElMessage.success('答案已提交')
}
</script>
