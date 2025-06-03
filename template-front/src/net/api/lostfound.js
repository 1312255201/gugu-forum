import { get, post } from "@/net";

/**
 * 创建失物招领信息
 * @param data 失物招领数据
 * @param success 成功回调
 * @param failure 失败回调
 */
export function createLostFound(data, success, failure) {
    post('/api/lost-found/create', data, success, failure)
}

/**
 * 获取失物招领列表
 * @param params 查询参数 {location, startTime, endTime, status}
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLostFoundList(params, success, failure) {
    const query = new URLSearchParams()
    if (params.location) query.append('location', params.location)
    if (params.startTime) query.append('startTime', params.startTime)
    if (params.endTime) query.append('endTime', params.endTime)
    if (params.status !== undefined && params.status !== null) query.append('status', params.status)
    
    const url = '/api/lost-found/list' + (query.toString() ? '?' + query.toString() : '')
    get(url, success, failure)
}

/**
 * 获取失物招领详情
 * @param id 失物招领ID
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLostFoundById(id, success, failure) {
    get(`/api/lost-found/${id}`, success, failure)
}

/**
 * 更新失物招领状态
 * @param id 失物招领ID
 * @param status 新状态
 * @param success 成功回调
 * @param failure 失败回调
 */
export function updateLostFoundStatus(id, status, success, failure) {
    post(`/api/lost-found/${id}/status`, { status }, success, failure)
}

/**
 * 删除失物招领
 * @param id 失物招领ID
 * @param success 成功回调
 * @param failure 失败回调
 */
export function deleteLostFound(id, success, failure) {
    post(`/api/lost-found/${id}/delete`, {}, success, failure)
} 