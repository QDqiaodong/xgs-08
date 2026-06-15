<template>
  <div class="event-create-page">
    <van-nav-bar title="记录生命周期" left-arrow @click-left="goBack" fixed placeholder />

    <div class="content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.eventType"
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
          <div class="section-title">上传图片（选填）</div>
          <van-uploader
            v-model="fileList"
            :max-count="9"
            :before-read="beforeRead"
            :after-read="afterRead"
            multiple
            upload-text="上传图片"
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
  images: ''
})

const fileList = ref([])
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
  if (file.type && !file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    showToast('图片大小不能超过 10MB')
    return false
  }
  return true
}

const afterRead = (file) => {
  file.status = 'uploading'
  file.message = '上传中...'
  setTimeout(() => {
    file.status = 'done'
  }, 500)
}

const onSubmit = async () => {
  submitting.value = true
  try {
    const images = fileList.value.map(f => f.content).filter(Boolean)
    if (images.length > 0) {
      form.images = JSON.stringify(images)
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
    showToast('记录失败')
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
  background: #f5f5f5;
  padding-bottom: 20px;
}

.content {
  padding-top: 10px;
}

.upload-section {
  margin: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.section-title {
  font-size: 14px;
  color: #969799;
  margin-bottom: 12px;
  font-weight: 500;
}
</style>
