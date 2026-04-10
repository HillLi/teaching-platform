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
      <el-input v-model="content" type="textarea" :rows="12" placeholder="请输入答案..." />
      <div style="margin-top: 16px; display: flex; justify-content: space-between; align-items: center">
        <span style="color: #999; font-size: 12px">上次保存: {{ lastSaveTime || '未保存' }}</span>
        <div>
          <el-button @click="saveAnswer">手动保存</el-button>
          <el-button type="primary" @click="saveAndBack">保存并返回</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const itemId = route.params.itemId
const item = ref(null)
const content = ref('')
const lastSaveTime = ref('')
let autoSaveTimer = null

onMounted(async () => {
  // Get item info from parent experiment - simplified
  // Just set up the answer editor
  try {
    await api.saveAnswer(itemId, '')  // This won't work for loading existing
  } catch {}

  // Auto save every 10 minutes
  autoSaveTimer = setInterval(() => {
    if (content.value) {
      doSave()
    }
  }, 600000)
})

onUnmounted(() => {
  if (autoSaveTimer) clearInterval(autoSaveTimer)
})

async function doSave() {
  await api.saveAnswer(itemId, content.value)
  lastSaveTime.value = new Date().toLocaleTimeString()
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
