<template>
  <div class="publish-page">
    <van-nav-bar title="发布作品" left-arrow @click-left="goBack" fixed placeholder />

    <div class="content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.title"
            name="title"
            label="标题"
            placeholder="请输入盆景作品标题"
            :rules="[{ required: true, message: '请填写标题' }]"
            maxlength="50"
            show-word-limit
          />

          <van-field
            v-model="form.content"
            name="content"
            label="介绍"
            type="textarea"
            placeholder="介绍一下你的盆景作品吧..."
            :autosize="{ minHeight: 100 }"
            maxlength="1000"
            show-word-limit
          />

          <van-field
            v-model="form.speciesId"
            name="speciesId"
            label="树种"
            placeholder="请选择树种"
            is-link
            readonly
            @click="showSpeciesPicker = true"
          />

          <van-field
            v-model="form.styleId"
            name="styleId"
            label="风格"
            placeholder="请选择风格"
            is-link
            readonly
            @click="showStylePicker = true"
          />

          <van-field
            v-model="form.treeAge"
            name="treeAge"
            label="树龄"
            type="number"
            placeholder="请输入树龄（年）"
          />

          <van-field
            v-model="form.potType"
            name="potType"
            label="盆器"
            placeholder="请输入盆器类型"
          />

          <van-field
            v-model="form.shapingIdeas"
            name="shapingIdeas"
            label="造型思路"
            type="textarea"
            placeholder="分享你的造型设计思路..."
            :autosize="{ minHeight: 80 }"
            maxlength="500"
            show-word-limit
          />

          <van-field
            v-model="form.carePoints"
            name="carePoints"
            label="养护要点"
            type="textarea"
            placeholder="分享日常养护经验..."
            :autosize="{ minHeight: 80 }"
            maxlength="500"
            show-word-limit
          />
        </van-cell-group>

        <div class="upload-section">
          <div class="section-title">上传图片</div>
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
            发布作品
          </van-button>
        </div>
      </van-form>
    </div>

    <van-popup v-model:show="showSpeciesPicker" position="bottom">
      <van-picker
        :columns="speciesColumns"
        title="选择树种"
        @confirm="onSpeciesConfirm"
        @cancel="showSpeciesPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showStylePicker" position="bottom">
      <van-picker
        :columns="styleColumns"
        title="选择风格"
        @confirm="onStyleConfirm"
        @cancel="showStylePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createPost } from '@/api/post'
import { getCategoriesByType } from '@/api/category'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  title: '',
  content: '',
  speciesId: null,
  styleId: null,
  treeAge: null,
  potType: '',
  shapingIdeas: '',
  carePoints: ''
})

const fileList = ref([])
const submitting = ref(false)
const showSpeciesPicker = ref(false)
const showStylePicker = ref(false)
const speciesList = ref([])
const styleList = ref([])
const speciesColumns = ref([])
const styleColumns = ref([])
const selectedSpeciesName = ref('')
const selectedStyleName = ref('')

const loadCategories = async () => {
  try {
    speciesList.value = await getCategoriesByType('species')
    styleList.value = await getCategoriesByType('style')
    speciesColumns.value = speciesList.value.map(s => ({ text: s.name, value: s.id }))
    styleColumns.value = styleList.value.map(s => ({ text: s.name, value: s.id }))
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const onSpeciesConfirm = ({ selectedOptions }) => {
  form.speciesId = selectedOptions[0].value
  selectedSpeciesName.value = selectedOptions[0].text
  showSpeciesPicker.value = false
}

const onStyleConfirm = ({ selectedOptions }) => {
  form.styleId = selectedOptions[0].value
  selectedStyleName.value = selectedOptions[0].text
  showStylePicker.value = false
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
  if (fileList.value.length === 0) {
    showToast('请至少上传一张图片')
    return
  }

  submitting.value = true
  try {
    const postData = {
      userId: userStore.currentUser.id,
      ...form
    }

    const files = fileList.value.map(f => f.file).filter(Boolean)

    await createPost(postData, files)
    showToast('发布成功')
    setTimeout(() => {
      router.replace('/')
    }, 1000)
  } catch (e) {
    showToast('发布失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.publish-page {
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
