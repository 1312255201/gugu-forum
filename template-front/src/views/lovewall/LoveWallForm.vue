<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑表白信息' : '发布表白信息'"
    width="70%"
    :before-close="handleClose"
    class="love-wall-form-dialog">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="love-wall-form">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="formData.nickname"
              placeholder="请输入昵称"
              maxlength="50"
              show-word-limit/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄" prop="age">
            <el-input-number
              v-model="formData.age"
              :min="16"
              :max="60"
              placeholder="请输入年龄"
              style="width: 100%"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :label="0">女</el-radio>
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">其他</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系方式" prop="contact">
            <el-input
              v-model="formData.contact"
              placeholder="QQ/微信/手机号等"
              maxlength="200"
              show-word-limit/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="头像" prop="avatar">
        <div class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-success="handleAvatarSuccess"
            :on-error="handleUploadError">
            <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <p class="upload-tip">上传头像，建议尺寸1:1，大小不超过5MB</p>
        </div>
      </el-form-item>

      <el-form-item label="相册" prop="photos">
        <div class="photos-upload">
          <el-upload
            class="photos-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :file-list="photoFileList"
            list-type="picture-card"
            :limit="6"
            :before-upload="beforePhotoUpload"
            :on-success="handlePhotoSuccess"
            :on-remove="handlePhotoRemove"
            :on-error="handleUploadError"
            :on-exceed="handleExceed">
            <el-icon><Plus /></el-icon>
          </el-upload>
          <p class="upload-tip">最多上传6张照片，每张不超过5MB</p>
        </div>
      </el-form-item>

      <el-form-item label="兴趣标签">
        <div class="tags-section">
          <div class="selected-tags">
            <el-tag
              v-for="tag in formData.tags"
              :key="tag"
              closable
              @close="removeTag(tag)"
              class="tag-item">
              {{ tag }}
            </el-tag>
          </div>
          <div class="preset-tags">
            <span class="tags-label">快选标签：</span>
            <el-tag
              v-for="tag in presetTags"
              :key="tag"
              :type="formData.tags.includes(tag) ? 'success' : 'info'"
              @click="toggleTag(tag)"
              class="preset-tag"
              style="cursor: pointer; margin: 3px;">
              {{ tag }}
            </el-tag>
          </div>
          <div class="custom-tag">
            <el-input
              v-model="customTag"
              placeholder="输入自定义标签"
              @keyup.enter="addCustomTag"
              style="width: 150px; margin-top: 10px;">
              <template #append>
                <el-button @click="addCustomTag">添加</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="自我介绍" prop="introduction">
        <el-input
          v-model="formData.introduction"
          type="textarea"
          :rows="5"
          placeholder="介绍一下自己吧，让别人更了解你~"
          maxlength="1000"
          show-word-limit/>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '发布' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createLoveWall, updateLoveWall } from '@/net/lovewall.js'
import axios from 'axios'
import {accessHeader} from "@/net/index.js";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  editData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

// 响应式数据
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const isEdit = computed(() => !!props.editData)
const formRef = ref(null)
const submitting = ref(false)
const customTag = ref('')

// 表单数据
const formData = ref({
  nickname: '',
  age: null,
  gender: null,
  contact: '',
  avatar: '',
  photos: [],
  tags: [],
  introduction: ''
})

// 照片文件列表
const photoFileList = ref([])

// 预设标签
const presetTags = [
  '音乐', '电影', '读书', '旅行', '运动', '游戏', '美食', '摄影',
  '绘画', '书法', '舞蹈', '唱歌', '健身', '瑜伽', '篮球', '足球',
  '羽毛球', '乒乓球', '游泳', '跑步', '登山', '骑行', '滑雪', '潜水',
  '编程', '设计', '写作', '手工', '园艺', '烹饪', '咖啡', '茶艺',
  '动漫', '小说', '历史', '哲学', '心理学', '天文', '地理', '生物'
]

// 上传配置
const uploadUrl = computed(() => axios.defaults.baseURL + '/api/image/cache')
const uploadHeaders = computed(() => ({
  accessHeader
}))

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 50, message: '昵称长度在1-50个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 16, max: 60, message: '年龄需在16-60岁之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { min: 1, max: 200, message: '联系方式长度在1-200个字符', trigger: 'blur' }
  ],
  avatar: [
    { required: true, message: '请上传头像', trigger: 'blur' }
  ],
  introduction: [
    { required: true, message: '请输入自我介绍', trigger: 'blur' },
    { min: 10, max: 1000, message: '自我介绍长度在10-1000个字符', trigger: 'blur' }
  ]
}

// 监听编辑数据变化
watch(() => props.editData, (newData) => {
  if (newData) {
    formData.value = {
      nickname: newData.nickname || '',
      age: newData.age || null,
      gender: newData.gender,
      contact: newData.contact || '',
      avatar: newData.avatar || '',
      photos: newData.photos || [],
      tags: newData.tags || [],
      introduction: newData.introduction || ''
    }
    
    // 更新照片文件列表
    photoFileList.value = (newData.photos || []).map((url, index) => ({
      name: `photo${index + 1}`,
      url: url
    }))
  }
}, { immediate: true, deep: true })

// 监听对话框打开
watch(dialogVisible, (visible) => {
  if (visible && !props.editData) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  formData.value = {
    nickname: '',
    age: null,
    gender: null,
    contact: '',
    avatar: '',
    photos: [],
    tags: [],
    introduction: ''
  }
  photoFileList.value = []
  customTag.value = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 头像上传前检查
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.data) {
    // 构建完整的图片URL
    formData.value.avatar = axios.defaults.baseURL + '/images' + response.data
    ElMessage.success('头像上传成功')
    // 手动触发avatar字段验证
    if (formRef.value) {
      formRef.value.validateField('avatar')
    }
  } else {
    ElMessage.error('头像上传失败')
  }
}

// 照片上传前检查
const beforePhotoUpload = (file) => {
  return beforeAvatarUpload(file)
}

// 照片上传成功
const handlePhotoSuccess = (response, file) => {
  if (response.data) {
    // 构建完整的图片URL
    formData.value.photos.push(axios.defaults.baseURL + '/images' + response.data)
    ElMessage.success('照片上传成功')
  } else {
    ElMessage.error('照片上传失败')
  }
}

// 移除照片
const handlePhotoRemove = (file) => {
  const index = photoFileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1 && index < formData.value.photos.length) {
    formData.value.photos.splice(index, 1)
  }
}

// 上传错误处理
const handleUploadError = (error) => {
  console.error('Upload error:', error)
  ElMessage.error('上传失败，请重试')
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传6张照片')
}

// 切换标签
const toggleTag = (tag) => {
  const index = formData.value.tags.indexOf(tag)
  if (index > -1) {
    formData.value.tags.splice(index, 1)
  } else {
    if (formData.value.tags.length < 10) {
      formData.value.tags.push(tag)
    } else {
      ElMessage.warning('最多只能选择10个标签')
    }
  }
}

// 移除标签
const removeTag = (tag) => {
  const index = formData.value.tags.indexOf(tag)
  if (index > -1) {
    formData.value.tags.splice(index, 1)
  }
}

// 添加自定义标签
const addCustomTag = () => {
  const tag = customTag.value.trim()
  if (!tag) return
  
  if (formData.value.tags.includes(tag)) {
    ElMessage.warning('标签已存在')
    return
  }
  
  if (formData.value.tags.length >= 10) {
    ElMessage.warning('最多只能选择10个标签')
    return
  }
  
  if (tag.length > 10) {
    ElMessage.warning('标签长度不能超过10个字符')
    return
  }
  
  formData.value.tags.push(tag)
  customTag.value = ''
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  // 提交前检查
  if (!formData.value.avatar) {
    ElMessage.error('请上传头像')
    return
  }
  
  if (!formData.value.introduction || formData.value.introduction.length < 10) {
    ElMessage.error('请输入至少10个字符的自我介绍')
    return
  }
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = { ...formData.value }
    
    const onSuccess = () => {
      ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
      emit('success')
      handleClose()
      submitting.value = false
    }
    
    const onFailure = (message) => {
      ElMessage.error(message || '操作失败')
      submitting.value = false
    }
    
    if (isEdit.value) {
      updateLoveWall(props.editData.id, submitData, onSuccess, onFailure)
    } else {
      createLoveWall(submitData, onSuccess, onFailure)
    }
  } catch (error) {
    console.error('Submit error:', error)
    
    // 如果是验证错误，显示具体的错误信息
    if (error && typeof error === 'object') {
      const firstErrorField = Object.keys(error)[0]
      if (firstErrorField && error[firstErrorField] && error[firstErrorField][0]) {
        ElMessage.error(error[firstErrorField][0].message)
      } else {
        ElMessage.error('表单验证失败，请检查输入')
      }
    } else if (error !== 'validation failed') {
      ElMessage.error('操作失败，请重试')
    }
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  if (!isEdit.value) {
    resetForm()
  }
}
</script>

<style scoped>
.love-wall-form-dialog {
  border-radius: 15px;
}

.love-wall-form {
  padding: 20px;
}

.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 120px;
  height: 120px;
  display: inline-block;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.photos-upload {
  width: 100%;
}

.photos-uploader {
  width: 100%;
}

.upload-tip {
  margin: 8px 0 0 0;
  color: #999;
  font-size: 12px;
}

.tags-section {
  width: 100%;
}

.selected-tags {
  margin-bottom: 15px;
  min-height: 32px;
}

.tag-item {
  margin: 3px;
}

.preset-tags {
  margin-bottom: 10px;
}

.tags-label {
  color: #666;
  font-size: 14px;
  margin-right: 10px;
}

.preset-tag {
  transition: all 0.2s;
}

.preset-tag:hover {
  transform: scale(1.05);
}

.custom-tag {
  margin-top: 10px;
}

.dialog-footer {
  text-align: right;
}

</style>