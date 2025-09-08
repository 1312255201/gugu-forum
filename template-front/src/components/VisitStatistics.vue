<template>
  <div class="visit-statistics">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stat-card">
            <div class="stat-icon today">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(summary.todayPv) }}</div>
              <div class="stat-label">今日PV</div>
              <div class="stat-growth" :class="{ positive: todayPvGrowth >= 0, negative: todayPvGrowth < 0 }">
                <el-icon><ArrowUp v-if="todayPvGrowth >= 0" /><ArrowDown v-else /></el-icon>
                {{ formatGrowthRate(todayPvGrowth) }}
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stat-card">
            <div class="stat-icon today">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(summary.todayUv) }}</div>
              <div class="stat-label">今日UV</div>
              <div class="stat-growth" :class="{ positive: todayUvGrowth >= 0, negative: todayUvGrowth < 0 }">
                <el-icon><ArrowUp v-if="todayUvGrowth >= 0" /><ArrowDown v-else /></el-icon>
                {{ formatGrowthRate(todayUvGrowth) }}
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stat-card">
            <div class="stat-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(summary.weekPv) }}</div>
              <div class="stat-label">本周PV</div>
              <div class="stat-sub">UV: {{ formatNumber(summary.weekUv) }}</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stat-card">
            <div class="stat-icon month">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(summary.monthPv) }}</div>
              <div class="stat-label">本月PV</div>
              <div class="stat-sub">UV: {{ formatNumber(summary.monthUv) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-container">
      <el-row :gutter="20">
        <!-- 趋势图表 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>访问趋势</span>
                <el-radio-group v-model="chartPeriod" size="small" @change="onChartPeriodChange">
                  <el-radio-button label="7">最近7天</el-radio-button>
                  <el-radio-button label="30">最近30天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChart" class="trend-chart"></div>
          </el-card>
        </el-col>
        
        <!-- 统计对比 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="chart-card">
            <template #header>
              <span>数据对比</span>
            </template>
            <div class="comparison-list">
              <div class="comparison-item">
                <div class="comparison-label">今日 vs 昨日</div>
                <div class="comparison-values">
                  <div class="comparison-pv">
                    <span class="label">PV:</span>
                    <span class="value">{{ formatNumber(summary.todayPv) }}</span>
                    <span class="vs">vs</span>
                    <span class="value">{{ formatNumber(summary.yesterdayPv) }}</span>
                  </div>
                  <div class="comparison-uv">
                    <span class="label">UV:</span>
                    <span class="value">{{ formatNumber(summary.todayUv) }}</span>
                    <span class="vs">vs</span>
                    <span class="value">{{ formatNumber(summary.yesterdayUv) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="comparison-item">
                <div class="comparison-label">本周 vs 上周</div>
                <div class="comparison-values">
                  <div class="comparison-pv">
                    <span class="label">PV:</span>
                    <span class="value">{{ formatNumber(summary.weekPv) }}</span>
                  </div>
                  <div class="comparison-uv">
                    <span class="label">UV:</span>
                    <span class="value">{{ formatNumber(summary.weekUv) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="comparison-item">
                <div class="comparison-label">本月统计</div>
                <div class="comparison-values">
                  <div class="comparison-pv">
                    <span class="label">PV:</span>
                    <span class="value">{{ formatNumber(summary.monthPv) }}</span>
                  </div>
                  <div class="comparison-uv">
                    <span class="label">UV:</span>
                    <span class="value">{{ formatNumber(summary.monthUv) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { View, User, Calendar, DataAnalysis, ArrowUp, ArrowDown, TrendCharts } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { 
  getStatisticsSummary, 
  getRecentStatistics, 
  recordVisit,
  formatNumber, 
  formatGrowthRate, 
  formatMonthDay,
  calculateGrowthRate 
} from '@/net/statistics.js';

// 响应式数据
const summary = reactive({
  todayPv: 0,
  todayUv: 0,
  yesterdayPv: 0,
  yesterdayUv: 0,
  weekPv: 0,
  weekUv: 0,
  monthPv: 0,
  monthUv: 0,
  recent7Days: [],
  recent30Days: []
});

const chartPeriod = ref('7');
const trendChart = ref(null);
let chartInstance = null;
const loading = ref(false);
const chartData = ref([]);
const chartContainer = ref(null);

// 计算增长率
const todayPvGrowth = computed(() => {
  return calculateGrowthRate(summary.todayPv, summary.yesterdayPv);
});

const todayUvGrowth = computed(() => {
  return calculateGrowthRate(summary.todayUv, summary.yesterdayUv);
});

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true;
  try {
    const response = await getStatisticsSummary();
    if (response.success) {
      // 确保所有数值字段都有默认值，避免null值导致的错误
      const data = response.data || {};
      Object.assign(summary, {
        todayPv: data.todayPv || 0,
        todayUv: data.todayUv || 0,
        yesterdayPv: data.yesterdayPv || 0,
        yesterdayUv: data.yesterdayUv || 0,
        weekPv: data.weekPv || 0,
        weekUv: data.weekUv || 0,
        monthPv: data.monthPv || 0,
        monthUv: data.monthUv || 0,
        recent7Days: data.recent7Days || [],
        recent30Days: data.recent30Days || []
      });
    } else {
      ElMessage.error('获取统计数据失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 加载图表数据
const loadChartData = async () => {
  try {
    const response = await getRecentStatistics(parseInt(chartPeriod.value));
    if (response.success) {
      chartData.value = response.data || [];
      updateChart();
    } else {
      ElMessage.error('加载图表数据失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载图表数据失败:', error);
    ElMessage.error('加载图表数据失败');
  }
};

// 初始化图表
const initChart = () => {
  if (!trendChart.value) return;
  
  chartInstance = echarts.init(trendChart.value);
  updateChart();
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstance?.resize();
  });
};

// 更新图表
const updateChart = () => {
  if (!chartInstance) return;
  
  const data = chartPeriod.value === '7' ? summary.recent7Days : summary.recent30Days;
  
  const dates = data.map(item => formatMonthDay(item.statisticsDate));
  const pvData = data.map(item => item.pageViews);
  const uvData = data.map(item => item.uniqueVisitors);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function(params) {
        let result = params[0].name + '<br/>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + formatNumber(param.value) + '<br/>';
        });
        return result;
      }
    },
    legend: {
      data: ['页面访问量(PV)', '独立访客数(UV)'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: function(value) {
          return formatNumber(value);
        },
        fontSize: 12
      }
    },
    series: [
      {
        name: '页面访问量(PV)',
        type: 'line',
        stack: false,
        smooth: true,
        lineStyle: {
          width: 3
        },
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(64, 158, 255, 0.3)'
            }, {
              offset: 1, color: 'rgba(64, 158, 255, 0.05)'
            }]
          }
        },
        data: pvData
      },
      {
        name: '独立访客数(UV)',
        type: 'line',
        stack: false,
        smooth: true,
        lineStyle: {
          width: 3
        },
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(103, 194, 58, 0.3)'
            }, {
              offset: 1, color: 'rgba(103, 194, 58, 0.05)'
            }]
          }
        },
        data: uvData
      }
    ]
  };
  
  chartInstance.setOption(option);
};

// 图表周期变化
const onChartPeriodChange = () => {
  loadChartData();
};

// 切换图表周期
const switchChartPeriod = (period) => {
  chartPeriod.value = period.toString();
  loadChartData();
};

// 响应式调整图表大小
const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

// 组件挂载
onMounted(async () => {
  // 记录页面访问
  try {
    await recordVisit();
  } catch (error) {
    console.error('记录页面访问失败:', error);
  }
  
  await loadStatistics();
  await loadChartData();
  await nextTick();
  initChart();
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeChart);
});

// 组件卸载时清理
const cleanup = () => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
  window.removeEventListener('resize', resizeChart);
};

// 暴露方法供父组件调用
defineExpose({
  loadStatistics
});
</script>

<style scoped>
.visit-statistics {
  padding: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  height: 100px;
}

.stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: #fff;
}

.stat-icon.today {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.stat-icon.week {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.stat-icon.month {
  background: linear-gradient(135deg, #E6A23C, #EEBE77);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-growth {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-growth.positive {
  color: #67C23A;
}

.stat-growth.negative {
  color: #F56C6C;
}

.stat-sub {
  font-size: 12px;
  color: #909399;
}

.charts-container {
  margin-top: 20px;
}

.chart-card {
  height: 400px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-chart {
  width: 100%;
  height: 320px;
}

.comparison-list {
  height: 320px;
  overflow-y: auto;
}

.comparison-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
}

.comparison-item:last-child {
  border-bottom: none;
}

.comparison-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
}

.comparison-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comparison-pv,
.comparison-uv {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.comparison-pv .label,
.comparison-uv .label {
  color: #909399;
  min-width: 25px;
}

.comparison-pv .value,
.comparison-uv .value {
  color: #303133;
  font-weight: 500;
}

.comparison-pv .vs,
.comparison-uv .vs {
  color: #C0C4CC;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .visit-statistics {
    padding: 10px;
  }
  
  .stat-card {
    padding: 15px;
    height: 80px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
    margin-right: 10px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .chart-card {
    height: 350px;
  }
  
  .trend-chart {
    height: 270px;
  }
  
  .comparison-list {
    height: 270px;
  }
}
</style>