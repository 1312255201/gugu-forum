<template>
  <div class="activity-admin-container">
    <div class="page-header">
      <h1>校园活动管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus/></el-icon>
        发布新活动
      </el-button>
    </div>

    <!-- 活动列表 -->
    <el-card class="activity-table-card">
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
          <el-button text @click="fetchActivities" ><el-icon><Refresh/></el-icon>刷新</el-button>
        </div>
      </template>

      <el-table :data="activities" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="activityTime" label="活动时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.activityTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentParticipants" label="参与人数" width="120">
          <template #default="{ row }">
            {{ row.currentParticipants }}{{ row.maxParticipants > 0 ? `/${row.maxParticipants}` : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="editActivity(row)" >
                <el-icon><Edit/> </el-icon>
                编辑
              </el-button>
              <el-dropdown @command="(command) => handleStatusCommand(command, row)">
                <el-button size="small" type="warning">
                  状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="start" :disabled="row.status === 1">开始活动</el-dropdown-item>
                    <el-dropdown-item command="end" :disabled="row.status === 2">结束活动</el-dropdown-item>
                    <el-dropdown-item command="cancel" :disabled="row.status === 3">取消活动</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button size="small" type="danger" @click="deleteActivityItem(row)" >
              <el-icon><Delete/></el-icon>
                 删除
               </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingActivity ? '编辑活动' : '发布新活动'"
      width="60%"
      :before-close="handleDialogClose"
    >
      <el-form :model="activityForm" :rules="activityRules" ref="activityFormRef" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="activityForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        
        <el-form-item label="活动时间" prop="activityTime">
          <el-date-picker
            v-model="activityForm.activityTime"
            type="datetime"
            placeholder="选择活动开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            placeholder="选择活动结束时间（可选）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="参与人数">
          <el-input-number 
            v-model="activityForm.maxParticipants" 
            :min="0" 
            placeholder="最大参与人数，0表示不限制"
            style="width: 100%"
          />
          <el-text type="info" size="small">设置为0表示不限制参与人数</el-text>
        </el-form-item>
        
        <el-form-item label="封面图片">
          <div class="upload-section">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleCoverUploadSuccess"
              :before-upload="beforeCoverUpload"
              accept="image/*"
            >
              <el-button type="primary" icon="Upload">选择封面图片</el-button>
            </el-upload>
            <div v-if="activityForm.coverImage" class="cover-preview">
              <img :src="activityForm.coverImage" alt="封面预览" />
              <el-button @click="activityForm.coverImage = ''" type="danger" icon="Delete" circle size="small" class="remove-cover" />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="活动内容" prop="content">
          <div class="editor-container">
            <QuillEditor 
              v-model:content="activityForm.content"
              content-type="html"
              :options="editorOptions"
              style="height: 300px;"
            />
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose" icon="Close">取消</el-button>
          <el-button type="primary" @click="submitActivity" :loading="submitting" icon="Check">
            {{ editingActivity ? '更新活动' : '发布活动' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh, Upload, ArrowDown, Close, Check } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { 
  getAdminActivityList, 
  createActivity, 
  updateActivity, 
  updateActivityStatus, 
  deleteActivity 
} from '@/net/activity'
import { accessHeader } from '@/net'
import axios from 'axios'

const loading = ref(false)
const submitting = ref(false)
const activities = ref([])
const showCreateDialog = ref(false)
const editingActivity = ref(null)
const activityFormRef = ref()

// 表单数据
const activityForm = reactive({
  title: '',
  content: '',
  location: '',
  activityTime: '',
  endTime: '',
  coverImage: '',
  maxParticipants: 0
})

// 表单验证规则
const activityRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入活动内容', trigger: 'blur' },
    { min: 1, max: 10000, message: '内容长度在 1 到 10000 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' },
    { min: 1, max: 100, message: '地点长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  activityTime: [
    { required: true, message: '请选择活动时间', trigger: 'change' }
  ]
}

// 富文本编辑器配置
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'indent': '-1'}, { 'indent': '+1' }],
      [{ 'size': ['small', false, 'large', 'huge'] }],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'align': [] }],
      ['link', 'image'],
      ['clean']
    ]
  },
  placeholder: '请输入活动详细内容...'
}

// 图片上传配置
const uploadUrl = `${axios.defaults.baseURL}/api/image/cache`
const uploadHeaders = accessHeader()

// 获取活动列表
const fetchActivities = () => {
  loading.value = true
  getAdminActivityList((data) => {
    activities.value = data || []
    loading.value = false
  }, (message) => {
    console.error('获取活动列表失败:', message)
    ElMessage.error(message || '获取活动列表失败')
    loading.value = false
  })
}

// 编辑活动
const editActivity = (activity) => {
  editingActivity.value = activity
  Object.keys(activityForm).forEach(key => {
    activityForm[key] = activity[key] || (key === 'maxParticipants' ? 0 : '')
  })
  showCreateDialog.value = true
}

// 处理状态命令
const handleStatusCommand = async (command, activity) => {
  let status
  let message
  
  switch (command) {
    case 'start':
      status = 1
      message = '确定要开始这个活动吗？'
      break
    case 'end':
      status = 2
      message = '确定要结束这个活动吗？'
      break
    case 'cancel':
      status = 3
      message = '确定要取消这个活动吗？'
      break
    default:
      return
  }
  
  try {
    await ElMessageBox.confirm(message, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    updateActivityStatus(activity.id, status, () => {
      ElMessage.success('状态更新成功')
      fetchActivities()
    }, (message) => {
      ElMessage.error(message || '状态更新失败')
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新活动状态失败:', error)
      ElMessage.error('更新活动状态失败')
    }
  }
}

// 删除活动
const deleteActivityItem = async (activity) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    deleteActivity(activity.id, () => {
      ElMessage.success('活动删除成功')
      fetchActivities()
    }, (message) => {
      ElMessage.error(message || '删除失败')
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除活动失败:', error)
      ElMessage.error('删除活动失败')
    }
  }
}

// 提交活动表单
const submitActivity = () => {
  activityFormRef.value?.validate((valid) => {
    if (!valid) return
    
    submitting.value = true
    
    const formData = { ...activityForm }
    
    const success = () => {
      ElMessage.success(editingActivity.value ? '活动更新成功' : '活动发布成功')
      handleDialogClose()
      fetchActivities()
      submitting.value = false
    }
    
    const failure = (message) => {
      console.error('提交活动失败:', message)
      ElMessage.error(message || '操作失败')
      submitting.value = false
    }
    
    if (editingActivity.value) {
      updateActivity(editingActivity.value.id, formData, success, failure)
    } else {
      createActivity(formData, success, failure)
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  showCreateDialog.value = false
  editingActivity.value = null
  activityFormRef.value?.resetFields()
  Object.keys(activityForm).forEach(key => {
    activityForm[key] = key === 'maxParticipants' ? 0 : ''
  })
}

// 封面图片上传成功
const handleCoverUploadSuccess = (response) => {
  if (response.data) {
    // 构建完整的图片URL，参考其他页面的实现
    activityForm.coverImage = (axios.defaults.baseURL || '') + '/images' + response.data
    ElMessage.success('封面图片上传成功')
  } else {
    ElMessage.error('图片上传失败：响应格式错误')
  }
}

// 封面图片上传前验证
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'     // 未开始
    case 1: return 'success'  // 进行中
    case 2: return 'warning'  // 已结束
    case 3: return 'danger'   // 已取消
    default: return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}



onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.activity-admin-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.activity-table-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-section {
  width: 100%;
}

.cover-preview {
  margin-top: 10px;
  position: relative;
  display: inline-block;
}

.cover-preview img {
  width: 200px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
}

.remove-cover {
  position: absolute;
  top: -8px;
  right: -8px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.ql-editor) {
  min-height: 200px;
}

:deep(.ql-toolbar) {
  border-top: none;
  border-left: none;
  border-right: none;
}

:deep(.ql-container) {
  border-bottom: none;
  border-left: none;
  border-right: none;
}
</style> 