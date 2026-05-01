<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
    this.checkLoginStatus()
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  methods: {
    checkLoginStatus() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')
      if (!token || !userInfo) {
        uni.redirectTo({
          url: '/pages/login/login'
        })
      }
    }
  }
}
</script>

<style>
/*每个页面公共css */
page {
  background-color: #F5F7FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
}

.container {
  padding: 20rpx;
}

.card {
  background-color: #ffffff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.btn-primary {
  background-color: #409EFF;
  color: #ffffff;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 32rpx;
  text-align: center;
}

.btn-primary:active {
  background-color: #66b1ff;
}

.btn-danger {
  background-color: #F56C6C;
  color: #ffffff;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 32rpx;
  text-align: center;
}

.btn-danger:active {
  background-color: #f89898;
}

.text-center {
  text-align: center;
}

.text-primary {
  color: #409EFF;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}

.text-muted {
  color: #909399;
}

.mt-20 {
  margin-top: 20rpx;
}

.mb-20 {
  margin-bottom: 20rpx;
}

.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
}

.flex-center {
  justify-content: center;
  align-items: center;
}

.flex-between {
  justify-content: space-between;
}

.flex-around {
  justify-content: space-around;
}
</style>
