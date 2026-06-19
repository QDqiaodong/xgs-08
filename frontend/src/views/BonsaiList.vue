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

      <div v-else>
        <div v-if="speciesWithCareData.length > 0" class="care-tips-section">
          <div class="section-header">
            <div class="section-title">
              <van-icon name="bulb-o" size="18" />
              <span>树种护理提示</span>
            </div>
            <div class="species-tabs">
              <div
                v-for="(species, idx) in speciesWithCareData"
                :key="species.name"
                class="species-tab"
                :class="{ active: idx === selectedSpeciesIndex }"
                @click="selectedSpeciesIndex = idx"
              >
                <span class="tab-icon">{{ species.careData.icon }}</span>
                <span class="tab-name">{{ species.name }}</span>
                <span v-if="species.count > 0" class="tab-count">{{ species.count }}</span>
              </div>
            </div>
          </div>
          <SpeciesCareTip
            v-if="selectedSpecies"
            :species-name="selectedSpecies.name"
            :default-expanded="false"
          />
        </div>

        <div v-if="bonsais.length === 0" class="empty-state">
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserBonsaiList } from '@/api/bonsai'
import { useUserStore } from '@/stores/user'
import { getCoverImage, BONSAI_PLACEHOLDER_SVG } from '@/utils/image'
import SpeciesCareTip from '@/components/SpeciesCareTip.vue'
import { getSpeciesCare, getAllSpeciesNames } from '@/utils/speciesCare'
import { normalizeSpecies, isUnknownSpecies } from '@/utils/speciesNormalizer'

const router = useRouter()
const userStore = useUserStore()

const activeFooter = ref(1)
const loading = ref(false)
const bonsais = ref([])
const coverErrors = ref({})
const selectedSpeciesIndex = ref(0)
const allSpeciesWithCareData = getAllSpeciesNames()
const recommendedSpecies = ['榕树', '真柏', '黑松']

const speciesWithCareData = computed(() => {
  const speciesCounts = new Map()
  bonsais.value.forEach(bonsai => {
    let speciesName = bonsai.species?.name || ''
    if (!speciesName) {
      for (const name of allSpeciesWithCareData) {
        if (bonsai.name?.includes(name)) {
          speciesName = name
          break
        }
      }
    }
    const normalized = normalizeSpecies(speciesName)
    if (normalized && !isUnknownSpecies(normalized)) {
      speciesCounts.set(normalized, (speciesCounts.get(normalized) || 0) + 1)
    }
  })

  const userSpeciesWithCare = Array.from(speciesCounts.keys())
    .filter(name => getSpeciesCare(name))
    .map(name => ({
      name,
      careData: getSpeciesCare(name),
      count: speciesCounts.get(name) || 0
    }))
    .sort((a, b) => b.count - a.count)

  if (userSpeciesWithCare.length > 0) {
    return userSpeciesWithCare
  }

  return recommendedSpecies
    .map(name => ({
      name,
      careData: getSpeciesCare(name),
      count: 0
    }))
    .filter(species => species.careData)
})

const selectedSpecies = computed(() => {
  if (speciesWithCareData.value.length === 0) return null
  return speciesWithCareData.value[selectedSpeciesIndex.value]
})

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

.care-tips-section {
  margin-bottom: var(--spacing-md);
}

.care-tips-section .section-header {
  margin-bottom: var(--spacing-sm);
}

.care-tips-section .section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.care-tips-section .section-title .van-icon {
  color: var(--color-warning);
}

.species-tabs {
  display: flex;
  gap: var(--spacing-xs);
  overflow-x: auto;
  padding-bottom: var(--spacing-xs);
  scrollbar-width: none;
}

.species-tabs::-webkit-scrollbar {
  display: none;
}

.species-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
  flex-shrink: 0;
  border: 1px solid transparent;
}

.species-tab.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.tab-icon {
  font-size: 14px;
}

.tab-name {
  font-weight: var(--font-weight-medium);
}

.tab-count {
  background: var(--color-bg-tertiary);
  padding: 1px 6px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
}

.species-tab.active .tab-count {
  background: var(--color-primary);
  color: #fff;
}
</style>
