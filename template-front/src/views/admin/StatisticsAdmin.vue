<template>
  <div class="statistics-admin">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          访问统计
        </h1>
        <p class="page-description">基于HyperLogLog算法的高效UV/PV统计分析</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </div>
    
    <div class="statistics-content">
      <VisitStatistics ref="visitStatisticsRef" />
    </div>
    
    <!-- 详细数据表格 -->
    <el-card class="data-table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>详细数据</span>
          <div class="header-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="onDateRangeChange"
              size="small"
            />
            <el-button type="primary" size="small" @click="exportData" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="tableData" 
        v-loading="tableLoading"
        stripe
        border
        style="width: 100%"
        :default-sort="{ prop: 'statisticsDate', order: 'descending' }"
      >
        <el-table-column prop="statisticsDate" label="日期" width="120" sortable>
          <template #default="{ row }">
            {{ formatDate(row.statisticsDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="pageViews" label="页面访问量(PV)" width="150" sortable>
          <template #default="{ row }">
            <el-tag type="primary" size="small">
              {{ formatNumber(row.pageViews) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="uniqueVisitors" label="独立访客数(UV)" width="150" sortable>
          <template #default="{ row }">
            <el-tag type="success" size="small">
              {{ formatNumber(row.uniqueVisitors) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="UV/PV比率" width="120">
          <template #default="{ row }">
            <span class="ratio-text">
              {{ calculateRatio(row.uniqueVisitors, row.pageViews) }}%
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="PV增长" width="120">
          <template #default="{ row, $index }">
            <span 
              v-if="$index < tableData.length - 1" 
              :class="getGrowthClass(row.pageViews, tableData[$index + 1].pageViews)"
            >
              {{ formatGrowthRate(calculateGrowthRate(row.pageViews, tableData[$index + 1].pageViews)) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="UV增长" width="120">
          <template #default="{ row, $index }">
            <span 
              v-if="$index < tableData.length - 1" 
              :class="getGrowthClass(row.uniqueVisitors, tableData[$index + 1].uniqueVisitors)"
            >
              {{ formatGrowthRate(calculateGrowthRate(row.uniqueVisitors, tableData[$index + 1].uniqueVisitors)) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="数据来源" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="isToday(row.statisticsDate) ? 'warning' : 'info'" 
              size="small"
            >
              {{ isToday(row.statisticsDate) ? '实时' : '历史' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <span class="remark-text">
              {{ getDateRemark(row.statisticsDate) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="table-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onPageSizeChange"
          @current-change="onCurrentPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { DataAnalysis, Refresh, Download } from '@element-plus/icons-vue';
import VisitStatistics from '@/components/VisitStatistics.vue';
import { 
  getStatisticsByDateRange,
  formatNumber, 
  formatDate, 
  formatGrowthRate,
  calculateGrowthRate 
} from '@/net/statistics.js';

// 响应式数据
const visitStatisticsRef = ref(null);
const loading = ref(false);
const tableLoading = ref(false);
const exporting = ref(false);
const dateRange = ref([]);
const tableData = ref([]);
const currentPage = ref(1);
const pageSize = ref(20);
const totalCount = ref(0);

// 初始化日期范围（最近30天）
const initDateRange = () => {
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - 29);
  
  dateRange.value = [
    formatDate(start),
    formatDate(end)
  ];
};

// 加载表格数据
const loadTableData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return;
  
  tableLoading.value = true;
  try {
    const response = await getStatisticsByDateRange(dateRange.value[0], dateRange.value[1]);
    if (response.success) {
      tableData.value = response.data || [];
      totalCount.value = tableData.value.length;
    } else {
      ElMessage.error('获取详细数据失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载表格数据失败:', error);
    ElMessage.error('获取详细数据失败');
  } finally {
    tableLoading.value = false;
  }
};

// 刷新数据
const refreshData = async () => {
  loading.value = true;
  try {
    // 刷新统计组件数据
    if (visitStatisticsRef.value) {
      await visitStatisticsRef.value.loadStatistics();
    }
    // 刷新表格数据
    await loadTableData();
    ElMessage.success('数据刷新成功');
  } catch (error) {
    console.error('刷新数据失败:', error);
    ElMessage.error('数据刷新失败');
  } finally {
    loading.value = false;
  }
};

// 日期范围变化
const onDateRangeChange = () => {
  loadTableData();
};

// 分页变化
const onPageSizeChange = () => {
  currentPage.value = 1;
};

const onCurrentPageChange = () => {
  // 这里可以实现服务端分页
};

// 导出数据
const exportData = async () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('没有数据可导出');
    return;
  }
  
  try {
    await ElMessageBox.confirm('确定要导出当前数据吗？', '导出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });
    
    exporting.value = true;
    
    // 构建CSV数据
    const headers = ['日期', '页面访问量(PV)', '独立访客数(UV)', 'UV/PV比率(%)', '数据来源', '备注'];
    const csvContent = [
      headers.join(','),
      ...tableData.value.map(row => [
        row.statisticsDate,
        row.pageViews,
        row.uniqueVisitors,
        calculateRatio(row.uniqueVisitors, row.pageViews),
        isToday(row.statisticsDate) ? '实时' : '历史',
        getDateRemark(row.statisticsDate)
      ].join(','))
    ].join('\n');
    
    // 下载文件
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `访问统计_${dateRange.value[0]}_${dateRange.value[1]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出数据失败:', error);
      ElMessage.error('数据导出失败');
    }
  } finally {
    exporting.value = false;
  }
};

// 工具函数
const calculateRatio = (uv, pv) => {
  if (pv === 0) return '0.0';
  return ((uv / pv) * 100).toFixed(1);
};

const getGrowthClass = (current, previous) => {
  const growth = calculateGrowthRate(current, previous);
  return {
    'growth-positive': growth > 0,
    'growth-negative': growth < 0,
    'growth-neutral': growth === 0
  };
};

const isToday = (date) => {
  const today = new Date();
  const targetDate = new Date(date);
  return today.toDateString() === targetDate.toDateString();
};

const getDateRemark = (date) => {
  const today = new Date();
  const targetDate = new Date(date);
  const diffTime = today - targetDate;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) return '今天';
  if (diffDays === 1) return '昨天';
  if (diffDays <= 7) return `${diffDays}天前`;
  if (diffDays <= 30) return `${Math.ceil(diffDays / 7)}周前`;
  return `${Math.ceil(diffDays / 30)}月前`;
};

// 组件挂载
onMounted(() => {
  initDateRange();
  loadTableData();
});
</script>

<style scoped>
.statistics-admin {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-description {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.statistics-content {
  margin-bottom: 20px;
}

.data-table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.header-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ratio-text {
  font-weight: 500;
  color: #606266;
}

.growth-positive {
  color: #67C23A;
  font-weight: 500;
}

.growth-negative {
  color: #F56C6C;
  font-weight: 500;
}

.growth-neutral {
  color: #909399;
}

.text-muted {
  color: #C0C4CC;
}

.remark-text {
  font-size: 12px;
  color: #909399;
}

.table-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-admin {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: flex-end;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-controls {
    justify-content: space-between;
  }
  
  .el-table {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .header-controls {
    flex-direction: column;
    gap: 8px;
  }
}
</style>