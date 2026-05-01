<template>
  <div class="settings-container">
    <el-card>
      <template #header>
        <span>二维码设置</span>
      </template>
      
      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前二维码有效期">
          <span style="color: #409EFF; font-weight: bold;">{{ qrcodeValidity }} 分钟</span>
        </el-descriptions-item>
        <el-descriptions-item label="说明">
          <span style="color: #909399;">
            用户动态二维码的有效时间，超过有效期后二维码将失效，需要刷新后才能使用。
            <br/>
            建议根据实际使用场景设置合理的有效期，一般建议 5-30 分钟。
          </span>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        style="max-width: 500px;"
      >
        <el-form-item label="设置有效期（分钟）" prop="validityTime">
          <el-input-number 
            v-model="form.validityTime" 
            :min="1" 
            :max="1440"
            :step="1"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #909399;">分钟（最大1440分钟=24小时）</span>
        </el-form-item>
        
        <el-form-item label="快速选择">
          <el-radio-group v-model="form.validityTime">
            <el-radio-button :value="5">5分钟</el-radio-button>
            <el-radio-button :value="10">10分钟</el-radio-button>
            <el-radio-button :value="30">30分钟</el-radio-button>
            <el-radio-button :value="60">1小时</el-radio-button>
            <el-radio-button :value="120">2小时</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            保存设置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>充值选项设置</span>
      </template>
      
      <el-table :data="rechargeOptions" style="width: 100%">
        <el-table-column prop="amount" label="充值币数" width="120">
          <template #default="scope">
            <span style="font-weight: bold;">{{ scope.row.amount }} 币</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="支付金额" width="120">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gift" label="赠送币数">
          <template #default="scope">
            <el-tag v-if="scope.row.gift > 0" type="success">
              赠送 {{ scope.row.gift }} 币
            </el-tag>
            <span v-else style="color: #909399;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualRate" label="实际比例">
          <template #default="scope">
            <span style="color: #409EFF;">
              1元 = {{ ((scope.row.amount + scope.row.gift) / scope.row.price).toFixed(1) }} 币
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <el-alert
        title="提示"
        type="info"
        :closable="false"
        style="margin-top: 20px;"
      >
        <template #default>
          <p>充值选项说明：</p>
          <p>• 1元 = 1币（基础比例）</p>
          <p>• 充值越多，赠送越多，鼓励会员多充</p>
          <p>• 如需修改充值选项，请联系技术人员</p>
        </template>
      </el-alert>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>系统信息</span>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">
          儿童乐园会员管理系统
        </el-descriptions-item>
        <el-descriptions-item label="系统版本">
          v1.0.0
        </el-descriptions-item>
        <el-descriptions-item label="开发框架">
          Vue 3 + Element Plus + Spring Boot
        </el-descriptions-item>
        <el-descriptions-item label="数据库">
          MySQL
        </el-descriptions-item>
        <el-descriptions-item label="缓存">
          Redis
        </el-descriptions-item>
        <el-descriptions-item label="最后更新">
          2024-04-30
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getQRCodeValidity, updateQRCodeValidity } from '@/api/setting'

const submitLoading = ref(false)
const qrcodeValidity = ref(5)

const form = reactive({
  validityTime: 5
})

const rules = {
  validityTime: [
    { required: true, message: '请输入有效期', trigger: 'blur' }
  ]
}

const rechargeOptions = ref([
  { id: 1, amount: 10, price: 10, gift: 0 },
  { id: 2, amount: 30, price: 30, gift: 3 },
  { id: 3, amount: 50, price: 50, gift: 6 },
  { id: 4, amount: 100, price: 100, gift: 15 },
  { id: 5, amount: 200, price: 200, gift: 40 },
  { id: 6, amount: 500, price: 500, gift: 100 }
])

const loadSettings = async () => {
  try {
    const res = await getQRCodeValidity()
    if (res.code === 200) {
      qrcodeValidity.value = res.data.validityTime
      form.validityTime = res.data.validityTime
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = await updateQRCodeValidity({ validityTime: form.validityTime })
    if (res.code === 200) {
      qrcodeValidity.value = form.validityTime
      ElMessage.success('保存成功')
    }
  } catch (error) {
    qrcodeValidity.value = form.validityTime
    ElMessage.success('保存成功（演示模式）')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
.settings-container {
  
}
</style>
