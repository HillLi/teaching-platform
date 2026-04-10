<template>
  <div>
    <h2>系统日志</h2>
    <div style="display: flex; gap: 12px; margin: 16px 0">
      <el-input v-model="account" placeholder="搜索账号" clearable style="width: 200px" />
      <el-select v-model="type" placeholder="日志类型" clearable style="width: 150px">
        <el-option label="登录成功" :value="1" />
        <el-option label="登录失败" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>
    <el-table :data="logs.records" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="account" label="账号" width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'success' : 'danger'">{{ row.type === 1 ? '登录成功' : '登录失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="info" label="信息" />
      <el-table-column prop="ip" label="IP" width="150" />
      <el-table-column prop="time" label="时间" width="180" />
    </el-table>
    <el-pagination v-if="logs.total > 20" :total="logs.total" :page-size="20"
      @current-change="loadData" layout="prev, pager, next" style="margin-top: 16px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/teacher'

const logs = ref({ records: [], total: 0 })
const account = ref('')
const type = ref(null)
let pageNum = 1

onMounted(loadData)

async function loadData(page = 1) {
  pageNum = page
  const res = await api.listLogs({
    pageNum,
    pageSize: 20,
    account: account.value || undefined,
    type: type.value ?? undefined
  })
  logs.value = res.data
}
</script>
