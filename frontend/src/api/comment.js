import request from '@/utils/request'

export function getPostComments(postId, params) {
  return request.get(`/comments/post/${postId}`, { params })
}

export function createComment(data) {
  return request.post('/comments', data)
}
