<template>
  <div class="profile-page page-container">
    <div class="profile-header">
      <div class="user-info">
        <div class="user-avatar">{{ user.nickname?.charAt(0) || '用' }}</div>
        <div class="user-detail">
          <h2 class="nickname">{{ user.nickname || user.username }}</h2>
          <p class="bio" v-if="user.bio">{{ user.bio }}</p>
        </div>
      </div>
      <div class="user-stats">
        <div class="stat-item">
          <div class="stat-value">{{ postCount }}</div>
          <div class="stat-label">作品</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ likeCount }}</div>
          <div class="stat-label">获赞</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ logCount }}</div>
          <div class="stat-label">养护记录</div>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="content card">
        <van-tabs v-model:active="activeTab" sticky>
          <van-tab title="我的作品">
            <div class="posts-grid">
              <div
                v-for="(post, index) in myPosts"
                :key="post.id"
                class="post-item"
                @click="goPostDetail(post.id)"
              >
                <div class="post-cover cover-wrapper">
                  <img 
                    :src="getPostCover(post, index)" 
                    :alt="post.title"
                    @error="onCoverError(index)"
                  />
                  <div class="post-stats">
                    <van-icon name="eye-o" size="10" /> {{ formatCount(post.viewCount) }}
                  </div>
                </div>
                <div class="post-title">{{ post.title }}</div>
              </div>
            </div>
            <div v-if="myPosts.length === 0" class="empty-state">
              <van-icon name="photo-o" class="empty-icon" />
              <span class="empty-text">暂无作品</span>
            </div>
          </van-tab>
          <van-tab title="养护记录">
            <div class="logs-list">
              <div v-for="log in myLogs" :key="log.id" class="log-item card">
                <div class="log-top">
                  <van-tag :type="getLogTypeTag(log.logType)" size="small">{{ getLogTypeName(log.logType) }}</van-tag>
                  <span class="log-date">{{ log.logDate }}</span>
                </div>
                <div class="log-content" v-if="log.content">{{ log.content }}</div>
              </div>
              <div v-if="myLogs.length === 0" class="empty-state">
                <van-icon name="notes-o" class="empty-icon" />
                <span class="empty-text">暂无养护记录</span>
              </div>
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </div>

    <van-tabbar v-model:active="activeFooter" route fixed placeholder>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/bonsais" icon="flower-o">盆景</van-tabbar-item>
      <van-tabbar-item to="/publish" icon="plus">发布</van-tabbar-item>
      <van-tabbar-item to="/care-logs" icon="notes-o">养护</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserPosts } from '@/api/post'
import { getUserCareLogs } from '@/api/careLog'
import { useUserStore } from '@/stores/user'
import { getCoverImage, PLACEHOLDER_SVG } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()

const user = ref(userStore.currentUser)
const activeTab = ref(0)
const activeFooter = ref(4)
const myPosts = ref([])
const myLogs = ref([])
const postCount = ref(0)
const likeCount = ref(0)
const logCount = ref(0)
const coverErrors = ref({})

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

const getPostCover = (post, index) => {
  if (coverErrors.value[index]) {
    return PLACEHOLDER_SVG
  }
  return getCoverImage(post)
}

const onCoverError = (index) => {
  coverErrors.value[index] = true
}

const goPostDetail = (id) => {
  router.push(`/post/${id}`)
}

const loadMyPosts = async () => {
  try {
    const data = await getUserPosts(user.value.id, { page: 0, size: 20 })
    myPosts.value = data.content || []
    postCount.value = data.totalElements || myPosts.value.length
    likeCount.value = myPosts.value.reduce((sum, post) => sum + (post.likeCount || 0), 0)
  } catch (e) {
    showToast('加载作品失败')
  }
}

const loadMyLogs = async () => {
  try {
    const data = await getUserCareLogs(user.value.id, { page: 0, size: 20 })
    myLogs.value = data.content || []
    logCount.value = data.totalElements || myLogs.value.length
  } catch (e) {
    showToast('加载记录失败')
  }
}

onMounted(() => {
  loadMyPosts()
  loadMyLogs()
})
</script>

<style scoped>
.profile-page {
  padding-bottom: 60px;
}

.profile-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, #05944a 100%);
  padding: var(--spacing-3xl) var(--spacing-lg) var(--spacing-xl);
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
}

.nickname {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-xs);
}

.bio {
  font-size: var(--font-size-sm);
  opacity: 0.9;
  line-height: var(--line-height-base);
}

.user-stats {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  backdrop-filter: blur(10px);
}

.user-stats .stat-item {
  text-align: center;
}

.user-stats .stat-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-xs);
}

.user-stats .stat-label {
  font-size: var(--font-size-xs);
  opacity: 0.9;
}

.content-wrapper {
  padding: var(--spacing-md);
  margin-top: calc(-1 * var(--spacing-md));
  position: relative;
  z-index: 1;
}

.content {
  overflow: hidden;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2px;
  padding: 2px;
}

.post-item {
  background: var(--color-bg-tertiary);
  aspect-ratio: 1;
  position: relative;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.post-item:active {
  transform: scale(0.98);
}

.post-cover {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-stats {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: var(--font-size-xs);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  display: flex;
  align-items: center;
  gap: 2px;
  backdrop-filter: blur(4px);
}

.post-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
  font-size: var(--font-size-xs);
  padding: 20px 8px 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logs-list {
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.log-item {
  padding: var(--spacing-md);
}

.log-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.log-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.log-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
}

.empty-state {
  padding: var(--spacing-3xl) var(--spacing-lg);
}
</style>
