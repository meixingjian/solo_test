import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const login = async (loginForm) => {
    try {
      const res = await login(loginForm)
      if (res.code === 200) {
        setToken(res.data.token)
        setUserInfo(res.data.userInfo)
        return true
      } else {
        ElMessage.error(res.message || '登录失败')
        return false
      }
    } catch (error) {
      ElMessage.error('登录失败：' + error.message)
      return false
    }
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        setUserInfo(res.data)
        return true
      }
      return false
    } catch (error) {
      return false
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    login,
    fetchUserInfo,
    logout
  }
})
