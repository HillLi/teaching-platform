<template>
  <div>
    <h2>课程资料</h2>
    <el-table :data="lectures" border stripe style="margin-top: 16px">
      <el-table-column prop="lectureId" label="ID" width="80" />
      <el-table-column prop="lectureName" label="资料名称" />
      <el-table-column label="资料类型" width="100">
        <template #default="{ row }">{{ lectureTypes[row.lectureType] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="lectureFiletype" label="文件类型" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="downloadFile(row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/student'

const lectures = ref([])
const lectureTypes = { 1: '讲义', 2: '代码', 3: '软件', 4: '参考资料' }

onMounted(async () => {
  const res = await api.listLectures()
  lectures.value = res.data
})

async function downloadFile(row) {
  const url = api.downloadLecture(row.lectureId)
  const resp = await fetch(url)
  const blob = await resp.blob()
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${row.lectureName}.${row.lectureFiletype}`
  a.click()
  URL.revokeObjectURL(a.href)
}
</script>
