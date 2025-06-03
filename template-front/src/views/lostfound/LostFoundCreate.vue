<template>
  <div class="create-container">
    <div class="header-section">
      <el-button @click="$router.back()" type="text" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>发布失物招领</h2>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="left"
        size="large"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入失物招领标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请详细描述丢失物品的特征、外观等信息"
            :rows="4"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="丢失位置" prop="location">
          <el-input
            v-model="form.location"
            placeholder="请输入丢失位置，如：图书馆、教学楼等"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="丢失时间" prop="lostTime">
          <el-date-picker
            v-model="form.lostTime"
            type="datetime"
            placeholder="请选择丢失时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactInfo">
          <el-input
            v-model="form.contactInfo"
            placeholder="请输入联系方式，如：手机号、QQ、微信等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="物品图片">
          <div class="upload-section">
            <el-upload
              ref="uploadRef"
              :action="uploadAction"
              :headers="uploadHeaders"
              :file-list="fileList"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleRemove"
              :before-upload="beforeUpload"
              list-type="picture-card"
              :limit="6"
              accept="image/*"
              name="file"
            >
              <el-icon class="upload-icon"><Plus /></el-icon>
              <template #tip>
                <div class="upload-tip">
                  支持 jpg、png、gif 格式，单张图片不超过 5MB，最多上传 6 张
                </div>
              </template>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="button-group">
            <el-button @click="$router.back()" size="large">取消</el-button>
            <el-button 
              type="primary" 
              @click="submitForm" 
              :loading="submitting"
              size="large"
            >
              发布失物招领
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createLostFound } from '@/net/api/lostfound'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { accessHeader } from '@/net'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const uploadRef = ref()
const submitting = ref(false)
const fileList = ref([])

// 上传配置 - 参考TopicEditor.vue的实现
const uploadAction = (axios.defaults.baseURL || '') + '/api/image/cache'
const uploadHeaders = accessHeader()

const form = reactive({
  title: '',
  description: '',
  location: '',
  lostTime: '',
  contactInfo: '',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 1, max: 1000, message: '描述长度在 1 到 1000 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入丢失位置', trigger: 'blur' },
    { min: 1, max: 100, message: '位置长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  lostTime: [
    { required: true, message: '请选择丢失时间', trigger: 'change' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { min: 1, max: 200, message: '联系方式长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

// 上传前检查
const beforeUpload = (file) => {
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

// 上传成功 - 参考TopicEditor.vue的响应处理方式
const handleUploadSuccess = (response, file) => {
  if (response.data) {
    // 构建完整的图片URL，参考TopicEditor.vue的方式
    const imageUrl = (axios.defaults.baseURL || '') + '/images' + response.data
    form.images.push(imageUrl)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败：响应格式错误')
    // 移除失败的文件
    const index = fileList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 上传失败
const handleUploadError = (error, file) => {
  ElMessage.error('图片上传失败')
  console.error('Upload error:', error)
  // 移除失败的文件
  const index = fileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

// 移除图片
const handleRemove = (file) => {
  // 根据响应数据找到对应的图片URL并移除
  if (file.response && file.response.data) {
    const imageUrl = (axios.defaults.baseURL || '') + '/images' + file.response.data
    const index = form.images.findIndex(url => url === imageUrl)
    if (index > -1) {
      form.images.splice(index, 1)
    }
  }
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      const submitData = {
        ...form,
        lostTime: new Date(form.lostTime).toISOString()
      }
      
      createLostFound(submitData, () => {
        ElMessage.success('失物招领发布成功!')
        router.push('/index/lost-found')
      }, (message) => {
        ElMessage.error(message || '发布失败')
        submitting.value = false
      })
    }
  })
}
</script>

<style lang="less" scoped>
.create-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    color: #303133;
    font-size: 24px;
  }
}

.form-card {
  .el-form {
    padding: 20px;
  }
  
  .upload-section {
    width: 100%;
    
    :deep(.el-upload--picture-card) {
      width: 100px;
      height: 100px;
    }
    
    :deep(.el-upload-list--picture-card .el-upload-list__item) {
      width: 100px;
      height: 100px;
    }
    
    .upload-icon {
      font-size: 28px;
      color: #8c939d;
    }
    
    .upload-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 10px;
      line-height: 1.4;
    }
  }
  
  .button-group {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 30px;
    
    .el-button {
      min-width: 120px;
    }
  }
}

@media (max-width: 768px) {
  .create-container {
    padding: 10px;
  }
  
  .form-card .el-form {
    padding: 15px;
  }
  
  .button-group {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style> 