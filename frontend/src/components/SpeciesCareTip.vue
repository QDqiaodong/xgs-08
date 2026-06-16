<template>
  <div v-if="careData" class="species-care-tip card" :style="{ borderTopColor: careData.themeColor }">
    <div class="care-header" @click="toggleExpand">
      <div class="care-title">
        <span class="species-icon">{{ careData.icon }}</span>
        <div class="title-text">
          <h3 class="species-name">{{ careData.name }}护理指南</h3>
          <span class="scientific-name">{{ careData.scientificName }}</span>
        </div>
      </div>
      <van-icon :name="expanded ? 'arrow-up' : 'arrow-down'" class="toggle-icon" />
    </div>

    <div v-if="expanded" class="care-content">
      <div class="care-section light-section">
        <div class="section-header">
          <div class="section-icon">
            <van-icon name="sun-o" />
          </div>
          <div class="section-info">
            <span class="section-label">光照要求</span>
            <van-tag :color="getLightTagColor(careData.light.level)" size="small">{{ careData.light.level }}</van-tag>
          </div>
        </div>
        <p class="section-desc">{{ careData.light.description }}</p>
        <ul class="section-tips">
          <li v-for="(tip, idx) in careData.light.tips" :key="idx">
            <van-icon name="passed" size="12" />
            <span>{{ tip }}</span>
          </li>
        </ul>
      </div>

      <div class="care-section water-section">
        <div class="section-header">
          <div class="section-icon water-icon">
            <van-icon name="down" />
          </div>
          <div class="section-info">
            <span class="section-label">水分管理</span>
            <van-tag :color="getWaterTagColor(careData.water.level)" size="small">{{ careData.water.level }}</van-tag>
          </div>
        </div>
        <p class="section-desc">{{ careData.water.description }}</p>
        <ul class="section-tips">
          <li v-for="(tip, idx) in careData.water.tips" :key="idx">
            <van-icon name="passed" size="12" />
            <span>{{ tip }}</span>
          </li>
        </ul>
      </div>

      <div class="care-section pruning-section">
        <div class="section-header">
          <div class="section-icon pruning-icon">
            <van-icon name="scissors-o" />
          </div>
          <div class="section-info">
            <span class="section-label">修剪指南</span>
            <van-tag :color="getPruningTagColor(careData.pruning.sensitivity)" size="small">
              敏感度: {{ careData.pruning.sensitivity }}
            </van-tag>
          </div>
        </div>
        <div class="best-time">
          <van-icon name="clock-o" size="12" />
          <span>最佳修剪期: {{ careData.pruning.bestTime }}</span>
        </div>
        <p class="section-desc">{{ careData.pruning.description }}</p>
        <ul class="section-tips">
          <li v-for="(tip, idx) in careData.pruning.tips" :key="idx">
            <van-icon name="passed" size="12" />
            <span>{{ tip }}</span>
          </li>
        </ul>
      </div>

      <div class="care-section special-section">
        <div class="section-header">
          <div class="section-icon special-icon">
            <van-icon name="info-o" />
          </div>
          <span class="section-label">{{ careData.special.title }}</span>
        </div>
        <ul class="section-tips special-tips">
          <li v-for="(point, idx) in careData.special.points" :key="idx">
            <span class="tip-number">{{ idx + 1 }}</span>
            <span>{{ point }}</span>
          </li>
        </ul>
      </div>
    </div>

    <div v-else class="care-summary">
      <div class="summary-item">
        <van-icon name="sun-o" size="16" />
        <span>{{ careData.light.level }}</span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <van-icon name="down" size="16" />
        <span>{{ careData.water.level }}</span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <van-icon name="scissors-o" size="16" />
        <span>修剪{{ careData.pruning.sensitivity }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getSpeciesCare } from '@/utils/speciesCare'

const props = defineProps({
  speciesName: {
    type: String,
    required: true
  },
  defaultExpanded: {
    type: Boolean,
    default: false
  }
})

const careData = computed(() => {
  return getSpeciesCare(props.speciesName)
})

const expanded = ref(props.defaultExpanded)

const toggleExpand = () => {
  expanded.value = !expanded.value
}

const getLightTagColor = (level) => {
  const colors = {
    '强光': '#ff976a',
    '充足光照': '#07c160',
    '充足散射光': '#1989fa',
    '半阴到全日照': '#7232dd',
    '全日照': '#ff6034'
  }
  return colors[level] || '#07c160'
}

const getWaterTagColor = (level) => {
  const colors = {
    '见干见湿': '#07c160',
    '保持湿润': '#1989fa',
    '充足水分': '#576b95',
    '偏干养护': '#969799'
  }
  return colors[level] || '#1989fa'
}

const getPruningTagColor = (sensitivity) => {
  const colors = {
    '低': '#07c160',
    '中等': '#ff976a',
    '较高': '#ee0a24',
    '高': '#c0392b'
  }
  return colors[sensitivity] || '#969799'
}
</script>

<style scoped>
.species-care-tip {
  border-top: 4px solid;
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  background: linear-gradient(135deg, #ffffff 0%, #f8faf9 100%);
}

.care-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: var(--spacing-xs) 0;
}

.care-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.species-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.title-text {
  display: flex;
  flex-direction: column;
}

.species-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.4;
}

.scientific-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-style: italic;
}

.toggle-icon {
  font-size: 18px;
  color: var(--color-text-tertiary);
  transition: transform var(--transition-base);
}

.care-content {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px dashed var(--color-border);
}

.care-section {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
}

.care-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.section-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.water-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.pruning-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.special-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.section-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.section-label {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.section-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-lg);
  margin: var(--spacing-sm) 0;
  padding-left: 44px;
}

.section-tips {
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 44px;
}

.section-tips li {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-lg);
  margin-bottom: var(--spacing-xs);
}

.section-tips li:last-child {
  margin-bottom: 0;
}

.section-tips .van-icon {
  color: var(--color-success);
  flex-shrink: 0;
  margin-top: 2px;
}

.best-time {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-xs);
  color: var(--color-warning);
  font-weight: var(--font-weight-medium);
  padding-left: 44px;
  margin-bottom: var(--spacing-xs);
}

.special-tips {
  padding-left: 44px;
}

.special-tips li {
  align-items: flex-start;
}

.tip-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
  margin-top: 1px;
}

.care-summary {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: var(--spacing-sm) 0;
  margin-top: var(--spacing-sm);
}

.summary-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.summary-item .van-icon {
  color: var(--color-primary);
}

.summary-divider {
  width: 1px;
  height: 20px;
  background: var(--color-border);
}

.light-section .section-label {
  color: #ff976a;
}

.water-section .section-label {
  color: #1989fa;
}

.pruning-section .section-label {
  color: #07c160;
}

.special-section .section-label {
  color: #ee0a24;
}
</style>
