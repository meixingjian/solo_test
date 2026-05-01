<template>
  <view class="records-container">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'consume' }"
        @tap="switchTab('consume')"
      >
        消费记录
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'recharge' }"
        @tap="switchTab('recharge')"
      >
        充值记录
      </view>
    </view>
    
    <view class="filter-section">
      <picker 
        mode="date" 
        :value="filterDate" 
        @change="onDateChange"
        class="date-picker"
      >
        <view class="picker-text">
          <text class="picker-icon">📅</text>
          <text>{{ filterDate || '全部' }}</text>
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
    </view>
    
    <view class="records-list">
      <view class="record-item" v-for="record in recordsList" :key="record.id">
        <view class="record-header">
          <view class="record-left">
            <text class="record-icon">{{ getRecordIcon(record.type) }}</text>
            <view class="record-info">
              <view class="record-title">{{ getRecordTitle(record) }}</view>
              <view class="record-time">{{ formatTime(record.createTime) }}</view>
            </view>
          </view>
          <view class="record-right">
            <text class="record-amount" :class="record.type === 'recharge' ? 'positive' : 'negative'">
              {{ record.type === 'recharge' ? '+' : '-' }}{{ record.amount }}
            </text>
            <text class="record-unit">{{ currentTab === 'recharge' ? '元' : '币' }}</text>
          </view>
        </view>
        
        <view class="record-detail" v-if="record.type === 'consume'">
          <view class="detail-row">
            <text class="detail-label">项目名称：</text>
            <text class="detail-value">{{ record.projectName || '--' }}</text>
          </view>
          <view class="detail-row" v-if="record.operator">
            <text class="detail-label">操作人：</text>
            <text class="detail-value">{{ record.operator }}</text>
          </view>
        </view>
        
        <view class="record-detail" v-else>
          <view class="detail-row">
            <text class="detail-label">支付方式：</text>
            <text class="detail-value">{{ record.paymentMethod || '微信支付' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">到账币数：</text>
            <text class="detail-value">{{ record.coinsReceived || record.amount }}币</text>
          </view>
          <view class="detail-row" v-if="record.giftCoins > 0">
            <text class="detail-label">赠送币数：</text>
            <text class="detail-value">{{ record.giftCoins }}币</text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-if="recordsList.length === 0 && !loading">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无{{ currentTab === 'recharge' ? '充值' : '消费' }}记录</text>
      </view>
      
      <view class="loading-state" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
      
      <view class="no-more" v-if="!hasMore && recordsList.length > 0">
        <text class="no-more-text">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getConsumeRecords } from '@/api/records.js'
import { getRechargeRecords } from '@/api/recharge.js'

export default {
  data() {
    return {
      currentTab: 'consume',
      filterDate: '',
      recordsList: [],
      page: 1,
      pageSize: 10,
      hasMore: true,
      loading: false
    }
  },
  onShow() {
    this.loadRecords(true)
  },
  onPullDownRefresh() {
    this.loadRecords(true).then(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadRecords(false)
    }
  },
  methods: {
    switchTab(tab) {
      if (this.currentTab !== tab) {
        this.currentTab = tab
        this.page = 1
        this.recordsList = []
        this.hasMore = true
        this.loadRecords(true)
      }
    },
    
    onDateChange(e) {
      this.filterDate = e.detail.value
      this.page = 1
      this.recordsList = []
      this.hasMore = true
      this.loadRecords(true)
    },
    
    async loadRecords(refresh = true) {
      if (this.loading) return
      
      if (refresh) {
        this.page = 1
        this.recordsList = []
        this.hasMore = true
      }
      
      this.loading = true
      
      try {
        const params = {
          page: this.page,
          size: this.pageSize,
          date: this.filterDate
        }
        
        let result
        if (this.currentTab === 'recharge') {
          result = await getRechargeRecords(params)
        } else {
          result = await getConsumeRecords(params)
        }
        
        const records = result.records || []
        if (records.length > 0) {
          this.recordsList = [...this.recordsList, ...records]
          this.page++
          this.hasMore = records.length === this.pageSize
        } else {
          this.hasMore = false
        }
      } catch (error) {
        console.error('加载记录失败:', error)
        this.hasMore = false
      } finally {
        this.loading = false
      }
    },
    
    getRecordIcon(type) {
      return type === 'recharge' ? '💰' : '🎮'
    },
    
    getRecordTitle(record) {
      if (record.type === 'recharge') {
        return '充值' + record.amount + '元'
      } else {
        return record.projectName || '消费'
      }
    },
    
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    }
  }
}
</script>

<style scoped>
.records-container {
  min-height: 100vh;
}

.tabs {
  display: flex;
  background-color: #ffffff;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #606266;
  position: relative;
}

.tab-item.active {
  color: #409EFF;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 6rpx;
  background-color: #409EFF;
  border-radius: 3rpx;
}

.filter-section {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
}

.date-picker {
  width: 100%;
}

.picker-text {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx;
  background-color: #f5f7fa;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #606266;
}

.picker-icon {
  margin-right: 10rpx;
}

.picker-arrow {
  margin-left: 10rpx;
  font-size: 20rpx;
}

.records-list {
  padding: 0 20rpx;
}

.record-item {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
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

.record-title {
  font-size: 30rpx;
  color: #333333;
  margin-bottom: 6rpx;
}

.record-time {
  font-size: 24rpx;
  color: #909399;
}

.record-right {
  display: flex;
  align-items: baseline;
}

.record-amount {
  font-size: 36rpx;
  font-weight: bold;
}

.record-amount.positive {
  color: #67C23A;
}

.record-amount.negative {
  color: #F56C6C;
}

.record-unit {
  font-size: 24rpx;
  color: #909399;
  margin-left: 4rpx;
}

.record-detail {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.detail-row {
  display: flex;
  margin-bottom: 10rpx;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 26rpx;
  color: #909399;
  width: 160rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #606266;
  flex: 1;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #909399;
}

.loading-state {
  text-align: center;
  padding: 40rpx 0;
}

.loading-text {
  font-size: 26rpx;
  color: #909399;
}

.no-more {
  text-align: center;
  padding: 40rpx 0;
}

.no-more-text {
  font-size: 26rpx;
  color: #c0c4cc;
}
</style>
