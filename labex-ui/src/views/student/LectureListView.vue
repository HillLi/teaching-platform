<template>
  <div>
    <h2>讲义下载</h2>
    <el-table :data="lectures" border stripe style="margin-top: 16px">
      <el-table-column prop="lectureId" label="ID" width="80" />
      <el-table-column prop="lectureName" label="讲义名称" />
      <el-table-column prop="lectureFiletype" label="文件类型" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary">
            <a :href="api.downloadLecture(row.lectureId)" style="color: inherit; text-decoration: none">下载</a>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api/student'

const lectures = ref([])

onMounted(async () => {
  const res = await api.listLectures()
  lectures.value = res.data
})
</script>
