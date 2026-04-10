<template>
  <div style="border: 1px solid #ccc">
    <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" style="border-bottom: 1px solid #ccc" />
    <Editor :defaultConfig="editorConfig" :modelValue="modelValue" :style="{ height: height + 'px', overflowY: 'hidden' }"
      @onCreated="handleCreated" @onChange="handleChange" />
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' },
  height: { type: Number, default: 300 }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = shallowRef(null)

const toolbarConfig = {}
const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {}
}

function handleCreated(editor) {
  editorRef.value = editor
}

function handleChange(editor) {
  emit('update:modelValue', editor.getHtml())
}

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})
</script>
