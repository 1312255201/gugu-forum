import {get, post} from "@/net";

/**
 * 创建表白墙信息
 * @param data 表白墙数据
 * @param success 成功回调
 * @param failure 失败回调
 */
export function createLoveWall(data, success, failure) {
    post('/api/love-wall/create', data, success, failure)
}

/**
 * 获取表白墙列表
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLoveWallList(success, failure) {
    get('/api/love-wall/list', success, failure)
}

/**
 * 根据性别筛选表白墙列表
 * @param gender 性别
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLoveWallListByGender(gender, success, failure) {
    get(`/api/love-wall/list/gender/${gender}`, success, failure)
}

/**
 * 根据年龄范围筛选表白墙列表
 * @param minAge 最小年龄
 * @param maxAge 最大年龄
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLoveWallListByAgeRange(minAge, maxAge, success, failure) {
    get(`/api/love-wall/list/age?minAge=${minAge}&maxAge=${maxAge}`, success, failure)
}

/**
 * 获取表白墙详情
 * @param id 表白墙ID
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getLoveWallById(id, success, failure) {
    get(`/api/love-wall/${id}`, success, failure)
}

/**
 * 获取我发布的表白墙列表
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getMyLoveWallList(success, failure) {
    get('/api/love-wall/my', success, failure)
}

/**
 * 点赞表白墙
 * @param id 表白墙ID
 * @param success 成功回调
 * @param failure 失败回调
 */
export function likeLoveWall(id, success, failure) {
    post(`/api/love-wall/like/${id}`, {}, success, failure)
}

/**
 * 删除表白墙
 * @param id 表白墙ID
 * @param success 成功回调
 * @param failure 失败回调
 */
export function deleteLoveWall(id, success, failure) {
    post(`/api/love-wall/delete/${id}`, {}, success, failure)
}

/**
 * 更新表白墙信息
 * @param id 表白墙ID
 * @param data 更新数据
 * @param success 成功回调
 * @param failure 失败回调
 */
export function updateLoveWall(id, data, success, failure) {
    post(`/api/love-wall/update/${id}`, data, success, failure)
}

/**
 * 获取待审核的表白墙列表（管理员）
 * @param success 成功回调
 * @param failure 失败回调
 */
export function getPendingLoveWallList(success, failure) {
    get('/api/admin/love-wall/pending', success, failure)
}

/**
 * 审核表白墙（管理员）
 * @param id 表白墙ID
 * @param status 审核状态 1-通过 2-拒绝
 * @param success 成功回调
 * @param failure 失败回调
 */
export function approveLoveWall(id, status, success, failure) {
    post(`/api/admin/love-wall/approve/${id}?status=${status}`, {}, success, failure)
} 