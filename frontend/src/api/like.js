import request from '@/utils/request'

export function checkLiked(params) {
  return request.get('/likes/check', { params })
}

export function toggleLike(params) {
  return request.post('/likes/toggle', null, { params })
}
