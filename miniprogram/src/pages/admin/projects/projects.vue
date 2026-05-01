<template>
  <view class="projects-container">
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索项目名称" 
          class="search-input"
          @confirm="searchProjects"
        />
      </view>
    </view>
    
    <view class="projects-list">
      <view class="project-card" v-for="project in filteredProjects" :key="project.id">
        <view class="project-header">
          <view class="project-icon">{{ getProjectIcon(project.name) }}</view>
          <view class="project-info">
            <view class="project-name">{{ project.name }}</view>
            <view class="project-status">
              <text class="status-tag" :class="{ active: project.status === 1 }">
                {{ project.status === 1 ? '正常营业' : '暂停营业' }}
              </text>
            </view>
          </view>
        </view>
        
        <view class="project-detail">
          <view class="detail-row">
            <text class="detail-label">扣币价格：</text>
            <text class="detail-value price">{{ project.price }} 币/次</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">项目描述：</text>
            <text class="detail-value">{{ project.description || '暂无描述' }}</text>
          </view>
          <view class="detail-row" v-if="project.maxCapacity">
            <text class="detail-label">最大容量：</text>
            <text class="detail-value">{{ project.maxCapacity }} 人</text>
          </view>
        </view>
        
        <view class="project-actions">
          <view class="action-btn" @tap="editProject(project)">
            <text class="action-icon">✏️</text>
            <text class="action-text">编辑</text>
          </view>
          <view class="action-btn" @tap="toggleProjectStatus(project)">
            <text class="action-icon">{{ project.status === 1 ? '⏸️' : '▶️' }}</text>
            <text class="action-text">{{ project.status === 1 ? '暂停' : '启用' }}</text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-if="filteredProjects.length === 0">
        <text class="empty-icon">🎮</text>
        <text class="empty-text">暂无娱乐项目</text>
      </view>
    </view>
    
    <view class="add-fab" @tap="showAddModal">
      <text class="add-icon">+</text>
    </view>
    
    <view class="project-modal" v-if="showModal">
      <view class="modal-overlay" @tap="hideModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑项目' : '添加项目' }}</text>
          <text class="modal-close" @tap="hideModal">×</text>
        </view>
        
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">项目名称 <text class="required">*</text></text>
            <input 
              type="text" 
              v-model="formData.name" 
              placeholder="请输入项目名称" 
              class="form-input"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">扣币价格 <text class="required">*</text></text>
            <view class="price-input-group">
              <input 
                type="number" 
                v-model="formData.price" 
                placeholder="请输入扣币价格" 
                class="form-input price-input"
              />
              <text class="price-unit">币/次</text>
            </view>
          </view>
          
          <view class="form-item">
            <text class="form-label">项目描述</text>
            <textarea 
              v-model="formData.description" 
              placeholder="请输入项目描述" 
              class="form-textarea"
              :maxlength="200"
            ></textarea>
            <text class="textarea-count">{{ formData.description.length }}/200</text>
          </view>
          
          <view class="form-item">
            <text class="form-label">最大容量（人）</text>
            <input 
              type="number" 
              v-model="formData.maxCapacity" 
              placeholder="请输入最大容量" 
              class="form-input"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">营业状态</text>
            <view class="status-radio">
              <view 
                class="radio-item" 
                :class="{ checked: formData.status === 1 }"
                @tap="formData.status = 1"
              >
                <view class="radio-circle"></view>
                <text class="radio-text">正常营业</text>
              </view>
              <view 
                class="radio-item" 
                :class="{ checked: formData.status === 0 }"
                @tap="formData.status = 0"
              >
                <view class="radio-circle"></view>
                <text class="radio-text">暂停营业</text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel" @tap="hideModal">取消</button>
          <button class="modal-btn confirm" @tap="saveProject">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getProjects } from '@/api/admin.js'

export default {
  data() {
    return {
      projects: [],
      searchKeyword: '',
      showModal: false,
      isEdit: false,
      editingProject: null,
      formData: {
        name: '',
        price: '',
        description: '',
        maxCapacity: '',
        status: 1
      }
    }
  },
  computed: {
    filteredProjects() {
      if (!this.searchKeyword) {
        return this.projects
      }
      const keyword = this.searchKeyword.toLowerCase()
      return this.projects.filter(project => 
        project.name.toLowerCase().includes(keyword)
      )
    }
  },
  onShow() {
    this.loadProjects()
  },
  onPullDownRefresh() {
    this.loadProjects().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadProjects() {
      try {
        const result = await getProjects()
        this.projects = result
      } catch (error) {
        console.error('加载项目列表失败:', error)
        this.projects = [
          { id: 1, name: '旋转木马', price: 5, description: '经典旋转木马，适合所有年龄段', maxCapacity: 30, status: 1 },
          { id: 2, name: '碰碰车', price: 3, description: '刺激有趣的碰碰车', maxCapacity: 20, status: 1 },
          { id: 3, name: '过山车', price: 8, description: '惊险刺激的过山车', maxCapacity: 15, status: 0 },
          { id: 4, name: '海盗船', price: 6, description: '摇摆的海盗船', maxCapacity: 25, status: 1 },
          { id: 5, name: '摩天轮', price: 10, description: '浪漫的摩天轮', maxCapacity: 40, status: 1 },
          { id: 6, name: '小火车', price: 2, description: '亲子小火车', maxCapacity: 50, status: 1 }
        ]
      }
    },
    
    searchProjects() {
      // 搜索逻辑由computed处理
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
        '娃娃机': '🧸',
        '蹦床': '🤸'
      }
      return iconMap[name] || '🎮'
    },
    
    editProject(project) {
      this.isEdit = true
      this.editingProject = project
      this.formData = {
        name: project.name,
        price: String(project.price),
        description: project.description || '',
        maxCapacity: project.maxCapacity ? String(project.maxCapacity) : '',
        status: project.status
      }
      this.showModal = true
    },
    
    showAddModal() {
      this.isEdit = false
      this.editingProject = null
      this.formData = {
        name: '',
        price: '',
        description: '',
        maxCapacity: '',
        status: 1
      }
      this.showModal = true
    },
    
    hideModal() {
      this.showModal = false
    },
    
    toggleProjectStatus(project) {
      const action = project.status === 1 ? '暂停' : '启用'
      uni.showModal({
        title: '提示',
        content: `确定要${action}"${project.name}"吗？`,
        success: (res) => {
          if (res.confirm) {
            project.status = project.status === 1 ? 0 : 1
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          }
        }
      })
    },
    
    saveProject() {
      if (!this.formData.name.trim()) {
        uni.showToast({
          title: '请输入项目名称',
          icon: 'none'
        })
        return
      }
      
      if (!this.formData.price || parseInt(this.formData.price) <= 0) {
        uni.showToast({
          title: '请输入有效的扣币价格',
          icon: 'none'
        })
        return
      }
      
      if (this.isEdit && this.editingProject) {
        Object.assign(this.editingProject, {
          name: this.formData.name,
          price: parseInt(this.formData.price),
          description: this.formData.description,
          maxCapacity: this.formData.maxCapacity ? parseInt(this.formData.maxCapacity) : null,
          status: this.formData.status
        })
        uni.showToast({
          title: '修改成功',
          icon: 'success'
        })
      } else {
        const newProject = {
          id: Date.now(),
          name: this.formData.name,
          price: parseInt(this.formData.price),
          description: this.formData.description,
          maxCapacity: this.formData.maxCapacity ? parseInt(this.formData.maxCapacity) : null,
          status: this.formData.status
        }
        this.projects.unshift(newProject)
        uni.showToast({
          title: '添加成功',
          icon: 'success'
        })
      }
      
      this.hideModal()
    }
  }
}
</script>

<style scoped>
.projects-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 140rpx;
}

.search-section {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  position: sticky;
  top: 0;
  z-index: 10;
}

.search-box {
  display: flex;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 50rpx;
  padding: 16rpx 30rpx;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
}

.projects-list {
  padding: 20rpx;
}

.project-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.project-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.project-icon {
  font-size: 60rpx;
  margin-right: 20rpx;
}

.project-info {
  flex: 1;
}

.project-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 8rpx;
}

.project-status {
  display: flex;
}

.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  color: #909399;
  background-color: #f4f4f5;
}

.status-tag.active {
  color: #67C23A;
  background-color: #f0f9eb;
}

.project-detail {
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 20rpx;
}

.detail-row {
  display: flex;
  margin-bottom: 12rpx;
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

.detail-value.price {
  color: #409EFF;
  font-weight: bold;
}

.project-actions {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  margin-left: 20rpx;
  background-color: #f5f7fa;
  border-radius: 8rpx;
}

.action-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.action-text {
  font-size: 26rpx;
  color: #606266;
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

.add-fab {
  position: fixed;
  right: 40rpx;
  bottom: 40rpx;
  width: 100rpx;
  height: 100rpx;
  background-color: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(64, 158, 255, 0.4);
  z-index: 100;
}

.add-icon {
  font-size: 60rpx;
  color: #ffffff;
  font-weight: lighter;
}

.project-modal {
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
  width: 650rpx;
  max-height: 80vh;
  background-color: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
  font-size: 48rpx;
  color: #909399;
}

.modal-body {
  padding: 30rpx;
  flex: 1;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  color: #606266;
  margin-bottom: 16rpx;
  display: block;
}

.required {
  color: #F56C6C;
}

.form-input {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.price-input-group {
  display: flex;
  align-items: center;
}

.price-input {
  flex: 1;
}

.price-unit {
  font-size: 28rpx;
  color: #909399;
  margin-left: 16rpx;
}

.form-textarea {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  font-size: 28rpx;
  height: 160rpx;
  box-sizing: border-box;
}

.textarea-count {
  font-size: 24rpx;
  color: #c0c4cc;
  text-align: right;
  margin-top: 8rpx;
  display: block;
}

.status-radio {
  display: flex;
}

.radio-item {
  display: flex;
  align-items: center;
  margin-right: 40rpx;
}

.radio-circle {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 50%;
  margin-right: 12rpx;
  position: relative;
}

.radio-item.checked .radio-circle {
  border-color: #409EFF;
}

.radio-item.checked .radio-circle::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20rpx;
  height: 20rpx;
  background-color: #409EFF;
  border-radius: 50%;
}

.radio-text {
  font-size: 28rpx;
  color: #606266;
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
