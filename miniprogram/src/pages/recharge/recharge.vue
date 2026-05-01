<template>
  <view class="recharge-container">
    <view class="balance-card">
      <view class="balance-label">当前余额</view>
      <view class="balance-value">
        <text class="amount">{{ balance }}</text>
        <text class="unit">币</text>
      </view>
      <view class="balance-tip">1元 = 1币</view>
    </view>
    
    <view class="section">
      <view class="section-title">选择充值金额</view>
      <view class="amount-grid">
        <view 
          class="amount-item" 
          :class="{ active: selectedAmount === option.amount }"
          v-for="option in rechargeOptions" 
          :key="option.id"
          @tap="selectAmount(option)"
        >
          <view class="amount-value">{{ option.amount }}币</view>
          <view class="amount-price">¥{{ option.price }}</view>
          <view class="amount-badge" v-if="option.gift">送{{ option.gift }}币</view>
        </view>
      </view>
    </view>
    
    <view class="section">
      <view class="section-title">自定义金额</view>
      <view class="custom-input">
        <text class="input-prefix">¥</text>
        <input 
          type="number" 
          v-model="customAmount" 
          placeholder="请输入充值金额" 
          class="amount-input"
          @input="onCustomAmountInput"
        />
        <text class="input-suffix">元 = {{ customAmount || 0 }}币</text>
      </view>
    </view>
    
    <view class="section">
      <view class="section-title">支付方式</view>
      <view class="payment-list">
        <view 
          class="payment-item" 
          :class="{ active: selectedPayment === 'wechat' }"
          @tap="selectPayment('wechat')"
        >
          <view class="payment-info">
            <text class="payment-icon">📱</text>
            <view class="payment-text">
              <view class="payment-name">微信支付</view>
              <view class="payment-desc">推荐使用</view>
            </view>
          </view>
          <view class="payment-check" :class="{ checked: selectedPayment === 'wechat' }"></view>
        </view>
      </view>
    </view>
    
    <view class="recharge-bottom">
      <view class="pay-info">
        <text class="pay-label">支付金额：</text>
        <text class="pay-amount">¥{{ totalAmount }}</text>
      </view>
      <button class="pay-btn" @tap="handleRecharge">立即充值</button>
    </view>
  </view>
</template>

<script>
import { getRechargeOptions, createRechargeOrder } from '@/api/recharge.js'

export default {
  data() {
    return {
      balance: 0,
      rechargeOptions: [],
      selectedAmount: 0,
      customAmount: '',
      selectedPayment: 'wechat',
      totalAmount: 0
    }
  },
  onShow() {
    this.loadData()
  },
  computed: {
    selectedOption() {
      return this.rechargeOptions.find(opt => opt.amount === this.selectedAmount)
    }
  },
  methods: {
    async loadData() {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        this.balance = userInfo.balance || 0
      }
      
      try {
        const result = await getRechargeOptions()
        this.rechargeOptions = result
      } catch (error) {
        console.error('加载充值选项失败:', error)
        this.rechargeOptions = [
          { id: 1, amount: 10, price: 10, gift: 0 },
          { id: 2, amount: 30, price: 30, gift: 3 },
          { id: 3, amount: 50, price: 50, gift: 6 },
          { id: 4, amount: 100, price: 100, gift: 15 },
          { id: 5, amount: 200, price: 200, gift: 40 },
          { id: 6, amount: 500, price: 500, gift: 100 }
        ]
      }
    },
    
    selectAmount(option) {
      this.selectedAmount = option.amount
      this.customAmount = ''
      this.calculateTotal()
    },
    
    onCustomAmountInput(e) {
      this.customAmount = e.detail.value
      if (this.customAmount) {
        this.selectedAmount = 0
      }
      this.calculateTotal()
    },
    
    calculateTotal() {
      if (this.selectedAmount > 0) {
        const option = this.selectedOption
        this.totalAmount = option ? option.price : 0
      } else if (this.customAmount) {
        this.totalAmount = parseFloat(this.customAmount) || 0
      } else {
        this.totalAmount = 0
      }
    },
    
    selectPayment(payment) {
      this.selectedPayment = payment
    },
    
    async handleRecharge() {
      if (this.totalAmount <= 0) {
        uni.showToast({
          title: '请选择或输入充值金额',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({
        title: '正在充值...'
      })
      
      try {
        const amount = this.selectedAmount > 0 ? this.selectedAmount : parseFloat(this.customAmount)
        const result = await createRechargeOrder(amount)
        
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: result.timeStamp,
          nonceStr: result.nonceStr,
          package: result.package,
          signType: result.signType,
          paySign: result.paySign,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '充值成功',
              icon: 'success'
            })
            
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          },
          fail: (err) => {
            uni.hideLoading()
            if (err.errMsg.indexOf('cancel') === -1) {
              uni.showToast({
                title: '支付失败',
                icon: 'none'
              })
            }
          }
        })
      } catch (error) {
        uni.hideLoading()
        console.error('充值失败:', error)
        uni.showToast({
          title: '充值失败，请稍后重试',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.recharge-container {
  min-height: 100vh;
  padding-bottom: 140rpx;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx;
  text-align: center;
  color: #ffffff;
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

.amount {
  font-size: 72rpx;
  font-weight: bold;
}

.unit {
  font-size: 28rpx;
  margin-left: 10rpx;
}

.balance-tip {
  font-size: 24rpx;
  opacity: 0.8;
  margin-top: 10rpx;
}

.section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.amount-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -10rpx;
}

.amount-item {
  width: calc(33.33% - 20rpx);
  margin: 10rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 20rpx 10rpx;
  text-align: center;
  position: relative;
}

.amount-item.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.amount-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 6rpx;
}

.amount-price {
  font-size: 24rpx;
  color: #909399;
}

.amount-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background-color: #F56C6C;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.custom-input {
  display: flex;
  align-items: center;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 20rpx;
}

.input-prefix {
  font-size: 36rpx;
  font-weight: bold;
  color: #409EFF;
  margin-right: 10rpx;
}

.amount-input {
  flex: 1;
  font-size: 36rpx;
  color: #333333;
}

.input-suffix {
  font-size: 26rpx;
  color: #909399;
  margin-left: 10rpx;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.payment-item:last-child {
  border-bottom: none;
}

.payment-info {
  display: flex;
  align-items: center;
}

.payment-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.payment-text {
  display: flex;
  flex-direction: column;
}

.payment-name {
  font-size: 30rpx;
  color: #333333;
  margin-bottom: 4rpx;
}

.payment-desc {
  font-size: 24rpx;
  color: #909399;
}

.payment-check {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 50%;
}

.payment-check.checked {
  border-color: #409EFF;
  background-color: #409EFF;
  position: relative;
}

.payment-check.checked::after {
  content: '';
  position: absolute;
  top: 10rpx;
  left: 6rpx;
  width: 20rpx;
  height: 12rpx;
  border-left: 4rpx solid #ffffff;
  border-bottom: 4rpx solid #ffffff;
  transform: rotate(-45deg);
}

.recharge-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.pay-info {
  display: flex;
  align-items: baseline;
}

.pay-label {
  font-size: 28rpx;
  color: #666666;
}

.pay-amount {
  font-size: 40rpx;
  font-weight: bold;
  color: #F56C6C;
}

.pay-btn {
  background-color: #409EFF;
  color: #ffffff;
  border-radius: 50rpx;
  padding: 20rpx 60rpx;
  font-size: 30rpx;
  border: none;
}

.pay-btn::after {
  border: none;
}
</style>
