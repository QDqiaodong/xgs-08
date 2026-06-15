<template>
  <div class="care-logs-page">
    <van-nav-bar title="养护日志" fixed placeholder>
      <template #right>
        <van-button type="primary" size="small" @click="showAddDialog = true">记录</van-button>
      </template>
    </van-nav-bar>

    <div class="content">
      <van-tabs v-model:active="activeTab">
        <van-tab title="全部记录">
          <div class="logs-list">
            <div v-for="log in careLogs" :key="log.id" class="log-card">
              <div class="log-header">
                <van-tag :type="getLogTypeTag(log.logType)" size="medium">{{ getLogTypeName(log.logType) }}</van-tag>
                <span class="log-date">{{ log.logDate }}</span>
              </div>
              <h3 class="log-title" v-if="log.title">{{ log.title }}</h3>
              <p class="log-content" v-if="log.content">{{ log.content }}</p>
            </div>
            <van-empty v-if="careLogs.length === 0 && !loading" description="暂无养护记录" />
            <div v-if="loading" class="loading-wrapper">
              <van-loading size="24px">加载中...</van-loading>
            </div>
          </div>
        </van-tab>
        <van-tab title="统计">
          <div class="stats-section">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ stats.total }}</div>
                <div class="stat-label">总记录</div>
              </div>
              <div class="stat-item water">
                <div class="stat-value">{{ stats.water }}</div>
                <div class="stat-label">浇水</div>
              </div>
              <div class="stat-item fertilize">
                <div class="stat-value">{{ stats.fertilize }}</div>
                <div class="stat-label">施肥</div>
              </div>
              <div class="stat-item prune">
                <div class="stat-value">{{ stats.prune }}</div>
                <div class="stat-label">修剪</div>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <van-tabbar v-model:active="activeFooter" route fixed placeholder>
      <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/bonsais" icon="flower-o">盆景</van-tabbar-item>
      <van-tabbar-item to="/publish" icon="plus">发布</van-tabbar-item>
      <van-tabbar-item to="/care-logs" icon="notes-o">养护</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>

    <van-dialog v-model:show="showAddDialog" title="添加养护记录" show-cancel-button @confirm="submitLog">
      <van-form>
        <van-field
          v-model="logForm.logType"
          label="类型"
          placeholder="请选择养护类型"
          is-link
          readonly
          @click="showTypePicker = true"
        />
        <van-field
          v-model="logForm.title"
          label="标题"
          placeholder="请输入标题（选填）"
        />
        <van-field
          v-model="logForm.content"
          label="内容"
          type="textarea"
          placeholder="记录养护详情..."
          :autosize="{ minHeight: 80 }"
        />
        <van-field
          v-model="logForm.logDate"
          label="日期"
          type="date"
          placeholder="选择日期"
        />
      </van-form>
    </van-dialog>

    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="logTypes"
        title="选择养护类型"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { getUserCareLogs, createCareLog } from '@/api/careLog'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref(0)
const activeFooter = ref(3)
const loading = ref(false)
const careLogs = ref([])
const showAddDialog = ref(false)
const showTypePicker = ref(false)

const logForm = reactive({
  logType: 'water',
  title: '',
  content: '',
  logDate: new Date().toISOString().split('T')[0]
})

const logTypes = [
  { text: '浇水', value: 'water' },
  { text: '施肥', value: 'fertilize' },
  { text: '修剪', value: 'prune' },
  { text: '换盆', value: 'repot' },
  { text: '其他', value: 'other' }
]

const selectedTypeName = ref('浇水')

const stats = computed(() => {
  const result = { total: careLogs.value.length, water: 0, fertilize: 0, prune: 0 }
  careLogs.value.forEach(log => {
    if (result.hasOwnProperty(log.logType)) {
      result[log.logType]++
    }
  })
  return result
})

const getLogTypeName = (type) => {
  const found = logTypes.find(t => t.value === type)
  return found ? found.text : type
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

const onTypeConfirm = ({ selectedOptions }) => {
  logForm.logType = selectedOptions[0].value
  selectedTypeName.value = selectedOptions[0].text
  showTypePicker.value = false
}

const loadLogs = async () => {
  loading.value = true
  try {
    const data = await getUserCareLogs(userStore.currentUser.id, { page: 0, size: 50 })
    careLogs.value = data.content || []
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const submitLog = async () => {
  if (!logForm.content.trim()) {
    showToast('请输入养护内容')
    return
  }
  try {
    await createCareLog({
      userId: userStore.currentUser.id,
      ...logForm
    })
    showToast('记录成功')
    showAddDialog.value = false
    logForm.title = ''
    logForm.content = ''
    loadLogs()
  } catch (e) {
    showToast('记录失败')
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.care-logs-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.content {
  padding: 10px;
}

.logs-list {
  padding: 10px 0;
}

.log-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.log-date {
  font-size: 12px;
  color: #999;
}

.log-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.log-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.stats-section {
  padding: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.stat-item.water .stat-value {
  color: #1989fa;
}

.stat-item.fertilize .stat-value {
  color: #07c160;
}

.stat-item.prune .stat-value {
  color: #ff976a;
}
</style>
