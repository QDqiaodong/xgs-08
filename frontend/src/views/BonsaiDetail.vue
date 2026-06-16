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
            <van-tag v-if="bonsai.treeAge" type="success" plain size="small">{{ bonsai.treeAge }}年树龄</van-tag>
            <van-tag v-if="bonsai.potType" type="warning" plain size="small">{{ bonsai.potType }}</van-tag>
          </div>
          <div v-if="bonsai.acquireDate" class="acquire-info">
            <van-icon name="calendar-o" size="14" />
            <span>入手：{{ formatDate(bonsai.acquireDate) }}</span>
          </div>
        </div>
      </div>

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
import { getBonsaiById, deleteBonsai } from '@/api/bonsai'
import { getEventsByBonsaiId, deleteEvent as deleteEventApi } from '@/api/lifecycleEvent'
import { getCoverImage, parseImages, getImageWithFallback, BONSAI_PLACEHOLDER_SVG, PLACEHOLDER_SVG } from '@/utils/image'
import BeforeAfterCompare from '@/components/BeforeAfterCompare.vue'

const router = useRouter()
const route = useRoute()

const activeFooter = ref(1)
const loading = ref(false)
const bonsai = ref(null)
const events = ref([])
const actionValue = ref(0)
const coverError = ref(false)
const eventImageErrors = ref({})
const eventBeforeImageErrors = ref({})
const actionOptions = ref([
  { text: '更多操作', value: 0 },
  { text: '编辑', value: 1 },
  { text: '删除', value: 2 }
])

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

const formatDate = (date) => {
  if (!date) return ''
  return date
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

const goBack = () => {
  router.back()
}

const goAddEvent = () => {
  router.push(`/bonsais/${route.params.id}/events/create`)
}

const onActionChange = (value) => {
  if (value === 1) {
    showToast('编辑功能开发中')
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
    await deleteEventApi(event.id)
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
</style>
