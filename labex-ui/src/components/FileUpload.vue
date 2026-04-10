<template>
  <el-upload
    :action="action"
    :accept="accept"
    :limit="limit"
    :auto-upload="autoUpload"
    :on-success="handleSuccess"
    :on-error="handleError"
    :before-upload="handleBeforeUpload"
    :file-list="fileList"
    v-bind="$attrs"
  >
    <slot>
      <el-button type="primary">点击上传</el-button>
    </slot>
    <template #tip>
      <div class="el-upload__tip">
        <slot name="tip" />
      </div>
    </template>
  </el-upload>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  action: { type: String, default: '' },
  accept: { type: String, default: '' },
  limit: { type: Number, default: 1 },
  autoUpload: { type: Boolean, default: true },
  maxSize: { type: Number, default: 100 }
})

const emit = defineEmits(['success', 'error'])

const fileList = ref([])

function handleBeforeUpload(file) {
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

function handleSuccess(response) {
  emit('success', response)
}

function handleError(error) {
  ElMessage.error('上传失败')
  emit('error', error)
}
</script>
