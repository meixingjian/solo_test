<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon member">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalMembers }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon project">
              <el-icon><SetUp /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalProjects }}</div>
              <div class="stat-label">开放项目</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon income">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayIncome }}</div>
              <div class="stat-label">今日收入（元）</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon consume">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayConsumes }}</div>
              <div class="stat-label">今日消费次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>最近消费记录</span>
            <el-button type="text" style="float: right;" @click="goToRecords">查看全部</el-button>
          </template>
          <el-table :data="recentRecords" style="width: 100%">
            <el-table-column prop="recordNo" label="记录编号" width="180" />
            <el-table-column prop="userName" label="会员" width="120" />
            <el-table-column prop="projectName" label="项目名称" />
            <el-table-column prop="quantity" label="次数" width="80" />
            <el-table-column prop="amount" label="扣币数" width="100">
              <template #default="scope">
                <span style="color: #F56C6C;">-{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="createTime" label="时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>热门项目排行</span>
          </template>
          <div class="project-ranking">
            <div 
              class="ranking-item" 
              v-for="(item, index) in projectRanking" 
              :key="item.id"
            >
              <div class="ranking-index" :class="'rank-' + (index + 1)">
                {{ index + 1 }}
              </div>
              <div class="ranking-info">
                <div class="ranking-name">{{ item.name }}</div>
                <div class="ranking-progress">
                  <el-progress 
                    :percentage="item.percentage" 
                    :show-text="false"
                    :stroke-width="10"
                  />
                </div>
              </div>
              <div class="ranking-count">{{ item.count }}次</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="goToProjects">
              <el-icon class="action-icon"><SetUp /></el-icon>
              <span class="action-text">项目管理</span>
            </div>
            <div class="action-item" @click="goToUsers">
              <el-icon class="action-icon"><User /></el-icon>
              <span class="action-text">会员管理</span>
            </div>
            <div class="action-item" @click="goToSettings">
              <el-icon class="action-icon"><Setting /></el-icon>
              <span class="action-text">系统设置</span>
            </div>
            <div class="action-item" @click="goToRecords">
              <el-icon class="action-icon"><Document /></el-icon>
              <span class="action-text">交易记录</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()

const stats = reactive({
  totalMembers: 128,
  totalProjects: 8,
  todayIncome: 2580,
  todayConsumes: 86
})

const recentRecords = ref([
  {
    id: 1,
    recordNo: 'CR202404301023450001',
    userName: '张三',
    projectName: '旋转木马',
    quantity: 2,
    amount: 10,
    operatorName: '管理员',
    createTime: '2024-04-30 10:23:45'
  },
  {
    id: 2,
    recordNo: 'CR202404301018320002',
    userName: '李四',
    projectName: '碰碰车',
    quantity: 1,
    amount: 3,
    operatorName: '管理员',
    createTime: '2024-04-30 10:18:32'
  },
  {
    id: 3,
    recordNo: 'CR202404301015200003',
    userName: '王五',
    projectName: '过山车',
    quantity: 1,
    amount: 8,
    operatorName: '管理员',
    createTime: '2024-04-30 10:15:20'
  },
  {
    id: 4,
    recordNo: 'CR202404301010150004',
    userName: '赵六',
    projectName: '海盗船',
    quantity: 2,
    amount: 12,
    operatorName: '管理员',
    createTime: '2024-04-30 10:10:15'
  },
  {
    id: 5,
    recordNo: 'CR202404301005300005',
    userName: '孙七',
    projectName: '摩天轮',
    quantity: 3,
    amount: 30,
    operatorName: '管理员',
    createTime: '2024-04-30 10:05:30'
  }
])

const projectRanking = ref([
  { id: 1, name: '旋转木马', count: 35, percentage: 85 },
  { id: 2, name: '碰碰车', count: 28, percentage: 70 },
  { id: 3, name: '小火车', count: 22, percentage: 55 },
  { id: 4, name: '海盗船', count: 18, percentage: 45 },
  { id: 5, name: '过山车', count: 15, percentage: 38 }
])

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const goToProjects = () => {
  router.push('/projects')
}

const goToUsers = () => {
  router.push('/users')
}

const goToRecords = () => {
  router.push('/records')
}

const goToSettings = () => {
  router.push('/settings')
}

onMounted(() => {
  // 加载统计数据
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 30px;
        color: #fff;
        
        &.member {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        &.project {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }
        &.income {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
        &.consume {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }
      
      .stat-info {
        margin-left: 20px;
        
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
  
  .project-ranking {
    .ranking-item {
      display: flex;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .ranking-index {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: bold;
        color: #fff;
        margin-right: 15px;
        
        &.rank-1 {
          background-color: #F56C6C;
        }
        &.rank-2 {
          background-color: #E6A23C;
        }
        &.rank-3 {
          background-color: #909399;
        }
        &.rank-4, &.rank-5 {
          background-color: #DCDFE6;
          color: #606266;
        }
      }
      
      .ranking-info {
        flex: 1;
        
        .ranking-name {
          font-size: 14px;
          color: #303133;
          margin-bottom: 5px;
        }
      }
      
      .ranking-count {
        font-size: 14px;
        color: #909399;
        margin-left: 15px;
      }
    }
  }
  
  .quick-actions {
    display: flex;
    justify-content: space-around;
    
    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
      padding: 20px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
        
        .action-icon {
          transform: scale(1.1);
        }
      }
      
      .action-icon {
        font-size: 40px;
        color: #409EFF;
        margin-bottom: 10px;
        transition: transform 0.3s;
      }
      
      .action-text {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>
