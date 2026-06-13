<template>
  <div class="profile-page">
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

    <div class="content">
      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="我的作品">
          <div class="posts-grid">
            <div
              v-for="post in myPosts"
              :key="post.id"
              class="post-item"
              @click="goPostDetail(post.id)"
            >
              <div class="post-cover">
                <img :src="getCoverImage(post)" alt="" />
                <div class="post-stats">
                  <van-icon name="eye-o" /> {{ post.viewCount }}
                </div>
              </div>
              <div class="post-title">{{ post.title }}</div>
            </div>
            <van-empty v-if="myPosts.length === 0" description="暂无作品" />
          </div>
        </van-tab>
        <van-tab title="养护记录">
          <div class="logs-list">
            <div v-for="log in myLogs" :key="log.id" class="log-item">
              <div class="log-top">
                <van-tag size="medium">{{ getLogTypeName(log.logType) }}</van-tag>
                <span class="log-date">{{ log.logDate }}</span>
              </div>
              <div class="log-content" v-if="log.content">{{ log.content }}</div>
            </div>
            <van-empty v-if="myLogs.length === 0" description="暂无养护记录" />
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <van-tabbar v-model:active="activeFooter" route fixed placeholder>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/care-logs" icon="notes-o">养护</van-tabbar-item>
      <van-tabbar-item to="/publish" icon="plus">发布</van-tabbar-item>
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

const router = useRouter()
const userStore = useUserStore()

const user = ref(userStore.currentUser)
const activeTab = ref(0)
const activeFooter = ref(3)
const myPosts = ref([])
const myLogs = ref([])
const postCount = ref(0)
const likeCount = ref(0)
const logCount = ref(0)

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

const getCoverImage = (post) => {
  if (post.images && post.images.length > 0) {
    const cover = post.images.find(img => img.isCover === 1) || post.images[0]
    return cover.thumbnailUrl || cover.imageUrl || 'https://picsum.photos/200/200?random=' + post.id
  }
  return 'https://picsum.photos/200/200?random=' + post.id
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
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px 20px;
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
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
  font-weight: 700;
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
}

.nickname {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.bio {
  font-size: 13px;
  opacity: 0.9;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px;
}

.user-stats .stat-item {
  text-align: center;
}

.user-stats .stat-value {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.user-stats .stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.content {
  background: #fff;
  margin: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2px;
  padding: 2px;
}

.post-item {
  background: #f5f5f5;
  aspect-ratio: 1;
  position: relative;
  cursor: pointer;
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
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.post-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
  font-size: 11px;
  padding: 20px 8px 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logs-list {
  padding: 12px;
}

.log-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.log-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-date {
  font-size: 12px;
  color: #999;
}

.log-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}
</style>
