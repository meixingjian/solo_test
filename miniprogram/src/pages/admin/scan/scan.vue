<template>
  <view class="scan-container">
    <view class="scan-header">
      <view class="header-title">扫码扣币</view>
      <view class="header-tip">请扫描用户的动态二维码</view>
    </view>
    
    <view class="scan-area" v-if="!showConfirm">
      <view class="scan-box">
        <view class="scan-frame">
          <view class="corner top-left"></view>
          <view class="corner top-right"></view>
          <view class="corner bottom-left"></view>
          <view class="corner bottom-right"></view>
          <view class="scan-line" :class="{ active: isScanning }"></view>
        </view>
        
        <button class="scan-btn" @tap="startScan">
          <text class="btn-icon">📷</text>
          <text class="btn-text">点击扫码</text>
        </button>
      </view>
      
      <view class="scan-tips">
        <view class="tip-item">
          <text class="tip-icon">1️⃣</text>
          <text class="tip-text">请让用户出示动态二维码</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">2️⃣</text>
          <text class="tip-text">将二维码放入扫描框内</text>
        </view>
        <view class="tip-item">
          <text class="tip-icon">3️⃣</text>
          <text class="tip-text">选择项目后确认扣费</text>
        </view>
      </view>
    </view>
    
    <view class="confirm-section" v-else>
      <view class="user-card">
        <view class="user-info">
          <image class="avatar" :src="userDetail.avatarUrl || '/static/icons/default-avatar.png'" mode="aspectFill"></image>
          <view class="info">
            <view class="nickname">{{ userDetail.nickName || '微信用户' }}</view>
            <view class="user-id">会员ID: {{ userDetail.id || '--' }}</view>
          </view>
        </view>
        
        <view class="balance-info">
          <view class="balance-item">
            <text class="balance-label">账户余额</text>
            <text class="balance-value">{{ userDetail.balance || 0 }} 币</text>
          </view>
        </view>
      </view>
      
      <view class="project-section">
        <view class="section-title">选择娱乐项目</view>
        <view class="project-list">
          <view 
            class="project-item" 
            :class="{ selected: selectedProjectId === project.id }"
            v-for="project in projects" 
            :key="project.id"
            @tap="selectProject(project)"
          >
            <view class="project-info">
              <view class="project-name">{{ project.name }}</view>
              <view class="project-desc">{{ project.description || '' }}</view>
            </view>
            <view class="project-price">
              <text class="price-value">{{ project.price }}</text>
              <text class="price-unit">币</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="quantity-section" v-if="selectedProject">
        <view class="section-title">选择次数</view>
        <view class="quantity-selector">
          <view class="quantity-btn" @tap="decreaseQuantity">-</view>
          <view class="quantity-value">{{ quantity }}</view>
          <view class="quantity-btn" @tap="increaseQuantity">+</view>
        </view>
      </view>
      
      <view class="summary-section" v-if="selectedProject">
        <view class="summary-row">
          <text class="summary-label">项目名称：</text>
          <text class="summary-value">{{ selectedProject.name }}</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">单次价格：</text>
          <text class="summary-value">{{ selectedProject.price }}币</text>
        </view>
        <view class="summary-row">
          <text class="summary-label">购买次数：</text>
          <text class="summary-value">{{ quantity }}次</text>
        </view>
        <view class="summary-row total">
          <text class="summary-label">总扣币数：</text>
          <text class="summary-value total-value">{{ totalAmount }}币</text>
        </view>
      </view>
      
      <view class="action-buttons">
        <button class="action-btn cancel" @tap="cancelScan">取消</button>
        <button 
          class="action-btn confirm" 
          :class="{ disabled: !canConfirm }"
          @tap="confirmDeduct"
          :disabled="!canConfirm"
        >
          确认扣币
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { scanQRCode, deductCoins, getProjects } from '@/api/admin.js'

export default {
  data() {
    return {
      isScanning: false,
      showConfirm: false,
      qrcodeData: null,
      userDetail: {},
      projects: [],
      selectedProjectId: null,
      selectedProject: null,
      quantity: 1
    }
  },
  computed: {
    totalAmount() {
      if (!this.selectedProject) return 0
      return this.selectedProject.price * this.quantity
    },
    canConfirm() {
      return this.selectedProject && 
             this.totalAmount > 0 && 
             (this.userDetail.balance || 0) >= this.totalAmount
    }
  },
  onShow() {
    this.loadProjects()
  },
  methods: {
    async loadProjects() {
      try {
        const result = await getProjects()
        this.projects = result
      } catch (error) {
        console.error('加载项目列表失败:', error)
        this.projects = [
          { id: 1, name: '旋转木马', price: 5, description: '经典旋转木马，适合所有年龄段' },
          { id: 2, name: '碰碰车', price: 3, description: '刺激有趣的碰碰车' },
          { id: 3, name: '过山车', price: 8, description: '惊险刺激的过山车' },
          { id: 4, name: '海盗船', price: 6, description: '摇摆的海盗船' }
        ]
      }
    },
    
    startScan() {
      this.isScanning = true
      
      uni.scanCode({
        success: async (res) => {
          this.isScanning = false
          
          try {
            const result = await scanQRCode(res.result)
            this.qrcodeData = res.result
            this.userDetail = result.userInfo
            this.showConfirm = true
          } catch (error) {
            console.error('扫码失败:', error)
            uni.showToast({
              title: '二维码无效或已过期',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          this.isScanning = false
          if (err.errMsg.indexOf('cancel') === -1) {
            uni.showToast({
              title: '扫码失败',
              icon: 'none'
            })
          }
        }
      })
    },
    
    selectProject(project) {
      this.selectedProjectId = project.id
      this.selectedProject = project
      this.quantity = 1
    },
    
    increaseQuantity() {
      if (this.quantity < 10) {
        this.quantity++
      }
    },
    
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--
      }
    },
    
    cancelScan() {
      this.showConfirm = false
      this.qrcodeData = null
      this.userDetail = {}
      this.selectedProjectId = null
      this.selectedProject = null
      this.quantity = 1
    },
    
    async confirmDeduct() {
      if (!this.canConfirm) return
      
      uni.showModal({
        title: '确认扣币',
        content: `确认从 ${this.userDetail.nickName || '用户'} 账户扣除 ${this.totalAmount} 币？\n项目：${this.selectedProject.name} x ${this.quantity}`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({
              title: '扣费中...'
            })
            
            try {
              await deductCoins({
                qrcodeData: this.qrcodeData,
                projectId: this.selectedProject.id,
                quantity: this.quantity,
                amount: this.totalAmount
              })
              
              uni.hideLoading()
              uni.showToast({
                title: '扣费成功',
                icon: 'success'
              })
              
              setTimeout(() => {
                this.cancelScan()
              }, 1500)
            } catch (error) {
              uni.hideLoading()
              console.error('扣费失败:', error)
              uni.showToast({
                title: '扣费失败，请重试',
                icon: 'none'
              })
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.scan-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.scan-header {
  background-color: #ffffff;
  padding: 30rpx;
  text-align: center;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 10rpx;
}

.header-tip {
  font-size: 26rpx;
  color: #909399;
}

.scan-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.scan-box {
  position: relative;
  width: 500rpx;
  height: 500rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-frame {
  width: 100%;
  height: 100%;
  position: relative;
}

.corner {
  position: absolute;
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid #409EFF;
}

.corner.top-left {
  top: 0;
  left: 0;
  border-right: none;
  border-bottom: none;
}

.corner.top-right {
  top: 0;
  right: 0;
  border-left: none;
  border-bottom: none;
}

.corner.bottom-left {
  bottom: 0;
  left: 0;
  border-right: none;
  border-top: none;
}

.corner.bottom-right {
  bottom: 0;
  right: 0;
  border-left: none;
  border-top: none;
}

.scan-line {
  position: absolute;
  left: 40rpx;
  right: 40rpx;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, #409EFF, transparent);
  top: 40rpx;
}

.scan-line.active {
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 40rpx;
  }
  50% {
    top: calc(100% - 80rpx);
  }
  100% {
    top: 40rpx;
  }
}

.scan-btn {
  position: absolute;
  bottom: -120rpx;
  background-color: #409EFF;
  color: #ffffff;
  border-radius: 50rpx;
  padding: 24rpx 60rpx;
  display: flex;
  align-items: center;
  border: none;
}

.scan-btn::after {
  border: none;
}

.btn-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.btn-text {
  font-size: 30rpx;
}

.scan-tips {
  margin-top: 160rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-left: 30rpx;
  margin-right: 30rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #606266;
  line-height: 1.6;
  flex: 1;
}

.confirm-section {
  padding: 20rpx;
  padding-bottom: 200rpx;
}

.user-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.info {
  flex: 1;
}

.nickname {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.user-id {
  font-size: 24rpx;
  color: #909399;
}

.balance-info {
  display: flex;
  justify-content: space-around;
}

.balance-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-label {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 8rpx;
}

.balance-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #409EFF;
}

.project-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.project-list {
  display: flex;
  flex-direction: column;
}

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.project-item:last-child {
  border-bottom: none;
}

.project-item.selected {
  background-color: #ecf5ff;
  margin: 0 -30rpx;
  padding-left: 30rpx;
  padding-right: 30rpx;
}

.project-info {
  flex: 1;
}

.project-name {
  font-size: 30rpx;
  color: #333333;
  margin-bottom: 6rpx;
}

.project-desc {
  font-size: 24rpx;
  color: #909399;
}

.project-price {
  display: flex;
  align-items: baseline;
}

.price-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #409EFF;
}

.price-unit {
  font-size: 26rpx;
  color: #409EFF;
  margin-left: 4rpx;
}

.quantity-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.quantity-selector {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-btn {
  width: 80rpx;
  height: 80rpx;
  background-color: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  color: #606266;
}

.quantity-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin: 0 40rpx;
  min-width: 80rpx;
  text-align: center;
}

.summary-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.summary-row {
  display: flex;
  margin-bottom: 16rpx;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-row.total {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 20rpx;
}

.summary-label {
  font-size: 28rpx;
  color: #909399;
  width: 180rpx;
}

.summary-value {
  font-size: 28rpx;
  color: #333333;
  flex: 1;
}

.summary-value.total-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #F56C6C;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  display: flex;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.action-btn {
  flex: 1;
  padding: 24rpx;
  font-size: 30rpx;
  border-radius: 12rpx;
  border: none;
}

.action-btn::after {
  border: none;
}

.action-btn.cancel {
  background-color: #f5f7fa;
  color: #606266;
  margin-right: 20rpx;
}

.action-btn.confirm {
  background-color: #409EFF;
  color: #ffffff;
}

.action-btn.confirm.disabled {
  background-color: #c0c4cc;
}
</style>
