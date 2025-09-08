<template>
  <div class="lost-found-container">
    <!-- 页面标题和发布按钮 -->
    <div class="header-section">
      <div class="title-section">
        <h2>失物招领</h2>
        <p>帮助大家找回丢失的物品</p>
      </div>
      <el-button type="primary" @click="$router.push('/index/lost-found/create')" size="large">
        <el-icon><Plus /></el-icon>
        发布失物招领
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-item">
            <label>丢失位置：</label>
            <el-input 
              v-model="filters.location" 
              placeholder="请输入丢失位置" 
              clearable
              style="width: 200px"
            />
          </div>
          <div class="filter-item">
            <label>丢失时间：</label>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
              style="width: 240px"
            />
          </div>
          <div class="filter-item">
            <label>状态：</label>
            <el-select v-model="filters.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="全部" :value="null" />
              <el-option label="寻找中" :value="0" />
              <el-option label="已找到" :value="1" />
              <el-option label="已过期" :value="2" />
            </el-select>
          </div>
          <div class="filter-item">
            <el-button type="primary" @click="searchLostFound" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 失物招领列表 -->
    <div class="list-section" v-loading="loading">
      <div class="empty-state" v-if="!loading && lostFoundList.length === 0">
        <el-empty description="暂无失物招领信息" />
      </div>
      <div class="lost-found-grid" v-else>
        <el-card 
          v-for="item in lostFoundList" 
          :key="item.id" 
          class="lost-found-card"
          shadow="hover"
          @click="viewDetail(item.id)"
        >
          <div class="card-header">
            <div class="status-badge">
              <el-tag 
                :type="getStatusType(item.status)" 
                size="small"
              >
                {{ item.statusText }}
              </el-tag>
            </div>
            <div class="post-time">
              {{ formatDate(item.createTime) }}
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="title">{{ item.title }}</h3>
            <div class="description">{{ item.description }}</div>
            
            <div class="info-row">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ item.location }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDate(item.lostTime) }}</span>
              </div>
            </div>
            
            <div class="images-preview" v-if="item.images && item.images.length > 0">
              <el-image
                v-for="(image, index) in item.images.slice(0, 3)"
                :key="index"
                :src="image"
                class="preview-image"
                fit="cover"
                :preview-src-list="item.images"
                :initial-index="index"
              />
              <div v-if="item.images.length > 3" class="more-images">
                +{{ item.images.length - 3 }}
              </div>
            </div>
            
            <div class="card-footer">
              <div class="author">
                <el-icon><User /></el-icon>
                <span>{{ item.username }}</span>
              </div>
              <div class="contact">
                <el-icon><Phone /></el-icon>
                <span>{{ item.contactInfo }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getLostFoundList } from '@/net/api/lostfound'
import { ElMessage } from 'element-plus'
import { 
  Plus, Search, Refresh, Location, Clock, User, Phone 
} from '@element-plus/icons-vue'
import { recordVisit } from '@/net/statistics.js';

const router = useRouter()
const loading = ref(false)
const lostFoundList = ref([])
const dateRange = ref([])

const filters = reactive({
  location: '',
  startTime: '',
  endTime: '',
  status: null
})

// 搜索失物招领
const searchLostFound = () => {
  loading.value = true
  getLostFoundList(filters, (data) => {
    lostFoundList.value = data
    loading.value = false
  }, (message) => {
    ElMessage.error(message)
    loading.value = false
  })
}

// 重置筛选条件
const resetFilters = () => {
  filters.location = ''
  filters.startTime = ''
  filters.endTime = ''
  filters.status = null
  dateRange.value = []
  searchLostFound()
}

// 处理日期范围变化
const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    filters.startTime = dates[0]
    filters.endTime = dates[1]
  } else {
    filters.startTime = ''
    filters.endTime = ''
  }
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/index/lost-found/${id}`)
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  // 记录页面访问
  try {
    await recordVisit();
  } catch (error) {
    console.error('记录页面访问失败:', error);
  }
  
  searchLostFound()
})
</script>

<style lang="less" scoped>
.lost-found-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .title-section {
    h2 {
      margin: 0 0 5px 0;
      color: #303133;
      font-size: 24px;
    }
    
    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.filter-card {
  margin-bottom: 20px;
  
  .filter-section {
    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      align-items: center;
      
      .filter-item {
        display: flex;
        align-items: center;
        gap: 8px;
        
        label {
          font-size: 14px;
          color: #606266;
          white-space: nowrap;
        }
      }
    }
  }
}

.list-section {
  min-height: 400px;
}

.lost-found-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.lost-found-card {
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .post-time {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .card-content {
    .title {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .description {
      font-size: 14px;
      color: #606266;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .info-row {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;
      
      .info-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #909399;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
    
    .images-preview {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
      
      .preview-image {
        width: 60px;
        height: 60px;
        border-radius: 4px;
        object-fit: cover;
      }
      
      .more-images {
        width: 60px;
        height: 60px;
        background: #f5f7fa;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: #909399;
      }
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
      
      .author, .contact {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #909399;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

@media (max-width: 768px) {
  .lost-found-container {
    padding: 10px;
  }
  
  .header-section {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .filter-section .filter-row {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
    
    .filter-item {
      flex-direction: column;
      align-items: stretch;
      gap: 5px;
    }
  }
  
  .lost-found-grid {
    grid-template-columns: 1fr;
  }
}
</style>