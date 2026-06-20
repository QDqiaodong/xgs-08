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
            <div class="logs-by-type">
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
              <template v-else>
                <div v-for="section in typeSections" :key="section.type" class="type-section" v-show="logsByType[section.type].length > 0">
                  <div class="type-header" :style="{ borderLeftColor: section.color }">
                    <div class="type-title">
                      <van-icon :name="section.icon" size="18" :color="section.color" />
                      <span class="type-name">{{ section.name }}</span>
                      <span class="type-count">{{ logsByType[section.type].length }}条</span>
                    </div>
                  </div>
                  <div class="type-logs">
                    <div v-for="log in logsByType[section.type]" :key="log.id" class="log-card" :class="'log-' + log.logType">
                      <div v-if="log.logType === 'water'" class="log-highlight water-highlight">
                        <van-icon name="down" size="16" />
                        <span class="highlight-label">浇水日期</span>
                        <span class="highlight-value">{{ log.logDate }}</span>
                      </div>
                      <div v-else-if="log.logType === 'fertilize'" class="log-highlight fertilize-highlight">
                        <van-icon name="balance-o" size="16" />
                        <span class="highlight-label">肥料</span>
                        <span class="highlight-value">{{ log.fertilizer || '未指定' }}</span>
                      </div>
                      <div v-else-if="log.logType === 'prune'" class="log-highlight prune-highlight">
                        <van-icon name="scissors-o" size="16" />
                        <span class="highlight-label">修剪部位</span>
                        <span class="highlight-value">{{ log.position || '未指定' }}</span>
                      </div>
                      <div v-else-if="log.logType === 'repot'" class="log-highlight repot-highlight">
                        <van-icon name="exchange" size="16" />
                        <span class="highlight-label">盆土</span>
                        <span class="highlight-value">{{ log.soilType || '未指定' }}</span>
                      </div>
                      <div v-else class="log-highlight other-highlight">
                        <van-icon name="more-o" size="16" />
                        <span class="highlight-label">日期</span>
                        <span class="highlight-value">{{ log.logDate }}</span>
                      </div>
                      <h3 class="log-title" v-if="log.title">{{ log.title }}</h3>
                      <p class="log-content" v-if="log.content">{{ log.content }}</p>
                      <div class="log-footer">
                        <span class="log-date">
                          <van-icon name="clock-o" size="10" />
                          {{ log.logDate }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
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
                <div class="stat-item card repot">
                  <div class="stat-icon">
                    <van-icon name="exchange" size="24" color="#ee0a24" />
                  </div>
                  <div class="stat-value">{{ stats.repot }}</div>
                  <div class="stat-label">换盆</div>
                </div>
                <div class="stat-item card other">
                  <div class="stat-icon">
                    <van-icon name="more-o" size="24" color="#969799" />
                  </div>
                  <div class="stat-value">{{ stats.other }}</div>
                  <div class="stat-label">其他</div>
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

    <van-popup v-model:show="showAddDialog" position="bottom" round :style="{ height: '90%' }" @open="resetLogForm">
      <div class="quick-log-panel">
        <div class="panel-header">
          <div class="panel-title">
            <van-icon name="edit" size="18" color="#07c160" />
            <span>记录养护</span>
          </div>
          <van-icon name="cross" size="22" @click="showAddDialog = false" />
        </div>

        <div class="panel-content">
          <div class="type-quick-select">
            <div class="section-label">养护类型</div>
            <div class="type-grid">
              <div
                v-for="type in logTypes"
                :key="type.value"
                class="type-item"
                :class="{ active: logForm.logType === type.value }"
                @click="selectLogType(type.value)"
              >
                <div class="type-icon" :class="'icon-' + type.value">
                  <van-icon :name="type.icon" size="22" />
                </div>
                <span class="type-name">{{ type.text }}</span>
              </div>
            </div>
          </div>

          <div class="date-row">
            <div class="section-label">养护日期</div>
            <van-cell-group inset class="date-cell-group">
              <van-field
                v-model="logForm.logDate"
                type="date"
                placeholder="选择日期"
                :border="false"
              >
                <template #left-icon>
                  <van-icon name="calendar-o" size="18" color="#969799" />
                </template>
              </van-field>
            </van-cell-group>
          </div>

          <div class="bonsai-row">
            <div class="section-label">
              <span>关联盆景</span>
              <span class="optional-tip">（选填）</span>
            </div>
            <van-cell-group inset class="bonsai-cell-group">
              <van-field
                :model-value="selectedBonsaiName || '不关联'"
                is-link
                readonly
                :border="false"
                @click="showBonsaiPicker = true"
              >
                <template #left-icon>
                  <van-icon name="flower-o" size="18" color="#969799" />
                </template>
              </van-field>
            </van-cell-group>
          </div>

          <div v-if="logForm.logType === 'fertilize'" class="extra-field">
            <div class="section-label">肥料</div>
            <van-field
              v-model="logForm.fertilizer"
              placeholder="如：复合肥、有机肥..."
              :border="false"
            >
              <template #left-icon>
                <van-icon name="balance-o" size="18" color="#969799" />
              </template>
            </van-field>
          </div>
          <div v-else-if="logForm.logType === 'prune'" class="extra-field">
            <div class="section-label">修剪部位</div>
            <van-field
              v-model="logForm.position"
              placeholder="如：顶枝、侧枝..."
              :border="false"
            >
              <template #left-icon>
                <van-icon name="scissors-o" size="18" color="#969799" />
              </template>
            </van-field>
          </div>
          <div v-else-if="logForm.logType === 'repot'" class="extra-field">
            <div class="section-label">盆土</div>
            <van-field
              v-model="logForm.soilType"
              placeholder="如：腐殖土、赤玉土..."
              :border="false"
            >
              <template #left-icon>
                <van-icon name="exchange" size="18" color="#969799" />
              </template>
            </van-field>
          </div>

          <div class="photo-section">
            <div class="section-label">
              <span>现场照片</span>
              <span class="optional-tip">（选填）</span>
            </div>
            <van-uploader
              v-model="photoFileList"
              :max-count="3"
              :before-read="beforePhotoRead"
              :after-read="afterPhotoRead"
              upload-text="拍照"
              :preview-size="70"
            />
          </div>

          <div class="content-section">
            <div class="section-label">
              <span>护理要点</span>
              <span class="char-count">{{ logForm.content.length }}/200</span>
            </div>
            <van-field
              v-model="logForm.content"
              type="textarea"
              placeholder="记录本次养护的关键要点..."
              :autosize="{ minHeight: 80, maxHeight: 120 }"
              maxlength="200"
              show-word-limit
              :border="false"
              class="content-textarea"
            />
          </div>

          <div class="quick-tips">
            <div class="tips-title">
              <van-icon name="bulb-o" size="12" color="#ff976a" />
              <span>快捷标签</span>
            </div>
            <div class="tips-tags">
              <van-tag
                v-for="tip in quickTips"
                :key="tip"
                size="medium"
                plain
                type="primary"
                @click="appendTip(tip)"
              >
                {{ tip }}
              </van-tag>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <van-button round block type="primary" size="large" @click="submitLog" :loading="submitting">
            <van-icon name="checked" />
            完成记录
          </van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showBonsaiPicker" position="bottom">
      <van-picker
        :columns="[{ text: '不关联', value: null }, ...bonsaiList.map(b => ({ text: b.name, value: b.id }))]"
        title="选择关联盆景"
        @confirm="onBonsaiConfirm"
        @cancel="showBonsaiPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { showToast } from 'vant'
import { getUserCareLogs, createCareLog } from '@/api/careLog'
import { getUserBonsaiList } from '@/api/bonsai'
import { useUserStore } from '@/stores/user'
import { getCareTips, groupLogsBySolarTerm, SEASON_ORDER } from '@/utils/solarTerms'
import { validateImageFile, uploadSingleImage } from '@/api/upload'

const userStore = useUserStore()

const activeTab = ref(0)
const activeFooter = ref(3)
const loading = ref(false)
const careLogs = ref([])
const showAddDialog = ref(false)
const showTypePicker = ref(false)
const showBonsaiPicker = ref(false)
const selectedSeason = ref('all')
const bonsaiList = ref([])
const submitting = ref(false)
const photoFileList = ref([])

const logForm = reactive({
  logType: 'water',
  title: '',
  content: '',
  logDate: new Date().toISOString().split('T')[0],
  fertilizer: '',
  position: '',
  soilType: '',
  bonsaiId: null,
  images: ''
})

const selectedBonsaiName = ref('')

const logTypes = [
  { text: '浇水', value: 'water', icon: 'down' },
  { text: '施肥', value: 'fertilize', icon: 'balance-o' },
  { text: '修剪', value: 'prune', icon: 'scissors-o' },
  { text: '换盆', value: 'repot', icon: 'exchange' },
  { text: '其他', value: 'other', icon: 'more-o' }
]

const quickTips = computed(() => {
  const tipsMap = {
    water: ['浇透', '叶面喷水', '见干见湿', '避正午'],
    fertilize: ['薄肥勤施', '复合肥', '有机肥', '叶面肥'],
    prune: ['摘心', '疏枝', '造型修剪', '剪病枝'],
    repot: ['修根', '换土', '消毒', '缓苗'],
    other: ['观察记录', '病虫害防治', '清理盆面', '转盆']
  }
  return tipsMap[logForm.logType] || tipsMap.other
})

const stats = computed(() => {
  const result = { total: careLogs.value.length, water: 0, fertilize: 0, prune: 0, repot: 0, other: 0 }
  careLogs.value.forEach(log => {
    if (result.hasOwnProperty(log.logType)) {
      result[log.logType]++
    } else {
      result.other++
    }
  })
  return result
})

const groupedLogs = computed(() => {
  return groupLogsBySolarTerm(careLogs.value)
})

const logsByType = computed(() => {
  const groups = {
    water: [],
    fertilize: [],
    prune: [],
    repot: [],
    other: []
  }
  careLogs.value.forEach(log => {
    if (groups.hasOwnProperty(log.logType)) {
      groups[log.logType].push(log)
    } else {
      groups.other.push(log)
    }
  })
  return groups
})

const typeSections = [
  { type: 'water', name: '浇水', icon: 'down', color: '#1989fa' },
  { type: 'fertilize', name: '施肥', icon: 'balance-o', color: '#07c160' },
  { type: 'prune', name: '修剪', icon: 'scissors-o', color: '#ff976a' },
  { type: 'repot', name: '换盆', icon: 'exchange', color: '#ee0a24' },
  { type: 'other', name: '其他', icon: 'more-o', color: '#969799' }
]

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

const selectLogType = (type) => {
  logForm.logType = type
  logForm.fertilizer = ''
  logForm.position = ''
  logForm.soilType = ''
}

const appendTip = (tip) => {
  if (logForm.content.length + tip.length + 1 > 200) {
    showToast('内容已达上限')
    return
  }
  if (logForm.content && !logForm.content.endsWith('，') && !logForm.content.endsWith('、')) {
    logForm.content += '、'
  }
  logForm.content += tip
}

const beforePhotoRead = (file) => {
  return validateImageFile(file)
}

const afterPhotoRead = async (file) => {
  file.status = 'uploading'
  file.message = '上传中...'
  try {
    await uploadSingleImage(file)
    file.status = 'done'
    file.message = ''
  } catch (e) {
    file.status = 'failed'
    file.message = '上传失败'
    showToast(e?.response?.data?.message || e?.message || '图片上传失败')
  }
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

const loadBonsaiList = async () => {
  try {
    bonsaiList.value = await getUserBonsaiList(userStore.currentUser.id)
  } catch (e) {
    console.warn('加载盆景列表失败', e)
  }
}

const getSelectedBonsai = () => {
  if (!logForm.bonsaiId) return null
  return bonsaiList.value.find(b => b.id === logForm.bonsaiId)
}

const validateLogDate = () => {
  if (!logForm.logDate) {
    showToast('养护日期不能为空')
    return false
  }

  const logDate = new Date(logForm.logDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxFutureDate = new Date(today)
  maxFutureDate.setDate(maxFutureDate.getDate() + 30)

  if (logDate > maxFutureDate) {
    showToast('养护日期不能超过未来30天')
    return false
  }

  const selectedBonsai = getSelectedBonsai()
  if (selectedBonsai && selectedBonsai.acquireDate) {
    const acquireDate = new Date(selectedBonsai.acquireDate)
    if (logDate < acquireDate) {
      showToast(`养护日期不能早于盆景入手日期（${selectedBonsai.acquireDate}）`)
      return false
    }
  }

  return true
}

const submitLog = async () => {
  if (!logForm.content.trim()) {
    showToast('请输入护理要点')
    return
  }
  if (logForm.logType === 'fertilize' && !logForm.fertilizer.trim()) {
    showToast('请输入肥料')
    return
  }
  if (logForm.logType === 'prune' && !logForm.position.trim()) {
    showToast('请输入修剪部位')
    return
  }
  if (logForm.logType === 'repot' && !logForm.soilType.trim()) {
    showToast('请输入盆土')
    return
  }
  if (!validateLogDate()) {
    return
  }

  const uploadingFile = photoFileList.value.find(f => f.status === 'uploading')
  if (uploadingFile) {
    showToast('图片正在上传中，请稍候')
    return
  }
  const failedFile = photoFileList.value.find(f => f.status === 'failed')
  if (failedFile) {
    showToast('存在上传失败的图片，请重新上传')
    return
  }

  submitting.value = true
  try {
    const images = photoFileList.value
      .filter(f => f.status === 'done' && f.uploadedUrl)
      .map(f => f.uploadedUrl)
      .join(',')

    await createCareLog({
      userId: userStore.currentUser.id,
      ...logForm,
      images
    })
    showAddDialog.value = false
    showToast('记录成功')
    await loadLogs()
  } catch (e) {
    showToast(e?.message || '记录失败')
  } finally {
    submitting.value = false
  }
}

const onBonsaiConfirm = ({ selectedOptions }) => {
  if (selectedOptions && selectedOptions.length > 0) {
    logForm.bonsaiId = selectedOptions[0].value
    selectedBonsaiName.value = selectedOptions[0].text
  }
  showBonsaiPicker.value = false
}

const resetLogForm = () => {
  logForm.logType = 'water'
  logForm.title = ''
  logForm.content = ''
  logForm.logDate = new Date().toISOString().split('T')[0]
  logForm.fertilizer = ''
  logForm.position = ''
  logForm.soilType = ''
  logForm.bonsaiId = null
  logForm.images = ''
  selectedBonsaiName.value = ''
  photoFileList.value = []
}

onMounted(() => {
  loadLogs()
  loadBonsaiList()
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

.logs-by-type {
  padding: var(--spacing-md) 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.type-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.type-header {
  padding-left: var(--spacing-sm);
  border-left: 3px solid;
}

.type-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.type-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.type-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  background: var(--color-bg-secondary);
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  margin-left: var(--spacing-xs);
}

.type-logs {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  padding-left: var(--spacing-xs);
}

.log-card {
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  border-left: 3px solid transparent;
}

.log-card.log-water {
  border-left-color: #1989fa;
}

.log-card.log-fertilize {
  border-left-color: #07c160;
}

.log-card.log-prune {
  border-left-color: #ff976a;
}

.log-card.log-repot {
  border-left-color: #ee0a24;
}

.log-card.log-other {
  border-left-color: #969799;
}

.log-highlight {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-sm);
}

.highlight-label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.highlight-value {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  flex: 1;
}

.water-highlight {
  background: rgba(25, 137, 250, 0.1);
  color: #1989fa;
}

.water-highlight .highlight-value {
  color: #1989fa;
}

.fertilize-highlight {
  background: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.fertilize-highlight .highlight-value {
  color: #07c160;
}

.prune-highlight {
  background: rgba(255, 151, 106, 0.1);
  color: #ff976a;
}

.prune-highlight .highlight-value {
  color: #ff976a;
}

.repot-highlight {
  background: rgba(238, 10, 36, 0.1);
  color: #ee0a24;
}

.repot-highlight .highlight-value {
  color: #ee0a24;
}

.other-highlight {
  background: rgba(150, 151, 153, 0.1);
  color: #969799;
}

.other-highlight .highlight-value {
  color: #969799;
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
  margin-bottom: var(--spacing-sm);
}

.log-footer {
  display: flex;
  justify-content: flex-end;
}

.log-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 2px;
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
  grid-template-columns: repeat(3, 1fr);
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

.stat-item.repot .stat-icon {
  background: rgba(238, 10, 36, 0.1);
}

.stat-item.repot .stat-value {
  color: #ee0a24;
}

.stat-item.other .stat-icon {
  background: rgba(150, 151, 153, 0.1);
}

.stat-item.other .stat-value {
  color: #969799;
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

.quick-log-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-page);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.section-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
  padding-left: var(--spacing-xs);
}

.optional-tip {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-normal);
}

.char-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-normal);
}

.type-quick-select {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--spacing-xs);
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-xs);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--color-bg-secondary);
}

.type-item:active {
  transform: scale(0.95);
}

.type-item.active {
  background: var(--color-primary-light);
}

.type-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.type-item.active .type-icon {
  background: var(--color-primary);
  color: #fff;
}

.type-icon.icon-water {
  background: rgba(25, 137, 250, 0.1);
  color: #1989fa;
}

.type-item.active .type-icon.icon-water {
  background: #1989fa;
  color: #fff;
}

.type-icon.icon-fertilize {
  background: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.type-item.active .type-icon.icon-fertilize {
  background: #07c160;
  color: #fff;
}

.type-icon.icon-prune {
  background: rgba(255, 151, 106, 0.1);
  color: #ff976a;
}

.type-item.active .type-icon.icon-prune {
  background: #ff976a;
  color: #fff;
}

.type-icon.icon-repot {
  background: rgba(238, 10, 36, 0.1);
  color: #ee0a24;
}

.type-item.active .type-icon.icon-repot {
  background: #ee0a24;
  color: #fff;
}

.type-icon.icon-other {
  background: rgba(150, 151, 153, 0.1);
  color: #969799;
}

.type-item.active .type-icon.icon-other {
  background: #969799;
  color: #fff;
}

.type-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.type-item.active .type-name {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.date-row,
.bonsai-row {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm) 0;
}

.date-cell-group,
.bonsai-cell-group {
  background: transparent !important;
}

.extra-field {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm) var(--spacing-md);
}

.photo-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.content-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.content-textarea {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
}

.quick-tips {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
}

.tips-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.tips-tags :deep(.van-tag) {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tips-tags :deep(.van-tag:active) {
  transform: scale(0.95);
}

.panel-footer {
  padding: var(--spacing-md);
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  flex-shrink: 0;
}

.panel-footer :deep(.van-button) {
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.3);
}
</style>
