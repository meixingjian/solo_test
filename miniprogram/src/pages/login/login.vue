<template>
  <view class="login-container">
    <view class="logo-section">
      <view class="logo">🎠</view>
      <view class="app-name">儿童乐园会员</view>
      <view class="app-desc">充值1元=1币，扫码即可畅玩</view>
    </view>
    
    <view class="login-section">
      <button class="wx-login-btn" @tap="handleWxLogin">
        <text class="btn-icon">📱</text>
        微信一键登录
      </button>
      
      <view class="agreement">
        <checkbox :checked="agreed" @tap="toggleAgreement" color="#409EFF"></checkbox>
        <text class="agreement-text">我已阅读并同意</text>
        <text class="agreement-link">用户协议</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link">隐私政策</text>
      </view>
    </view>
    
    <view class="footer">
      <text class="footer-text">欢迎来到儿童乐园</text>
    </view>
  </view>
</template>

<script>
import { wxLogin } from '@/api/auth.js'

export default {
  data() {
    return {
      agreed: false
    }
  },
  methods: {
    toggleAgreement() {
      this.agreed = !this.agreed
    },
    
    handleWxLogin() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先阅读并同意用户协议',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({
        title: '登录中...'
      })
      
      uni.login({
        provider: 'weixin',
        success: async (loginRes) => {
          try {
            const result = await wxLogin(loginRes.code)
            uni.setStorageSync('token', result.token)
            uni.setStorageSync('userInfo', result.userInfo)
            
            uni.hideLoading()
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            })
            
            setTimeout(() => {
              uni.switchTab({
                url: '/pages/index/index'
              })
            }, 1000)
          } catch (error) {
            uni.hideLoading()
            console.error('登录失败:', error)
          }
        },
        fail: (err) => {
          uni.hideLoading()
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          })
          console.error('uni.login失败:', err)
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 40rpx;
}

.logo-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100rpx;
}

.logo {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.app-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 20rpx;
}

.app-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-section {
  padding: 40rpx 0;
}

.wx-login-btn {
  background-color: #07c160;
  color: #ffffff;
  border-radius: 50rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  border: none;
}

.wx-login-btn::after {
  border: none;
}

.btn-icon {
  font-size: 40rpx;
  margin-right: 15rpx;
}

.agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.agreement-text {
  margin: 0 5rpx;
}

.agreement-link {
  color: #ffffff;
  text-decoration: underline;
}

.footer {
  text-align: center;
  padding-bottom: 40rpx;
}

.footer-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}
</style>
