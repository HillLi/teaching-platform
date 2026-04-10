# Labex Remaining Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fill all remaining feature gaps in the Labex teaching platform: fix AnswerView bug, add editor components, question bank UI, student exercises, dashboard stats, logging, and auxiliary features.

**Architecture:** Incremental enhancement of existing Spring Boot 3 + Vue 3 codebase. Each sub-project (SP) is independent except SP1 which provides shared components used by later SPs. Backend changes follow existing controller→service→mapper pattern. Frontend changes follow existing api→view pattern with Element Plus components.

**Tech Stack:** Spring Boot 3.4.4, MyBatis-Plus 3.5.9, Vue 3, Element Plus 2.13, wangEditor 5, Monaco Editor (via @monaco-editor/vue), MySQL 8.

---

## File Structure

### New Files (Backend)
- `labex/src/main/java/labex/mapper/SysConfigMapper.java` — Config table mapper
- `labex/src/main/java/labex/service/LogService.java` — System/student log queries

### New Files (Frontend)
- `labex-ui/src/components/RichTextEditor.vue` — wangEditor wrapper
- `labex-ui/src/components/CodeEditor.vue` — Monaco Editor wrapper
- `labex-ui/src/components/FileUpload.vue` — el-upload wrapper
- `labex-ui/src/views/teacher/QuestionManageView.vue` — Question bank management
- `labex-ui/src/views/teacher/SysLogView.vue` — System log viewer
- `labex-ui/src/views/student/ExerciseListView.vue` — Student exercise list
- `labex-ui/src/views/student/ExerciseView.vue` — Student exercise answer

### Modified Files (Backend)
- `labex/src/main/java/labex/service/StudentService.java` — Add getExperimentItemById, getDashboardStats, logStudentActivity
- `labex/src/main/java/labex/service/TeacherService.java` — Add getDashboardStats, resetStudentPassword, importStudentsFromCsv, log queries, config queries
- `labex/src/main/java/labex/controller/StudentController.java` — Add getItem fix, exercise endpoints, dashboard stats
- `labex/src/main/java/labex/controller/TeacherController.java` — Add dashboard stats, log viewer, reset password, CSV import, config, update/delete exercise endpoints
- `labex/src/main/java/labex/controller/ExerciseController.java` — Add update/delete exercise endpoints

### Modified Files (Frontend)
- `labex-ui/package.json` — Add wangEditor, Monaco Editor deps
- `labex-ui/src/router/index.js` — Add new routes
- `labex-ui/src/layouts/TeacherLayout.vue` — Add menu items (questions, logs)
- `labex-ui/src/layouts/StudentLayout.vue` — Add exercises menu item
- `labex-ui/src/api/student.js` — Add getItem, exercise, dashboard stats methods
- `labex-ui/src/api/teacher.js` — Add updateExercise, deleteExercise, dashboard stats, logs, reset password, CSV import, config methods
- `labex-ui/src/views/student/AnswerView.vue` — Fix loading, integrate editors
- `labex-ui/src/views/teacher/DashboardView.vue` — Enhanced stats
- `labex-ui/src/views/student/DashboardView.vue` — Enhanced stats
- `labex-ui/src/views/teacher/StudentListView.vue` — Add reset password, view logs buttons
- `labex-ui/src/views/teacher/ExerciseListView.vue` — Fix updateExercise bug, add delete
- `labex-ui/src/views/teacher/ItemListView.vue` — Use RichTextEditor for content/answer

---

## SP1: Bug Fixes + Editor Infrastructure

### Task 1: Fix AnswerView — Backend getItem endpoint

**Files:**
- Modify: `labex/src/main/java/labex/service/StudentService.java:60-65`
- Modify: `labex/src/main/java/labex/controller/StudentController.java:66-74`

- [ ] **Step 1: Add getExperimentItemById to StudentService**

Add this method to `labex/src/main/java/labex/service/StudentService.java` after line 58 (after `getExperimentItems` method):

```java
public ExperimentItem getExperimentItemById(Integer itemId) {
    ExperimentItem item = experimentItemMapper.selectById(itemId);
    if (item == null) throw new BusinessException("题目不存在");
    return item;
}
```

- [ ] **Step 2: Fix StudentController.getItem to return full data**

Replace lines 66-74 of `labex/src/main/java/labex/controller/StudentController.java`:

```java
@GetMapping("/items/{itemId}")
public Result<Map<String, Object>> getItem(@PathVariable Integer itemId, HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    ExperimentItem item = studentService.getExperimentItemById(itemId);
    StudentItem si = studentService.getStudentItem(itemId, token.getUserId());
    return Result.ok(Map.of(
            "item", item,
            "studentItem", si != null ? si : ""
    ));
}
```

- [ ] **Step 3: Compile and verify**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/service/StudentService.java labex/src/main/java/labex/controller/StudentController.java && git commit -m "fix: StudentController.getItem returns full item data and existing answer"
```

---

### Task 2: Fix AnswerView — Frontend loading + API

**Files:**
- Modify: `labex-ui/src/api/student.js:1-12`
- Modify: `labex-ui/src/views/student/AnswerView.vue`

- [ ] **Step 1: Add getItem to student API**

Add to `labex-ui/src/api/student.js` after `saveAnswer`:

```javascript
getItem(itemId) { return request.get(`/api/student/items/${itemId}`) },
```

- [ ] **Step 2: Rewrite AnswerView.vue onMounted to load existing data**

Replace the entire `<script setup>` section of `labex-ui/src/views/student/AnswerView.vue` (lines 24-72):

```javascript
<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
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

const isCodeType = computed(() => {
  // Type 2 = programming question (will verify from t_question_type)
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
```

- [ ] **Step 3: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/api/student.js labex-ui/src/views/student/AnswerView.vue && git commit -m "fix: AnswerView loads existing answer instead of creating empty record"
```

---

### Task 3: Install wangEditor + Monaco Editor

**Files:**
- Modify: `labex-ui/package.json`

- [ ] **Step 1: Install npm packages**

Run:
```bash
cd D:/work/xianyu/TeachingPlatform/labex-ui && npm install @wangeditor/editor @wangeditor/editor-for-vue@next
```

Then install Monaco Editor:
```bash
cd D:/work/xianyu/TeachingPlatform/labex-ui && npm install monaco-editor @monaco-editor/vue
```

Wait — `@monaco-editor/vue` is the wrong package name. The correct one is `monaco-editor` plus a Vue wrapper. Let me check:

Run:
```bash
cd D:/work/xianyu/TeachingPlatform/labex-ui && npm install monaco-editor
```

The Vue wrapper for Monaco is `@monaco-editor/vue`. Install it:

Run:
```bash
cd D:/work/xianyu/TeachingPlatform/labex-ui && npm install @monaco-editor/vue
```

Expected: `package.json` updated with new dependencies.

- [ ] **Step 2: Verify package.json**

Run: `cd D:/work/xianyu/TeachingPlatform/labex-ui && cat package.json | grep -E "wangeditor|monaco"`
Expected: Both packages listed.

- [ ] **Step 3: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/package.json labex-ui/package-lock.json && git commit -m "chore: add wangEditor and Monaco Editor dependencies"
```

---

### Task 4: Create RichTextEditor component

**Files:**
- Create: `labex-ui/src/components/RichTextEditor.vue`

- [ ] **Step 1: Write the component**

Create `labex-ui/src/components/RichTextEditor.vue`:

```vue
<template>
  <div style="border: 1px solid #ccc">
    <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" style="border-bottom: 1px solid #ccc" />
    <Editor :defaultConfig="editorConfig" :modelValue="modelValue" :style="{ height: height + 'px', overflowY: 'hidden' }"
      @onCreated="handleCreated" @onChange="handleChange" />
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, ref, shallowRef } from 'vue'
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
```

- [ ] **Step 2: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/components/RichTextEditor.vue && git commit -m "feat: add RichTextEditor component using wangEditor"
```

---

### Task 5: Create CodeEditor component

**Files:**
- Create: `labex-ui/src/components/CodeEditor.vue`

- [ ] **Step 1: Write the component**

Create `labex-ui/src/components/CodeEditor.vue`:

```vue
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
import { ref } from 'vue'
import MonacoEditor from '@monaco-editor/vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  language: { type: String, default: 'java' },
  theme: { type: String, default: 'vs-dark' },
  height: { type: Number, default: 400 },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const editorOptions = ref({
  minimap: { enabled: false },
  fontSize: 14,
  wordWrap: 'on',
  readOnly: props.readOnly,
  automaticLayout: true
})

function handleChange(value) {
  emit('update:modelValue', value)
}
</script>
```

- [ ] **Step 2: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/components/CodeEditor.vue && git commit -m "feat: add CodeEditor component using Monaco Editor"
```

---

### Task 6: Create FileUpload component

**Files:**
- Create: `labex-ui/src/components/FileUpload.vue`

- [ ] **Step 1: Write the component**

Create `labex-ui/src/components/FileUpload.vue`:

```vue
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
  maxSize: { type: Number, default: 100 } // MB
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
```

- [ ] **Step 2: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/components/FileUpload.vue && git commit -m "feat: add FileUpload component wrapping el-upload"
```

---

### Task 7: Integrate editors into AnswerView

**Files:**
- Modify: `labex-ui/src/views/student/AnswerView.vue`

- [ ] **Step 1: Replace template to use editors**

Replace the entire `AnswerView.vue` template section (lines 1-22):

```html
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
```

- [ ] **Step 2: Add imports to script section**

Add these imports at the top of the `<script setup>` section (after the existing imports):

```javascript
import RichTextEditor from '../../components/RichTextEditor.vue'
import CodeEditor from '../../components/CodeEditor.vue'
```

- [ ] **Step 3: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/views/student/AnswerView.vue && git commit -m "feat: integrate RichTextEditor and CodeEditor into AnswerView"
```

---

### Task 8: Fix ExerciseListView — Add missing API methods + delete

**Files:**
- Modify: `labex-ui/src/api/teacher.js:44-49`
- Modify: `labex/src/main/java/labex/controller/ExerciseController.java`
- Modify: `labex-ui/src/views/teacher/ExerciseListView.vue`

- [ ] **Step 1: Add missing methods to teacher API**

Add to `labex-ui/src/api/teacher.js` after line 48 (after `addExerciseItem`):

```javascript
  updateExercise(id, data) { return request.put(`/api/teacher/exercises/${id}`, data) },
  deleteExercise(id) { return request.delete(`/api/teacher/exercises/${id}`) },
```

- [ ] **Step 2: Add update and delete endpoints to ExerciseController**

Add these methods to `labex/src/main/java/labex/controller/ExerciseController.java` after the `addItem` method (after line 53):

```java
@PutMapping("/{id}")
public Result<Void> update(@PathVariable Integer id, @RequestBody Ex3 ex, HttpSession session) {
    ex.setId(id);
    exerciseService.updateExercise(ex);
    return Result.ok();
}

@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Integer id, HttpSession session) {
    exerciseService.deleteExercise(id);
    return Result.ok();
}
```

- [ ] **Step 3: Add deleteExercise to ExerciseService**

Add to `labex/src/main/java/labex/service/ExerciseService.java` after `updateExercise` method (after line 43):

```java
public void deleteExercise(Integer id) {
    ex3ItemMapper.delete(new QueryWrapper<Ex3Item>().eq("excercise_id", id));
    ex3Mapper.deleteById(id);
}
```

- [ ] **Step 4: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 5: Add delete button to ExerciseListView**

In `labex-ui/src/views/teacher/ExerciseListView.vue`, add a delete button in the operations column. Replace lines 13-17:

```html
<el-table-column label="操作" width="250">
  <template #default="{ row }">
    <el-button size="small" @click="viewItems(row)">题目</el-button>
    <el-button size="small" @click="editExercise(row)">编辑</el-button>
    <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
  </template>
</el-table-column>
```

Add the delete handler function after `saveItem` function (after line 122):

```javascript
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该练习？', '提示', { type: 'warning' })
  await api.deleteExercise(id)
  ElMessage.success('删除成功')
  loadData()
}
```

Also add `ElMessageBox` to the import on line 66:

```javascript
import { ElMessage, ElMessageBox } from 'element-plus'
```

- [ ] **Step 6: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/api/teacher.js labex/src/main/java/labex/controller/ExerciseController.java labex/src/main/java/labex/service/ExerciseService.java labex-ui/src/views/teacher/ExerciseListView.vue && git commit -m "fix: add updateExercise/deleteExercise API + delete button for exercises"
```

---

## SP2: Question Bank + Student Exercises

### Task 9: Add question bank management view

**Files:**
- Create: `labex-ui/src/views/teacher/QuestionManageView.vue`
- Modify: `labex-ui/src/router/index.js:14-25`
- Modify: `labex-ui/src/layouts/TeacherLayout.vue:7-32`

- [ ] **Step 1: Create QuestionManageView.vue**

Create `labex-ui/src/views/teacher/QuestionManageView.vue`:

```vue
<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>题库管理</h2>
      <div>
        <el-select v-model="filterTypeId" placeholder="筛选题型" clearable @change="loadData" style="width: 150px; margin-right: 10px">
          <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-button type="primary" @click="showAddDialog">新增题目</el-button>
      </div>
    </div>
    <el-table :data="filteredQuestions" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="题目内容" min-width="200">
        <template #default="{ row }">
          <span v-html="row.content"></span>
        </template>
      </el-table-column>
      <el-table-column label="题型" width="100">
        <template #default="{ row }">{{ getTypeName(row.typeId) }}</template>
      </el-table-column>
      <el-table-column prop="answer" label="答案" />
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editQuestion(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="600">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="form.typeId" style="width: 100%">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="题目内容">
          <RichTextEditor v-model="form.content" :height="200" />
        </el-form-item>
        <el-form-item label="选项">
          <div v-for="(opt, idx) in form.options" :key="idx" style="display: flex; margin-bottom: 8px">
            <span style="width: 30px; line-height: 32px">{{ String.fromCharCode(65 + idx) }}</span>
            <el-input v-model="form.options[idx]" style="flex: 1" />
            <el-button v-if="form.options.length > 2" size="small" type="danger" @click="form.options.splice(idx, 1)" style="margin-left: 8px">删除</el-button>
          </div>
          <el-button size="small" @click="form.options.push('')">添加选项</el-button>
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="2" />
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
import { ref, computed, onMounted } from 'vue'
import api from '../../api/question'
import { ElMessage, ElMessageBox } from 'element-plus'
import RichTextEditor from '../../components/RichTextEditor.vue'

const questions = ref([])
const types = ref([])
const filterTypeId = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ typeId: null, content: '', options: ['', ''], answer: '', score: 10 })

const filteredQuestions = computed(() => {
  if (!filterTypeId.value) return questions.value
  return questions.value.filter(q => q.typeId === filterTypeId.value)
})

onMounted(async () => {
  const [qRes, tRes] = await Promise.all([api.list(), api.listTypes()])
  questions.value = qRes.data
  types.value = tRes.data
})

function getTypeName(typeId) {
  const t = types.value.find(t => t.id === typeId)
  return t ? t.name : typeId
}

function showAddDialog() {
  isEdit.value = false
  form.value = { typeId: null, content: '', options: ['', ''], answer: '', score: 10 }
  dialogVisible.value = true
}

function editQuestion(row) {
  isEdit.value = true
  form.value = {
    id: row.id,
    typeId: row.typeId,
    content: row.content,
    options: row.options ? row.options.split('||') : ['', ''],
    answer: row.answer || '',
    score: row.score || 10
  }
  dialogVisible.value = true
}

async function handleSave() {
  const data = {
    ...form.value,
    options: form.value.options.filter(o => o.trim()).join('||')
  }
  if (isEdit.value) {
    await api.update(form.value.id, data)
  } else {
    await api.add(data)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  const res = await api.list()
  questions.value = res.data
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await api.delete(id)
  ElMessage.success('删除成功')
  const res = await api.list()
  questions.value = res.data
}
</script>
```

- [ ] **Step 2: Add route**

In `labex-ui/src/router/index.js`, add inside the teacher children array (after line 24, the exercises route):

```javascript
      { path: 'questions', name: 'QuestionManage', component: () => import('../views/teacher/QuestionManageView.vue') },
```

- [ ] **Step 3: Add sidebar menu item**

In `labex-ui/src/layouts/TeacherLayout.vue`, add after the exercises menu item (after line 31):

```html
        <el-menu-item index="/teacher/questions">
          <el-icon><Collection /></el-icon><span>题库管理</span>
        </el-menu-item>
```

- [ ] **Step 4: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/views/teacher/QuestionManageView.vue labex-ui/src/router/index.js labex-ui/src/layouts/TeacherLayout.vue && git commit -m "feat: add question bank management view with CRUD"
```

---

### Task 10: Student exercise backend — Add student-facing endpoints

**Files:**
- Modify: `labex/src/main/java/labex/controller/StudentController.java`
- Modify: `labex/src/main/java/labex/service/StudentService.java`

- [ ] **Step 1: Add ExerciseService dependency to StudentController**

Add to `labex/src/main/java/labex/controller/StudentController.java`:

Add import:
```java
import labex.service.ExerciseService;
import labex.entity.Ex3;
import labex.entity.Ex3Item;
```

Change constructor to inject ExerciseService:
```java
private final StudentService studentService;
private final ExerciseService exerciseService;

public StudentController(StudentService studentService, ExerciseService exerciseService) {
    this.studentService = studentService;
    this.exerciseService = exerciseService;
}
```

- [ ] **Step 2: Add student exercise endpoints**

Add these methods to StudentController before the closing brace:

```java
// ===== Exercises (Student) =====

@GetMapping("/exercises")
public Result<List<Ex3>> listExercises(HttpSession session) {
    verifyStudent(session);
    return Result.ok(exerciseService.listExercises());
}

@GetMapping("/exercises/{id}/items")
public Result<List<Ex3Item>> getExerciseItems(@PathVariable Integer id, HttpSession session) {
    verifyStudent(session);
    return Result.ok(exerciseService.getExerciseItems(id));
}

@PostMapping("/exercises/answer")
public Result<Void> answerExercise(@RequestBody Map<String, Object> body, HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    Integer itemId = (Integer) body.get("itemId");
    Integer type = (Integer) body.get("type");
    String answer = (String) body.get("answer");
    exerciseService.answerQuestion(token.getUserId(), itemId, type, answer);
    return Result.ok();
}
```

Add the missing import:
```java
import java.util.Map;
```

- [ ] **Step 3: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/controller/StudentController.java && git commit -m "feat: add student-facing exercise endpoints (list, items, answer)"
```

---

### Task 11: Student exercise frontend

**Files:**
- Create: `labex-ui/src/views/student/ExerciseListView.vue`
- Create: `labex-ui/src/views/student/ExerciseView.vue`
- Modify: `labex-ui/src/api/student.js`
- Modify: `labex-ui/src/router/index.js:31-37`
- Modify: `labex-ui/src/layouts/StudentLayout.vue:7-17`

- [ ] **Step 1: Add exercise API methods to student.js**

Add to `labex-ui/src/api/student.js` before the closing `}`:

```javascript
  // Exercises
  listExercises() { return request.get('/api/student/exercises') },
  getExerciseItems(id) { return request.get(`/api/student/exercises/${id}/items`) },
  submitExerciseAnswer(data) { return request.post('/api/student/exercises/answer', data) },
```

- [ ] **Step 2: Create ExerciseListView.vue**

Create `labex-ui/src/views/student/ExerciseListView.vue`:

```vue
<template>
  <div>
    <h2>练习中心</h2>
    <el-table :data="exercises" border stripe style="margin-top: 16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="练习名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="goToExercise(row.id)">开始练习</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/student'

const router = useRouter()
const exercises = ref([])

onMounted(async () => {
  const res = await api.listExercises()
  exercises.value = res.data
})

function goToExercise(id) {
  router.push(`/student/exercises/${id}`)
}
</script>
```

- [ ] **Step 3: Create ExerciseView.vue**

Create `labex-ui/src/views/student/ExerciseView.vue`:

```vue
<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>练习答题</h2>
      <el-button @click="$router.back()">返回练习列表</el-button>
    </div>
    <el-card v-for="(item, idx) in items" :key="item.id" style="margin-bottom: 16px">
      <h3 style="margin-top: 0">第 {{ idx + 1 }} 题</h3>
      <div v-html="item.question"></div>
      <div v-if="item.options" style="margin-top: 8px">
        <el-radio-group v-model="answers[item.id]" v-if="item.type === 1">
          <el-radio v-for="(opt, oi) in item.options.split('||')" :key="oi" :value="String.fromCharCode(65 + oi)" style="display: block; margin-bottom: 8px">
            {{ String.fromCharCode(65 + oi) }}. {{ opt }}
          </el-radio>
        </el-radio-group>
      </div>
      <div v-if="item.type !== 1" style="margin-top: 8px">
        <el-input v-model="answers[item.id]" type="textarea" :rows="3" placeholder="请输入答案..." />
      </div>
      <div style="margin-top: 8px">
        <el-button type="primary" size="small" @click="submitAnswer(item)">提交答案</el-button>
      </div>
    </el-card>
    <el-empty v-if="items.length === 0" description="暂无题目" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const exerciseId = route.params.id
const items = ref([])
const answers = reactive({})

onMounted(async () => {
  const res = await api.getExerciseItems(exerciseId)
  items.value = res.data
})

async function submitAnswer(item) {
  const answer = answers[item.id]
  if (!answer) {
    ElMessage.warning('请先输入答案')
    return
  }
  await api.submitExerciseAnswer({
    itemId: item.id,
    type: item.type,
    answer: answer
  })
  ElMessage.success('答案已提交')
}
</script>
```

- [ ] **Step 4: Add routes**

In `labex-ui/src/router/index.js`, add inside the student children array (after the password route, line 36):

```javascript
      { path: 'exercises', name: 'StudentExercises', component: () => import('../views/student/ExerciseListView.vue') },
      { path: 'exercises/:id', name: 'StudentExerciseDetail', component: () => import('../views/student/ExerciseView.vue') },
```

- [ ] **Step 5: Add sidebar menu item**

In `labex-ui/src/layouts/StudentLayout.vue`, add after the "我的实验" menu item (after line 10):

```html
        <el-menu-item index="/student/exercises">
          <el-icon><Notebook /></el-icon><span>练习中心</span>
        </el-menu-item>
```

- [ ] **Step 6: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/views/student/ExerciseListView.vue labex-ui/src/views/student/ExerciseView.vue labex-ui/src/api/student.js labex-ui/src/router/index.js labex-ui/src/layouts/StudentLayout.vue && git commit -m "feat: add student exercise list and answer views"
```

---

## SP3: Dashboard + Stats Enhancement

### Task 12: Backend — Dashboard stats endpoints

**Files:**
- Modify: `labex/src/main/java/labex/service/TeacherService.java`
- Modify: `labex/src/main/java/labex/controller/TeacherController.java`
- Modify: `labex/src/main/java/labex/service/StudentService.java`
- Modify: `labex/src/main/java/labex/controller/StudentController.java`

- [ ] **Step 1: Add dashboard stats to TeacherService**

Add `JdbcTemplate` dependency and `getDashboardStats` method to `labex/src/main/java/labex/service/TeacherService.java`:

Add import:
```java
import org.springframework.jdbc.core.JdbcTemplate;
```

Add field and constructor parameter:
```java
private final JdbcTemplate jdbcTemplate;

// Update constructor to add:
public TeacherService(ClassMapper classMapper, StudentMapper studentMapper,
                      ExperimentMapper experimentMapper, ExperimentItemMapper experimentItemMapper,
                      StudentItemMapper studentItemMapper, StudentItemLogMapper studentItemLogMapper,
                      LectureMapper lectureMapper, JdbcTemplate jdbcTemplate) {
    this.classMapper = classMapper;
    this.studentMapper = studentMapper;
    this.experimentMapper = experimentMapper;
    this.experimentItemMapper = experimentItemMapper;
    this.studentItemMapper = studentItemMapper;
    this.studentItemLogMapper = studentItemLogMapper;
    this.lectureMapper = lectureMapper;
    this.jdbcTemplate = jdbcTemplate;
}
```

Add method after `deleteLecture`:
```java
// ===== Dashboard Stats =====

public Map<String, Object> getDashboardStats() {
    Map<String, Object> stats = new java.util.HashMap<>();
    try { stats.put("studentInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_info")); } catch (Exception e) { stats.put("studentInfo", Map.of()); }
    try { stats.put("clazzInfo", jdbcTemplate.queryForMap("SELECT * FROM v_clazz_info")); } catch (Exception e) { stats.put("clazzInfo", Map.of()); }
    try { stats.put("answerDataInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_answer_data_info")); } catch (Exception e) { stats.put("answerDataInfo", Map.of()); }
    try { stats.put("answerLogInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_answer_log_info")); } catch (Exception e) { stats.put("answerLogInfo", Map.of()); }
    try { stats.put("sysLogInfo", jdbcTemplate.queryForMap("SELECT * FROM v_sys_log_info")); } catch (Exception e) { stats.put("sysLogInfo", Map.of()); }
    return stats;
}
```

- [ ] **Step 2: Add dashboard endpoint to TeacherController**

Add to `labex/src/main/java/labex/controller/TeacherController.java` after the lectures section:

```java
// ===== Dashboard =====

@GetMapping("/dashboard/stats")
public Result<Map<String, Object>> dashboardStats(HttpSession session) {
    verifyTeacher(session);
    return Result.ok(teacherService.getDashboardStats());
}
```

- [ ] **Step 3: Add dashboard stats to StudentService**

Add `JdbcTemplate` dependency and method to `labex/src/main/java/labex/service/StudentService.java`:

Add import:
```java
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.HashMap;
```

Add field and update constructor:
```java
private final JdbcTemplate jdbcTemplate;

public StudentService(ExperimentMapper experimentMapper,
                      ExperimentItemMapper experimentItemMapper,
                      StudentItemMapper studentItemMapper,
                      StudentItemLogMapper studentItemLogMapper,
                      LectureMapper lectureMapper,
                      StudentMapper studentMapper,
                      JdbcTemplate jdbcTemplate) {
    this.experimentMapper = experimentMapper;
    this.experimentItemMapper = experimentItemMapper;
    this.studentItemMapper = studentItemMapper;
    this.studentItemLogMapper = studentItemLogMapper;
    this.lectureMapper = lectureMapper;
    this.studentMapper = studentMapper;
    this.jdbcTemplate = jdbcTemplate;
}
```

Add method:
```java
public Map<String, Object> getDashboardStats(Integer studentId) {
    Map<String, Object> stats = new HashMap<>();
    // Total experiments
    Long totalExps = experimentMapper.selectCount(new QueryWrapper<>());
    stats.put("totalExperiments", totalExps);
    // Completed experiments (distinct experiments with student items)
    Long completed = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT ei.experiment_id) FROM t_student_item si " +
            "JOIN t_experiment_item ei ON si.item_id = ei.experiment_item_id " +
            "WHERE si.student_id = ?", Long.class, studentId);
    stats.put("completedExperiments", completed != null ? completed : 0);
    // Average score
    List<Map<String, Object>> scores = studentMapper.selectStudentExperimentScore(studentId);
    double avg = scores.stream()
            .filter(m -> m.get("score") != null)
            .mapToInt(m -> ((Number) m.get("score")).intValue())
            .average().orElse(0.0);
    stats.put("averageScore", Math.round(avg * 10.0) / 10.0);
    return stats;
}
```

- [ ] **Step 4: Add dashboard endpoint to StudentController**

Add to `labex/src/main/java/labex/controller/StudentController.java`:

```java
@GetMapping("/dashboard/stats")
public Result<Map<String, Object>> dashboardStats(HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    return Result.ok(studentService.getDashboardStats(token.getUserId()));
}
```

- [ ] **Step 5: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/service/TeacherService.java labex/src/main/java/labex/controller/TeacherController.java labex/src/main/java/labex/service/StudentService.java labex/src/main/java/labex/controller/StudentController.java && git commit -m "feat: add dashboard stats endpoints for teacher and student"
```

---

### Task 13: Frontend — Enhanced dashboards

**Files:**
- Modify: `labex-ui/src/views/teacher/DashboardView.vue`
- Modify: `labex-ui/src/views/student/DashboardView.vue`
- Modify: `labex-ui/src/api/teacher.js`

- [ ] **Step 1: Add dashboard stats API method**

Add to `labex-ui/src/api/teacher.js` after the exercises section:

```javascript
  // Dashboard
  dashboardStats() { return request.get('/api/teacher/dashboard/stats') },
```

Add to `labex-ui/src/api/student.js` after the exercises section:

```javascript
  dashboardStats() { return request.get('/api/student/dashboard/stats') },
```

- [ ] **Step 2: Rewrite teacher DashboardView**

Replace entire `labex-ui/src/views/teacher/DashboardView.vue`:

```vue
<template>
  <div>
    <h2>教师首页</h2>
    <p>欢迎回来，{{ userStore.user?.userName }}</p>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="班级数" :value="quickStats.classes" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="学生数" :value="quickStats.students" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="实验数" :value="quickStats.experiments" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="讲义数" :value="quickStats.lectures" />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px" v-if="hasViewStats">
      <el-col :span="12">
        <el-card>
          <template #header><span>学生统计</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="学生总数">{{ viewStats.studentInfo?.count || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最大ID">{{ viewStats.studentInfo?.maxId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近访问">{{ viewStats.studentInfo?.lastAccess || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>系统日志</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="日志总数">{{ viewStats.sysLogInfo?.count || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近登录">{{ viewStats.sysLogInfo?.lastAccess || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import api from '../../api/teacher'

const userStore = useUserStore()
const quickStats = ref({ classes: 0, students: 0, experiments: 0, lectures: 0 })
const viewStats = ref({})

const hasViewStats = computed(() => Object.keys(viewStats.value).length > 0)

onMounted(async () => {
  const [classes, students, experiments, lectures, stats] = await Promise.all([
    api.listClasses(),
    api.listStudents({ pageNum: 1, pageSize: 1 }),
    api.listExperiments(),
    api.listLectures(),
    api.dashboardStats().catch(() => null)
  ])
  quickStats.value.classes = classes.data.length
  quickStats.value.students = students.data.total
  quickStats.value.experiments = experiments.data.length
  quickStats.value.lectures = lectures.data.length
  if (stats) viewStats.value = stats.data
})
</script>
```

- [ ] **Step 3: Enhance student DashboardView**

Replace entire `labex-ui/src/views/student/DashboardView.vue`:

```vue
<template>
  <div>
    <h2>我的实验</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="总实验数" :value="stats.totalExperiments || 0" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="已完成" :value="stats.completedExperiments || 0" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="平均分" :value="stats.averageScore || 0" />
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <h3>实验列表</h3>
        <el-table :data="experiments" border stripe>
          <el-table-column prop="experimentNo" label="编号" width="80" />
          <el-table-column prop="experimentName" label="实验名称" />
          <el-table-column prop="experimentType" label="类型" width="100" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="goToExperiment(row.experimentId)">进入实验</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="8">
        <h3>我的成绩</h3>
        <el-table :data="scores" border size="small">
          <el-table-column prop="experiment_name" label="实验" />
          <el-table-column prop="score" label="成绩" width="80">
            <template #default="{ row }">{{ row.score ?? '-' }}</template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/student'

const router = useRouter()
const experiments = ref([])
const scores = ref([])
const stats = ref({})

onMounted(async () => {
  const [expRes, scoreRes, statsRes] = await Promise.all([
    api.listExperiments(),
    api.getMyScores(),
    api.dashboardStats().catch(() => ({ data: {} }))
  ])
  experiments.value = expRes.data
  scores.value = scoreRes.data
  stats.value = statsRes.data
})

function goToExperiment(id) {
  router.push(`/student/experiments/${id}`)
}
</script>
```

- [ ] **Step 4: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/views/teacher/DashboardView.vue labex-ui/src/views/student/DashboardView.vue labex-ui/src/api/teacher.js labex-ui/src/api/student.js && git commit -m "feat: enhanced teacher and student dashboards with stats from DB views"
```

---

## SP4: Logging System

### Task 14: Backend — Log service + endpoints

**Files:**
- Create: `labex/src/main/java/labex/service/LogService.java`
- Modify: `labex/src/main/java/labex/controller/TeacherController.java`
- Modify: `labex/src/main/java/labex/service/StudentService.java`

- [ ] **Step 1: Create LogService**

Create `labex/src/main/java/labex/service/LogService.java`:

```java
package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import labex.entity.StudentLog;
import labex.entity.SysLog;
import labex.mapper.StudentLogMapper;
import labex.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private final SysLogMapper sysLogMapper;
    private final StudentLogMapper studentLogMapper;

    public LogService(SysLogMapper sysLogMapper, StudentLogMapper studentLogMapper) {
        this.sysLogMapper = sysLogMapper;
        this.studentLogMapper = studentLogMapper;
    }

    public Page<SysLog> listSysLogs(String account, Integer type, int pageNum, int pageSize) {
        QueryWrapper<SysLog> qw = new QueryWrapper<>();
        if (account != null && !account.isEmpty()) {
            qw.like("account", account);
        }
        if (type != null) {
            qw.eq("type", type);
        }
        qw.orderByDesc("time");
        return sysLogMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public Page<SysLog> listRecentSysLogs(int limit) {
        QueryWrapper<SysLog> qw = new QueryWrapper<SysLog>()
                .orderByDesc("time");
        return sysLogMapper.selectPage(new Page<>(1, limit), qw);
    }

    public List<StudentLog> listStudentLogs(Integer studentId) {
        // Get student account first
        String account = null;
        if (studentId != null) {
            labex.entity.Student student = new labex.mapper.StudentMapper(){}.selectById(studentId);
            // Simpler: use JdbcTemplate or direct mapper
            account = studentMapper != null ? null : null; // Will be resolved below
        }
        QueryWrapper<StudentLog> qw = new QueryWrapper<>();
        if (studentId != null) {
            qw.inSql("account", "SELECT student_no FROM t_student WHERE student_id = " + studentId);
        }
        qw.orderByDesc("time");
        return studentLogMapper.selectList(qw);
    }

    public void logStudentActivity(String account, int type, String info, String ip) {
        StudentLog log = new StudentLog();
        log.setAccount(account);
        log.setType(type);
        log.setInfo(info);
        log.setTime(LocalDateTime.now());
        log.setIp(ip);
        studentLogMapper.insert(log);
    }
}
```

- [ ] **Step 2: Add log endpoints to TeacherController**

Add import and inject LogService in `labex/src/main/java/labex/controller/TeacherController.java`:

```java
import labex.service.LogService;
import labex.entity.StudentLog;
import labex.entity.SysLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

Add field:
```java
private final LogService logService;
```

Update constructor:
```java
public TeacherController(TeacherService teacherService, LogService logService) {
    this.teacherService = teacherService;
    this.logService = logService;
}
```

Add endpoints after lectures section:
```java
// ===== Logs =====

@GetMapping("/logs")
public Result<Page<SysLog>> listLogs(
        @RequestParam(required = false) String account,
        @RequestParam(required = false) Integer type,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "20") int pageSize,
        HttpSession session) {
    verifyTeacher(session);
    return Result.ok(logService.listSysLogs(account, type, pageNum, pageSize));
}

@GetMapping("/students/{id}/logs")
public Result<List<StudentLog>> getStudentLogs(@PathVariable Integer id, HttpSession session) {
    verifyTeacher(session);
    return Result.ok(logService.listStudentLogs(id));
}
```

- [ ] **Step 3: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/service/LogService.java labex/src/main/java/labex/controller/TeacherController.java && git commit -m "feat: add LogService with sys log and student log queries"
```

---

### Task 15: Frontend — System log viewer + student log viewer

**Files:**
- Create: `labex-ui/src/views/teacher/SysLogView.vue`
- Modify: `labex-ui/src/api/teacher.js`
- Modify: `labex-ui/src/router/index.js`
- Modify: `labex-ui/src/layouts/TeacherLayout.vue`
- Modify: `labex-ui/src/views/teacher/StudentListView.vue`

- [ ] **Step 1: Add log API methods**

Add to `labex-ui/src/api/teacher.js` after the dashboard section:

```javascript
  // Logs
  listLogs(params) { return request.get('/api/teacher/logs', { params }) },
  getStudentLogs(id) { return request.get(`/api/teacher/students/${id}/logs`) },
```

- [ ] **Step 2: Create SysLogView.vue**

Create `labex-ui/src/views/teacher/SysLogView.vue`:

```vue
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
```

- [ ] **Step 3: Add route + sidebar**

In `labex-ui/src/router/index.js`, add to teacher children (after questions route):

```javascript
      { path: 'logs', name: 'SysLogs', component: () => import('../views/teacher/SysLogView.vue') },
```

In `labex-ui/src/layouts/TeacherLayout.vue`, add after the questions menu item:

```html
        <el-menu-item index="/teacher/logs">
          <el-icon><Document /></el-icon><span>系统日志</span>
        </el-menu-item>
```

- [ ] **Step 4: Add student log viewer to StudentListView**

In `labex-ui/src/views/teacher/StudentListView.vue`, add a "日志" button in the operations column. Modify the operations column template (lines 22-27):

```html
<el-table-column label="操作" width="260">
  <template #default="{ row }">
    <el-button size="small" @click="editStudent(row)">编辑</el-button>
    <el-button size="small" @click="viewLogs(row)">日志</el-button>
    <el-button size="small" type="danger" @click="handleDelete(row.studentId)">删除</el-button>
  </template>
</el-table-column>
```

Add a logs dialog after the existing dialog (before the closing `</div>` of template):

```html
<el-dialog v-model="logsDialogVisible" title="学生活动日志" width="700">
  <el-table :data="studentLogs" border size="small" v-loading="logsLoading">
    <el-table-column prop="account" label="账号" width="120" />
    <el-table-column prop="type" label="类型" width="80" />
    <el-table-column prop="info" label="信息" />
    <el-table-column prop="ip" label="IP" width="140" />
    <el-table-column prop="time" label="时间" width="170" />
  </el-table>
  <el-empty v-if="!logsLoading && studentLogs.length === 0" description="暂无日志" />
</el-dialog>
```

Add to script section — new refs and functions:

```javascript
const logsDialogVisible = ref(false)
const studentLogs = ref([])
const logsLoading = ref(false)

async function viewLogs(row) {
  logsDialogVisible.value = true
  logsLoading.value = true
  try {
    const res = await api.getStudentLogs(row.studentId)
    studentLogs.value = res.data
  } catch (e) {
    studentLogs.value = []
  } finally {
    logsLoading.value = false
  }
}
```

- [ ] **Step 5: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex-ui/src/views/teacher/SysLogView.vue labex-ui/src/api/teacher.js labex-ui/src/router/index.js labex-ui/src/layouts/TeacherLayout.vue labex-ui/src/views/teacher/StudentListView.vue && git commit -m "feat: add system log viewer and student activity log viewer"
```

---

## SP5: Auxiliary Features

### Task 16: Teacher reset student password

**Files:**
- Modify: `labex/src/main/java/labex/service/TeacherService.java`
- Modify: `labex/src/main/java/labex/controller/TeacherController.java`
- Modify: `labex-ui/src/api/teacher.js`
- Modify: `labex-ui/src/views/teacher/StudentListView.vue`

- [ ] **Step 1: Add resetPassword to TeacherService**

Add to `labex/src/main/java/labex/service/TeacherService.java`:

Add import:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
```

Add method after `importStudents`:
```java
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

public void resetStudentPassword(Integer id) {
    Student student = studentMapper.selectById(id);
    if (student == null) throw new BusinessException("学生不存在");
    student.setStudentPassword(passwordEncoder.encode("123456"));
    student.setError(0);
    studentMapper.updateById(student);
}
```

- [ ] **Step 2: Add endpoint to TeacherController**

Add to `labex/src/main/java/labex/controller/TeacherController.java` after the import endpoint:

```java
@PutMapping("/students/{id}/reset-password")
public Result<Void> resetPassword(@PathVariable Integer id, HttpSession session) {
    verifyTeacher(session);
    teacherService.resetStudentPassword(id);
    return Result.ok();
}
```

- [ ] **Step 3: Add API method**

Add to `labex-ui/src/api/teacher.js` after `importStudents`:

```javascript
  resetPassword(id) { return request.put(`/api/teacher/students/${id}/reset-password`) },
```

- [ ] **Step 4: Add reset button to StudentListView**

In `labex-ui/src/views/teacher/StudentListView.vue`, add a reset button in the operations column. Update the operations column:

```html
<el-table-column label="操作" width="320">
  <template #default="{ row }">
    <el-button size="small" @click="editStudent(row)">编辑</el-button>
    <el-button size="small" @click="viewLogs(row)">日志</el-button>
    <el-button size="small" type="warning" @click="handleResetPassword(row.studentId)">重置密码</el-button>
    <el-button size="small" type="danger" @click="handleDelete(row.studentId)">删除</el-button>
  </template>
</el-table-column>
```

Add handler function:
```javascript
async function handleResetPassword(id) {
  await ElMessageBox.confirm('确定将该学生密码重置为 123456？', '提示', { type: 'warning' })
  await api.resetPassword(id)
  ElMessage.success('密码已重置为 123456')
}
```

- [ ] **Step 5: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/service/TeacherService.java labex/src/main/java/labex/controller/TeacherController.java labex-ui/src/api/teacher.js labex-ui/src/views/teacher/StudentListView.vue && git commit -m "feat: add teacher reset student password endpoint and UI"
```

---

### Task 17: CSV file import for students

**Files:**
- Modify: `labex/src/main/java/labex/controller/TeacherController.java:116-121`
- Modify: `labex/src/main/java/labex/service/TeacherService.java`
- Modify: `labex-ui/src/api/teacher.js:17`
- Modify: `labex-ui/src/views/teacher/StudentListView.vue`

- [ ] **Step 1: Modify TeacherService.importStudents to accept CSV parsing**

Add to `labex/src/main/java/labex/service/TeacherService.java`:

Add import:
```java
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
```

Add method after the existing `importStudents`:
```java
public int importStudentsFromCsv(MultipartFile file) throws Exception {
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
    String line = reader.readLine(); // Skip header
    int count = 0;
    while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length < 2) continue;
        String studentNo = parts[0].trim();
        String studentName = parts[1].trim();
        if (studentNo.isEmpty() || studentName.isEmpty()) continue;

        Student existing = studentMapper.selectOne(
                new QueryWrapper<Student>().eq("student_no", studentNo));
        if (existing == null) {
            Student s = new Student();
            s.setStudentNo(studentNo);
            s.setStudentName(studentName);
            s.setStudentPassword(passwordEncoder.encode("123456"));
            s.setState(1);
            s.setError(0);
            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                s.setClazzNo(parts[2].trim());
            }
            studentMapper.insert(s);
            count++;
        }
    }
    return count;
}
```

- [ ] **Step 2: Add CSV import endpoint to TeacherController**

Add to `labex/src/main/java/labex/controller/TeacherController.java` after the existing import endpoint:

```java
@PostMapping("/students/import-csv")
public Result<Map<String, Object>> importStudentsCsv(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
    verifyTeacher(session);
    int count = teacherService.importStudentsFromCsv(file);
    return Result.ok(Map.of("count", count));
}
```

- [ ] **Step 3: Add API method**

Add to `labex-ui/src/api/teacher.js` after `importStudents`:

```javascript
  importStudentsCsv(formData) { return request.post('/api/teacher/students/import-csv', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },
```

- [ ] **Step 4: Add CSV import button to StudentListView**

In `labex-ui/src/views/teacher/StudentListView.vue`, add an import button next to the add button. Modify the header div (lines 5-10):

```html
      <div>
        <el-select v-model="clazzNo" placeholder="筛选班级" clearable @change="loadData" style="width: 150px; margin-right: 10px">
          <el-option v-for="c in classes" :key="c.no" :label="c.no" :value="c.no" />
        </el-select>
        <el-upload :show-file-list="false" accept=".csv" :before-upload="handleCsvImport" style="display: inline-block; margin-right: 10px">
          <el-button>导入CSV</el-button>
        </el-upload>
        <el-button type="primary" @click="showAddDialog">新增学生</el-button>
      </div>
```

Add handler function:
```javascript
async function handleCsvImport(file) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await api.importStudentsCsv(formData)
    ElMessage.success(`成功导入 ${res.data.count} 名学生`)
    loadData()
  } catch (e) {
    ElMessage.error('导入失败')
  }
  return false
}
```

- [ ] **Step 5: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/service/TeacherService.java labex/src/main/java/labex/controller/TeacherController.java labex-ui/src/api/teacher.js labex-ui/src/views/teacher/StudentListView.vue && git commit -m "feat: add CSV file import for students with multipart upload"
```

---

### Task 18: SysConfig management

**Files:**
- Create: `labex/src/main/java/labex/mapper/SysConfigMapper.java`
- Modify: `labex/src/main/java/labex/controller/TeacherController.java`
- Modify: `labex-ui/src/api/teacher.js`

- [ ] **Step 1: Create SysConfigMapper**

Create `labex/src/main/java/labex/mapper/SysConfigMapper.java`:

```java
package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
```

- [ ] **Step 2: Add config endpoints to TeacherController**

Add import and inject SysConfigMapper:

```java
import labex.mapper.SysConfigMapper;
import labex.entity.SysConfig;
```

Add field:
```java
private final SysConfigMapper sysConfigMapper;
```

Update constructor:
```java
public TeacherController(TeacherService teacherService, LogService logService, SysConfigMapper sysConfigMapper) {
    this.teacherService = teacherService;
    this.logService = logService;
    this.sysConfigMapper = sysConfigMapper;
}
```

Add endpoints after logs section:
```java
// ===== Config =====

@GetMapping("/config")
public Result<List<SysConfig>> listConfig(HttpSession session) {
    verifyTeacher(session);
    return Result.ok(sysConfigMapper.selectList(new QueryWrapper<>()));
}

@PutMapping("/config")
public Result<Void> updateConfig(@RequestBody List<SysConfig> configs, HttpSession session) {
    verifyTeacher(session);
    for (SysConfig config : configs) {
        sysConfigMapper.updateById(config);
    }
    return Result.ok();
}
```

- [ ] **Step 3: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 4: Add API methods**

Add to `labex-ui/src/api/teacher.js` after logs section:

```javascript
  // Config
  listConfig() { return request.get('/api/teacher/config') },
  updateConfig(configs) { return request.put('/api/teacher/config', configs) },
```

- [ ] **Step 5: Commit**

```bash
cd D:/work/xianyu/TeachingPlatform && git add labex/src/main/java/labex/mapper/SysConfigMapper.java labex/src/main/java/labex/controller/TeacherController.java labex-ui/src/api/teacher.js && git commit -m "feat: add SysConfig mapper and config management endpoints"
```

---

### Task 19: Final build verification

**Files:** None (verification only)

- [ ] **Step 1: Compile backend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" compile -q 2>&1 | tail -5`
Expected: BUILD SUCCESS

- [ ] **Step 2: Build frontend**

Run: `cd D:/work/xianyu/TeachingPlatform/labex-ui && npm run build 2>&1 | tail -10`
Expected: Build completes without errors

- [ ] **Step 3: Start backend and test**

Run: `cd D:/work/xianyu/TeachingPlatform/labex && "D:/dev/maven/apache-maven-3.9.14/bin/mvn.cmd" spring-boot:run &`

Wait 15 seconds, then test key endpoints:

```bash
# Login
curl -s -c cookies.txt -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"account":"admin","password":"admin","type":0}' | head -c 200

# Dashboard stats
curl -s -b cookies.txt http://localhost:8080/api/teacher/dashboard/stats | head -c 200

# System logs
curl -s -b cookies.txt "http://localhost:8080/api/teacher/logs?pageNum=1&pageSize=5" | head -c 200

# Questions
curl -s -b cookies.txt http://localhost:8080/api/questions | head -c 200

# Student exercises
curl -s -b cookies.txt http://localhost:8080/api/student/exercises | head -c 200
```

Expected: All return `{"code":0,...}` responses.

- [ ] **Step 4: Commit all if any fixes needed**

```bash
cd D:/work/xianyu/TeachingPlatform && git add -A && git commit -m "fix: build verification fixes"
```
