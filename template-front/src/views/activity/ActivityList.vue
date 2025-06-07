<template>
  <div class="activity-container">
    <div class="page-header">
      <h1>校园活动</h1>
      <p>探索丰富多彩的校园活动，参与精彩的大学生活</p>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-select v-model="selectedStatus" @change="filterActivities" placeholder="按状态筛选" clearable style="width: 150px;">
        <el-option label="全部" value="" />
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已结束" :value="2" />
      </el-select>
      <el-button @click="fetchActivities"  type="primary" text><el-icon><Refresh/></el-icon>刷新</el-button>
    </div>

    <!-- 活动列表 -->
    <div class="activity-list" v-loading="loading">
      <div 
        v-for="activity in filteredActivities" 
        :key="activity.id" 
        class="activity-card"
        @click="goToDetail(activity.id)"
      >
        <div class="activity-cover">
          <img 
            v-if="activity.coverImage" 
            :src="activity.coverImage" 
            :alt="activity.title"
            @error="handleImageError"
          />
          <div v-else class="default-cover">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        
        <div class="activity-info">
          <div class="activity-header">
            <h3 class="activity-title">{{ activity.title }}</h3>
            <el-tag 
              :type="getStatusType(activity.status)"
              size="small"
            >
              {{ activity.statusText }}
            </el-tag>
          </div>
          
          <div class="activity-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ activity.location }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(activity.activityTime) }}</span>
            </div>
            <div class="meta-item" v-if="activity.maxParticipants > 0">
              <el-icon><User /></el-icon>
              <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</span>
            </div>
          </div>
          
          <div class="activity-summary">
            {{ getTextContent(activity.content) }}
          </div>
          
          <div class="activity-footer">
            <span class="organizer">发布者：{{ activity.adminUsername }}</span>
            <span class="publish-time">{{ formatDate(activity.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredActivities.length === 0" class="empty-state">
      <el-empty description="暂无校园活动" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Location, Clock, User, Refresh } from '@element-plus/icons-vue'
import { getActivityList } from '@/net/activity'

const router = useRouter()
const loading = ref(false)
const activities = ref([])
const selectedStatus = ref('')

// 计算筛选后的活动
const filteredActivities = computed(() => {
  if (selectedStatus.value === '') {
    return activities.value
  }
  return activities.value.filter(activity => activity.status === selectedStatus.value)
})

// 获取活动列表
const fetchActivities = () => {
  loading.value = true
  getActivityList((data) => {
    activities.value = data || []
    loading.value = false
  }, (message) => {
    console.error('获取活动列表失败:', message)
    ElMessage.error(message || '获取活动列表失败')
    loading.value = false
  })
}

// 筛选活动
const filterActivities = () => {
  // 通过computed自动处理
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/index/activity/${id}`)
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

// 获取文本内容（去除HTML标签）
const getTextContent = (html) => {
  if (!html) return ''
  const div = document.createElement('div')
  div.innerHTML = html
  const text = div.textContent || div.innerText || ''
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = '<div class="default-cover"><i class="el-icon-picture"></i></div>'
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.activity-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 32px;
  font-weight: bold;
}

.page-header p {
  color: #7f8c8d;
  font-size: 16px;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.activity-card {
  border: 1px solid #e6ebf5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  min-height: 180px;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.activity-cover {
  width: 280px;
  height: 180px;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
  flex-shrink: 0;
}

.activity-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #c0c4cc;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.activity-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.activity-title {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0;
  flex: 1;
  margin-right: 15px;
  line-height: 1.3;
}

.activity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.meta-item .el-icon {
  margin-right: 6px;
  color: #909399;
}

.activity-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-top: auto;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

@media (max-width: 768px) {
  .activity-container {
    padding: 15px;
  }
  
  .activity-card {
    flex-direction: column;
    min-height: auto;
  }
  
  .activity-cover {
    width: 100%;
    height: 200px;
  }
  
  .activity-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .activity-title {
    font-size: 18px;
  }
  
  .activity-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style> 