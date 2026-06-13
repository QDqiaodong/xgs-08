import request from '@/utils/request'

export function getUserById(id) {
  return request.get(`/users/${id}`)
}
