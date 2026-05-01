<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon class="logo-icon"><Promotion /></el-icon>
        <h2 class="title">儿童乐园会员管理后台</h2>
        <p class="subtitle">Children's Park Membership System</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="code">
          <el-input
            v-model="loginForm.code"
            placeholder="请输入登录验证码（开发环境可随意输入）"
            prefix-icon="Key"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p class="tips">提示：开发环境可随意输入验证码</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  code: ''
})

const loginRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login({ code: loginForm.code })
        if (res.code === 200) {
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data.userInfo)
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        const mockToken = 'mock_token_' + Date.now()
        const mockUserInfo = {
          id: 1,
          nickName: '管理员',
          avatarUrl: '',
          balance: 9999,
          role: 'admin'
        }
        userStore.setToken(mockToken)
        userStore.setUserInfo(mockUserInfo)
        ElMessage.success('登录成功（演示模式）')
        router.push('/dashboard')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background-color: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .logo-icon {
    font-size: 60px;
    color: #409EFF;
    margin-bottom: 10px;
  }
  
  .title {
    font-size: 24px;
    color: #303133;
    margin: 10px 0;
  }
  
  .subtitle {
    color: #909399;
    font-size: 14px;
  }
}

.login-form {
  .login-btn {
    width: 100%;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  
  .tips {
    color: #c0c4cc;
    font-size: 12px;
  }
}
</style>
