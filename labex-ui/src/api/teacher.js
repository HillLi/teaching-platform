import request from '../utils/request'

export default {
  // Classes
  listClasses() { return request.get('/api/teacher/classes') },
  getClass(no) { return request.get(`/api/teacher/classes/${no}`) },
  addClass(data) { return request.post('/api/teacher/classes', data) },
  updateClass(no, data) { return request.put(`/api/teacher/classes/${no}`, data) },
  deleteClass(no) { return request.delete(`/api/teacher/classes/${no}`) },

  // Students
  listStudents(params) { return request.get('/api/teacher/students', { params }) },
  getStudent(id) { return request.get(`/api/teacher/students/${id}`) },
  addStudent(data) { return request.post('/api/teacher/students', data) },
  updateStudent(id, data) { return request.put(`/api/teacher/students/${id}`, data) },
  deleteStudent(id) { return request.delete(`/api/teacher/students/${id}`) },
  importStudents(data) { return request.post('/api/teacher/students/import', data) },
  resetPassword(id) { return request.put(`/api/teacher/students/${id}/reset-password`) },
  importStudentsCsv(formData) { return request.post('/api/teacher/students/import-csv', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },

  // Experiments
  listExperiments() { return request.get('/api/teacher/experiments') },
  getExperiment(id) { return request.get(`/api/teacher/experiments/${id}`) },
  addExperiment(data) { return request.post('/api/teacher/experiments', data) },
  updateExperiment(id, data) { return request.put(`/api/teacher/experiments/${id}`, data) },
  deleteExperiment(id) { return request.delete(`/api/teacher/experiments/${id}`) },

  // Experiment Items
  listItems(expId) { return request.get(`/api/teacher/experiments/${expId}/items`) },
  addItem(expId, data) { return request.post(`/api/teacher/experiments/${expId}/items`, data) },
  updateItem(itemId, data) { return request.put(`/api/teacher/experiments/items/${itemId}`, data) },
  deleteItem(itemId) { return request.delete(`/api/teacher/experiments/items/${itemId}`) },
  setItemAnswer(itemId, answer) { return request.put(`/api/teacher/experiments/items/${itemId}/answer`, { answer }) },

  // Reports / Grading
  listSubmittedStudents(params) { return request.get('/api/teacher/reports/students', { params }) },
  getStudentReport(studentId, expId) { return request.get(`/api/teacher/reports/students/${studentId}/experiments/${expId}`) },
  submitScore(data) { return request.post('/api/teacher/reports/scores', data) },
  getClassScore(clazzNo, expId) { return request.get(`/api/teacher/reports/classes/${clazzNo}/experiments/${expId}`) },

  // Lectures
  listLectures() { return request.get('/api/teacher/lectures') },
  uploadLecture(formData) { return request.post('/api/teacher/lectures', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },
  deleteLecture(id) { return request.delete(`/api/teacher/lectures/${id}`) },

  // Exercises
  listExercises() { return request.get('/api/teacher/exercises') },
  addExercise(data) { return request.post('/api/teacher/exercises', data) },
  getExerciseItems(id) { return request.get(`/api/teacher/exercises/${id}/items`) },
  addExerciseItem(id, data) { return request.post(`/api/teacher/exercises/${id}/items`, data) },
  updateExercise(id, data) { return request.put(`/api/teacher/exercises/${id}`, data) },
  deleteExercise(id) { return request.delete(`/api/teacher/exercises/${id}`) },

  // Dashboard
  dashboardStats() { return request.get('/api/teacher/dashboard/stats') },

  // Logs
  listLogs(params) { return request.get('/api/teacher/logs', { params }) },
  getStudentLogs(id) { return request.get(`/api/teacher/students/${id}/logs`) },

  // Config
  listConfig() { return request.get('/api/teacher/config') },
  updateConfig(configs) { return request.put('/api/teacher/config', configs) },
}
