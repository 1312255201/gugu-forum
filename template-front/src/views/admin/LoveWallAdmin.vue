<template>
  <div class="love-wall-admin">
    <div class="admin-header">
      <h2>表白墙管理</h2>
      <p>审核用户发布的表白信息，确保内容健康合规</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审核列表</span>
          <el-button @click="refreshData" :icon="Refresh" circle />
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="pendingList.length === 0" class="empty-container">
        <el-empty description="暂无待审核的表白信息" />
      </div>

      <div v-else class="pending-list">
        <div v-for="item in pendingList" :key="item.id" class="pending-item">
          <el-card class="item-card">
            <div class="item-content">
              <div class="item-header">
                <div class="user-info">
                  <img :src="item.avatar" :alt="item.nickname" class="user-avatar" />
                  <div class="user-details">
                    <h4>{{ item.nickname }}</h4>
                    <p>{{ item.age }}岁 · {{ item.genderText }}</p>
                    <p class="submit-time">提交时间：{{ formatTime(item.createTime) }}</p>
                  </div>
                </div>
                <div class="action-buttons">
                  <el-button type="success" @click="approveItem(item.id, 1)">
                    <el-icon><Check /></el-icon>
                    通过
                  </el-button>
                  <el-button type="danger" @click="approveItem(item.id, 2)">
                    <el-icon><Close /></el-icon>
                    拒绝
                  </el-button>
                  <el-button @click="viewDetail(item)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                </div>
              </div>

              <div class="item-body">
                <div class="tags-section" v-if="item.tags && item.tags.length > 0">
                  <span class="section-label">兴趣标签：</span>
                  <el-tag v-for="tag in item.tags" :key="tag" size="small" class="tag-item">
                    {{ tag }}
                  </el-tag>
                </div>

                <div class="introduction-section">
                  <span class="section-label">自我介绍：</span>
                  <p class="introduction-text">{{ item.introduction }}</p>
                </div>

                <div class="contact-section">
                  <span class="section-label">联系方式：</span>
                  <span class="contact-text">{{ item.contact }}</span>
                </div>

                <div class="photos-section" v-if="item.photos && item.photos.length > 0">
                  <span class="section-label">相册：</span>
                  <div class="photos-preview">
                    <img 
                      v-for="(photo, index) in item.photos.slice(0, 4)" 
                      :key="index" 
                      :src="photo" 
                      :alt="`照片${index + 1}`"
                      @click="previewPhotos(item.photos, index)"
                      class="photo-thumb"/>
                    <div v-if="item.photos.length > 4" class="more-photos">
                      +{{ item.photos.length - 4 }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="表白信息详情"
      width="70%"
      class="detail-dialog">
      <div v-if="selectedItem" class="detail-content">
        <div class="detail-header">
          <div class="avatar-section">
            <img :src="selectedItem.avatar" :alt="selectedItem.nickname" class="detail-avatar" />
            <div class="basic-info">
              <h3>{{ selectedItem.nickname }}</h3>
              <p>{{ selectedItem.age }}岁 · {{ selectedItem.genderText }}</p>
              <div class="tags">
                <el-tag v-for="tag in selectedItem.tags" :key="tag" size="small">{{ tag }}</el-tag>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-body">
          <div class="photos-gallery" v-if="selectedItem.photos && selectedItem.photos.length > 0">
            <h4>相册</h4>
            <div class="photos-grid">
              <img 
                v-for="(photo, index) in selectedItem.photos" 
                :key="index" 
                :src="photo" 
                :alt="`照片${index + 1}`"
                @click="previewPhotos(selectedItem.photos, index)"
              />
            </div>
          </div>

          <div class="introduction-section">
            <h4>自我介绍</h4>
            <p>{{ selectedItem.introduction }}</p>
          </div>

          <div class="contact-section">
            <h4>联系方式</h4>
            <p>{{ selectedItem.contact }}</p>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button type="success" @click="approveItem(selectedItem.id, 1)" v-if="selectedItem">
            通过审核
          </el-button>
          <el-button type="danger" @click="approveItem(selectedItem.id, 2)" v-if="selectedItem">
            拒绝审核
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { Refresh, Check, Close, View } from '@element-plus/icons-vue'
import { getPendingLoveWallList, approveLoveWall } from '@/net/lovewall.js'

// 响应式数据
const loading = ref(false)
const pendingList = ref([])
const showDetailDialog = ref(false)
const selectedItem = ref(null)

// 生命周期
onMounted(() => {
  fetchPendingList()
})

// 获取待审核列表
const fetchPendingList = () => {
  loading.value = true
  getPendingLoveWallList((data) => {
    pendingList.value = data || []
    loading.value = false
  }, (message) => {
    ElMessage.error(message || '获取待审核列表失败')
    loading.value = false
  })
}

// 刷新数据
const refreshData = () => {
  fetchPendingList()
}

// 审核项目
const approveItem = async (id, status) => {
  const statusText = status === 1 ? '通过' : '拒绝'
  
  try {
    await ElMessageBox.confirm(
      `确定要${statusText}这条表白信息吗？`,
      '确认审核',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    approveLoveWall(id, status, () => {
      ElMessage.success(`${statusText}成功`)
      fetchPendingList()
      if (selectedItem.value && selectedItem.value.id === id) {
        showDetailDialog.value = false
        selectedItem.value = null
      }
    }, (message) => {
      ElMessage.error(message || `${statusText}失败`)
    })
  } catch (error) {
    if (error !== 'cancel') {
      // 用户取消了操作
    }
  }
}

// 查看详情
const viewDetail = (item) => {
  selectedItem.value = item
  showDetailDialog.value = true
}

// 预览照片
const previewPhotos = (photos, index) => {
  const imageViewer = ElImageViewer({
    urlList: photos,
    initialIndex: index,
    hideOnClickModal: true
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleString()
}
</script>

<style scoped>
.love-wall-admin {
  padding: 20px;
}

.admin-header {
  margin-bottom: 30px;
}

.admin-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.admin-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container,
.empty-container {
  padding: 40px;
  text-align: center;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pending-item {
  width: 100%;
}

.item-card {
  transition: all 0.3s ease;
}

.item-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.item-content {
  padding: 10px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.user-info {
  display: flex;
  gap: 15px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e91e63;
}

.user-details h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.user-details p {
  margin: 3px 0;
  color: #666;
  font-size: 14px;
}

.submit-time {
  font-size: 12px !important;
  color: #999 !important;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.item-body {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.section-label {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.tags-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}

.tag-item {
  margin: 2px;
}

.introduction-section {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
}

.introduction-text {
  margin: 8px 0 0 0;
  line-height: 1.6;
  color: #555;
}

.contact-section {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #fff3cd;
  border-radius: 8px;
}

.contact-text {
  color: #856404;
  font-weight: 500;
}

.photos-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.photos-preview {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.photo-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease;
  border: 2px solid #ddd;
}

.photo-thumb:hover {
  transform: scale(1.05);
  border-color: #e91e63;
}

.more-photos {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
  font-weight: bold;
}

.detail-dialog {
  border-radius: 15px;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.avatar-section {
  display: flex;
  gap: 20px;
}

.detail-avatar {
  width: 120px;
  height: 120px;
  border-radius: 15px;
  object-fit: cover;
  border: 3px solid #ff69b4;
}

.basic-info h3 {
  margin: 0 0 10px 0;
  color: #e91e63;
  font-size: 1.5rem;
}

.basic-info p {
  margin: 5px 0;
  color: #666;
}

.tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.detail-body {
  margin-bottom: 20px;
}

.photos-gallery {
  margin-bottom: 30px;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.photos-grid img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.photos-grid img:hover {
  transform: scale(1.05);
}

.introduction-section,
.contact-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
}

.introduction-section h4,
.contact-section h4 {
  margin: 0 0 10px 0;
  color: #e91e63;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .love-wall-admin {
    padding: 10px;
  }
  
  .item-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .photos-preview {
    justify-content: center;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .avatar-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style> 