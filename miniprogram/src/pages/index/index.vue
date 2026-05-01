<template>
  <view class="index-container">
    <view class="user-card">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'" mode="aspectFill"></image>
        <view class="info">
          <view class="nickname">{{ userInfo.nickName || '微信用户' }}</view>
          <view class="user-id">会员ID: {{ userInfo.id || '--' }}</view>
        </view>
      </view>
      
      <view class="balance-section">
        <view class="balance-label">当前余额</view>
        <view class="balance-value">
          <text class="coin-icon">🎯</text>
          <text class="amount">{{ balance }}</text>
          <text class="unit">币</text>
        </view>
        <view class="balance-tip">1元 = 1币</view>
      </view>
      
      <view class="action-buttons">
        <view class="action-btn" @tap="goToRecharge">
          <text class="btn-icon">💰</text>
          <text class="btn-text">充值</text>
        </view>
        <view class="action-btn" @tap="goToQRCode">
          <text class="btn-icon">📱</text>
          <text class="btn-text">二维码</text>
        </view>
        <view class="action-btn" @tap="goToRecords">
          <text class="btn-icon">📋</text>
          <text class="btn-text">记录</text>
        </view>
      </view>
    </view>
    
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门项目</text>
        <text class="section-more" @tap="viewAllProjects">查看全部 ></text>
      </view>
      
      <view class="projects-grid">
        <view class="project-item" v-for="project in hotProjects" :key="project.id">
          <view class="project-icon">{{ project.icon }}</view>
          <view class="project-name">{{ project.name }}</view>
          <view class="project-price">
            <text class="price-value">{{ project.price }}</text>
            <text class="price-unit">币/次</text>
          </view>
        </view>
      </view>
    </view>
    
    <view class="section">
      <view class="section-header">
        <text class="section-title">最近消费</text>
      </view>
      
      <view class="records-list">
        <view class="record-item" v-for="record in recentRecords" :key="record.id">
          <view class="record-left">
            <view class="record-icon">🎮</view>
            <view class="record-info">
              <view class="record-name">{{ record.projectName }}</view>
              <view class="record-time">{{ formatTime(record.createTime) }}</view>
            </view>
          </view>
          <view class="record-right">
            <text class="record-amount">-{{ record.amount }}币</text>
          </view>
        </view>
        
        <view class="empty-record" v-if="recentRecords.length === 0">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无消费记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo } from '@/api/auth.js'
import { getProjects } from '@/api/admin.js'
import { getConsumeRecords } from '@/api/records.js'

export default {
  data() {
    return {
      userInfo: {},
      balance: 0,
      hotProjects: [],
      recentRecords: []
    }
  },
  onShow() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadUserInfo(),
        this.loadProjects(),
        this.loadRecentRecords()
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
    
    async loadProjects() {
      try {
        const result = await getProjects()
        this.hotProjects = result.slice(0, 4).map(project => ({
          ...project,
          icon: this.getProjectIcon(project.name)
        }))
      } catch (error) {
        console.error('加载项目列表失败:', error)
        this.hotProjects = [
          { id: 1, name: '旋转木马', price: 5, icon: '🎠' },
          { id: 2, name: '碰碰车', price: 3, icon: '🚗' },
          { id: 3, name: '过山车', price: 8, icon: '🎢' },
          { id: 4, name: '海盗船', price: 6, icon: '⛵' }
        ]
      }
    },
    
    async loadRecentRecords() {
      try {
        const result = await getConsumeRecords({ page: 1, size: 3 })
        this.recentRecords = result.records || []
      } catch (error) {
        console.error('加载消费记录失败:', error)
        this.recentRecords = []
      }
    },
    
    getProjectIcon(name) {
      const iconMap = {
        '旋转木马': '🎠',
        '碰碰车': '🚗',
        '过山车': '🎢',
        '海盗船': '⛵',
        '摩天轮': '🎡',
        '小火车': '🚂',
        '沙池': '🏖️',
        '滑梯': '🛝',
        '投篮机': '🏀',
        '娃娃机': '🧸'
      }
      return iconMap[name] || '🎮'
    },
    
    goToRecharge() {
      uni.navigateTo({
        url: '/pages/recharge/recharge'
      })
    },
    
    goToQRCode() {
      uni.switchTab({
        url: '/pages/qrcode/qrcode'
      })
    },
    
    goToRecords() {
      uni.switchTab({
        url: '/pages/records/records'
      })
    },
    
    viewAllProjects() {
      uni.navigateTo({
        url: '/pages/admin/projects/projects'
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
    }
  }
}
</script>

<style scoped>
.index-container {
  min-height: 100vh;
  padding: 20rpx;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  color: #ffffff;
  margin-bottom: 30rpx;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
}

.info {
  flex: 1;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.user-id {
  font-size: 24rpx;
  opacity: 0.8;
}

.balance-section {
  text-align: center;
  margin-bottom: 40rpx;
}

.balance-label {
  font-size: 28rpx;
  opacity: 0.9;
  margin-bottom: 10rpx;
}

.balance-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.coin-icon {
  font-size: 48rpx;
  margin-right: 10rpx;
}

.amount {
  font-size: 72rpx;
  font-weight: bold;
}

.unit {
  font-size: 28rpx;
  margin-left: 5rpx;
}

.balance-tip {
  font-size: 24rpx;
  opacity: 0.8;
  margin-top: 10rpx;
}

.action-buttons {
  display: flex;
  justify-content: space-around;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  padding: 20rpx 40rpx;
}

.btn-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.btn-text {
  font-size: 26rpx;
}

.section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.section-more {
  font-size: 26rpx;
  color: #909399;
}

.projects-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -10rpx;
}

.project-item {
  width: calc(50% - 20rpx);
  margin: 10rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  padding: 20rpx;
  text-align: center;
}

.project-icon {
  font-size: 60rpx;
  margin-bottom: 10rpx;
}

.project-name {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 8rpx;
}

.project-price {
  font-size: 24rpx;
}

.price-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #409EFF;
}

.price-unit {
  color: #909399;
}

.records-list {
  min-height: 200rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  display: flex;
  align-items: center;
}

.record-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.record-info {
  display: flex;
  flex-direction: column;
}

.record-name {
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 6rpx;
}

.record-time {
  font-size: 24rpx;
  color: #909399;
}

.record-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #F56C6C;
}

.empty-record {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #909399;
}
</style>
