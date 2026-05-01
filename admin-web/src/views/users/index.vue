<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>会员管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="会员名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入会员名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="会员角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 150px;">
            <el-option label="普通会员" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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
      
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickName" label="会员名称" width="150">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="32" :src="scope.row.avatarUrl" style="margin-right: 10px;">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span>{{ scope.row.nickName || '微信用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="scope">
            {{ scope.row.phone || '未绑定' }}
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="120">
          <template #default="scope">
            <span style="color: #409EFF; font-weight: bold;">{{ scope.row.balance }} 币</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'warning' : 'info'">
              {{ scope.row.role === 'admin' ? '管理员' : '普通会员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">
              详情
            </el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              link 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
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

    <el-dialog 
      v-model="detailVisible" 
      title="会员详情" 
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="会员ID">
          {{ currentUser.id }}
        </el-descriptions-item>
        <el-descriptions-item label="会员名称">
          {{ currentUser.nickName || '微信用户' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ currentUser.phone || '未绑定' }}
        </el-descriptions-item>
        <el-descriptions-item label="账户余额">
          <span style="color: #409EFF; font-weight: bold;">{{ currentUser.balance }} 币</span>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentUser.role === 'admin' ? 'warning' : 'info'">
            {{ currentUser.role === 'admin' ? '管理员' : '普通会员' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatTime(currentUser.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const detailVisible = ref(false)
const currentUser = ref({})

const searchForm = reactive({
  keyword: '',
  role: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 50
})

const userList = ref([
  { id: 1, nickName: '张三', phone: '13800138001', balance: 150, role: 'user', status: 1, createTime: '2024-04-01 10:30:00', avatarUrl: '' },
  { id: 2, nickName: '李四', phone: '13800138002', balance: 85, role: 'user', status: 1, createTime: '2024-04-02 14:20:00', avatarUrl: '' },
  { id: 3, nickName: '王五', phone: '13800138003', balance: 200, role: 'user', status: 1, createTime: '2024-04-03 09:15:00', avatarUrl: '' },
  { id: 4, nickName: '赵六', phone: '13800138004', balance: 50, role: 'user', status: 0, createTime: '2024-04-04 16:45:00', avatarUrl: '' },
  { id: 5, nickName: '管理员', phone: '13800138000', balance: 9999, role: 'admin', status: 1, createTime: '2024-03-01 00:00:00', avatarUrl: '' },
  { id: 6, nickName: '孙七', phone: '', balance: 30, role: 'user', status: 1, createTime: '2024-04-05 11:20:00', avatarUrl: '' },
  { id: 7, nickName: '周八', phone: '13800138005', balance: 120, role: 'user', status: 1, createTime: '2024-04-06 13:30:00', avatarUrl: '' },
  { id: 8, nickName: '吴九', phone: '13800138006', balance: 0, role: 'user', status: 1, createTime: '2024-04-07 15:40:00', avatarUrl: '' },
  { id: 9, nickName: '郑十', phone: '13800138007', balance: 75, role: 'user', status: 1, createTime: '2024-04-08 10:10:00', avatarUrl: '' },
  { id: 10, nickName: '钱十一', phone: '', balance: 250, role: 'user', status: 1, createTime: '2024-04-09 09:50:00', avatarUrl: '' }
])

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const handleSearch = () => {
  ElMessage.info('搜索功能演示')
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
}

const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

const handleView = (row) => {
  currentUser.value = { ...row }
  detailVisible.value = true
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}"${row.nickName}"的账户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  } catch (error) {
    // 取消操作
  }
}

onMounted(() => {
  // 加载数据
})
</script>

<style lang="scss" scoped>
.users-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
