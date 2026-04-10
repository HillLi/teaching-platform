<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>实验管理</h2>
      <el-button type="primary" @click="showAddDialog">新增实验</el-button>
    </div>
    <el-table :data="experiments" border stripe>
      <el-table-column prop="experimentNo" label="编号" width="80" />
      <el-table-column prop="experimentName" label="实验名称" />
      <el-table-column prop="experimentType" label="类型" width="100" />
      <el-table-column prop="instructionType" label="指导书类型" width="120" />
      <el-table-column prop="state" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.state === 1 ? 'success' : 'danger'">{{ row.state === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="manageItems(row)">题目管理</el-button>
          <el-button size="small" @click="editExperiment(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.experimentId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑实验' : '新增实验'" width="500">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实验编号">
          <el-input-number v-model="form.experimentNo" :min="1" />
        </el-form-item>
        <el-form-item label="实验名称">
          <el-input v-model="form.experimentName" />
        </el-form-item>
        <el-form-item label="实验类型">
          <el-input-number v-model="form.experimentType" :min="1" />
        </el-form-item>
        <el-form-item label="指导书类型">
          <el-input v-model="form.instructionType" />
        </el-form-item>
        <el-form-item label="实验要求">
          <el-input v-model="form.experimentRequirement" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const experiments = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ experimentNo: 1, experimentName: '', experimentType: 1, instructionType: 'html', experimentRequirement: '' })

onMounted(loadData)

async function loadData() {
  const res = await api.listExperiments()
  experiments.value = res.data
}

function showAddDialog() {
  isEdit.value = false
  form.value = { experimentNo: experiments.value.length + 1, experimentName: '', experimentType: 1, instructionType: 'html', experimentRequirement: '' }
  dialogVisible.value = true
}

function editExperiment(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

function manageItems(row) {
  router.push(`/teacher/experiments/${row.experimentId}/items`)
}

async function handleSave() {
  if (isEdit.value) {
    await api.updateExperiment(form.value.experimentId, form.value)
  } else {
    await api.addExperiment(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该实验及其所有题目？', '提示', { type: 'warning' })
  await api.deleteExperiment(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
