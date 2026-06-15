<template>
  <div class="post-detail page-container">
    <van-nav-bar title="帖子详情" left-arrow @click-left="goBack" fixed placeholder />

    <div v-if="post" class="content-wrapper">
      <div class="post-main card">
        <div class="post-header">
          <h1 class="post-title">{{ post.title }}</h1>
          <div class="post-info">
            <span class="info-item">
              <van-icon name="clock-o" size="12" />
              {{ formatDate(post.createdAt) }}
            </span>
            <span class="info-item">
              <van-icon name="eye-o" size="12" />
              {{ formatCount(post.viewCount) }}
            </span>
          </div>
        </div>

        <div class="post-images">
          <img
            v-for="(image, index) in getPostImages()"
            :key="index"
            :src="image"
            :alt="post.title"
            class="post-image"
            @click="previewImage(index)"
            @error="onImageError(index)"
          />
        </div>

        <div v-if="hasPostMeta()" class="post-meta">
          <van-tag v-if="post.species" type="primary" plain size="small">{{ post.species.name }}</van-tag>
          <van-tag v-if="post.style" type="success" plain size="small">{{ post.style.name }}</van-tag>
          <van-tag v-if="post.treeAge" type="warning" plain size="small">{{ post.treeAge }}年树龄</van-tag>
          <van-tag v-if="post.potType" type="default" plain size="small">{{ post.potType }}</van-tag>
        </div>

        <div class="post-section" v-if="post.content">
          <h3 class="section-title">作品介绍</h3>
          <div class="section-content">{{ post.content }}</div>
        </div>

        <div class="post-section" v-if="post.shapingIdeas">
          <h3 class="section-title">造型思路</h3>
          <div class="section-content">{{ post.shapingIdeas }}</div>
        </div>

        <div class="post-section" v-if="post.carePoints">
          <h3 class="section-title">养护要点</h3>
          <div class="section-content">{{ post.carePoints }}</div>
        </div>
      </div>

      <div class="post-section-card card">
        <h3 class="section-title">养护日志</h3>
        <div class="care-logs">
          <div v-for="log in careLogs" :key="log.id" class="care-log-item">
            <div class="log-header">
              <van-tag :type="getLogTypeTag(log.logType)" size="small">{{ getLogTypeName(log.logType) }}</van-tag>
              <span class="log-date">{{ log.logDate }}</span>
            </div>
            <div class="log-title" v-if="log.title">{{ log.title }}</div>
            <div class="log-content" v-if="log.content">{{ log.content }}</div>
          </div>
          <div v-if="careLogs.length === 0" class="empty-state">
            <van-icon name="notes-o" class="empty-icon" />
            <span class="empty-text">暂无养护记录</span>
          </div>
        </div>
      </div>

      <div class="post-section-card card">
        <h3 class="section-title">评论 ({{ post.commentCount }})</h3>
        <div class="comments">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-user">
              <div class="comment-avatar">{{ comment.user?.nickname?.charAt(0) || '用' }}</div>
              <span class="username">{{ comment.user?.nickname || '盆景爱好者' }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
          <div v-if="comments.length === 0" class="empty-state">
            <van-icon name="comment-o" class="empty-icon" />
            <span class="empty-text">暂无评论，快来抢沙发</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-bar">
      <div class="action-item" @click="toggleLike">
        <van-icon :name="isLiked ? 'like' : 'like-o'" :color="isLiked ? '#ee0a24' : '#646566'" size="22" />
        <span>{{ formatCount(post?.likeCount || 0) }}</span>
      </div>
      <div class="action-item" @click="showCommentInput = true">
        <van-icon name="comment-o" size="22" />
        <span>评论</span>
      </div>
      <van-button type="primary" size="small" @click="showCommentInput = true">我要评论</van-button>
    </div>

    <van-popup v-model:show="showCommentInput" position="bottom" :style="{ height: '40%' }">
      <div class="comment-input-wrapper">
        <van-nav-bar title="发表评论" left-text="取消" @click-left="showCommentInput = false">
          <template #right>
            <van-button type="primary" size="small" @click="submitComment">发布</van-button>
          </template>
        </van-nav-bar>
        <van-field
          v-model="commentContent"
          type="textarea"
          placeholder="请输入评论内容..."
          :autosize="{ minHeight: 120 }"
          maxlength="500"
          show-word-limit
        />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import { getPostById } from '@/api/post'
import { getPostCareLogs } from '@/api/careLog'
import { getPostComments, createComment } from '@/api/comment'
import { checkLiked, toggleLike as toggleLikeApi } from '@/api/like'
import { useUserStore } from '@/stores/user'
import { parseImages, getImageWithFallback, PLACEHOLDER_SVG, parseImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const careLogs = ref([])
const comments = ref([])
const isLiked = ref(false)
const showCommentInput = ref(false)
const commentContent = ref('')
const imageErrors = ref({})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
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

const getLogTypeName = (type) => {
  const types = {
    water: '浇水',
    fertilize: '施肥',
    prune: '修剪',
    repot: '换盆',
    other: '其他'
  }
  return types[type] || type
}

const getLogTypeTag = (type) => {
  const types = {
    water: 'primary',
    fertilize: 'success',
    prune: 'warning',
    repot: 'danger',
    other: 'default'
  }
  return types[type] || 'default'
}

const hasPostMeta = () => {
  return post.value && (post.value.species || post.value.style || post.value.treeAge || post.value.potType)
}

const getPostImages = () => {
  if (!post.value || !post.value.images) return []
  const images = parseImages(post.value.images)
  return images.map((img, idx) => {
    if (imageErrors.value[idx]) {
      return PLACEHOLDER_SVG
    }
    const url = typeof img === 'string' ? img : parseImageUrl(img)
    return getImageWithFallback(url)
  })
}

const onImageError = (index) => {
  imageErrors.value[index] = true
}

const previewImage = (index) => {
  const images = getPostImages()
  showImagePreview({
    images: images,
    startPosition: index
  })
}

const loadPost = async () => {
  try {
    post.value = await getPostById(route.params.id)
  } catch (e) {
    showToast('加载失败')
  }
}

const loadCareLogs = async () => {
  try {
    careLogs.value = await getPostCareLogs(route.params.id)
  } catch (e) {
    console.error('加载养护日志失败', e)
  }
}

const loadComments = async () => {
  try {
    const data = await getPostComments(route.params.id, { page: 0, size: 20 })
    comments.value = data.content || []
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const checkLikeStatus = async () => {
  try {
    isLiked.value = await checkLiked({
      userId: userStore.currentUser.id,
      targetType: 'post',
      targetId: route.params.id
    })
  } catch (e) {
    console.error('检查点赞状态失败', e)
  }
}

const toggleLike = async () => {
  try {
    await toggleLikeApi({
      userId: userStore.currentUser.id,
      targetType: 'post',
      targetId: route.params.id
    })
    isLiked.value = !isLiked.value
    if (post.value) {
      post.value.likeCount += isLiked.value ? 1 : -1
    }
  } catch (e) {
    showToast('操作失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  try {
    await createComment({
      postId: route.params.id,
      userId: userStore.currentUser.id,
      content: commentContent.value
    })
    showToast('评论成功')
    showCommentInput.value = false
    commentContent.value = ''
    loadComments()
    post.value.commentCount++
  } catch (e) {
    showToast('评论失败')
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadPost()
  loadCareLogs()
  loadComments()
  checkLikeStatus()
})
</script>

<style scoped>
.post-detail {
  padding-bottom: 70px;
}

.content-wrapper {
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.post-main {
  padding: var(--spacing-lg);
}

.post-section-card {
  padding: var(--spacing-lg);
}

.post-header {
  margin-bottom: var(--spacing-md);
}

.post-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-sm);
  margin-bottom: var(--spacing-sm);
}

.post-info {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  display: flex;
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.post-images {
  margin-bottom: var(--spacing-md);
}

.post-image {
  width: 100%;
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.post-image:active {
  transform: scale(0.98);
}

.post-image:last-child {
  margin-bottom: 0;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.post-section {
  margin-bottom: var(--spacing-lg);
}

.post-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.section-content {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: var(--line-height-xl);
}

.care-logs {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.care-log-item {
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.log-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.log-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.log-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
}

.comments {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.comment-item {
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border-light);
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.username {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-left: auto;
}

.comment-content {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: var(--line-height-lg);
  padding-left: 40px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-bg-card);
  padding: var(--spacing-sm) var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.action-item:active {
  opacity: 0.7;
}

.action-item span {
  font-size: var(--font-size-xs);
}

.bottom-bar .van-button {
  margin-left: auto;
}

.comment-input-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.comment-input-wrapper .van-field {
  flex: 1;
}
</style>
