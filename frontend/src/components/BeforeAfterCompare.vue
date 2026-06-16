<template>
  <div class="before-after-compare" :class="{ 'is-comparing': isComparing }">
    <div v-if="mode === 'slider'" class="compare-slider-wrapper">
      <div class="compare-container" ref="compareRef">
        <div class="image-layer after-layer">
          <img :src="resolvedAfterImage" :alt="afterLabel" class="compare-image" @error="onAfterImageError" />
          <div class="image-label after-label">
            <van-tag type="primary" size="medium">{{ afterLabel }}</van-tag>
          </div>
        </div>
        
        <div class="image-layer before-layer" :style="{ clipPath: `inset(0 ${100 - sliderPosition}% 0 0)` }">
          <img :src="resolvedBeforeImage" :alt="beforeLabel" class="compare-image" @error="onBeforeImageError" />
          <div class="image-label before-label">
            <van-tag type="warning" size="medium">{{ beforeLabel }}</van-tag>
          </div>
        </div>
        
        <div 
          class="slider-handle" 
          :style="{ left: `${sliderPosition}%` }"
          @mousedown="startDrag"
          @touchstart="startDrag"
        >
          <div class="slider-line"></div>
          <div class="slider-button">
            <van-icon name="arrow-left" />
            <van-icon name="arrow-right" />
          </div>
        </div>
      </div>
      
      <div class="compare-controls">
        <div class="control-hint">
          <van-icon name="swap" />
          <span>拖动滑块对比造型变化</span>
        </div>
        <van-button size="small" type="default" @click="toggleMode">
          <van-icon name="apps-o" />
          并排对照
        </van-button>
      </div>
    </div>
    
    <div v-else class="compare-side-by-side">
      <div class="side-by-side-content">
        <div class="side-item">
          <div class="side-image-wrapper">
            <img :src="resolvedBeforeImage" :alt="beforeLabel" class="side-image" @error="onBeforeImageError" />
            <div class="side-label before-side-label">
              <van-tag type="warning" size="medium">{{ beforeLabel }}</van-tag>
            </div>
          </div>
          <div class="side-caption">
            <van-icon name="clock-o" />
            <span>造型前</span>
          </div>
        </div>
        
        <div class="compare-divider">
          <div class="divider-arrow">
            <van-icon name="arrow" />
          </div>
          <div class="divider-text">{{ transformationType }}</div>
        </div>
        
        <div class="side-item">
          <div class="side-image-wrapper">
            <img :src="resolvedAfterImage" :alt="afterLabel" class="side-image" @error="onAfterImageError" />
            <div class="side-label after-side-label">
              <van-tag type="primary" size="medium">{{ afterLabel }}</van-tag>
            </div>
          </div>
          <div class="side-caption">
            <van-icon name="completed" />
            <span>造型后</span>
          </div>
        </div>
      </div>
      
      <div class="compare-controls side-controls">
        <van-button size="small" type="primary" @click="toggleMode">
          <van-icon name="swap" />
          滑动对照
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { BONSAI_PLACEHOLDER_SVG, getImageWithFallback } from '@/utils/image'

const props = defineProps({
  beforeImage: {
    type: String,
    required: true
  },
  afterImage: {
    type: String,
    required: true
  },
  beforeLabel: {
    type: String,
    default: '修剪前'
  },
  afterLabel: {
    type: String,
    default: '修剪后'
  },
  eventType: {
    type: String,
    default: 'pruning'
  }
})

const sliderPosition = ref(50)
const isComparing = ref(false)
const mode = ref('slider')
const compareRef = ref(null)
const beforeImageError = ref(false)
const afterImageError = ref(false)

const transformationType = computed(() => {
  const types = {
    pruning: '修剪',
    wiring: '蟠扎',
    repotting: '换盆',
    planting: '定植',
    acquire: '入手',
    other: '造型'
  }
  return types[props.eventType] || '造型'
})

const resolvedBeforeImage = computed(() => {
  return beforeImageError.value ? BONSAI_PLACEHOLDER_SVG : getImageWithFallback(props.beforeImage, true)
})

const resolvedAfterImage = computed(() => {
  return afterImageError.value ? BONSAI_PLACEHOLDER_SVG : getImageWithFallback(props.afterImage, true)
})

const onBeforeImageError = () => {
  beforeImageError.value = true
}

const onAfterImageError = () => {
  afterImageError.value = true
}

const toggleMode = () => {
  mode.value = mode.value === 'slider' ? 'side' : 'slider'
  sliderPosition.value = 50
}

const startDrag = (e) => {
  e.preventDefault()
  isComparing.value = true
  
  const updatePosition = (clientX) => {
    if (!compareRef.value) return
    const rect = compareRef.value.getBoundingClientRect()
    let position = ((clientX - rect.left) / rect.width) * 100
    position = Math.max(0, Math.min(100, position))
    sliderPosition.value = position
  }
  
  const onMove = (e) => {
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    updatePosition(clientX)
  }
  
  const onEnd = () => {
    isComparing.value = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onEnd)
    document.removeEventListener('touchmove', onMove)
    document.removeEventListener('touchend', onEnd)
  }
  
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onEnd)
  document.addEventListener('touchmove', onMove, { passive: false })
  document.addEventListener('touchend', onEnd)
}

onMounted(() => {})

onBeforeUnmount(() => {})
</script>

<style scoped>
.before-after-compare {
  width: 100%;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.compare-slider-wrapper {
  width: 100%;
}

.compare-container {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  user-select: none;
  touch-action: none;
}

.image-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.compare-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.before-layer {
  z-index: 2;
  will-change: clip-path;
}

.after-layer {
  z-index: 1;
}

.image-label {
  position: absolute;
  top: var(--spacing-md);
  z-index: 10;
}

.before-label {
  left: var(--spacing-md);
}

.after-label {
  right: var(--spacing-md);
}

.slider-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 4px;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
  cursor: ew-resize;
  transform: translateX(-50%);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.slider-line {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2px;
  height: 100%;
  background: #fff;
  transform: translateX(-50%);
}

.slider-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #fff;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.4);
  transition: transform var(--transition-fast);
}

.slider-button:active {
  transform: translate(-50%, -50%) scale(0.95);
}

.is-comparing .slider-button {
  transform: translate(-50%, -50%) scale(1.1);
}

.compare-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-bg-tertiary);
  border-top: 1px solid var(--color-border);
}

.control-hint {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.compare-side-by-side {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.side-by-side-content {
  display: flex;
  align-items: stretch;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
}

.side-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.side-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.side-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.side-label {
  position: absolute;
  top: var(--spacing-sm);
  z-index: 5;
}

.before-side-label {
  left: var(--spacing-sm);
}

.after-side-label {
  right: var(--spacing-sm);
}

.side-caption {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.compare-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md) 0;
  background: var(--color-bg-tertiary);
}

.divider-arrow {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  margin-bottom: var(--spacing-xs);
  animation: pulse-arrow 2s ease-in-out infinite;
}

@keyframes pulse-arrow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.divider-text {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.side-controls {
  justify-content: center;
}

@media (max-width: 480px) {
  .side-by-side-content {
    gap: var(--spacing-sm);
    padding: var(--spacing-md);
  }
  
  .slider-button {
    width: 40px;
    height: 40px;
  }
  
  .control-hint span {
    display: none;
  }
}
</style>
