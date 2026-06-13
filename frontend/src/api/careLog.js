import request from '@/utils/request'

export function getUserCareLogs(userId, params) {
  return request.get(`/care-logs/user/${userId}`, { params })
}

export function getPostCareLogs(postId) {
  return request.get(`/care-logs/post/${postId}`)
}

export function createCareLog(data) {
  return request.post('/care-logs', data)
}
