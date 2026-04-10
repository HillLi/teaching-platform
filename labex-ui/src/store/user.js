import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)

  async function login(account, password, type) {
    const res = await api.login({ account, password, type })
    user.value = res.data
    return res.data
  }

  async function logout() {
    await api.logout()
    user.value = null
  }

  async function fetchUser() {
    try {
      const res = await api.current()
      user.value = res.data
      return res.data
    } catch {
      user.value = null
      return null
    }
  }

  function isTeacher() {
    return user.value?.userType === 0
  }

  function isStudent() {
    return user.value?.userType === 1
  }

  return { user, login, logout, fetchUser, isTeacher, isStudent }
})
