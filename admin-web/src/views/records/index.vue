<template>
  <div class="records-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>交易记录</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="消费记录" name="consume">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="会员名称">
              <el-input v-model="searchForm.keyword" placeholder="请输入会员名称" clearable style="width: 180px;" />
            </el-form-item>
            <el-form-item label="项目名称">
              <el-select v-model="searchForm.projectName" placeholder="请选择项目" clearable style="width: 150px;">
                <el-option label="旋转木马" value="旋转木马" />
                <el-option label="碰碰车" value="碰碰车" />
                <el-option label="过山车" value="过山车" />
                <el-option label="海盗船" value="海盗船" />
                <el-option label="摩天轮" value="摩天轮" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 280px;"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="consumeRecords" style="width: 100%" v-loading="loading">
            <el-table-column prop="recordNo" label="记录编号" width="200" />
            <el-table-column prop="userName" label="会员名称" width="120" />
            <el-table-column prop="projectName" label="项目名称" />
            <el-table-column prop="quantity" label="次数" width="80" />
            <el-table-column prop="amount" label="扣币数" width="100">
              <template #default="scope">
                <span style="color: #F56C6C; font-weight: bold;">-{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="createTime" label="交易时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="充值记录" name="recharge">
          <el-form :inline="true" :model="rechargeSearchForm" class="search-form">
            <el-form-item label="会员名称">
              <el-input v-model="rechargeSearchForm.keyword" placeholder="请输入会员名称" clearable style="width: 180px;" />
            </el-form-item>
            <el-form-item label="支付状态">
              <el-select v-model="rechargeSearchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
                <el-option label="待支付" :value="0" />
                <el-option label="已支付" :value="1" />
                <el-option label="已取消" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="rechargeSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 280px;"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRechargeSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleRechargeReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="rechargeRecords" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column prop="userName" label="会员名称" width="120" />
            <el-table-column prop="amount" label="充值金额" width="120">
              <template #default="scope">
                <span style="color: #67C23A; font-weight: bold;">¥{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="coinsReceived" label="到账币数" width="120">
              <template #default="scope">
                <span style="color: #409EFF; font-weight: bold;">{{ scope.row.coinsReceived }} 币</span>
              </template>
            </el-table-column>
            <el-table-column prop="giftCoins" label="赠送币数" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.giftCoins > 0" type="success">
                  +{{ scope.row.giftCoins }}
                </el-tag>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="100">
              <template #default="scope">
                {{ scope.row.paymentMethod === 'wechat' ? '微信支付' : scope.row.paymentMethod }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'info' : 'warning'">
                  {{ scope.row.status === 1 ? '已支付' : scope.row.status === 2 ? '已取消' : '待支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="充值时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const activeTab = ref('consume')

const searchForm = reactive({
  keyword: '',
  projectName: '',
  dateRange: []
})

const rechargeSearchForm = reactive({
  keyword: '',
  status: null,
  dateRange: []
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 50
})

const consumeRecords = ref([
  { id: 1, recordNo: 'CR202404301023450001', userName: '张三', projectName: '旋转木马', quantity: 2, amount: 10, operatorName: '管理员', createTime: '2024-04-30 10:23:45' },
  { id: 2, recordNo: 'CR202404301018320002', userName: '李四', projectName: '碰碰车', quantity: 1, amount: 3, operatorName: '管理员', createTime: '2024-04-30 10:18:32' },
  { id: 3, recordNo: 'CR202404301015200003', userName: '王五', projectName: '过山车', quantity: 1, amount: 8, operatorName: '管理员', createTime: '2024-04-30 10:15:20' },
  { id: 4, recordNo: 'CR202404301010150004', userName: '赵六', projectName: '海盗船', quantity: 2, amount: 12, operatorName: '管理员', createTime: '2024-04-30 10:10:15' },
  { id: 5, recordNo: 'CR202404301005300005', userName: '孙七', projectName: '摩天轮', quantity: 3, amount: 30, operatorName: '管理员', createTime: '2024-04-30 10:05:30' },
  { id: 6, recordNo: 'CR202404300955200006', userName: '张三', projectName: '小火车', quantity: 4, amount: 8, operatorName: '管理员', createTime: '2024-04-30 09:55:20' },
  { id: 7, recordNo: 'CR202404300948150007', userName: '周八', projectName: '旋转木马', quantity: 1, amount: 5, operatorName: '管理员', createTime: '2024-04-30 09:48:15' },
  { id: 8, recordNo: 'CR202404300942000008', userName: '李四', projectName: '碰碰车', quantity: 3, amount: 9, operatorName: '管理员', createTime: '2024-04-30 09:42:00' },
  { id: 9, recordNo: 'CR202404300935300009', userName: '王五', projectName: '过山车', quantity: 2, amount: 16, operatorName: '管理员', createTime: '2024-04-30 09:35:30' },
  { id: 10, recordNo: 'CR202404300930150010', userName: '孙七', projectName: '旋转木马', quantity: 1, amount: 5, operatorName: '管理员', createTime: '2024-04-30 09:30:15' }
])

const rechargeRecords = ref([
  { id: 1, orderNo: 'RC202404301000000001', userName: '张三', amount: 100, coinsReceived: 115, giftCoins: 15, paymentMethod: 'wechat', status: 1, createTime: '2024-04-30 10:00:00' },
  { id: 2, orderNo: 'RC202404300930000002', userName: '李四', amount: 50, coinsReceived: 56, giftCoins: 6, paymentMethod: 'wechat', status: 1, createTime: '2024-04-30 09:30:00' },
  { id: 3, orderNo: 'RC202404300900000003', userName: '王五', amount: 200, coinsReceived: 240, giftCoins: 40, paymentMethod: 'wechat', status: 1, createTime: '2024-04-30 09:00:00' },
  { id: 4, orderNo: 'RC202404291830000004', userName: '赵六', amount: 10, coinsReceived: 10, giftCoins: 0, paymentMethod: 'wechat', status: 1, createTime: '2024-04-29 18:30:00' },
  { id: 5, orderNo: 'RC202404291500000005', userName: '孙七', amount: 500, coinsReceived: 600, giftCoins: 100, paymentMethod: 'wechat', status: 1, createTime: '2024-04-29 15:00:00' },
  { id: 6, orderNo: 'RC202404291200000006', userName: '周八', amount: 30, coinsReceived: 33, giftCoins: 3, paymentMethod: 'wechat', status: 0, createTime: '2024-04-29 12:00:00' },
  { id: 7, orderNo: 'RC202404282000000007', userName: '张三', amount: 100, coinsReceived: 115, giftCoins: 15, paymentMethod: 'wechat', status: 1, createTime: '2024-04-28 20:00:00' },
  { id: 8, orderNo: 'RC202404281600000008', userName: '李四', amount: 50, coinsReceived: 56, giftCoins: 6, paymentMethod: 'wechat', status: 2, createTime: '2024-04-28 16:00:00' }
])

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const handleTabClick = () => {
  pagination.currentPage = 1
}

const handleSearch = () => {
  ElMessage.info('搜索功能演示')
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.projectName = ''
  searchForm.dateRange = []
}

const handleRechargeSearch = () => {
  ElMessage.info('搜索功能演示')
}

const handleRechargeReset = () => {
  rechargeSearchForm.keyword = ''
  rechargeSearchForm.status = null
  rechargeSearchForm.dateRange = []
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

onMounted(() => {
  // 加载数据
})
</script>

<style lang="scss" scoped>
.records-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
