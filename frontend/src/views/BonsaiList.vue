<template>
  <div class="bonsai-list-page page-container">
    <van-nav-bar title="我的盆景" fixed placeholder>
      <template #right>
        <van-button type="primary" size="small" @click="goCreate">新增</van-button>
      </template>
    </van-nav-bar>

    <div class="content content-wrapper">
      <div v-if="loading" class="loading-wrapper">
        <van-loading size="24px">加载中...</van-loading>
      </div>

      <div v-else-if="bonsais.length === 0" class="empty-state">
        <van-icon name="flower-o" class="empty-icon" />
        <span class="empty-text">还没有盆景，快去添加一盆吧</span>
        <van-button type="primary" size="small" @click="goCreate" style="margin-top: 16px;">
          <van-icon name="plus" />添加盆景
        </van-button>
      </div>

      <div v-else class="bonsai-grid">
        <div
          v-for="(bonsai, index) in bonsais"
          :key="bonsai.id"
          class="bonsai-card card"
          @click="goDetail(bonsai.id)"
        >
          <div class="bonsai-cover cover-wrapper">
            <img
              :src="getBonsaiCover(bonsai, index)"
              :alt="bonsai.name"
              class="cover-img"
              @error="onCoverError(index)"
            />
          </div>
          <div class="bonsai-info">
            <div class="bonsai-name text-ellipsis">{{ bonsai.name }}</div>
            <div class="bonsai-meta">
              <van-tag v-if="bonsai.species" size="mini" type="primary" plain>{{ bonsai.species.name }}</van-tag>
              <van-tag v-if="bonsai.treeAge" size="mini" type="success" plain>{{ bonsai.treeAge }}年</van-tag>
            </div>
            <div v-if="bonsai.acquireDate" class="acquire-date">
              <van-icon name="calendar-o" size="10" />
              <span>{{ formatDate(bonsai.acquireDate) }}</span>
            </div>
          </div>
        </div>
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
import { getUserBonsaiList } from '@/api/bonsai'
import { useUserStore } from '@/stores/user'
import { getCoverImage, BONSAI_PLACEHOLDER_SVG } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()

const activeFooter = ref(1)
const loading = ref(false)
const bonsais = ref([])
const coverErrors = ref({})

const formatDate = (date) => {
  if (!date) return ''
  return date
}

const getBonsaiCover = (bonsai, index) => {
  if (coverErrors.value[index]) {
    return BONSAI_PLACEHOLDER_SVG
  }
  return getCoverImage(bonsai, { useBonsaiPlaceholder: true })
}

const onCoverError = (index) => {
  coverErrors.value[index] = true
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
  padding-bottom: 60px;
}

.content {
  padding-top: var(--spacing-md);
}

.bonsai-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.bonsai-card {
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.bonsai-card:active {
  transform: scale(0.98);
  box-shadow: var(--shadow-sm);
}

.bonsai-cover {
  width: 100%;
  aspect-ratio: 1;
  background: var(--color-bg-tertiary);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bonsai-info {
  padding: var(--spacing-sm);
}

.bonsai-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.bonsai-meta {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xs);
  flex-wrap: wrap;
}

.acquire-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>
