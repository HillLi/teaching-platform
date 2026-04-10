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
      <RichTextEditor v-if="!isCodeType" v-model="content" :height="350" />
      <CodeEditor v-else v-model="content" language="java" :height="350" />
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'
import RichTextEditor from '../../components/RichTextEditor.vue'
import CodeEditor from '../../components/CodeEditor.vue'

const route = useRoute()
const router = useRouter()
const itemId = route.params.itemId
const item = ref(null)
const content = ref('')
const lastSaveTime = ref('')
let autoSaveTimer = null

const isCodeType = computed(() => {
  return item.value?.experimentItemType === 2
})

onMounted(async () => {
  try {
    const res = await api.getItem(itemId)
    item.value = res.data.item
    if (res.data.studentItem && res.data.studentItem.content) {
      content.value = res.data.studentItem.content
    }
  } catch (e) {
    ElMessage.error('加载题目失败')
  }

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
