<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>{{ experiment?.experimentName }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <div v-if="experiment?.experimentRequirement" style="margin-bottom: 16px">
      <h3>实验要求</h3>
      <div v-html="experiment.experimentRequirement"></div>
    </div>
    <el-table :data="items" border stripe>
      <el-table-column prop="experimentItemNo" label="题号" width="80" />
      <el-table-column prop="experimentItemName" label="题目名" width="200" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          {{ typeName(row.experimentItemType) }}
        </template>
      </el-table-column>
      <el-table-column prop="experimentItemScore" label="分值" width="80" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="hasAnswer(row) ? 'success' : 'info'">
            {{ hasAnswer(row) ? '已作答' : '未作答' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="goAnswer(row.experimentItemId)">作答</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../../api/student'

const route = useRoute()
const router = useRouter()
const experiment = ref(null)
const items = ref([])

// We'll track answered items via a separate mechanism
const answeredItems = ref(new Set())

onMounted(async () => {
  const [expRes, itemsRes] = await Promise.all([
    api.getExperiment(route.params.id),
    api.getExperimentItems(route.params.id)
  ])
  experiment.value = expRes.data
  items.value = itemsRes.data
  // Populate answered status from backend
  const set = new Set()
  items.value.forEach(item => {
    if (item.answered) set.add(item.experimentItemId)
  })
  answeredItems.value = set
})

function hasAnswer(row) {
  return answeredItems.value.has(row.experimentItemId)
}

const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }
function typeName(type) {
  return typeMap[type] || type
}

function goAnswer(itemId) {
  router.push(`/student/answer/${itemId}`)
}
</script>
