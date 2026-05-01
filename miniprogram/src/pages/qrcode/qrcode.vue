<template>
  <view class="qrcode-container">
    <view class="qrcode-card">
      <view class="user-header">
        <image class="avatar" :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'" mode="aspectFill"></image>
        <view class="user-info">
          <view class="nickname">{{ userInfo.nickName || '微信用户' }}</view>
          <view class="user-id">会员ID: {{ userInfo.id || '--' }}</view>
        </view>
      </view>
      
      <view class="balance-section">
        <view class="balance-label">当前余额</view>
        <view class="balance-value">
          <text class="amount">{{ balance }}</text>
          <text class="unit">币</text>
        </view>
      </view>
      
      <view class="qrcode-section">
        <view class="qrcode-box">
          <image 
            v-if="qrcodeImage" 
            :src="qrcodeImage" 
            class="qrcode-image"
            mode="aspectFit"
          ></image>
          <view v-else class="qrcode-placeholder">
            <text class="placeholder-icon">📱</text>
            <text class="placeholder-text">加载中...</text>
          </view>
        </view>
        
        <view class="qrcode-status" v-if="qrcodeData">
          <view class="status-row">
            <text class="status-label">二维码状态：</text>
            <text class="status-value" :class="{ active: isActive }">
              {{ isActive ? '有效' : '已过期' }}
            </text>
          </view>
          <view class="status-row" v-if="expireTime">
            <text class="status-label">有效时间：</text>
            <text class="status-value">{{ formatTime(expireTime) }}</text>
          </view>
          <view class="status-row" v-if="countdown > 0">
            <text class="status-label">剩余时间：</text>
            <text class="countdown">{{ formatCountdown(countdown) }}</text>
          </view>
        </view>
      </view>
      
      <view class="action-section">
        <view class="action-btn" @tap="refreshQRCode">
          <text class="btn-icon">🔄</text>
          <text class="btn-text">刷新二维码</text>
        </view>
        <view class="action-btn" @tap="goToRecharge">
          <text class="btn-icon">💰</text>
          <text class="btn-text">立即充值</text>
        </view>
      </view>
    </view>
    
    <view class="tips-section">
      <view class="tips-title">使用说明</view>
      <view class="tips-list">
        <view class="tip-item">
          <text class="tip-icon">1️⃣</text>
          <text class="tip-text">出示此二维码给工作人员扫码</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">2️⃣</text>
          <text class="tip-text">工作人员确认项目名称和扣费数量</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">3️⃣</text>
          <text class="tip-text">确认无误后完成扣费</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">⚠️</text>
          <text class="tip-text">二维码有效时间可后台设置，过期请刷新</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { generateQRCode, getQRCodeInfo, refreshQRCode as apiRefreshQRCode } from '@/api/qrcode.js'
import { getUserInfo } from '@/api/auth.js'

export default {
  data() {
    return {
      userInfo: {},
      balance: 0,
      qrcodeImage: '',
      qrcodeData: null,
      isActive: false,
      expireTime: null,
      countdown: 0,
      countdownTimer: null
    }
  },
  onShow() {
    this.loadData()
  },
  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadUserInfo(),
        this.loadQRCode()
      ])
    },
    
    async loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          this.userInfo = userInfo
          this.balance = userInfo.balance || 0
        }
        
        const result = await getUserInfo()
        this.userInfo = result
        this.balance = result.balance || 0
        uni.setStorageSync('userInfo', result)
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    
    async loadQRCode() {
      try {
        const result = await getQRCodeInfo()
        
        if (result && result.isActive) {
          this.qrcodeData = result
          this.qrcodeImage = result.qrcodeImage
          this.isActive = result.isActive
          this.expireTime = result.expireTime
          this.startCountdown(result.expireTime)
        } else {
          await this.generateNewQRCode()
        }
      } catch (error) {
        console.error('获取二维码信息失败:', error)
        await this.generateNewQRCode()
      }
    },
    
    async generateNewQRCode() {
      try {
        const result = await generateQRCode()
        this.qrcodeData = result
        this.qrcodeImage = result.qrcodeImage
        this.isActive = result.isActive
        this.expireTime = result.expireTime
        this.startCountdown(result.expireTime)
      } catch (error) {
        console.error('生成二维码失败:', error)
        uni.showToast({
          title: '生成二维码失败',
          icon: 'none'
        })
      }
    },
    
    async refreshQRCode() {
      uni.showLoading({
        title: '刷新中...'
      })
      
      try {
        const result = await apiRefreshQRCode()
        this.qrcodeData = result
        this.qrcodeImage = result.qrcodeImage
        this.isActive = result.isActive
        this.expireTime = result.expireTime
        this.startCountdown(result.expireTime)
        
        uni.hideLoading()
        uni.showToast({
          title: '刷新成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        console.error('刷新二维码失败:', error)
        uni.showToast({
          title: '刷新失败',
          icon: 'none'
        })
      }
    },
    
    startCountdown(expireTime) {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
      }
      
      this.updateCountdown(expireTime)
      
      this.countdownTimer = setInterval(() => {
        this.updateCountdown(expireTime)
      }, 1000)
    },
    
    updateCountdown(expireTime) {
      const now = Date.now()
      const expire = new Date(expireTime).getTime()
      const remaining = Math.max(0, Math.floor((expire - now) / 1000))
      
      this.countdown = remaining
      this.isActive = remaining > 0
    },
    
    goToRecharge() {
      uni.navigateTo({
        url: '/pages/recharge/recharge'
      })
    },
    
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      return `${month}-${day} ${hour}:${minute}`
    },
    
    formatCountdown(seconds) {
      if (seconds <= 0) return '已过期'
      const minutes = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.qrcode-container {
  min-height: 100vh;
  padding: 20rpx;
}

.qrcode-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 6rpx;
}

.user-id {
  font-size: 24rpx;
  color: #909399;
}

.balance-section {
  text-align: center;
  margin-bottom: 30rpx;
}

.balance-label {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 10rpx;
}

.balance-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.amount {
  font-size: 56rpx;
  font-weight: bold;
  color: #409EFF;
}

.unit {
  font-size: 28rpx;
  color: #409EFF;
  margin-left: 8rpx;
}

.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
}

.qrcode-box {
  width: 400rpx;
  height: 400rpx;
  background-color: #f5f7fa;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed #e0e0e0;
}

.qrcode-image {
  width: 360rpx;
  height: 360rpx;
}

.qrcode-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.placeholder-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.placeholder-text {
  font-size: 26rpx;
  color: #909399;
}

.qrcode-status {
  margin-top: 20rpx;
  width: 100%;
  padding: 20rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
}

.status-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.status-row:last-child {
  margin-bottom: 0;
}

.status-label {
  font-size: 26rpx;
  color: #606266;
}

.status-value {
  font-size: 26rpx;
  color: #909399;
}

.status-value.active {
  color: #67C23A;
  font-weight: bold;
}

.countdown {
  font-size: 32rpx;
  font-weight: bold;
  color: #409EFF;
}

.action-section {
  display: flex;
  justify-content: space-around;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
}

.btn-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.btn-text {
  font-size: 26rpx;
  color: #333333;
}

.tips-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.tips-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15rpx;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  font-size: 28rpx;
  margin-right: 10rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #606266;
  line-height: 1.6;
  flex: 1;
}
</style>
