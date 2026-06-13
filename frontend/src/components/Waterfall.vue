<template>
  <div class="waterfall-container">
    <div class="waterfall-column" v-for="column in 2" :key="column">
      <div
        v-for="(post, index) in getColumnPosts(column - 1)"
        :key="post.id"
        class="waterfall-item"
        @click="goDetail(post.id)"
      >
        <div class="post-image">
          <img
            :src="getCoverImage(post)"
            :alt="post.title"
            loading="lazy"
            @load="onImageLoad(post.id, $event)"
          />
          <div v-if="post.isHot" class="hot-tag">热门</div>
        </div>
        <div class="post-info">
          <h3 class="post-title">{{ post.title }}</h3>
          <div class="post-meta">
            <span class="view-count">
              <van-icon name="eye-o" /> {{ post.viewCount }}
            </span>
            <span class="like-count">
              <van-icon name="like-o" /> {{ post.likeCount }}
            </span>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

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

const getCoverImage = (post) => {
  if (post.images && post.images.length > 0) {
    const cover = post.images.find(img => img.isCover === 1) || post.images[0]
    return cover.thumbnailUrl || cover.imageUrl || 'https://picsum.photos/400/300?random=' + post.id
  }
  return 'https://picsum.photos/400/300?random=' + post.id
}

const getColumnPosts = (columnIndex) => {
  return props.posts.filter((_, index) => index % 2 === columnIndex)
}

const onImageLoad = (postId, event) => {
  imageHeights.value[postId] = event.target.offsetHeight
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
  gap: 8px;
  padding: 0 4px;
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.waterfall-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.2s;
}

.waterfall-item:active {
  transform: scale(0.98);
}

.post-image {
  position: relative;
  width: 100%;
  background: #f0f0f0;
}

.post-image img {
  width: 100%;
  display: block;
}

.hot-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(90deg, #ff6b6b, #ee5a24);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.post-info {
  padding: 10px;
}

.post-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.loading-more {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
}

.load-trigger {
  height: 20px;
}
</style>
