import request from '../utils/request'

export default {
  login(data) { return request.post('/api/auth/login', data) },
  logout() { return request.post('/api/auth/logout') },
  current() { return request.get('/api/auth/current') }
}
