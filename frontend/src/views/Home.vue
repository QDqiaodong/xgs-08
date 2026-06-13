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
        <van-dropdown-menu>
          <van-dropdown-item v-model="selectedSpecies" :options="speciesOptions" @change="filterPosts" />
          <van-dropdown-item v-model="selectedStyle" :options="styleOptions" @change="filterPosts" />
        </van-dropdown-menu>
      </div>
    </div>

    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <Waterfall :posts="posts" :loading="loading" @load-more="loadMore" />
      </van-pull-refresh>
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
const speciesOptions = ref([{ text: '全部树种', value: 0 }])
const styleOptions = ref([{ text: '全部风格', value: 0 }])

const loadCategories = async () => {
  try {
    const [species, styles] = await Promise.all([
      getCategoriesByType('species'),
      getCategoriesByType('style')
    ])
    speciesOptions.value.push(...species.map(s => ({ text: s.name, value: s.id })))
    styleOptions.value.push(...styles.map(s => ({ text: s.name, value: s.id })))
  } catch (e) {
    console.error('加载分类失败', e)
  }
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
  background: #f5f5f5;
}

.filter-bar {
  background: #fff;
  position: sticky;
  top: 46px;
  z-index: 100;
}

.category-filter {
  border-top: 1px solid #ebedf0;
}

.content {
  padding: 8px;
  padding-bottom: 60px;
}
</style>
