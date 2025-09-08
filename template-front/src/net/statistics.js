import { get, post } from "@/net/index.js";

/**
 * 记录页面访问
 * @returns {Promise} 请求结果
 */
export function recordVisit() {
    return post('/api/statistics/visit', {});
}

/**
 * 获取统计汇总数据
 * 包含今日、昨日、本周、本月的PV/UV数据，以及最近7天和30天的趋势数据
 * @returns {Promise} 统计汇总数据
 */
export function getStatisticsSummary() {
    return get('/api/statistics/summary');
}

/**
 * 获取指定日期的统计数据
 * @param {string} date 日期，格式：yyyy-MM-dd
 * @returns {Promise} 指定日期的统计数据
 */
export function getStatisticsByDate(date) {
    return get('/api/statistics/date', {
        date: date
    });
}

/**
 * 获取日期范围内的统计数据
 * @param {string} startDate 开始日期，格式：yyyy-MM-dd
 * @param {string} endDate 结束日期，格式：yyyy-MM-dd
 * @returns {Promise} 日期范围内的统计数据列表
 */
export function getStatisticsByDateRange(startDate, endDate) {
    return get('/api/statistics/range', {
        startDate: startDate,
        endDate: endDate
    });
}

/**
 * 获取最近N天的统计数据
 * @param {number} days 天数，默认7天
 * @returns {Promise} 最近N天的统计数据列表
 */
export function getRecentStatistics(days = 7) {
    return get('/api/statistics/recent', {
        days: days
    });
}

/**
 * 格式化数字显示
 * @param {number} num 数字
 * @returns {string} 格式化后的字符串
 */
export function formatNumber(num) {
    if (num >= 10000) {
        return (num / 10000).toFixed(1) + 'w';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
}

/**
 * 格式化日期显示
 * @param {string|Date} date 日期
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * 格式化月日显示
 * @param {string|Date} date 日期
 * @returns {string} 格式化后的月日字符串
 */
export function formatMonthDay(date) {
    const d = new Date(date);
    const month = d.getMonth() + 1;
    const day = d.getDate();
    return `${month}/${day}`;
}

/**
 * 计算增长率
 * @param {number} current 当前值
 * @param {number} previous 之前值
 * @returns {number} 增长率百分比
 */
export function calculateGrowthRate(current, previous) {
    if (previous === 0) {
        return current > 0 ? 100 : 0;
    }
    return ((current - previous) / previous * 100);
}

/**
 * 格式化增长率显示
 * @param {number} rate 增长率
 * @returns {string} 格式化后的增长率字符串
 */
export function formatGrowthRate(rate) {
    const sign = rate >= 0 ? '+' : '';
    return `${sign}${rate.toFixed(1)}%`;
}