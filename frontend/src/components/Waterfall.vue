<template>
  <div class="waterfall-container">
    <div class="waterfall-column" v-for="column in 2" :key="column">
      <div
        v-for="(post, index) in getColumnPosts(column - 1)"
        :key="post.id"
        class="waterfall-item card"
        @click="goDetail(post.id)"
      >
        <div class="post-cover cover-wrapper" :style="{ aspectRatio: getAspectRatio(post) }">
          <img
            :src="getPostCoverImage(post)"
            :alt="post.title"
            loading="lazy"
            @load="onImageLoad(post.id, $event)"
            @error="onImageError(post.id)"
          />
          <div v-if="post.isHot" class="hot-tag">
            <van-icon name="fire-o" size="10" />
            <span>热门</span>
          </div>
          <div v-if="post.species" class="species-tag">
            {{ post.species.name }}
          </div>
        </div>
        <div class="post-content">
          <h3 class="post-title text-ellipsis-2">{{ post.title }}</h3>
          <div v-if="post.style" class="post-style">
            <van-tag size="mini" type="primary" plain>{{ post.style.name }}</van-tag>
          </div>
          <div class="post-footer">
            <div class="post-meta">
              <span class="meta-item">
                <van-icon name="eye-o" size="12" />
                <span>{{ formatCount(post.viewCount) }}</span>
              </span>
              <span class="meta-item">
                <van-icon name="like-o" size="12" />
                <span>{{ formatCount(post.likeCount) }}</span>
              </span>
            </div>
            <div v-if="post.user" class="post-author">
              <div class="author-avatar">
                {{ post.user.nickname?.charAt(0) || '用' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="loading" class="loading-more">
      <van-loading size="20px">加载中...</van-loading>
    </div>
    <div v-if="!loading && posts.length > 0" class="load-trigger" ref="loadTrigger"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCoverImage } from '@/utils/image'

const props = defineProps({
  posts: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['load-more'])

const router = useRouter()
const loadTrigger = ref(null)
const observer = ref(null)
const imageHeights = ref({})
const imageErrorMap = ref({})

const getPostCoverImage = (post) => {
  if (imageErrorMap.value[post.id]) {
    return getCoverImage(post)
  }
  return getCoverImage(post)
}

const getAspectRatio = (post) => {
  if (imageHeights.value[post.id]) {
    return `auto`
  }
  return '4 / 3'
}

const getColumnPosts = (columnIndex) => {
  return props.posts.filter((_, index) => index % 2 === columnIndex)
}

const formatCount = (count) => {
  if (!count) return 0
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}

const onImageLoad = (postId, event) => {
  imageHeights.value[postId] = event.target.offsetHeight
}

const onImageError = (postId) => {
  imageErrorMap.value[postId] = true
}

const goDetail = (id) => {
  router.push(`/post/${id}`)
}

const setupObserver = () => {
  if (loadTrigger.value) {
    observer.value = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !props.loading) {
          emit('load-more')
        }
      },
      { threshold: 0.1 }
    )
    observer.value.observe(loadTrigger.value)
  }
}

onMounted(() => {
  setupObserver()
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})

watch(() => props.posts.length, () => {
  setTimeout(() => {
    if (observer.value && loadTrigger.value) {
      observer.value.disconnect()
      observer.value.observe(loadTrigger.value)
    }
  }, 100)
})
</script>

<style scoped>
.waterfall-container {
  display: flex;
  gap: var(--spacing-sm);
  padding: 0 var(--spacing-xs);
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.waterfall-item {
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.waterfall-item:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.post-cover {
  width: 100%;
  background: var(--color-bg-tertiary);
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hot-tag {
  position: absolute;
  top: var(--spacing-xs);
  left: var(--spacing-xs);
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: #fff;
  font-size: var(--font-size-xs);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  display: flex;
  align-items: center;
  gap: 2px;
  font-weight: var(--font-weight-medium);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.species-tag {
  position: absolute;
  top: var(--spacing-xs);
  right: var(--spacing-xs);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: var(--font-size-xs);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  backdrop-filter: blur(4px);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-content {
  padding: var(--spacing-sm) var(--spacing-sm) var(--spacing-xs);
}

.post-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  line-height: var(--line-height-sm);
  margin-bottom: var(--spacing-xs);
  min-height: 42px;
}

.post-style {
  margin-bottom: var(--spacing-xs);
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-xs);
  border-top: 1px solid var(--color-border-light);
}

.post-meta {
  display: flex;
  gap: var(--spacing-md);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.post-author {
  display: flex;
  align-items: center;
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, #d4edda 100%);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.loading-more {
  position: absolute;
  bottom: var(--spacing-lg);
  left: 50%;
  transform: translateX(-50%);
}

.load-trigger {
  height: 20px;
}
</style>
