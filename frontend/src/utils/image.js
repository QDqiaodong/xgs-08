export const PLACEHOLDER_SVG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"><rect width="400" height="300" fill="%23f0f2f5"/><g fill="%23c9cdd4" transform="translate(140, 90)"><path d="M60 0C26.863 0 0 26.863 0 60s26.863 60 60 60 60-26.863 60-60S93.137 0 60 0zm0 108c-26.51 0-48-21.49-48-48s21.49-48 48-48 48 21.49 48 48-21.49 48-48 48z"/><path d="M113.4 207.6c-6.8-34.2-36.6-60-73.4-60s-66.6 25.8-73.4 60h146.8z"/></g><text x="200" y="260" text-anchor="middle" fill="%23969799" font-size="14" font-family="sans-serif">暂无图片</text></svg>'

export const BONSAI_PLACEHOLDER_SVG = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 400"><rect width="400" height="400" fill="%23f0f2f5"/><g fill="%238b6914" transform="translate(180, 280)"><rect x="0" y="0" width="40" height="80" rx="4"/></g><g fill="%232d6a4f" transform="translate(100, 120)"><ellipse cx="100" cy="80" rx="90" ry="70"/><ellipse cx="60" cy="100" rx="50" ry="40"/><ellipse cx="140" cy="100" rx="50" ry="40"/></g><text x="200" y="360" text-anchor="middle" fill="%23969799" font-size="14" font-family="sans-serif">暂无封面</text></svg>'

export const parseImageUrl = (image) => {
  if (!image) return null
  if (typeof image === 'string') {
    return image.trim() || null
  }
  if (typeof image === 'object') {
    return image.thumbnailUrl || image.imageUrl || image.url || null
  }
  return null
}

export const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) {
    return images
      .map(img => parseImageUrl(img))
      .filter(url => url && url.length > 0)
  }
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) {
        return parsed
          .map(img => parseImageUrl(img))
          .filter(url => url && url.length > 0)
      }
      const singleUrl = parseImageUrl(images)
      return singleUrl ? [singleUrl] : []
    } catch {
      const singleUrl = parseImageUrl(images)
      return singleUrl ? [singleUrl] : []
    }
  }
  return []
}

export const getCoverImage = (item, options = {}) => {
  const { useBonsaiPlaceholder = false } = options
  
  if (item.coverImage) {
    const url = parseImageUrl(item.coverImage)
    if (url) return url
  }
  
  if (item.images) {
    const images = parseImages(item.images)
    if (images.length > 0) {
      if (typeof item.images === 'object' && Array.isArray(item.images)) {
        const cover = item.images.find(img => img && img.isCover === 1)
        if (cover) {
          const url = parseImageUrl(cover)
          if (url) return url
        }
      }
      return images[0]
    }
  }
  
  return useBonsaiPlaceholder ? BONSAI_PLACEHOLDER_SVG : PLACEHOLDER_SVG
}

export const getImageWithFallback = (url, useBonsaiPlaceholder = false) => {
  if (!url) {
    return useBonsaiPlaceholder ? BONSAI_PLACEHOLDER_SVG : PLACEHOLDER_SVG
  }
  return url
}
