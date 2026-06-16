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
          <van-tab title="节气视图">
            <div class="solar-term-view">
              <div v-if="loading" class="loading-wrapper">
                <van-loading size="24px">加载中...</van-loading>
              </div>
              <div v-else-if="careLogs.length === 0" class="empty-state">
                <van-icon name="notes-o" class="empty-icon" />
                <span class="empty-text">暂无养护记录</span>
                <van-button type="primary" size="small" @click="showAddDialog = true" style="margin-top: 16px;">
                  <van-icon name="plus" />记录养护
                </van-button>
              </div>
              <div v-else>
                <div class="season-filter">
                  <van-radio-group v-model="selectedSeason" direction="horizontal">
                    <van-radio name="all">全部</van-radio>
                    <van-radio name="spring">春季</van-radio>
                    <van-radio name="summer">夏季</van-radio>
                    <van-radio name="autumn">秋季</van-radio>
                    <van-radio name="winter">冬季</van-radio>
                  </van-radio-group>
                </div>
                <div v-for="season in displaySeasons" :key="season" class="season-section">
                  <div class="season-header" :style="{ background: groupedLogs[season].seasonInfo.bgColor, color: groupedLogs[season].seasonInfo.color }">
                    <span class="season-icon">{{ groupedLogs[season].seasonInfo.icon }}</span>
                    <span class="season-name">{{ groupedLogs[season].seasonInfo.name }}</span>
                    <span class="season-count">{{ getSeasonLogCount(groupedLogs[season]) }}条记录</span>
                  </div>
                  <div class="terms-container">
                    <div v-for="(term, termName) in groupedLogs[season].terms" :key="termName" class="term-card" :class="{ 'has-logs': term.logs.length > 0 }">
                      <div class="term-header" :style="{ borderLeftColor: groupedLogs[season].seasonInfo.color }">
                        <span class="term-name">{{ termName }}</span>
                        <span class="term-date">{{ String(term.month).padStart(2, '0') }}-{{ String(term.day).padStart(2, '0') }}</span>
                      </div>
                      <div v-if="term.logs.length > 0" class="term-logs">
                        <div v-for="log in term.logs" :key="log.id" class="term-log-item">
                          <van-tag :type="getLogTypeTag(log.logType)" size="small">{{ getLogTypeName(log.logType) }}</van-tag>
                          <span class="term-log-content">{{ log.content || log.title }}</span>
                          <span class="term-log-date">{{ log.logDate }}</span>
                        </div>
                      </div>
                      <div v-else class="term-empty">
                        <van-icon name="clock-o" size="12" />
                        <span>暂无记录</span>
                      </div>
                      <div v-if="term.logs.length > 0" class="term-tips">
                        <div class="tip-header">
                          <van-icon name="info-o" size="12" />
                          <span>{{ termName }}养护建议</span>
                        </div>
                        <div class="tip-content">
                          <div v-if="getCareTips(termName).water" class="tip-item">
                            <van-icon name="down" size="12" />
                            <span>浇水: {{ getCareTips(termName).water }}</span>
                          </div>
                          <div v-if="getCareTips(termName).fertilize" class="tip-item">
                            <van-icon name="balance-o" size="12" />
                            <span>施肥: {{ getCareTips(termName).fertilize }}</span>
                          </div>
                          <div v-if="getCareTips(termName).prune" class="tip-item">
                            <van-icon name="scissors-o" size="12" />
                            <span>修剪: {{ getCareTips(termName).prune }}</span>
                          </div>
                          <div v-if="getCareTips(termName).repot" class="tip-item">
                            <van-icon name="exchange" size="12" />
                            <span>换盆: {{ getCareTips(termName).repot }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
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
import { getCareTips, groupLogsBySolarTerm, SEASON_ORDER } from '@/utils/solarTerms'

const userStore = useUserStore()

const activeTab = ref(0)
const activeFooter = ref(3)
const loading = ref(false)
const careLogs = ref([])
const showAddDialog = ref(false)
const showTypePicker = ref(false)
const selectedSeason = ref('all')

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
  const result = { total: careLogs.value.length, water: 0, fertilize: 0, prune: 0, repot: 0, other: 0 }
  careLogs.value.forEach(log => {
    if (result.hasOwnProperty(log.logType)) {
      result[log.logType]++
    }
  })
  return result
})

const groupedLogs = computed(() => {
  return groupLogsBySolarTerm(careLogs.value)
})

const displaySeasons = computed(() => {
  if (selectedSeason.value === 'all') {
    return SEASON_ORDER
  }
  return [selectedSeason.value]
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

const getSeasonLogCount = (seasonData) => {
  let count = 0
  Object.values(seasonData.terms).forEach(term => {
    count += term.logs.length
  })
  return count
}

const onTypeConfirm = ({ selectedOptions }) => {
  logForm.logType = selectedOptions[0].value
  selectedTypeName.value = selectedOptions[0].text
  showTypePicker.value = false
}

const loadLogs = async () => {
  loading.value = true
  try {
    const data = await getUserCareLogs(userStore.currentUser.id, { page: 0, size: 100 })
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

.solar-term-view {
  padding: var(--spacing-md) 0;
}

.season-filter {
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  margin-bottom: var(--spacing-md);
}

.season-filter :deep(.van-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.season-filter :deep(.van-radio) {
  flex: 1;
  min-width: 60px;
  justify-content: center;
}

.season-section {
  margin-bottom: var(--spacing-lg);
}

.season-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-lg);
}

.season-icon {
  font-size: var(--font-size-xl);
}

.season-name {
  flex: 1;
}

.season-count {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-normal);
  opacity: 0.8;
}

.terms-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-bg-tertiary);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
}

.term-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.term-card.has-logs {
  border-color: var(--color-border);
  box-shadow: var(--shadow-sm);
}

.term-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  padding-left: var(--spacing-sm);
  border-left: 3px solid;
}

.term-name {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
}

.term-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  background: var(--color-bg-secondary);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
}

.term-logs {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.term-log-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.term-log-content {
  flex: 1;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.term-log-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

.term-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
  color: var(--color-text-placeholder);
  font-size: var(--font-size-sm);
}

.term-tips {
  margin-top: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-primary);
}

.tip-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  margin-bottom: var(--spacing-xs);
}

.tip-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-xs);
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.tip-item .van-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--color-primary);
}

@media (min-width: 600px) {
  .terms-container {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
