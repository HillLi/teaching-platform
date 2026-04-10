import request from '../utils/request'

export default {
  list() { return request.get('/api/questions') },
  add(data) { return request.post('/api/questions', data) },
  update(id, data) { return request.put(`/api/questions/${id}`, data) },
  delete(id) { return request.delete(`/api/questions/${id}`) },
  listTypes() { return request.get('/api/questions/types') },
}
