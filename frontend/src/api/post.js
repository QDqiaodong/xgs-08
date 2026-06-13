import request from '@/utils/request'

export function getHotPosts() {
  return request.get('/posts/hot')
}

export function getPosts(params) {
  return request.get('/posts', { params })
}

export function getPostById(id) {
  return request.get(`/posts/${id}`)
}

export function createPost(data, images) {
  const formData = new FormData()
  Object.keys(data).forEach(key => {
    if (data[key] !== undefined && data[key] !== null) {
      formData.append(key, data[key])
    }
  })
  if (images && images.length > 0) {
    images.forEach((file, index) => {
      formData.append('images', file)
    })
  }
  return request.post('/posts', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getUserPosts(userId, params) {
  return request.get(`/posts/user/${userId}`, { params })
}
