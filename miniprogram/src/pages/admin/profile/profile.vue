<template>
  <view class="profile-container">
    <view class="user-card" v-if="userInfo">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatarUrl || '/static/icons/default-avatar.png'" mode="aspectFill"></image>
        <view class="info">
          <view class="nickname">{{ userInfo.nickName || '微信用户' }}</view>
          <view class="user-role">
            <text class="role-tag" v-if="userInfo.role === 'admin'">管理员</text>
            <text class="role-tag ordinary" v-else>普通会员</text>
          </view>
        </view>
      </view>
      
      <view class="balance-section" v-if="userInfo.role !== 'admin'">
        <view class="balance-item">
          <text class="balance-label">账户余额</text>
          <text class="balance-value">{{ balance }} 币</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section" v-if="userInfo.role === 'admin'">
      <view class="section-title">管理员功能</view>
      
      <view class="menu-list">
        <view class="menu-item" @tap="goToScan">
          <view class="menu-left">
            <text class="menu-icon">📷</text>
            <text class="menu-name">扫码扣币</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @tap="goToProjects">
          <view class="menu-left">
            <text class="menu-icon">🎮</text>
            <text class="menu-name">项目管理</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @tap="goToQRCodeSettings">
          <view class="menu-left">
            <text class="menu-icon">⏱️</text>
            <text class="menu-name">二维码有效期设置</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="section-title">会员功能</view>
      
      <view class="menu-list">
        <view class="menu-item" @tap="goToRecharge">
          <view class="menu-left">
            <text class="menu-icon">💰</text>
            <text class="menu-name">充值中心</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @tap="goToQRCode">
          <view class="menu-left">
            <text class="menu-icon">📱</text>
            <text class="menu-name">我的二维码</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @tap="goToRecords">
          <view class="menu-left">
            <text class="menu-icon">📋</text>
            <text class="menu-name">消费记录</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="section-title">其他</view>
      
      <view class="menu-list">
        <view class="menu-item" @tap="showAbout">
          <view class="menu-left">
            <text class="menu-icon">ℹ️</text>
            <text class="menu-name">关于我们</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <view class="menu-item" @tap="showFeedback">
          <view class="menu-left">
            <text class="menu-icon">💬</text>
            <text class="menu-name">意见反馈</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>
    
    <view class="logout-section">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>
    
    <view class="qrcode-validity-modal" v-if="showQRCodeModal">
      <view class="modal-overlay" @tap="closeQRCodeModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">二维码有效期设置</text>
          <text class="modal-close" @tap="closeQRCodeModal">×</text>
        </view>
        
        <view class="modal-body">
          <view class="input-section">
            <text class="input-label">设置有效期（分钟）</text>
            <view class="input-group">
              <input 
                type="number" 
                v-model="qrcodeValidityInput" 
                placeholder="请输入有效期"
                class="validity-input"
              />
              <text class="input-unit">分钟</text>
            </view>
          </view>
          
          <view class="preset-section">
            <text class="preset-label">快速选择</text>
            <view class="preset-buttons">
              <view 
                class="preset-btn" 
                :class="{ active: qrcodeValidityInput === '5' }"
                @tap="setPresetValidity('5')"
              >
                5分钟
              </view>
              <view 
                class="preset-btn" 
                :class="{ active: qrcodeValidityInput === '10' }"
                @tap="setPresetValidity('10')"
              >
                10分钟
              </view>
              <view 
                class="preset-btn" 
                :class="{ active: qrcodeValidityInput === '30' }"
                @tap="setPresetValidity('30')"
              >
                30分钟
              </view>
              <view 
                class="preset-btn" 
                :class="{ active: qrcodeValidityInput === '60' }"
                @tap="setPresetValidity('60')"
              >
                60分钟
              </view>
            </view>
          </view>
          
          <view class="current-setting">
            <text class="setting-label">当前设置：</text>
            <text class="setting-value">{{ qrcodeValidity }}分钟</text>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel" @tap="closeQRCodeModal">取消</button>
          <button class="modal-btn confirm" @tap="saveQRCodeValidity">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo } from '@/api/auth.js'
import { getQRCodeValidity, updateQRCodeValidity } from '@/api/admin.js'

export default {
  data() {
    return {
      userInfo: {},
      balance: 0,
      showQRCodeModal: false,
      qrcodeValidity: 5,
      qrcodeValidityInput: '5'
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadUserInfo(),
        this.loadQRCodeValidity()
      ])
    },
    
    async loadUserInfo() {
      try {
        const storedUserInfo = uni.getStorageSync('userInfo')
        if (storedUserInfo) {
          this.userInfo = storedUserInfo
          this.balance = storedUserInfo.balance || 0
        }
        
        const result = await getUserInfo()
        this.userInfo = result
        this.balance = result.balance || 0
        uni.setStorageSync('userInfo', result)
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    
    async loadQRCodeValidity() {
      try {
        const result = await getQRCodeValidity()
        this.qrcodeValidity = result.validityTime
        this.qrcodeValidityInput = String(result.validityTime)
      } catch (error) {
        console.error('加载二维码有效期设置失败:', error)
      }
    },
    
    goToScan() {
      uni.navigateTo({
        url: '/pages/admin/scan/scan'
      })
    },
    
    goToProjects() {
      uni.navigateTo({
        url: '/pages/admin/projects/projects'
      })
    },
    
    goToQRCodeSettings() {
      this.qrcodeValidityInput = String(this.qrcodeValidity)
      this.showQRCodeModal = true
    },
    
    closeQRCodeModal() {
      this.showQRCodeModal = false
    },
    
    setPresetValidity(value) {
      this.qrcodeValidityInput = value
    },
    
    async saveQRCodeValidity() {
      const validityTime = parseInt(this.qrcodeValidityInput)
      
      if (!validityTime || validityTime <= 0) {
        uni.showToast({
          title: '请输入有效的有效期',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({
        title: '保存中...'
      })
      
      try {
        await updateQRCodeValidity(validityTime)
        this.qrcodeValidity = validityTime
        this.showQRCodeModal = false
        
        uni.hideLoading()
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        console.error('保存二维码有效期失败:', error)
      }
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
    
    showAbout() {
      uni.showModal({
        title: '关于我们',
        content: '儿童乐园会员小程序 v1.0.0\n\n让孩子的快乐更简单！\n\n客服热线：400-123-4567',
        showCancel: false
      })
    },
    
    showFeedback() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  padding-bottom: 40rpx;
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  color: #ffffff;
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
  margin-bottom: 10rpx;
}

.user-role {
  display: flex;
}

.role-tag {
  background-color: rgba(255, 255, 255, 0.3);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.role-tag.ordinary {
  background-color: rgba(255, 255, 255, 0.2);
}

.balance-section {
  display: flex;
  justify-content: space-around;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.balance-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-label {
  font-size: 26rpx;
  opacity: 0.9;
  margin-bottom: 8rpx;
}

.balance-value {
  font-size: 40rpx;
  font-weight: bold;
}

.menu-section {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 0 30rpx;
}

.section-title {
  font-size: 28rpx;
  color: #909399;
  padding: 20rpx 0;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-name {
  font-size: 30rpx;
  color: #333333;
}

.menu-arrow {
  font-size: 36rpx;
  color: #c0c4cc;
}

.logout-section {
  padding: 40rpx 40rpx;
}

.logout-btn {
  background-color: #ffffff;
  color: #F56C6C;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 30rpx;
  border: none;
}

.logout-btn::after {
  border: none;
}

.qrcode-validity-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 600rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.modal-close {
  font-size: 40rpx;
  color: #909399;
}

.modal-body {
  padding: 30rpx;
}

.input-section {
  margin-bottom: 30rpx;
}

.input-label {
  font-size: 28rpx;
  color: #606266;
  margin-bottom: 16rpx;
  display: block;
}

.input-group {
  display: flex;
  align-items: center;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  padding: 0 20rpx;
}

.validity-input {
  flex: 1;
  font-size: 32rpx;
  padding: 20rpx 0;
}

.input-unit {
  font-size: 28rpx;
  color: #909399;
}

.preset-section {
  margin-bottom: 30rpx;
}

.preset-label {
  font-size: 28rpx;
  color: #606266;
  margin-bottom: 16rpx;
  display: block;
}

.preset-buttons {
  display: flex;
  flex-wrap: wrap;
  margin: -10rpx;
}

.preset-btn {
  width: calc(50% - 20rpx);
  margin: 10rpx;
  padding: 20rpx;
  text-align: center;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #606266;
}

.preset-btn.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
  color: #409EFF;
}

.current-setting {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #f5f7fa;
  border-radius: 8rpx;
}

.setting-label {
  font-size: 26rpx;
  color: #909399;
}

.setting-value {
  font-size: 28rpx;
  color: #409EFF;
  font-weight: bold;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  padding: 30rpx;
  font-size: 30rpx;
  border: none;
  background: none;
}

.modal-btn.cancel {
  color: #606266;
  border-right: 1rpx solid #f0f0f0;
}

.modal-btn.confirm {
  color: #409EFF;
}
</style>
