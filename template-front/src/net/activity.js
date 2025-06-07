import {get, post} from "@/net";

// 用户端活动API
export function getActivityList(success, failure) {
    get('/api/activity/list', success, failure)
}

export function getActivityById(id, success, failure) {
    get(`/api/activity/${id}`, success, failure)
}

export function getActivityByStatus(status, success, failure) {
    get(`/api/activity/list/${status}`, success, failure)
}

// 管理员活动API
export function createActivity(data, success, failure) {
    post('/api/admin/activity/create', data, success, failure)
}

export function getAdminActivityList(success, failure) {
    get('/api/admin/activity/list', success, failure)
}

export function getAdminActivityById(id, success, failure) {
    get(`/api/admin/activity/${id}`, success, failure)
}

export function updateActivity(id, data, success, failure) {
    post(`/api/admin/activity/${id}/update`, data, success, failure)
}

export function updateActivityStatus(id, status, success, failure) {
    post(`/api/admin/activity/${id}/status`, { status }, success, failure)
}

export function deleteActivity(id, success, failure) {
    post(`/api/admin/activity/${id}/delete`, {}, success, failure)
} 