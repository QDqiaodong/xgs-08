<template>
  <div class="care-logs-page page-container">
    <van-nav-bar title="养护日志" fixed placeholder>
      <template #right>
        <van-button type="primary" size="small" @click="showAddDialog = true">记录</van-button>
      </template>
    </van-nav-bar>

    <div class="content-wrapper">
      <div class="content card">
        <van-tabs v-model:active="activeTab">
          <van-tab title="全部记录">
            <div class="logs-list">
              <div v-for="log in careLogs" :key="log.id" class="log-card">
                <div class="log-header">
                  <van-tag :type="getLogTypeTag(log.logType)" size="small">{{ getLogTypeName(log.logType) }}</van-tag>
                  <span class="log-date">
                    <van-icon name="clock-o" size="10" />
                    {{ log.logDate }}
                  </span>
                </div>
                <h3 class="log-title" v-if="log.title">{{ log.title }}</h3>
                <p class="log-content" v-if="log.content">{{ log.content }}</p>
              </div>
              <div v-if="careLogs.length === 0 && !loading" class="empty-state">
                <van-icon name="notes-o" class="empty-icon" />
                <span class="empty-text">暂无养护记录</span>
                <van-button type="primary" size="small" @click="showAddDialog = true" style="margin-top: 16px;">
                  <van-icon name="plus" />记录养护
                </van-button>
              </div>
              <div v-if="loading" class="loading-wrapper">
                <van-loading size="24px">加载中...</van-loading>
              </div>
            </div>
          </van-tab>
          <van-tab title="统计">
            <div class="stats-section">
              <div class="stats-grid">
                <div class="stat-item card">
                  <div class="stat-icon">
                    <van-icon name="orders-o" size="24" color="#323233" />
                  </div>
                  <div class="stat-value">{{ stats.total }}</div>
                  <div class="stat-label">总记录</div>
                </div>
                <div class="stat-item card water">
                  <div class="stat-icon">
                    <van-icon name="down" size="24" color="#1989fa" />
                  </div>
                  <div class="stat-value">{{ stats.water }}</div>
                  <div class="stat-label">浇水</div>
                </div>
                <div class="stat-item card fertilize">
                  <div class="stat-icon">
                    <van-icon name="balance-o" size="24" color="#07c160" />
                  </div>
                  <div class="stat-value">{{ stats.fertilize }}</div>
                  <div class="stat-label">施肥</div>
                </div>
                <div class="stat-item card prune">
                  <div class="stat-icon">
                    <van-icon name="scissors-o" size="24" color="#ff976a" />
                  </div>
                  <div class="stat-value">{{ stats.prune }}</div>
                  <div class="stat-label">修剪</div>
                </div>
              </div>
            </div>
          </van-tab>
        </van-tabs>
      </div>
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
  padding-bottom: 60px;
}

.content-wrapper {
  padding: var(--spacing-md);
}

.content {
  overflow: hidden;
}

.logs-list {
  padding: var(--spacing-md) 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.log-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.log-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 2px;
}

.log-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.log-content {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: var(--line-height-lg);
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--spacing-3xl);
}

.stats-section {
  padding: var(--spacing-md);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.stat-item {
  padding: var(--spacing-lg);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-xs);
}

.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

.stat-item.water .stat-icon {
  background: rgba(25, 137, 250, 0.1);
}

.stat-item.water .stat-value {
  color: #1989fa;
}

.stat-item.fertilize .stat-icon {
  background: rgba(7, 193, 96, 0.1);
}

.stat-item.fertilize .stat-value {
  color: #07c160;
}

.stat-item.prune .stat-icon {
  background: rgba(255, 151, 106, 0.1);
}

.stat-item.prune .stat-value {
  color: #ff976a;
}
</style>
