<template>
  <div class="event-create-page">
    <van-nav-bar title="记录生命周期" left-arrow @click-left="goBack" fixed placeholder />

    <div class="content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            :value="selectedTypeName"
            name="eventType"
            label="事件类型"
            placeholder="请选择事件类型"
            is-link
            readonly
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择事件类型' }]"
          />

          <van-field
            v-model="form.title"
            name="title"
            label="标题"
            placeholder="请输入标题（选填）"
            maxlength="100"
            show-word-limit
          />

          <van-field
            v-model="form.content"
            name="content"
            label="内容"
            type="textarea"
            placeholder="记录一下这次的详情..."
            :autosize="{ minHeight: 100 }"
            maxlength="1000"
            show-word-limit
            :rules="[{ required: true, message: '请填写记录内容' }]"
          />

          <van-field
            v-model="form.eventDate"
            name="eventDate"
            label="日期"
            type="date"
            placeholder="选择事件日期"
            :rules="[{ required: true, message: '请选择日期' }]"
          />
        </van-cell-group>

        <div class="upload-section">
          <div class="section-title">
            <van-icon name="photo-o" />
            造型前照片（选填）
          </div>
          <div class="upload-hint">记录造型改造前的状态，用于前后对比</div>
          <van-uploader
            v-model="beforeFileList"
            :max-count="9"
            :before-read="beforeRead"
            :after-read="(file) => afterRead(file, 'before')"
            multiple
            upload-text="上传造型前照片"
          />
        </div>

        <div class="upload-section">
          <div class="section-title">
            <van-icon name="photograph" />
            造型后照片（选填）
          </div>
          <div class="upload-hint">记录造型完成后的效果</div>
          <van-uploader
            v-model="afterFileList"
            :max-count="9"
            :before-read="beforeRead"
            :after-read="(file) => afterRead(file, 'after')"
            multiple
            upload-text="上传造型后照片"
          />
        </div>

        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            保存记录
          </van-button>
        </div>
      </van-form>
    </div>

    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="eventTypeColumns"
        title="选择事件类型"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { createEvent } from '@/api/lifecycleEvent'
import { validateImageFile, uploadSingleImage } from '@/api/upload'

const router = useRouter()
const route = useRoute()

const eventTypes = [
  { text: '入手', value: 'acquire' },
  { text: '定植', value: 'planting' },
  { text: '修剪', value: 'pruning' },
  { text: '蟠扎', value: 'wiring' },
  { text: '换盆', value: 'repotting' },
  { text: '其他', value: 'other' }
]

const form = reactive({
  eventType: '',
  title: '',
  content: '',
  eventDate: new Date().toISOString().split('T')[0],
  images: '',
  beforeImages: ''
})

const beforeFileList = ref([])
const afterFileList = ref([])
const submitting = ref(false)
const showTypePicker = ref(false)
const eventTypeColumns = ref(eventTypes)
const selectedTypeName = ref('')

const onTypeConfirm = ({ selectedOptions }) => {
  form.eventType = selectedOptions[0].value
  selectedTypeName.value = selectedOptions[0].text
  showTypePicker.value = false
}

const beforeRead = (file) => {
  return validateImageFile(file)
}

const afterRead = async (file, type = 'after') => {
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

const onSubmit = async () => {
  const allFiles = [...beforeFileList.value, ...afterFileList.value]
  const uploadingFile = allFiles.find(f => f.status === 'uploading')
  if (uploadingFile) {
    showToast('图片正在上传中，请稍候')
    return
  }
  const failedFile = allFiles.find(f => f.status === 'failed')
  if (failedFile) {
    showToast('存在上传失败的图片，请重新上传')
    return
  }

  submitting.value = true
  try {
    const afterImages = afterFileList.value.map(f => f.uploadedUrl).filter(Boolean)
    if (afterImages.length > 0) {
      form.images = JSON.stringify(afterImages)
    }

    const beforeImages = beforeFileList.value.map(f => f.uploadedUrl).filter(Boolean)
    if (beforeImages.length > 0) {
      form.beforeImages = JSON.stringify(beforeImages)
    }

    const data = {
      bonsaiId: Number(route.params.id),
      ...form
    }

    await createEvent(data)
    showToast('记录成功')
    setTimeout(() => {
      router.back()
    }, 1000)
  } catch (e) {
    showToast(e?.response?.data?.message || e?.message || '记录失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.event-create-page {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 20px;
}

.content {
  padding-top: 10px;
}

.upload-section {
  margin: 16px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
  font-weight: var(--font-weight-semibold);
}

.upload-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--spacing-md);
}
</style>
