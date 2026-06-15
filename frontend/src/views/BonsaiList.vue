<template>
  <div class="bonsai-list-page">
    <van-nav-bar title="我的盆景" fixed placeholder>
      <template #right>
        <van-button type="primary" size="small" @click="goCreate">新增</van-button>
      </template>
    </van-nav-bar>

    <div class="content">
      <div v-if="loading" class="loading-wrapper">
        <van-loading size="24px">加载中...</van-loading>
      </div>

      <van-empty v-else-if="bonsais.length === 0" description="还没有盆景，快去添加一盆吧" />

      <div v-else class="bonsai-grid">
        <div
          v-for="bonsai in bonsais"
          :key="bonsai.id"
          class="bonsai-card"
          @click="goDetail(bonsai.id)"
        >
          <div class="bonsai-cover">
            <img
              v-if="bonsai.coverImage"
              :src="bonsai.coverImage"
              :alt="bonsai.name"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <van-icon name="flower-o" size="40" color="#ccc" />
            </div>
          </div>
          <div class="bonsai-info">
            <div class="bonsai-name">{{ bonsai.name }}</div>
            <div class="bonsai-meta">
              <span v-if="bonsai.species" class="species-tag">{{ bonsai.species.name }}</span>
              <span v-if="bonsai.treeAge" class="age-tag">{{ bonsai.treeAge }}年</span>
            </div>
            <div v-if="bonsai.acquireDate" class="acquire-date">
              入手：{{ formatDate(bonsai.acquireDate) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <van-tabbar v-model:active="activeFooter" route fixed placeholder>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/bonsais" icon="flower-o">盆景</van-tabbar-item>
      <van-tabbar-item to="/publish" icon="plus">发布</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserBonsaiList } from '@/api/bonsai'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeFooter = ref(1)
const loading = ref(false)
const bonsais = ref([])

const formatDate = (date) => {
  if (!date) return ''
  return date
}

const loadBonsais = async () => {
  if (!userStore.currentUser) return
  loading.value = true
  try {
    const data = await getUserBonsaiList(userStore.currentUser.id)
    bonsais.value = data || []
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const goCreate = () => {
  router.push('/bonsais/create')
}

const goDetail = (id) => {
  router.push(`/bonsais/${id}`)
}

onMounted(() => {
  loadBonsais()
})
</script>

<style scoped>
.bonsai-list-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.content {
  padding: 12px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.bonsai-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.bonsai-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.bonsai-cover {
  width: 100%;
  aspect-ratio: 1;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.bonsai-info {
  padding: 12px;
}

.bonsai-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bonsai-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}

.species-tag,
.age-tag {
  font-size: 12px;
  padding: 2px 6px;
  background: #f2f3f5;
  color: #646566;
  border-radius: 4px;
}

.acquire-date {
  font-size: 12px;
  color: #969799;
}
</style>
