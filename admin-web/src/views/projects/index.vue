<template>
  <div class="projects-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>娱乐项目管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增项目
          </el-button>
        </div>
      </template>
      
      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="description" label="项目描述" show-overflow-tooltip />
        <el-table-column prop="price" label="扣币价格" width="120">
          <template #default="scope">
            <span style="color: #409EFF; font-weight: bold;">{{ scope.row.price }} 币/次</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxCapacity" label="最大容量" width="120">
          <template #default="scope">
            {{ scope.row.maxCapacity || '不限' }} 人
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常营业' : '暂停营业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              link 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '暂停' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑项目' : '新增项目'" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="扣币价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="1" 
            :max="999"
            style="width: 100%;"
          />
          <span style="color: #909399; font-size: 12px;">币/次</span>
        </el-form-item>
        <el-form-item label="最大容量">
          <el-input-number 
            v-model="form.maxCapacity" 
            :min="0" 
            :max="999"
            style="width: 100%;"
          />
          <span style="color: #909399; font-size: 12px;">人（0表示不限）</span>
        </el-form-item>
        <el-form-item label="营业状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常营业</el-radio>
            <el-radio :value="0">暂停营业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number 
            v-model="form.sort" 
            :min="0" 
            :max="999"
            style="width: 100%;"
          />
          <span style="color: #909399; font-size: 12px;">数字越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjects, addProject, updateProject, deleteProject, toggleProjectStatus } from '@/api/project'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const projectList = ref([])

const form = reactive({
  id: null,
  name: '',
  description: '',
  price: 1,
  maxCapacity: 0,
  status: 1,
  sort: 0
})

const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入扣币价格', trigger: 'blur' }
  ]
}

const loadProjects = async () => {
  loading.value = true
  try {
    const res = await getProjects()
    if (res.code === 200) {
      projectList.value = res.data
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    projectList.value = [
      { id: 1, name: '旋转木马', description: '经典旋转木马，适合所有年龄段', price: 5, maxCapacity: 30, status: 1, sort: 1 },
      { id: 2, name: '碰碰车', description: '刺激有趣的碰碰车', price: 3, maxCapacity: 20, status: 1, sort: 2 },
      { id: 3, name: '过山车', description: '惊险刺激的过山车', price: 8, maxCapacity: 15, status: 1, sort: 3 },
      { id: 4, name: '海盗船', description: '摇摆的海盗船', price: 6, maxCapacity: 25, status: 0, sort: 4 },
      { id: 5, name: '摩天轮', description: '浪漫的摩天轮', price: 10, maxCapacity: 40, status: 1, sort: 5 }
    ]
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    price: 1,
    maxCapacity: 0,
    status: 1,
    sort: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description || '',
    price: row.price,
    maxCapacity: row.maxCapacity || 0,
    status: row.status,
    sort: row.sort || 0
  })
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '暂停' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await toggleProjectStatus(row.id)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadProjects()
    }
  } catch (error) {
    if (error !== 'cancel') {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.success(`${action}成功（演示模式）`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteProject(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadProjects()
    }
  } catch (error) {
    if (error !== 'cancel') {
      const index = projectList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        projectList.value.splice(index, 1)
      }
      ElMessage.success('删除成功（演示模式）')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (isEdit.value) {
          res = await updateProject(form.id, form)
        } else {
          res = await addProject(form)
        }
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
          dialogVisible.value = false
          loadProjects()
        }
      } catch (error) {
        if (isEdit.value) {
          const index = projectList.value.findIndex(item => item.id === form.id)
          if (index > -1) {
            Object.assign(projectList.value[index], form)
          }
        } else {
          projectList.value.unshift({
            id: Date.now(),
            ...form
          })
        }
        ElMessage.success(isEdit.value ? '修改成功（演示模式）' : '添加成功（演示模式）')
        dialogVisible.value = false
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadProjects()
})
</script>

<style lang="scss" scoped>
.projects-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
