import request from '@/utils/request'
import { showToast } from 'vant'

const ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
const MAX_FILE_SIZE = 10 * 1024 * 1024

export function validateImageFile(file) {
  if (!file) {
    showToast('文件不能为空')
    return false
  }

  const name = file.name
  if (!name || name.trim() === '') {
    showToast('文件名不能为空')
    return false
  }

  if (name.includes('..') || name.includes('/') || name.includes('\\')) {
    showToast('文件名包含非法字符')
    return false
  }

  const lastDotIndex = name.lastIndexOf('.')
  if (lastDotIndex === -1 || lastDotIndex === name.length - 1) {
    showToast('文件缺少合法扩展名')
    return false
  }

  const extension = name.substring(lastDotIndex + 1).toLowerCase()
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    showToast('不支持的文件扩展名，仅支持 ' + ALLOWED_EXTENSIONS.join(', '))
    return false
  }

  if (file.type && !ALLOWED_MIME_TYPES.includes(file.type.toLowerCase())) {
    showToast('不支持的文件类型: ' + file.type)
    return false
  }

  if (file.size > MAX_FILE_SIZE) {
    showToast('文件大小超过限制，最大支持 10MB')
    return false
  }

  return true
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export async function uploadSingleImage(fileItem) {
  if (!fileItem || !fileItem.file) {
    throw new Error('文件不存在')
  }
  const res = await uploadImage(fileItem.file)
  const urls = res.data
  if (urls && urls.length >= 1) {
    fileItem.uploadedUrl = urls[0]
    fileItem.uploadedThumbnailUrl = urls[1] || urls[0]
  }
  return urls
}
