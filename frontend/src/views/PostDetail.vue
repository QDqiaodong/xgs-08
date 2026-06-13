<template>
  <div class="post-detail">
    <van-nav-bar title="盆景详情" left-arrow @click-left="goBack" fixed placeholder />

    <div v-if="post" class="content">
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-info">
          <span>{{ formatDate(post.createdAt) }}</span>
          <span>浏览 {{ post.viewCount }}</span>
        </div>
      </div>

      <div class="post-images">
        <van-image
          v-for="image in post.images"
          :key="image.id"
          :src="image.imageUrl"
          :preview-src-list="post.images.map(i => i.imageUrl)"
          fit="cover"
          class="post-image"
        />
      </div>

      <div class="post-meta">
        <div class="meta-item" v-if="post.species">
          <span class="label">树种:</span>
          <span class="value">{{ post.species.name }}</span>
        </div>
        <div class="meta-item" v-if="post.style">
          <span class="label">风格:</span>
          <span class="value">{{ post.style.name }}</span>
        </div>
        <div class="meta-item" v-if="post.treeAge">
          <span class="label">树龄:</span>
          <span class="value">{{ post.treeAge }} 年</span>
        </div>
        <div class="meta-item" v-if="post.potType">
          <span class="label">盆器:</span>
          <span class="value">{{ post.potType }}</span>
        </div>
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

      <div class="post-section">
        <h3 class="section-title">养护日志</h3>
        <div class="care-logs">
          <div v-for="log in careLogs" :key="log.id" class="care-log-item">
            <div class="log-header">
              <van-tag :type="getLogTypeTag(log.logType)">{{ getLogTypeName(log.logType) }}</van-tag>
              <span class="log-date">{{ log.logDate }}</span>
            </div>
            <div class="log-title" v-if="log.title">{{ log.title }}</div>
            <div class="log-content" v-if="log.content">{{ log.content }}</div>
          </div>
          <van-empty v-if="careLogs.length === 0" description="暂无养护记录" />
        </div>
      </div>

      <div class="post-section">
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
          <van-empty v-if="comments.length === 0" description="暂无评论" />
        </div>
      </div>
    </div>

    <div class="bottom-bar">
      <div class="action-item" @click="toggleLike">
        <van-icon :name="isLiked ? 'like' : 'like-o'" :color="isLiked ? '#ee0a24' : '#646566'" size="22" />
        <span>{{ post?.likeCount || 0 }}</span>
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
import { showToast } from 'vant'
import { getPostById } from '@/api/post'
import { getPostCareLogs } from '@/api/careLog'
import { getPostComments, createComment } from '@/api/comment'
import { checkLiked, toggleLike as toggleLikeApi } from '@/api/like'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const careLogs = ref([])
const comments = ref([])
const isLiked = ref(false)
const showCommentInput = ref(false)
const commentContent = ref('')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
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
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.content {
  background: #fff;
  margin: 10px;
  border-radius: 8px;
  padding: 16px;
}

.post-header {
  margin-bottom: 16px;
}

.post-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
}

.post-info {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 16px;
}

.post-images {
  margin-bottom: 16px;
}

.post-image {
  width: 100%;
  margin-bottom: 8px;
  border-radius: 8px;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.meta-item {
  font-size: 14px;
}

.meta-item .label {
  color: #999;
  margin-right: 4px;
}

.meta-item .value {
  color: #333;
  font-weight: 500;
}

.post-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #07c160;
}

.section-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

.care-logs {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.care-log-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-date {
  font-size: 12px;
  color: #999;
}

.log-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.log-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.comments {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebedf0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e8f3ea;
  color: #2d6a4f;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.comment-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #646566;
  cursor: pointer;
}

.action-item span {
  font-size: 11px;
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
