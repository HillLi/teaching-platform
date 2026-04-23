import request from '../utils/request'

export default {
  listExperiments() { return request.get('/api/student/experiments') },
  getExperiment(id) { return request.get(`/api/student/experiments/${id}`) },
  getExperimentItems(id) { return request.get(`/api/student/experiments/${id}/items`) },
  saveAnswer(itemId, content) { return request.post(`/api/student/items/${itemId}/answer`, { content }) },
  getItem(itemId) { return request.get(`/api/student/items/${itemId}`) },
  getMyScores() { return request.get('/api/student/scores') },
  listLectures() { return request.get('/api/student/lectures') },
  downloadLecture(id) { return `/api/student/lectures/${id}/download` },
  changePassword(oldPassword, newPassword) { return request.put('/api/student/password', { oldPassword, newPassword }) },
  uploadAnswerFile(itemId, formData) { return request.post(`/api/student/items/${itemId}/upload`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },

  // Exercises
  listExercises() { return request.get('/api/student/exercises') },
  getExerciseItems(id) { return request.get(`/api/student/exercises/${id}/items`) },
  submitExerciseAnswer(data) { return request.post('/api/student/exercises/answer', data) },
  getExerciseScore(id) { return request.get(`/api/student/exercises/${id}/score`) },

  // Exams
  listExams() { return request.get('/api/student/exams') },
  startExam(id) { return request.post(`/api/student/exams/${id}/start`) },
  getExamItems(id) { return request.get(`/api/student/exams/${id}/items`) },
  submitExam(id, data) { return request.post(`/api/student/exams/${id}/submit`, data) },
  getExamScore(id) { return request.get(`/api/student/exams/${id}/score`) },

  // Dashboard
  dashboardStats() { return request.get('/api/student/dashboard/stats') },
}
