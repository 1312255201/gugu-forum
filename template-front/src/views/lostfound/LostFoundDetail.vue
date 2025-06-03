<template>
  <div class="detail-container" v-loading="loading">
    <div class="header-section">
      <el-button @click="$router.back()" type="text" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>失物招领详情</h2>
    </div>

    <el-card class="detail-card" shadow="never" v-if="lostFound">
      <div class="detail-header">
        <div class="title-section">
          <h1>{{ lostFound.title }}</h1>
          <div class="meta-info">
            <el-tag :type="getStatusType(lostFound.status)" size="large">
              {{ lostFound.statusText }}
            </el-tag>
            <span class="publish-time">
              发布于 {{ formatDate(lostFound.createTime) }}
            </span>
          </div>
        </div>
        
        <!-- 操作按钮（仅作者可见） -->
        <div class="action-buttons" v-if="isAuthor">
          <el-dropdown @command="handleCommand">
            <el-button type="primary">
              操作 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="found" v-if="lostFound.status === 0">
                  <el-icon><Check /></el-icon>
                  标记为已找到
                </el-dropdown-item>
                <el-dropdown-item command="reopen" v-if="lostFound.status === 1">
                  <el-icon><Refresh /></el-icon>
                  重新开启寻找
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <el-divider />

      <div class="detail-content">
        <div class="info-section">
          <div class="info-grid">
            <div class="info-item">
              <div class="label">
                <el-icon><User /></el-icon>
                发布者
              </div>
              <div class="value">{{ lostFound.username }}</div>
            </div>
            
            <div class="info-item">
              <div class="label">
                <el-icon><Location /></el-icon>
                丢失位置
              </div>
              <div class="value">{{ lostFound.location }}</div>
            </div>
            
            <div class="info-item">
              <div class="label">
                <el-icon><Clock /></el-icon>
                丢失时间
              </div>
              <div class="value">{{ formatDate(lostFound.lostTime) }}</div>
            </div>
            
            <div class="info-item">
              <div class="label">
                <el-icon><Phone /></el-icon>
                联系方式
              </div>
              <div class="value contact-info">{{ lostFound.contactInfo }}</div>
            </div>
          </div>
        </div>

        <div class="description-section">
          <h3>详细描述</h3>
          <div class="description-content">
            {{ lostFound.description }}
          </div>
        </div>

        <div class="images-section" v-if="lostFound.images && lostFound.images.length > 0">
          <h3>物品图片</h3>
          <div class="images-grid">
            <el-image
              v-for="(image, index) in lostFound.images"
              :key="index"
              :src="image"
              class="detail-image"
              fit="cover"
              :preview-src-list="lostFound.images"
              :initial-index="index"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 空状态 -->
    <el-card v-else-if="!loading" class="empty-card" shadow="never">
      <el-empty description="未找到该失物招领信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLostFoundById, updateLostFoundStatus, deleteLostFound } from '@/net/api/lostfound'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowDown, User, Location, Clock, Phone, 
  Check, Refresh, Delete 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const lostFound = ref(null)

// 计算是否为作者
const isAuthor = computed(() => {
  // 这里需要获取当前用户ID，暂时用简单判断
  // 实际项目中应该从用户状态管理中获取当前用户信息
  return lostFound.value && lostFound.value.uid === getCurrentUserId()
})

// 获取当前用户ID（示例实现）
const getCurrentUserId = () => {
  // 这里应该从用户状态管理或token中获取当前用户ID
  // 暂时返回一个示例值
  return 1
}

// 获取详情
const fetchDetail = () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('无效的ID')
    router.back()
    return
  }

  loading.value = true
  getLostFoundById(id, (data) => {
    lostFound.value = data
    loading.value = false
  }, (message) => {
    ElMessage.error(message)
    loading.value = false
  })
}

// 处理操作命令
const handleCommand = (command) => {
  switch (command) {
    case 'found':
      updateStatus(1, '确定要标记为已找到吗？')
      break
    case 'reopen':
      updateStatus(0, '确定要重新开启寻找吗？')
      break
    case 'delete':
      handleDelete()
      break
  }
}

// 更新状态
const updateStatus = (status, confirmText) => {
  ElMessageBox.confirm(confirmText, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    updateLostFoundStatus(lostFound.value.id, status, () => {
      ElMessage.success('状态更新成功')
      fetchDetail() // 重新获取数据
    }, (message) => {
      ElMessage.error(message)
    })
  })
}

// 删除
const handleDelete = () => {
  ElMessageBox.confirm('删除后无法恢复，确定要删除这条失物招领吗？', '确认删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    deleteLostFound(lostFound.value.id, () => {
      ElMessage.success('删除成功')
      router.push('/index/lost-found')
    }, (message) => {
      ElMessage.error(message)
    })
  })
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

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="less" scoped>
.detail-container {
  padding: 20px;
  max-width: 1000px;
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

.detail-card {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .title-section {
      flex: 1;
      
      h1 {
        margin: 0 0 10px 0;
        font-size: 28px;
        color: #303133;
        line-height: 1.3;
      }
      
      .meta-info {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .publish-time {
          color: #909399;
          font-size: 14px;
        }
      }
    }
    
    .action-buttons {
      margin-left: 20px;
    }
  }
  
  .detail-content {
    .info-section {
      margin-bottom: 30px;
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 20px;
        
        .info-item {
          .label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
            
            .el-icon {
              font-size: 16px;
            }
          }
          
          .value {
            font-size: 16px;
            color: #303133;
            font-weight: 500;
            
            &.contact-info {
              background: #f5f7fa;
              padding: 8px 12px;
              border-radius: 4px;
              border: 1px solid #e4e7ed;
            }
          }
        }
      }
    }
    
    .description-section {
      margin-bottom: 30px;
      
      h3 {
        margin: 0 0 15px 0;
        font-size: 18px;
        color: #303133;
      }
      
      .description-content {
        background: #f8f9fa;
        padding: 20px;
        border-radius: 8px;
        line-height: 1.6;
        color: #606266;
        white-space: pre-wrap;
      }
    }
    
    .images-section {
      h3 {
        margin: 0 0 15px 0;
        font-size: 18px;
        color: #303133;
      }
      
      .images-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 15px;
        
        .detail-image {
          width: 100%;
          height: 200px;
          border-radius: 8px;
          cursor: pointer;
          transition: transform 0.3s ease;
          
          &:hover {
            transform: scale(1.02);
          }
        }
      }
    }
  }
}

.empty-card {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 10px;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch !important;
    
    .action-buttons {
      margin-left: 0 !important;
    }
  }
  
  .info-grid {
    grid-template-columns: 1fr !important;
  }
  
  .images-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)) !important;
    
    .detail-image {
      height: 150px !important;
    }
  }
}
</style> 