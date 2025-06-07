<template>
  <div class="activity-detail-container">
    <div v-if="loading" class="loading-container">
      <el-loading />
    </div>
    
    <div v-else-if="activity" class="activity-detail">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button @click="goBack"  type="primary" text>
          <el-icon><ArrowLeft/></el-icon>
          返回活动列表
        </el-button>
      </div>

      <!-- 活动头部 -->
      <div class="activity-header">
        <div class="cover-section">
          <img 
            v-if="activity.coverImage" 
            :src="activity.coverImage" 
            :alt="activity.title"
            class="cover-image"
            @error="handleImageError"
          />
          <div v-else class="default-cover">
            <el-icon size="80"><Calendar /></el-icon>
          </div>
        </div>
        
        <div class="header-info">
          <div class="title-section">
            <h1 class="activity-title">{{ activity.title }}</h1>
            <el-tag 
              :type="getStatusType(activity.status)"
              size="large"
            >
              {{ activity.statusText }}
            </el-tag>
          </div>
          
          <div class="meta-section">
            <div class="meta-grid">
              <div class="meta-item">
                <el-icon class="meta-icon"><Location /></el-icon>
                <div>
                  <div class="meta-label">活动地点</div>
                  <div class="meta-value">{{ activity.location }}</div>
                </div>
              </div>
              
              <div class="meta-item">
                <el-icon class="meta-icon"><Clock /></el-icon>
                <div>
                  <div class="meta-label">活动时间</div>
                  <div class="meta-value">{{ formatDateTime(activity.activityTime) }}</div>
                  <div v-if="activity.endTime" class="meta-sub">
                    至 {{ formatDateTime(activity.endTime) }}
                  </div>
                </div>
              </div>
              
              <div class="meta-item" v-if="activity.maxParticipants > 0">
                <el-icon class="meta-icon"><User /></el-icon>
                <div>
                  <div class="meta-label">参与人数</div>
                  <div class="meta-value">
                    {{ activity.currentParticipants }}/{{ activity.maxParticipants }}人
                  </div>
                  <el-progress 
                    :percentage="participantPercentage" 
                    :show-text="false"
                    :stroke-width="4"
                  />
                </div>
              </div>
              
              <div class="meta-item">
                <el-icon class="meta-icon"><UserFilled /></el-icon>
                <div>
                  <div class="meta-label">发布者</div>
                  <div class="meta-value">{{ activity.adminUsername }}</div>
                  <div class="meta-sub">{{ formatDate(activity.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动内容 -->
      <div class="activity-content">
        <h2>活动详情</h2>
        <div class="content-body" v-html="activity.content"></div>
      </div>
    </div>
    
    <div v-else class="error-state">
      <el-empty description="活动不存在或已被删除">
        <el-icon><ArrowLeft/></el-icon>
        <el-button type="primary" @click="goBack">返回活动列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Location, Clock, User, UserFilled, ArrowLeft } from '@element-plus/icons-vue'
import { getActivityById } from '@/net/activity'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activity = ref(null)

// 计算参与度百分比
const participantPercentage = computed(() => {
  if (!activity.value || activity.value.maxParticipants <= 0) {
    return 0
  }
  return Math.round((activity.value.currentParticipants / activity.value.maxParticipants) * 100)
})

// 获取活动详情
const fetchActivity = () => {
  loading.value = true
  const id = route.params.id
  getActivityById(id, (data) => {
    activity.value = data
    loading.value = false
  }, (message) => {
    console.error('获取活动详情失败:', message)
    ElMessage.error(message || '获取活动详情失败')
    loading.value = false
  })
}

// 返回活动列表
const goBack = () => {
  router.push('/index/activity')
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

// 格式化日期
const formatDate = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleDateString('zh-CN')
}

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.style.display = 'none'
}

onMounted(() => {
  fetchActivity()
})
</script>

<style scoped>
.activity-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-button {
  margin-bottom: 20px;
}

.activity-header {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 30px;
}

.cover-section {
  flex-shrink: 0;
  width: 300px;
}

.cover-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.default-cover {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  color: #c0c4cc;
}

.header-info {
  flex: 1;
}

.title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 25px;
}

.activity-title {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0;
  flex: 1;
  margin-right: 20px;
  line-height: 1.3;
}

.meta-section {
  margin-top: 20px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.meta-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
  flex-shrink: 0;
}

.meta-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 2px;
}

.meta-sub {
  font-size: 12px;
  color: #606266;
}

.activity-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.activity-content h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 22px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.content-body {
  line-height: 1.8;
  color: #2c3e50;
  font-size: 16px;
}

.content-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 15px 0;
}

.content-body :deep(p) {
  margin-bottom: 15px;
}

.content-body :deep(h1),
.content-body :deep(h2),
.content-body :deep(h3) {
  color: #2c3e50;
  margin: 20px 0 15px 0;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
}

@media (max-width: 768px) {
  .activity-detail-container {
    padding: 15px;
  }
  
  .activity-header {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .cover-section {
    width: 100%;
  }
  
  .title-section {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .meta-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .activity-content {
    padding: 20px;
  }
}
</style> 