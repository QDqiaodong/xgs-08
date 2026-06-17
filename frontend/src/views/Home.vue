<template>
  <div class="home-page">
    <van-nav-bar title="盆景造型养护交流圈" fixed placeholder>
      <template #right>
        <van-icon name="plus" size="20" @click="goPublish" />
      </template>
    </van-nav-bar>

    <div class="filter-bar">
      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="全部" @click="filterPosts(null, null)" />
        <van-tab title="热门" @click="loadHotPosts" />
      </van-tabs>

      <div class="category-filter">
        <div class="filter-buttons">
          <div
            class="filter-btn"
            :class="{ active: showSpeciesFilter }"
            @click="toggleSpeciesFilter"
          >
            <span>{{ selectedSpeciesText }}</span>
            <van-icon :name="showSpeciesFilter ? 'arrow-up' : 'arrow-down'" />
          </div>
          <div
            class="filter-btn"
            :class="{ active: showStyleFilter }"
            @click="toggleStyleFilter"
          >
            <span>{{ selectedStyleText }}</span>
            <van-icon :name="showStyleFilter ? 'arrow-up' : 'arrow-down'" />
          </div>
        </div>
      </div>
    </div>

    <van-popup
      v-model:show="showSpeciesFilter"
      position="top"
      round
      :style="{ height: '60%' }"
    >
      <div class="filter-popup">
        <div class="filter-popup-header">
          <span class="filter-title">选择树种</span>
          <van-button type="primary" size="small" plain @click="clearSpeciesFilter">重置</van-button>
        </div>
        <div class="filter-popup-content">
          <div class="filter-option-group" v-for="group in speciesGroups" :key="group.name">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-options">
              <van-tag
                v-for="item in group.items"
                :key="item.value"
                :type="selectedSpecies === item.value ? 'primary' : 'default'"
                size="medium"
                plain
                :class="{ 'tag-selected': selectedSpecies === item.value }"
                @click="selectSpecies(item.value)"
              >
                <span v-if="item.icon">{{ item.icon }} </span>{{ item.text }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <van-popup
      v-model:show="showStyleFilter"
      position="top"
      round
      :style="{ height: '60%' }"
    >
      <div class="filter-popup">
        <div class="filter-popup-header">
          <span class="filter-title">选择造型风格</span>
          <van-button type="primary" size="small" plain @click="clearStyleFilter">重置</van-button>
        </div>
        <div class="filter-popup-content">
          <div class="filter-option-group" v-for="group in styleGroups" :key="group.name">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-options">
              <van-tag
                v-for="item in group.items"
                :key="item.value"
                :type="selectedStyle === item.value ? 'primary' : 'default'"
                size="medium"
                plain
                :class="{ 'tag-selected': selectedStyle === item.value }"
                @click="selectStyle(item.value)"
              >
                {{ item.text }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <van-overlay
      :show="showSpeciesFilter || showStyleFilter"
      @click="closeAllFilters"
    ></van-overlay>

    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <Waterfall :posts="posts" :loading="loading" @load-more="loadMore" />
      </van-pull-refresh>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts, getHotPosts } from '@/api/post'
import { getCategoriesByType } from '@/api/category'
import Waterfall from '@/components/Waterfall.vue'

const router = useRouter()
const activeTab = ref(0)
const activeFooter = ref(0)
const refreshing = ref(false)
const loading = ref(false)
const posts = ref([])
const page = ref(0)
const hasMore = ref(true)
const selectedSpecies = ref(0)
const selectedStyle = ref(0)
const speciesList = ref([])
const styleList = ref([])
const showSpeciesFilter = ref(false)
const showStyleFilter = ref(false)

const speciesGroups = computed(() => {
  const groups = new Map()
  speciesList.value.forEach(s => {
    const groupName = s.groupName || '其他'
    if (!groups.has(groupName)) {
      groups.set(groupName, [])
    }
    groups.get(groupName).push({
      text: s.name,
      value: s.id,
      icon: s.icon
    })
  })
  return Array.from(groups.entries()).map(([name, items]) => ({ name, items }))
})

const styleGroups = computed(() => {
  const groups = new Map()
  styleList.value.forEach(s => {
    const groupName = s.groupName || '其他'
    if (!groups.has(groupName)) {
      groups.set(groupName, [])
    }
    groups.get(groupName).push({
      text: s.name,
      value: s.id
    })
  })
  return Array.from(groups.entries()).map(([name, items]) => ({ name, items }))
})

const selectedSpeciesText = computed(() => {
  if (selectedSpecies.value === 0) return '全部树种'
  const species = speciesList.value.find(s => s.id === selectedSpecies.value)
  return species ? species.name : '全部树种'
})

const selectedStyleText = computed(() => {
  if (selectedStyle.value === 0) return '全部风格'
  const style = styleList.value.find(s => s.id === selectedStyle.value)
  return style ? style.name : '全部风格'
})

const loadCategories = async () => {
  try {
    const [species, styles] = await Promise.all([
      getCategoriesByType('species'),
      getCategoriesByType('style')
    ])
    speciesList.value = species
    styleList.value = styles
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const toggleSpeciesFilter = () => {
  showSpeciesFilter.value = !showSpeciesFilter.value
  showStyleFilter.value = false
}

const toggleStyleFilter = () => {
  showStyleFilter.value = !showStyleFilter.value
  showSpeciesFilter.value = false
}

const closeAllFilters = () => {
  showSpeciesFilter.value = false
  showStyleFilter.value = false
}

const selectSpecies = (value) => {
  selectedSpecies.value = value
  showSpeciesFilter.value = false
  filterPosts()
}

const selectStyle = (value) => {
  selectedStyle.value = value
  showStyleFilter.value = false
  filterPosts()
}

const clearSpeciesFilter = () => {
  selectedSpecies.value = 0
  showSpeciesFilter.value = false
  filterPosts()
}

const clearStyleFilter = () => {
  selectedStyle.value = 0
  showStyleFilter.value = false
  filterPosts()
}

const filterPosts = () => {
  page.value = 0
  posts.value = []
  hasMore.value = true
  loadPosts()
}

const loadPosts = async () => {
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10
    }
    if (selectedSpecies.value > 0) {
      params.speciesId = selectedSpecies.value
    }
    if (selectedStyle.value > 0) {
      params.styleId = selectedStyle.value
    }
    const data = await getPosts(params)
    if (data.content.length === 0 || data.content.length < 10) {
      hasMore.value = false
    }
    posts.value.push(...data.content)
    page.value++
  } catch (e) {
    console.error('加载帖子失败', e)
  } finally {
    loading.value = false
  }
}

const loadHotPosts = async () => {
  try {
    const data = await getHotPosts()
    posts.value = data
    hasMore.value = false
  } catch (e) {
    console.error('加载热门失败', e)
  }
}

const loadMore = () => {
  if (activeTab.value === 0) {
    loadPosts()
  }
}

const onRefresh = async () => {
  page.value = 0
  posts.value = []
  hasMore.value = true
  if (activeTab.value === 0) {
    await loadPosts()
  } else {
    await loadHotPosts()
  }
  refreshing.value = false
}

const goPublish = () => {
  router.push('/publish')
}

onMounted(() => {
  loadCategories()
  loadPosts()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.filter-bar {
  background: var(--color-bg-card);
  position: sticky;
  top: 46px;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.filter-buttons {
  display: flex;
  border-top: 1px solid var(--color-border);
}

.filter-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px 16px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  border-right: 1px solid var(--color-border);
}

.filter-btn:last-child {
  border-right: none;
}

.filter-btn.active {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

.filter-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-card);
}

.filter-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.filter-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.filter-popup-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.filter-option-group {
  margin-bottom: 20px;
}

.filter-option-group:last-child {
  margin-bottom: 0;
}

.group-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid var(--color-primary);
}

.group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.group-options :deep(.van-tag) {
  padding: 8px 16px;
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  border-width: 1px;
}

.group-options :deep(.van-tag--plain) {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-secondary);
}

.group-options :deep(.van-tag--primary.van-tag--plain) {
  background: var(--color-primary-light);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.tag-selected {
  background: var(--color-primary-light) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

.content {
  padding: var(--spacing-sm);
  padding-bottom: 60px;
}
</style>
