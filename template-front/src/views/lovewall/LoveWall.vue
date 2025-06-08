<template>
  <div class="love-wall-container">
    <!-- 顶部导航 -->
    <div class="top-nav">
      <h1 class="title">💕 校园表白墙 💕</h1>
      <div class="nav-buttons">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus/></el-icon>
          发布表白
        </el-button>
        <el-button @click="showMyPosts">
          <el-icon><User/></el-icon>
          我的发布
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-select v-model="genderFilter" placeholder="按性别筛选" clearable @change="applyFilters">
        <el-option label="全部" value="" />
        <el-option label="男生" :value="1" />
        <el-option label="女生" :value="0" />
        <el-option label="其他" :value="2" />
      </el-select>
      <el-select v-model="ageFilter" placeholder="按年龄筛选" clearable @change="applyFilters">
        <el-option label="全部" value="" />
        <el-option label="18-22岁" value="18-22" />
        <el-option label="23-26岁" value="23-26" />
        <el-option label="27-30岁" value="27-30" />
      </el-select>
      <el-button @click="refreshData" type="primary" text>
        <el-icon><Refresh/></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 心形3D展示区域 -->
    <div class="heart-container" ref="heartContainer">
      <div class="heart-3d" :class="{ 'heart-beating': isHeartBeating }">
        <div 
          v-for="(item, index) in displayedItems" 
          :key="item.id"
          class="photo-item"
          :style="getPhotoPosition(index)"
          @click="selectItem(item)"
          :class="{ 'selected': selectedItem && selectedItem.id === item.id }"
        >
          <div class="photo-wrapper">
            <img :src="item.avatar" :alt="item.nickname" />
            <div class="photo-info">
              <span class="nickname">{{ item.nickname }}</span>
              <span class="age">{{ item.age }}岁</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中的详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="`${selectedItem?.nickname || ''} 的表白信息`"
      width="80%"
      :show-close="true"
      class="detail-dialog"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :modal="true"
      :append-to-body="true"
      destroy-on-close
    >
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
          <div class="like-section">
            <el-button 
              @click.stop="likeItem(selectedItem.id)" 
              :type="likedItems.has(selectedItem.id) ? 'info' : 'danger'" 
              circle 
              :disabled="likeLoading || likedItems.has(selectedItem.id)"
              v-loading="likeLoading"
            >
              {{ likedItems.has(selectedItem.id) ? '💔' : '❤️' }} {{ selectedItem.likeCount }}
            </el-button>
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
                @click="previewPhoto(selectedItem.photos, index)"
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

        <div class="detail-footer" v-if="selectedItem.isMine">
          <el-button @click="editItem(selectedItem)">编辑</el-button>
          <el-button type="danger" @click="deleteItem(selectedItem.id)">删除</el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeDetailDialog">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑表白信息弹窗 -->
    <LoveWallForm 
      v-model:visible="showCreateDialog" 
      :edit-data="editingItem"
      @success="handleFormSuccess"
    />

    <!-- 我的发布列表弹窗 -->
    <el-dialog 
      v-model="showMyDialog" 
      title="我的表白发布" 
      width="70%"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :modal="true"
      :append-to-body="true"
      destroy-on-close
    >
      <div class="my-posts-list">
        <div v-if="myPosts.length === 0" class="empty-state">
          <el-empty description="您还没有发布过表白信息" />
        </div>
        <div v-else class="posts-grid">
          <div v-for="post in myPosts" :key="post.id" class="post-card">
            <img :src="post.avatar" :alt="post.nickname" />
            <div class="card-info">
              <h4>{{ post.nickname }}</h4>
              <p class="status">{{ post.statusText }}</p>
              <p class="time">{{ formatTime(post.createTime) }}</p>
              <div class="card-actions">
                <el-button size="small" @click="editItem(post)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteItem(post.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus'
import { Plus, User, Refresh } from '@element-plus/icons-vue'
import LoveWallForm from './LoveWallForm.vue'
import { 
  getLoveWallList, 
  getLoveWallListByGender, 
  getLoveWallListByAgeRange,
  getMyLoveWallList,
  likeLoveWall,
  deleteLoveWall 
} from '@/net/lovewall.js'

// 响应式数据
const loveWallList = ref([])
const genderFilter = ref('')
const ageFilter = ref('')
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showMyDialog = ref(false)
const selectedItem = ref(null)
const editingItem = ref(null)
const myPosts = ref([])
const isHeartBeating = ref(false)
const heartContainer = ref(null)
const likeLoading = ref(false)
const likedItems = ref(new Set()) // 记录已点赞的项目

// 计算属性
const displayedItems = computed(() => {
  const maxItems = Math.min(loveWallList.value.length, 40)
  return loveWallList.value.slice(0, maxItems)
})

// 心形位置计算 - 只分布在心形边缘
const getPhotoPosition = (index) => {
  const total = displayedItems.value.length
  if (total === 0) return { left: '50%', top: '50%', transform: 'translate(-50%, -50%)' }
  
  // 将头像均匀分布在心形边缘轮廓上，确保足够间距
  const t = (index / total) * 2 * Math.PI
  
  // 心形参数方程 - 进一步放大，增加间距
  const scale_factor = 450 // 进一步增加心形大小
  const x = scale_factor * Math.pow(Math.sin(t), 3)
  const y = -scale_factor * (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 16
  
  // 减少径向偏移，让位置更准确
  const radialOffset = 2 + (Math.sin(index * 1.618) * 2) // 减少随机偏移
  const offsetX = Math.cos(t + Math.PI/2) * radialOffset // 垂直于切线方向的小偏移
  const offsetY = Math.sin(t + Math.PI/2) * radialOffset
  
  // 添加3D效果，但幅度减小
  const z = Math.sin(t * 2) * 5
  const dynamicScale = 0.95 + 0.05 * Math.sin(Date.now() * 0.001 + index * 0.5)
  
  return {
    left: `calc(50% + ${x + offsetX}px)`,
    top: `calc(50% + ${y + offsetY}px)`,
    transform: `translate(-50%, -50%) translateZ(${z}px) scale(${dynamicScale})`,
    animationDelay: `${index * 0.2}s`,
    zIndex: Math.floor(10 + (z + 10))
  }
}

// 监听弹窗状态变化
watch(showDetailDialog, (newVal) => {
  if (!newVal) {
    // 弹窗关闭时重置选中状态
    selectedItem.value = null
    likeLoading.value = false
  }
})

// 生命周期
onMounted(() => {
  fetchLoveWallList()
  startHeartBeat()
})

// 开始心跳动画
const startHeartBeat = () => {
  setInterval(() => {
    isHeartBeating.value = true
    setTimeout(() => {
      isHeartBeating.value = false
    }, 1000)
  }, 3000)
}

// 获取表白墙列表
const fetchLoveWallList = () => {
  getLoveWallList((data) => {
    loveWallList.value = data || []
    // 从本地存储恢复已点赞状态
    const savedLikes = localStorage.getItem('lovewall_likes')
    if (savedLikes) {
      try {
        const likesArray = JSON.parse(savedLikes)
        likedItems.value = new Set(likesArray)
      } catch (e) {
        console.warn('恢复点赞状态失败:', e)
      }
    }
  }, (message) => {
    ElMessage.error(message || '获取表白墙列表失败')
  })
}

// 应用筛选器
const applyFilters = () => {
  const onSuccess = (data) => {
    loveWallList.value = data || []
  }
  const onFailure = (message) => {
    ElMessage.error(message || '筛选失败')
  }
  
  if (genderFilter.value !== '') {
    getLoveWallListByGender(genderFilter.value, onSuccess, onFailure)
  } else if (ageFilter.value !== '') {
    const [minAge, maxAge] = ageFilter.value.split('-').map(Number)
    getLoveWallListByAgeRange(minAge, maxAge, onSuccess, onFailure)
  } else {
    getLoveWallList(onSuccess, onFailure)
  }
}

// 刷新数据
const refreshData = () => {
  genderFilter.value = ''
  ageFilter.value = ''
  fetchLoveWallList()
}

// 选择项目
const selectItem = (item) => {
  selectedItem.value = item
  showDetailDialog.value = true
}

// 关闭详情弹窗
const closeDetailDialog = () => {
  showDetailDialog.value = false
  selectedItem.value = null
  // 确保loading状态重置
  likeLoading.value = false
}

// 点赞
const likeItem = (id) => {
  // 防止重复点击
  if (likeLoading.value) {
    return
  }
  
  // 检查是否已经点过赞
  if (likedItems.value.has(id)) {
    ElMessage.warning('您已经点过赞了')
    return
  }
  
  likeLoading.value = true
  
  likeLoveWall(id, () => {
    ElMessage.success('点赞成功')
    // 记录已点赞
    likedItems.value.add(id)
    // 保存到本地存储
    localStorage.setItem('lovewall_likes', JSON.stringify([...likedItems.value]))
    // 更新点赞数
    const item = loveWallList.value.find(item => item.id === id)
    if (item) {
      item.likeCount += 1
    }
    if (selectedItem.value && selectedItem.value.id === id) {
      selectedItem.value.likeCount += 1
    }
    likeLoading.value = false
  }, (message) => {
    ElMessage.error(message || '点赞失败')
    likeLoading.value = false
  })
}

// 显示我的发布
const showMyPosts = () => {
  getMyLoveWallList((data) => {
    myPosts.value = data || []
    showMyDialog.value = true
  }, (message) => {
    ElMessage.error(message || '获取我的发布失败')
  })
}

// 编辑项目
const editItem = (item) => {
  editingItem.value = item
  showCreateDialog.value = true
  showMyDialog.value = false
  showDetailDialog.value = false
}

// 删除项目
const deleteItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条表白信息吗？', '确认删除', {
      type: 'warning'
    })
    
    deleteLoveWall(id, () => {
      ElMessage.success('删除成功')
      fetchLoveWallList()
      showMyPosts()
      if (selectedItem.value && selectedItem.value.id === id) {
        closeDetailDialog()
      }
    }, (message) => {
      ElMessage.error(message || '删除失败')
    })
  } catch (error) {
    if (error !== 'cancel') {
      // 用户取消了操作
    }
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  editingItem.value = null
  fetchLoveWallList()
  showMyPosts()
}

// 预览照片
const previewPhoto = (photos, index) => {
  // 使用Element Plus的图片预览组件
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
.love-wall-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
  padding: 20px;
  position: relative;
  overflow-x: hidden;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
}

.title {
  margin: 0;
  color: #e91e63;
  font-size: 2rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-buttons {
  display: flex;
  gap: 10px;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.heart-container {
  position: relative;
  height: 800px;
  width: 100%;
  perspective: 1500px;
  margin: 40px 0;
}

.heart-3d {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.5s ease;
}

.heart-beating {
  animation: heartBeat 1s ease-in-out;
}

@keyframes heartBeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.photo-item {
  position: absolute;
  width: 90px;
  height: 90px;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: float 3s ease-in-out infinite;
  transform-origin: center;
}

.photo-item:hover {
  transform: translate(-50%, -50%) scale(1.2) !important;
  z-index: 100 !important;
}

.photo-item.selected {
  transform: translate(-50%, -50%) scale(1.3) !important;
  z-index: 200 !important;
  box-shadow: 0 0 20px rgba(233, 30, 99, 0.6);
}

@keyframes float {
  0%, 100% { 
    transform: translate(-50%, -50%) translateY(0px); 
  }
  50% { 
    transform: translate(-50%, -50%) translateY(-10px); 
  }
}

.photo-wrapper {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  border: 4px solid #ff69b4;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.3),
              0 0 0 2px rgba(255, 255, 255, 0.8);
  background: white;
}

.photo-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-info {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  white-space: nowrap;
  backdrop-filter: blur(5px);
}

.nickname {
  color: #e91e63;
  font-weight: bold;
}

.age {
  color: #666;
  margin-left: 5px;
}

.detail-dialog {
  border-radius: 15px;
}

/* 修复弹窗遮罩层问题 */
.detail-dialog .el-dialog__header {
  background: transparent;
}

.detail-dialog .el-dialog {
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* 确保遮罩层正确显示 */
.el-overlay {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.detail-content {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.like-section {
  text-align: center;
}

.detail-body {
  margin-bottom: 20px;
}

.photos-gallery {
  margin-bottom: 30px;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.photos-grid img {
  width: 100%;
  height: 100px;
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

.detail-footer {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.my-posts-list {
  max-height: 500px;
  overflow-y: auto;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.post-card {
  background: white;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-card img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 10px;
}

.card-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.card-info p {
  margin: 3px 0;
  font-size: 14px;
  color: #666;
}

.card-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .top-nav {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .filter-bar {
    flex-wrap: wrap;
  }
  
  .heart-container {
    height: 400px;
  }
  
  .photo-item {
    width: 60px;
    height: 60px;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style> 