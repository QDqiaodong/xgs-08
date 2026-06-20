<template>
  <div class="bonsai-detail-page page-container">
    <van-nav-bar title="盆景详情" left-arrow @click-left="goBack" fixed placeholder>
      <template #right>
        <van-dropdown-menu>
          <van-dropdown-item v-model="actionValue" :options="actionOptions" @change="onActionChange" />
        </van-dropdown-menu>
      </template>
    </van-nav-bar>

    <div v-if="loading" class="loading-wrapper">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <div v-else-if="bonsai" class="content content-wrapper">
      <div class="bonsai-header card">
        <div class="bonsai-cover cover-wrapper">
          <img
            :src="getBonsaiCover()"
            :alt="bonsai.name"
            class="cover-img"
            @error="onCoverError"
          />
        </div>
        <div class="bonsai-basic">
          <h2 class="bonsai-name">{{ bonsai.name }}</h2>
          <div class="bonsai-tags">
            <van-tag v-if="bonsai.species" type="primary" plain size="small">{{ bonsai.species.name }}</van-tag>
            <van-tag v-if="bonsai.style" type="danger" plain size="small">{{ bonsai.style.name }}</van-tag>
            <van-tag v-if="bonsai.treeAge" type="success" plain size="small">{{ bonsai.treeAge }}年树龄</van-tag>
            <van-tag v-if="bonsai.potType" type="warning" plain size="small">{{ bonsai.potType }}</van-tag>
          </div>
          <div v-if="bonsai.acquireDate" class="acquire-info">
            <van-icon name="calendar-o" size="14" />
            <span>入手：{{ formatDate(bonsai.acquireDate) }}</span>
          </div>
        </div>
      </div>

      <SpeciesCareTip
        v-if="bonsai.species"
        :species-name="bonsai.species.name"
        :default-expanded="true"
      />

      <div v-if="bonsai.description" class="description-section card">
        <div class="section-title">简介</div>
        <p class="description-text">{{ bonsai.description }}</p>
      </div>

      <div v-if="hasOutlineData" class="outline-section card">
        <div class="section-title">
          <van-icon name="eye-o" />
          <span>树形轮廓观察卡</span>
        </div>
        <div class="outline-grid">
          <div class="outline-item" :class="{ empty: !bonsai.trunkShape }">
            <div class="outline-icon-wrapper outline-icon-trunk">
              <van-icon name="underway-o" />
            </div>
            <div class="outline-label">干型</div>
            <div class="outline-value">{{ bonsai.trunkShape || '暂无记录' }}</div>
          </div>
          <div class="outline-item" :class="{ empty: !bonsai.branchSupport }">
            <div class="outline-icon-wrapper outline-icon-branch">
              <van-icon name="share-o" />
            </div>
            <div class="outline-label">枝托</div>
            <div class="outline-value">{{ bonsai.branchSupport || '暂无记录' }}</div>
          </div>
          <div class="outline-item" :class="{ empty: !bonsai.crownWidth }">
            <div class="outline-icon-wrapper outline-icon-crown">
              <van-icon name="arrow-up" />
            </div>
            <div class="outline-label">冠幅</div>
            <div class="outline-value">{{ bonsai.crownWidth || '暂无记录' }}</div>
          </div>
          <div class="outline-item" :class="{ empty: !bonsai.potSurface }">
            <div class="outline-icon-wrapper outline-icon-pot">
              <van-icon name="wap-home-o" />
            </div>
            <div class="outline-label">盆面</div>
            <div class="outline-value">{{ bonsai.potSurface || '暂无记录' }}</div>
          </div>
        </div>
      </div>

      <div v-if="hasStageImages" class="stage-images-section card">
        <div class="section-title">
          <van-icon name="photos" />
          <span>养护阶段变化</span>
        </div>
        <div class="stage-tabs">
          <div
            v-for="(stage, idx) in availableStages"
            :key="stage.value"
            class="stage-tab"
            :class="{ active: activeStage === stage.value }"
            :style="activeStage === stage.value ? { borderColor: stage.color, color: stage.color } : {}"
            @click="activeStage = stage.value"
          >
            <span class="stage-tab-icon">{{ stage.icon }}</span>
            <span>{{ stage.label }}</span>
          </div>
        </div>
        <div class="stage-images-grid">
          <img
            v-for="(img, idx) in activeStageImages"
            :key="idx"
            :src="getStageImage(img)"
            class="stage-image"
            @click="previewStageImage(idx)"
            @error="onStageImageError(idx)"
          />
        </div>
        <div v-if="activeStageNote" class="stage-note">
          <van-icon name="description-o" size="12" />
          <span>{{ activeStageNote }}</span>
        </div>
        <div class="stage-progress">
          <div class="progress-bar">
            <div
              v-for="(stage, idx) in STAGE_LIST"
              :key="stage.value"
              class="progress-segment"
              :class="{ filled: hasStage(stage.value) }"
              :style="hasStage(stage.value) ? { background: stage.color } : {}"
              :title="stage.label + (hasStage(stage.value) ? '（已有图片）' : '（暂无图片）')"
            ></div>
          </div>
          <div class="progress-labels">
            <div v-for="stage in STAGE_LIST" :key="stage.value" class="progress-label">
              <span>{{ stage.icon }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="care-summary-section card">
        <div class="section-title">
          <van-icon name="notes-o" />
          <span>养护摘要</span>
        </div>
        <div class="care-summary-grid">
          <div class="care-summary-item water">
            <div class="care-icon-wrapper">
              <van-icon name="down" size="20" />
            </div>
            <div class="care-label">浇水</div>
            <div class="care-date">
              {{ careSummary?.lastWaterDate || '暂无记录' }}
            </div>
            <div v-if="careSummary?.lastWaterDate" class="care-days">
              {{ getDaysAgo(careSummary.lastWaterDate) }}
            </div>
          </div>
          <div class="care-summary-item prune">
            <div class="care-icon-wrapper">
              <van-icon name="scissors-o" size="20" />
            </div>
            <div class="care-label">修剪</div>
            <div class="care-date">
              {{ careSummary?.lastPruneDate || '暂无记录' }}
            </div>
            <div v-if="careSummary?.lastPruneDate" class="care-days">
              {{ getDaysAgo(careSummary.lastPruneDate) }}
            </div>
          </div>
          <div class="care-summary-item fertilize">
            <div class="care-icon-wrapper">
              <van-icon name="balance-o" size="20" />
            </div>
            <div class="care-label">施肥</div>
            <div class="care-date">
              {{ careSummary?.lastFertilizeDate || '暂无记录' }}
            </div>
            <div v-if="careSummary?.lastFertilizeDate" class="care-days">
              {{ getDaysAgo(careSummary.lastFertilizeDate) }}
            </div>
          </div>
          <div class="care-summary-item repot">
            <div class="care-icon-wrapper">
              <van-icon name="exchange" size="20" />
            </div>
            <div class="care-label">换盆</div>
            <div class="care-date">
              {{ careSummary?.lastRepotDate || '暂无记录' }}
            </div>
            <div v-if="careSummary?.lastRepotDate" class="care-days">
              {{ getDaysAgo(careSummary.lastRepotDate) }}
            </div>
          </div>
        </div>
      </div>

      <div class="timeline-section card">
        <div class="timeline-header">
          <div class="section-title">生命周期时间线</div>
          <van-button type="primary" size="small" @click="goAddEvent">
            <van-icon name="plus" />记录事件
          </van-button>
        </div>

        <div v-if="events.length === 0" class="empty-state">
          <van-icon name="clock-o" class="empty-icon" />
          <span class="empty-text">还没有记录生命周期事件</span>
        </div>

        <div v-else class="timeline">
          <div
            v-for="(event, index) in events"
            :key="event.id"
            class="timeline-item"
          >
            <div class="timeline-left">
              <div class="event-dot" :style="{ background: getEventColor(event.eventType) }">
                <van-icon :name="getEventIcon(event.eventType)" color="#fff" size="14" />
              </div>
              <div v-if="index < events.length - 1" class="timeline-line"></div>
            </div>
            <div class="timeline-content">
              <div class="event-card">
                <div class="event-header">
                  <van-tag :type="getEventTagType(event.eventType)" size="small">
                    {{ getEventTypeName(event.eventType) }}
                  </van-tag>
                  <span class="event-date">{{ formatDate(event.eventDate) }}</span>
                </div>
                <h4 v-if="event.title" class="event-title">{{ event.title }}</h4>
                <p v-if="event.content" class="event-content">{{ event.content }}</p>
                
                <div v-if="hasBeforeAfterCompare(event)" class="before-after-section">
                  <div class="compare-section-header">
                    <van-icon name="eye-o" />
                    <span>造型前后对照</span>
                  </div>
                  <BeforeAfterCompare
                    v-for="(pair, pairIndex) in getComparePairs(event)"
                    :key="pairIndex"
                    :before-image="pair.before"
                    :after-image="pair.after"
                    :before-label="getBeforeLabel(event.eventType)"
                    :after-label="getAfterLabel(event.eventType)"
                    :event-type="event.eventType"
                    class="compare-item"
                  />
                </div>
                
                <div v-if="getEventImages(event).length > 0 && !hasBeforeAfterCompare(event)" class="event-images">
                  <img
                    v-for="(img, imgIndex) in getEventImages(event)"
                    :key="imgIndex"
                    :src="img"
                    class="event-image"
                    @click="previewImage(index, imgIndex)"
                    @error="onEventImageError(event.id, imgIndex)"
                  />
                </div>
                <div class="event-actions">
                  <van-button
                    type="default"
                    size="small"
                    @click="deleteEvent(event, index)"
                  >删除</van-button>
                </div>
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
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog, showImagePreview } from 'vant'
import { getBonsaiById, deleteBonsai, getCareSummary, getStageImages } from '@/api/bonsai'
import { getEventsByBonsaiId, deleteEvent as deleteEventApi } from '@/api/lifecycleEvent'
import { getCoverImage, parseImages, getImageWithFallback, BONSAI_PLACEHOLDER_SVG, PLACEHOLDER_SVG } from '@/utils/image'
import BeforeAfterCompare from '@/components/BeforeAfterCompare.vue'
import SpeciesCareTip from '@/components/SpeciesCareTip.vue'
import { useUserStore } from '@/stores/user'
import { STAGE_LIST, getStageInfo } from '@/utils/bonsaiValidator'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeFooter = ref(1)
const loading = ref(false)
const bonsai = ref(null)
const events = ref([])
const careSummary = ref(null)
const actionValue = ref(0)
const coverError = ref(false)
const eventImageErrors = ref({})
const eventBeforeImageErrors = ref({})
const actionOptions = ref([
  { text: '更多操作', value: 0 },
  { text: '编辑', value: 1 },
  { text: '删除', value: 2 }
])

const stageImages = ref([])
const stageImageErrors = ref({})
const activeStage = ref(STAGE_LIST[0].value)

const eventTypes = [
  { text: '入手', value: 'acquire', icon: 'logistics', color: '#1989fa', tagType: 'primary' },
  { text: '定植', value: 'planting', icon: 'wap-home-o', color: '#07c160', tagType: 'success' },
  { text: '修剪', value: 'pruning', icon: 'scissors-o', color: '#ff976a', tagType: 'warning' },
  { text: '蟠扎', value: 'wiring', icon: 'orders-o', color: '#7232dd', tagType: 'primary' },
  { text: '换盆', value: 'repotting', icon: 'description', color: '#ee0a24', tagType: 'danger' },
  { text: '其他', value: 'other', icon: 'more-o', color: '#969799', tagType: 'default' }
]

const hasOutlineData = computed(() => {
  if (!bonsai.value) return false
  return !!(bonsai.value.trunkShape || bonsai.value.branchSupport || bonsai.value.crownWidth || bonsai.value.potSurface)
})

const stageImagesGrouped = computed(() => {
  const grouped = {}
  STAGE_LIST.forEach(stage => {
    grouped[stage.value] = []
  })
  stageImages.value.forEach(img => {
    if (grouped[img.stage]) {
      grouped[img.stage].push(img)
    }
  })
  return grouped
})

const hasStageImages = computed(() => {
  return stageImages.value.length > 0
})

const availableStages = computed(() => {
  return STAGE_LIST.filter(stage => {
    return stageImagesGrouped.value[stage.value] && stageImagesGrouped.value[stage.value].length > 0
  })
})

const activeStageImages = computed(() => {
  if (!activeStage.value) return []
  return stageImagesGrouped.value[activeStage.value] || []
})

const activeStageNote = computed(() => {
  const imgs = activeStageImages.value
  if (imgs.length > 0 && imgs[0].note) {
    return imgs[0].note
  }
  return ''
})

const hasStage = (stageValue) => {
  return stageImagesGrouped.value[stageValue] && stageImagesGrouped.value[stageValue].length > 0
}

const getStageImage = (img) => {
  const errorKey = `stage_${img.id}`
  if (stageImageErrors.value[errorKey]) {
    return PLACEHOLDER_SVG
  }
  return getImageWithFallback(img.imageUrl)
}

const onStageImageError = (imageId) => {
  const errorKey = `stage_${imageId}`
  stageImageErrors.value[errorKey] = true
}

const previewStageImage = (idx) => {
  const allImages = activeStageImages.value.map(img => {
    const errorKey = `stage_${img.id}`
    if (stageImageErrors.value[errorKey]) {
      return PLACEHOLDER_SVG
    }
    return getImageWithFallback(img.imageUrl)
  })
  showImagePreview({
    images: allImages,
    startPosition: idx
  })
}

const formatDate = (date) => {
  if (!date) return ''
  return date
}

const getDaysAgo = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  date.setHours(0, 0, 0, 0)
  const diffTime = today.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 30) return `${diffDays}天前`
  if (diffDays < 365) {
    const months = Math.floor(diffDays / 30)
    return `${months}个月前`
  }
  const years = Math.floor(diffDays / 365)
  return `${years}年前`
}

const getBonsaiCover = () => {
  if (coverError.value) {
    return BONSAI_PLACEHOLDER_SVG
  }
  return getCoverImage(bonsai.value, { useBonsaiPlaceholder: true })
}

const onCoverError = () => {
  coverError.value = true
}

const getEventImages = (event) => {
  const images = parseImages(event.images)
  const errorKey = `event_${event.id}`
  const errors = eventImageErrors.value[errorKey] || {}
  return images.map((img, idx) => {
    if (errors[idx]) {
      return PLACEHOLDER_SVG
    }
    return getImageWithFallback(img)
  })
}

const getEventBeforeImages = (event) => {
  const images = parseImages(event.beforeImages)
  const errorKey = `event_before_${event.id}`
  const errors = eventBeforeImageErrors.value[errorKey] || {}
  return images.map((img, idx) => {
    if (errors[idx]) {
      return PLACEHOLDER_SVG
    }
    return getImageWithFallback(img)
  })
}

const hasBeforeAfterCompare = (event) => {
  const beforeImages = getEventBeforeImages(event)
  const afterImages = getEventImages(event)
  return beforeImages.length > 0 && afterImages.length > 0
}

const getComparePairs = (event) => {
  const beforeImages = getEventBeforeImages(event)
  const afterImages = getEventImages(event)
  const pairs = []
  const maxLen = Math.max(beforeImages.length, afterImages.length)
  
  for (let i = 0; i < maxLen; i++) {
    pairs.push({
      before: beforeImages[i] || beforeImages[beforeImages.length - 1],
      after: afterImages[i] || afterImages[afterImages.length - 1]
    })
  }
  
  return pairs
}

const onEventImageError = (eventId, imgIndex) => {
  const errorKey = `event_${eventId}`
  if (!eventImageErrors.value[errorKey]) {
    eventImageErrors.value[errorKey] = {}
  }
  eventImageErrors.value[errorKey][imgIndex] = true
}

const onEventBeforeImageError = (eventId, imgIndex) => {
  const errorKey = `event_before_${eventId}`
  if (!eventBeforeImageErrors.value[errorKey]) {
    eventBeforeImageErrors.value[errorKey] = {}
  }
  eventBeforeImageErrors.value[errorKey][imgIndex] = true
}

const getBeforeLabel = (eventType) => {
  const labels = {
    pruning: '修剪前',
    wiring: '蟠扎前',
    repotting: '换盆前',
    planting: '定植前',
    acquire: '入手前',
    other: '造型前'
  }
  return labels[eventType] || '造型前'
}

const getAfterLabel = (eventType) => {
  const labels = {
    pruning: '修剪后',
    wiring: '蟠扎后',
    repotting: '换盆后',
    planting: '定植后',
    acquire: '入手后',
    other: '造型后'
  }
  return labels[eventType] || '造型后'
}

const getEventTypeName = (type) => {
  const found = eventTypes.find(t => t.value === type)
  return found ? found.text : type
}

const getEventIcon = (type) => {
  const found = eventTypes.find(t => t.value === type)
  return found ? found.icon : 'more-o'
}

const getEventColor = (type) => {
  const found = eventTypes.find(t => t.value === type)
  return found ? found.color : '#969799'
}

const getEventTagType = (type) => {
  const found = eventTypes.find(t => t.value === type)
  return found ? found.tagType : 'default'
}

const previewImage = (eventIndex, imgIndex) => {
  const allImages = []
  let startPosition = 0
  for (let i = 0; i < events.value.length; i++) {
    const imgs = getEventImages(events.value[i])
    if (i < eventIndex) {
      startPosition += imgs.length
    }
    allImages.push(...imgs)
  }
  startPosition += imgIndex
  showImagePreview({
    images: allImages,
    startPosition: startPosition
  })
}

const loadBonsai = async () => {
  loading.value = true
  try {
    const id = route.params.id
    bonsai.value = await getBonsaiById(id)
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const loadEvents = async () => {
  try {
    const id = route.params.id
    events.value = await getEventsByBonsaiId(id)
  } catch (e) {
    showToast('加载事件失败')
  }
}

const loadCareSummary = async () => {
  try {
    const id = route.params.id
    careSummary.value = await getCareSummary(id)
  } catch (e) {
    console.warn('加载养护摘要失败', e)
  }
}

const loadStageImages = async () => {
  try {
    const id = route.params.id
    stageImages.value = await getStageImages(id)
    if (availableStages.value.length > 0) {
      activeStage.value = availableStages.value[0].value
    }
  } catch (e) {
    console.warn('加载阶段图片失败', e)
  }
}

const goBack = () => {
  router.back()
}

const goAddEvent = () => {
  router.push(`/bonsais/${route.params.id}/events/create`)
}

const onActionChange = (value) => {
  if (value === 1) {
    router.push(`/bonsais/${route.params.id}/edit`)
  } else if (value === 2) {
    confirmDeleteBonsai()
  }
  actionValue.value = 0
}

const confirmDeleteBonsai = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '删除后所有生命周期记录也会被删除，确定要删除这盆盆景吗？'
    })
    await deleteBonsai(route.params.id)
    showToast('删除成功')
    setTimeout(() => {
      router.replace('/bonsais')
    }, 1000)
  } catch (e) {
    if (e !== 'cancel') {
      showToast('删除失败')
    }
  }
}

const deleteEvent = async (event, index) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这条生命周期记录吗？'
    })
    await deleteEventApi(event.id, userStore.currentUser.id)
    events.value.splice(index, 1)
    showToast('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      showToast('删除失败')
    }
  }
}

onMounted(() => {
  loadBonsai()
  loadEvents()
  loadCareSummary()
  loadStageImages()
})
</script>

<style scoped>
.bonsai-detail-page {
  padding-bottom: 40px;
}

.content {
  padding-top: var(--spacing-md);
}

.bonsai-header {
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.bonsai-cover {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bonsai-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bonsai-name {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.bonsai-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.acquire-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: auto;
}

.description-section {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.description-section .section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.description-text {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: var(--line-height-lg);
  margin: 0;
}

.timeline-section {
  padding: var(--spacing-lg);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.timeline-header .section-title {
  margin-bottom: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.timeline {
  position: relative;
  padding-left: 4px;
}

.timeline-item {
  display: flex;
  padding-bottom: var(--spacing-lg);
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  flex-shrink: 0;
}

.event-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  z-index: 1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: var(--color-border);
  margin-top: var(--spacing-xs);
}

.timeline-content {
  flex: 1;
  padding-left: var(--spacing-sm);
  padding-top: 4px;
}

.event-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.event-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.event-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.event-content {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
  margin: 0 0 var(--spacing-sm) 0;
}

.event-images {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.event-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xs);
  object-fit: cover;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.event-image:active {
  transform: scale(0.95);
}

.before-after-section {
  margin-bottom: var(--spacing-md);
}

.compare-section-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--color-primary-light);
  border-radius: var(--radius-xs);
  align-self: flex-start;
}

.compare-item {
  margin-bottom: var(--spacing-sm);
}

.compare-item:last-child {
  margin-bottom: 0;
}

.event-actions {
  display: flex;
  justify-content: flex-end;
}

.outline-section {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.outline-section .section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.outline-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.outline-item {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: all var(--transition-base);
}

.outline-item.empty {
  background: var(--color-bg-secondary);
  opacity: 0.6;
}

.outline-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-sm);
  font-size: 22px;
}

.outline-icon-trunk {
  background: linear-gradient(135deg, #8B5A2B 0%, #A0522D 100%);
  color: #fff;
}

.outline-icon-branch {
  background: linear-gradient(135deg, #2E8B57 0%, #3CB371 100%);
  color: #fff;
}

.outline-icon-crown {
  background: linear-gradient(135deg, #1E90FF 0%, #4169E1 100%);
  color: #fff;
}

.outline-icon-pot {
  background: linear-gradient(135deg, #CD853F 0%, #D2691E 100%);
  color: #fff;
}

.outline-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.outline-value {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
}

@media (max-width: 360px) {
  .outline-grid {
    grid-template-columns: 1fr;
  }
}

.care-summary-section {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.care-summary-section .section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  padding-left: var(--spacing-sm);
  border-left: 3px solid var(--color-primary);
}

.care-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.care-summary-item {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 1px solid transparent;
  transition: all var(--transition-base);
}

.care-summary-item.water {
  border-color: rgba(25, 137, 250, 0.2);
}

.care-summary-item.prune {
  border-color: rgba(255, 151, 106, 0.2);
}

.care-summary-item.fertilize {
  border-color: rgba(7, 193, 96, 0.2);
}

.care-summary-item.repot {
  border-color: rgba(238, 10, 36, 0.2);
}

.care-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-sm);
}

.care-summary-item.water .care-icon-wrapper {
  background: rgba(25, 137, 250, 0.1);
  color: #1989fa;
}

.care-summary-item.prune .care-icon-wrapper {
  background: rgba(255, 151, 106, 0.1);
  color: #ff976a;
}

.care-summary-item.fertilize .care-icon-wrapper {
  background: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.care-summary-item.repot .care-icon-wrapper {
  background: rgba(238, 10, 36, 0.1);
  color: #ee0a24;
}

.care-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.care-date {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
  margin-bottom: 2px;
}

.care-days {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

@media (max-width: 360px) {
  .care-summary-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.stage-images-section {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.stage-images-section .section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  padding-left: var(--spacing-sm);
  border-left: 3px solid #409eff;
}

.stage-tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
}

.stage-tabs::-webkit-scrollbar {
  display: none;
}

.stage-tab {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.stage-tab.active {
  background: #f0f7ff;
  border-width: 2px;
  font-weight: 600;
}

.stage-tab-icon {
  font-size: 14px;
}

.stage-images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.stage-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: transform 0.2s;
}

.stage-image:active {
  transform: scale(0.95);
}

@media (max-width: 360px) {
  .stage-images-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stage-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 12px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-md);
  line-height: 1.5;
}

.stage-progress {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px dashed var(--color-border);
}

.progress-bar {
  display: flex;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
  background: var(--color-bg-secondary);
  margin-bottom: 8px;
}

.progress-segment {
  flex: 1;
  background: var(--color-border);
  transition: background 0.3s;
  margin-right: 2px;
}

.progress-segment:last-child {
  margin-right: 0;
}

.progress-segment.filled {
  background: var(--color-primary);
}

.progress-labels {
  display: flex;
  justify-content: space-between;
}

.progress-label {
  flex: 1;
  text-align: center;
  font-size: 12px;
  opacity: 0.5;
  transition: opacity 0.3s;
}

.progress-label:has(+ .progress-label .progress-segment.filled),
.progress-label:nth-child(1):has(~ .progress-label:nth-child(2) .progress-segment.filled) {
  opacity: 1;
}
</style>
