<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>考试批改 - {{ examName }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 12px">
      <span style="font-weight: 500">班级筛选：</span>
      <el-select v-model="selectedClazz" placeholder="全部班级" clearable style="width: 200px" @change="onClazzChange">
        <el-option v-for="c in examClasses" :key="c.clazzNo" :label="c.clazzNo" :value="c.clazzNo" />
      </el-select>
    </div>

    <el-table :data="filteredSubmissions" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="提交状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'">
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="gradeStudent(row)" v-if="row.status >= 1">批改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批改对话框 -->
    <el-dialog v-model="gradingVisible" title="逐题批改" width="960">
      <el-table :data="detail" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题目" min-width="120">
          <template #default="{ row }">
            <template v-if="row.type === 1">
              <div style="line-height: 2.2; font-size: 14px">
                <template v-for="(seg, si) in getSegments(row.content)" :key="si">
                  <span>{{ seg }}</span>
                  <span v-if="si < getSegments(row.content).length - 1"
                    style="border-bottom: 2px solid #409eff; padding: 0 20px; margin: 0 4px; color: #999; font-size: 12px">第{{ si + 1 }}空</span>
                </template>
              </div>
            </template>
            <span v-else>{{ truncate(row.content) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ typeMap[row.type] }}</template>
        </el-table-column>
        <el-table-column prop="maxScore" label="满分" width="60" />
        <el-table-column label="学生答案" min-width="160">
          <template #default="{ row }">
            <template v-if="row.type === 1">
              <div style="width: 100%">
                <div v-for="(ans, idx) in getBlankAnswers(row.studentAnswer, getBlankCount(row.content))" :key="idx"
                  style="display: flex; align-items: center; margin-bottom: 4px">
                  <span style="width: 50px; flex-shrink: 0; color: #409eff; font-size: 12px">第{{ idx + 1 }}空：</span>
                  <span v-if="ans" style="border-bottom: 1px solid #333; padding: 0 4px">{{ ans }}</span>
                  <span v-else style="color: #999">(未填)</span>
                </div>
              </div>
            </template>
            <template v-else-if="getAnswerText(row)">
              <span v-if="getAnswerText(row).length <= 50">{{ getAnswerText(row) }}</span>
              <span v-else>
                {{ getAnswerText(row).substring(0, 50) }}...
                <el-popover placement="left" :width="480" trigger="click">
                  <template #reference>
                    <el-button type="primary" link size="small">详情</el-button>
                  </template>
                  <div style="max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all">{{ getAnswerText(row) }}</div>
                </el-popover>
              </span>
            </template>
            <span v-else style="color: #999">(未答)</span>
          </template>
        </el-table-column>
        <el-table-column label="正确答案" min-width="120">
          <template #default="{ row }">
            <template v-if="row.type === 1">
              <div style="width: 100%">
                <div v-for="(ans, idx) in getBlankAnswers(row.referenceAnswer, getBlankCount(row.content))" :key="idx"
                  style="display: flex; align-items: center; margin-bottom: 4px">
                  <span style="width: 50px; flex-shrink: 0; color: #67c23a; font-size: 12px">第{{ idx + 1 }}空：</span>
                  <span>{{ ans || '-' }}</span>
                </div>
              </div>
            </template>
            <span v-else>{{ row.referenceAnswer || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="自动评分" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.autoScored" type="success" size="small">{{ row.currentScore }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.currentScore" :min="0" :max="row.maxScore" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="gradingVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGrading">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api/teacher'
import { ElMessage } from 'element-plus'

const route = useRoute()
const examId = route.params.id
const examName = ref('')
const submissions = ref([])
const selectedClazz = ref('')
const examClasses = ref([])
const gradingVisible = ref(false)
const detail = ref([])
const currentStudentId = ref(null)
const statusMap = { 0: '未提交', 1: '待批改', 2: '已批改' }
const typeMap = { 1: '填空', 2: '单选', 3: '多选', 4: '判断', 5: '简答', 6: '编程', 7: '综合' }

const filteredSubmissions = computed(() => {
  if (!selectedClazz.value) return submissions.value
  return submissions.value.filter(s => s.clazzNo === selectedClazz.value)
})

function truncate(text) { return !text ? '-' : text.length > 40 ? text.substring(0, 40) + '...' : text }
function formatTime(t) { return t ? t.replace('T', ' ') : '-' }
function getAnswerText(row) { return row.studentAnswer || row.studentContent || '' }
function onClazzChange() { /* filteredSubmissions auto-updates via computed */ }
function getSegments(content) { return content ? content.split(/_{2,}/) : [''] }
function getBlankCount(content) { return Math.max(getSegments(content).length - 1, 0) }
function getBlankAnswers(pipedAnswer, blankCount) {
  if (!blankCount) return []
  const parts = pipedAnswer ? pipedAnswer.split('|') : []
  return Array.from({ length: blankCount }, (_, i) => (i < parts.length ? parts[i].trim() : '') || null)
}

onMounted(async () => {
  const exams = (await api.listExams()).data
  const exam = exams.find(e => e.id === Number(examId))
  examName.value = exam ? exam.name : ''
  const [subRes, classRes] = await Promise.all([
    api.getExamSubmissions(examId),
    api.getExamClasses(examId)
  ])
  submissions.value = subRes.data
  examClasses.value = classRes.data.map(no => ({ clazzNo: no }))
})

async function loadData() {
  const res = await api.getExamSubmissions(examId)
  submissions.value = res.data
}

async function gradeStudent(row) {
  currentStudentId.value = row.studentId
  const res = await api.getExamSubmissionDetail(examId, row.studentId)
  detail.value = res.data.map(item => ({ ...item, currentScore: item.score ?? 0 }))
  gradingVisible.value = true
}

async function saveGrading() {
  for (const item of detail.value) {
    await api.submitExamScore({
      answerId: item.answerId,
      examItemId: item.itemId,
      studentId: currentStudentId.value,
      score: item.currentScore
    })
  }
  ElMessage.success('评分已提交')
  gradingVisible.value = false
  loadData()
}
</script>
