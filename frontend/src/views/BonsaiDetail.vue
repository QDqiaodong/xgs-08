<template>
  <div class="bonsai-detail-page">
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

    <div v-else-if="bonsai" class="content">
      <div class="bonsai-header">
        <div class="bonsai-cover">
          <img
            v-if="bonsai.coverImage"
            :src="bonsai.coverImage"
            :alt="bonsai.name"
            class="cover-img"
          />
          <div v-else class="cover-placeholder">
            <van-icon name="flower-o" size="60" color="#ccc" />
          </div>
        </div>
        <div class="bonsai-basic">
          <h2 class="bonsai-name">{{ bonsai.name }}</h2>
          <div class="bonsai-tags">
            <van-tag v-if="bonsai.species" type="primary" plain>{{ bonsai.species.name }}</van-tag>
            <van-tag v-if="bonsai.treeAge" type="success" plain>{{ bonsai.treeAge }}年树龄</van-tag>
            <van-tag v-if="bonsai.potType" type="warning" plain>{{ bonsai.potType }}</van-tag>
          </div>
          <div v-if="bonsai.acquireDate" class="acquire-info">
            <van-icon name="calendar-o" />
            <span>入手日期：{{ formatDate(bonsai.acquireDate) }}</span>
          </div>
        </div>
      </div>

      <div v-if="bonsai.description" class="description-section">
        <div class="section-title">简介</div>
        <p class="description-text">{{ bonsai.description }}</p>
      </div>

      <div class="timeline-section">
        <div class="timeline-header">
          <div class="section-title">生命周期时间线</div>
          <van-button type="primary" size="small" @click="goAddEvent">
            <van-icon name="plus" />记录事件
          </van-button>
        </div>

        <van-empty v-if="events.length === 0" description="还没有记录生命周期事件" />

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
                  <van-tag :type="getEventTagType(event.eventType)" size="medium">
                    {{ getEventTypeName(event.eventType) }}
                  </van-tag>
                  <span class="event-date">{{ formatDate(event.eventDate) }}</span>
                </div>
                <h4 v-if="event.title" class="event-title">{{ event.title }}</h4>
                <p v-if="event.content" class="event-content">{{ event.content }}</p>
                <div v-if="event.images && event.images.length > 0" class="event-images">
                  <img
                    v-for="(img, imgIndex) in parseImages(event.images)"
                    :key="imgIndex"
                    :src="img"
                    class="event-image"
                    @click="previewImage(index, imgIndex)"
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog, showImagePreview } from 'vant'
import { getBonsaiById, deleteBonsai } from '@/api/bonsai'
import { getEventsByBonsaiId, deleteEvent as deleteEventApi } from '@/api/lifecycleEvent'

const router = useRouter()
const route = useRoute()

const activeFooter = ref(1)
const loading = ref(false)
const bonsai = ref(null)
const events = ref([])
const actionValue = ref(0)
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

const formatDate = (date) => {
  if (!date) return ''
  return date
}

const parseImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    const parsed = JSON.parse(imagesStr)
    return Array.isArray(parsed) ? parsed : [imagesStr]
  } catch {
    return [imagesStr]
  }
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
    const imgs = parseImages(events.value[i].images)
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
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.content {
  padding: 12px;
}

.bonsai-header {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.bonsai-cover {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
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

.bonsai-basic {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bonsai-name {
  font-size: 20px;
  font-weight: 600;
  color: #323233;
  margin: 0 0 10px 0;
}

.bonsai-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.acquire-info {
  font-size: 13px;
  color: #646566;
  display: flex;
  align-items: center;
  gap: 4px;
}

.description-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}

.description-text {
  font-size: 14px;
  color: #646566;
  line-height: 1.6;
  margin: 0;
}

.timeline-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.timeline-header .section-title {
  margin-bottom: 0;
}

.timeline {
  position: relative;
  padding-left: 4px;
}

.timeline-item {
  display: flex;
  padding-bottom: 16px;
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
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #ebedf0;
  margin-top: 4px;
}

.timeline-content {
  flex: 1;
  padding-left: 12px;
  padding-top: 4px;
}

.event-card {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.event-date {
  font-size: 12px;
  color: #969799;
}

.event-title {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
  margin: 0 0 6px 0;
}

.event-content {
  font-size: 14px;
  color: #646566;
  line-height: 1.5;
  margin: 0 0 10px 0;
}

.event-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.event-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
}

.event-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
