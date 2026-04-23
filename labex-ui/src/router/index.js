import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue')
  },
  {
    path: '/teacher',
    component: () => import('../layouts/TeacherLayout.vue'),
    meta: { role: 0 },
    children: [
      { path: '', name: 'TeacherDashboard', component: () => import('../views/teacher/DashboardView.vue') },
      { path: 'classes', name: 'ClassList', component: () => import('../views/teacher/ClassListView.vue') },
      { path: 'students', name: 'StudentList', component: () => import('../views/teacher/StudentListView.vue') },
      { path: 'experiments', name: 'ExperimentList', component: () => import('../views/teacher/ExperimentListView.vue') },
      { path: 'experiments/:id/items', name: 'ItemList', component: () => import('../views/teacher/ItemListView.vue') },
      { path: 'grading', name: 'Grading', component: () => import('../views/teacher/GradingView.vue') },
      { path: 'grading/:studentId/:expId', name: 'Report', component: () => import('../views/teacher/ReportView.vue') },
      { path: 'class-report', name: 'ClassReport', component: () => import('../views/teacher/ClassReportView.vue') },
      { path: 'lectures', name: 'LectureList', component: () => import('../views/teacher/LectureListView.vue') },
      { path: 'exercises', name: 'ExerciseList', component: () => import('../views/teacher/ExerciseListView.vue') },
      { path: 'exercise-grading/:id', name: 'ExerciseGrading', component: () => import('../views/teacher/ExerciseGradingView.vue') },
      { path: 'exercise-scores/:id', name: 'ExerciseScores', component: () => import('../views/teacher/ExerciseScoreView.vue') },
      { path: 'exams', name: 'ExamList', component: () => import('../views/teacher/ExamListView.vue') },
      { path: 'exam-grading/:id', name: 'ExamGrading', component: () => import('../views/teacher/ExamGradingView.vue') },
      { path: 'exam-scores/:id', name: 'ExamScores', component: () => import('../views/teacher/ExamScoreView.vue') },
      { path: 'logs', name: 'SysLogs', component: () => import('../views/teacher/SysLogView.vue') },
    ]
  },
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    meta: { role: 1 },
    children: [
      { path: '', name: 'StudentDashboard', component: () => import('../views/student/DashboardView.vue') },
      { path: 'experiments/:id', name: 'StudentExperiment', component: () => import('../views/student/ExperimentView.vue') },
      { path: 'answer/:itemId', name: 'StudentAnswer', component: () => import('../views/student/AnswerView.vue') },
      { path: 'lectures', name: 'StudentLectures', component: () => import('../views/student/LectureListView.vue') },
      { path: 'password', name: 'StudentPassword', component: () => import('../views/student/PasswordView.vue') },
      { path: 'exercises', name: 'StudentExercises', component: () => import('../views/student/ExerciseListView.vue') },
      { path: 'exercises/:id', name: 'StudentExerciseDetail', component: () => import('../views/student/ExerciseView.vue') },
      { path: 'exams', name: 'StudentExams', component: () => import('../views/student/ExamListView.vue') },
      { path: 'exams/:id', name: 'StudentExamTake', component: () => import('../views/student/ExamTakeView.vue') },
      { path: 'exams/:id/score', name: 'StudentExamScore', component: () => import('../views/student/ExamScoreView.vue') },
    ]
  },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    next()
    return
  }

  const store = useUserStore()
  if (!store.user) {
    const user = await store.fetchUser()
    if (!user) {
      next('/login')
      return
    }
  }

  const requiredRole = to.matched.find(r => r.meta?.role !== undefined)?.meta?.role
  if (requiredRole !== undefined && store.user.userType !== requiredRole) {
    next(store.user.userType === 0 ? '/teacher' : '/student')
    return
  }

  next()
})

export default router
