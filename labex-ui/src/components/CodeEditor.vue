<template>
  <MonacoEditor
    :value="modelValue"
    :language="language"
    :theme="theme"
    :height="height + 'px'"
    :options="editorOptions"
    @change="handleChange"
  />
</template>

<script setup>
import { computed } from 'vue'
import MonacoEditor from 'monaco-editor-vue3'

const props = defineProps({
  modelValue: { type: String, default: '' },
  language: { type: String, default: 'java' },
  theme: { type: String, default: 'vs-dark' },
  height: { type: Number, default: 400 },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const editorOptions = computed(() => ({
  minimap: { enabled: false },
  fontSize: 14,
  wordWrap: 'on',
  readOnly: props.readOnly,
  automaticLayout: true
}))

function handleChange(value) {
  emit('update:modelValue', value)
}
</script>
