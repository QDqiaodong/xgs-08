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
        </van-cell-group>

        <div class="shaping-section">
          <div class="shaping-section-title">造型思路</div>
          
          <van-field
            v-model="form.trunkTreatment"
            name="trunkTreatment"
            label="主干处理"
            type="textarea"
            placeholder="描述主干的造型处理方式，如弯曲、截干、雕饰等..."
            :autosize="{ minHeight: 60 }"
            maxlength="300"
            show-word-limit
          />
          
          <van-field
            v-model="form.branchSelection"
            name="branchSelection"
            label="枝托取舍"
            type="textarea"
            placeholder="描述枝片的选择和布局，如主枝、辅枝的安排..."
            :autosize="{ minHeight: 60 }"
            maxlength="300"
            show-word-limit
          />
          
          <van-field
            v-model="form.potLayout"
            name="potLayout"
            label="盆面布局"
            type="textarea"
            placeholder="描述盆面设计，如配石、铺苔、摆件的布置..."
            :autosize="{ minHeight: 60 }"
            maxlength="300"
            show-word-limit
          />
          
          <van-field
            v-model="form.futureDirection"
            name="futureDirection"
            label="未来培养"
            type="textarea"
            placeholder="描述未来的培养方向和预期效果..."
            :autosize="{ minHeight: 60 }"
            maxlength="300"
            show-word-limit
          />
        </div>

        <van-cell-group inset>
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

    <van-popup v-model:show="showSpeciesPicker" position="bottom" round :style="{ height: '60%' }">
      <div class="picker-popup">
        <div class="picker-popup-header">
          <van-button type="default" size="small" @click="showSpeciesPicker = false">取消</van-button>
          <span class="picker-title">选择树种</span>
          <van-button type="primary" size="small" @click="confirmSpecies">确定</van-button>
        </div>
        <div class="picker-popup-content">
          <div class="picker-option-group" v-for="group in speciesGroups" :key="group.name">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-options">
              <van-tag
                v-for="item in group.items"
                :key="item.value"
                :type="tempSpeciesId === item.value ? 'primary' : 'default'"
                size="medium"
                plain
                :class="{ 'tag-selected': tempSpeciesId === item.value }"
                @click="tempSpeciesId = item.value; tempSpeciesName = item.text"
              >
                <span v-if="item.icon">{{ item.icon }} </span>{{ item.text }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showStylePicker" position="bottom" round :style="{ height: '60%' }">
      <div class="picker-popup">
        <div class="picker-popup-header">
          <van-button type="default" size="small" @click="showStylePicker = false">取消</van-button>
          <span class="picker-title">选择造型风格</span>
          <van-button type="primary" size="small" @click="confirmStyle">确定</van-button>
        </div>
        <div class="picker-popup-content">
          <div class="picker-option-group" v-for="group in styleGroups" :key="group.name">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-options">
              <van-tag
                v-for="item in group.items"
                :key="item.value"
                :type="tempStyleId === item.value ? 'primary' : 'default'"
                size="medium"
                plain
                :class="{ 'tag-selected': tempStyleId === item.value }"
                @click="tempStyleId = item.value; tempStyleName = item.text"
              >
                {{ item.text }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createPost } from '@/api/post'
import { getCategoriesByType } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { validateImageFile } from '@/api/upload'

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
  trunkTreatment: '',
  branchSelection: '',
  potLayout: '',
  futureDirection: '',
  carePoints: ''
})

const fileList = ref([])
const submitting = ref(false)
const showSpeciesPicker = ref(false)
const showStylePicker = ref(false)
const speciesList = ref([])
const styleList = ref([])
const tempSpeciesId = ref(null)
const tempSpeciesName = ref('')
const tempStyleId = ref(null)
const tempStyleName = ref('')
const selectedSpeciesName = ref('')
const selectedStyleName = ref('')

const speciesGroups = computed(() => {
  const groups = new Map()
  speciesList.value.forEach(s => {
    const groupName = s.groupName || '其他'
    if (!groups.has(groupName)) {
      groups.set(groupName, [])
    }
    groups.get(groupName).push({
      text: s.name,
      value: s.id,
      icon: s.icon
    })
  })
  return Array.from(groups.entries()).map(([name, items]) => ({ name, items }))
})

const styleGroups = computed(() => {
  const groups = new Map()
  styleList.value.forEach(s => {
    const groupName = s.groupName || '其他'
    if (!groups.has(groupName)) {
      groups.set(groupName, [])
    }
    groups.get(groupName).push({
      text: s.name,
      value: s.id
    })
  })
  return Array.from(groups.entries()).map(([name, items]) => ({ name, items }))
})

const loadCategories = async () => {
  try {
    speciesList.value = await getCategoriesByType('species')
    styleList.value = await getCategoriesByType('style')
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const confirmSpecies = () => {
  if (tempSpeciesId.value !== null) {
    form.speciesId = tempSpeciesId.value
    selectedSpeciesName.value = tempSpeciesName.value
  }
  showSpeciesPicker.value = false
}

const confirmStyle = () => {
  if (tempStyleId.value !== null) {
    form.styleId = tempStyleId.value
    selectedStyleName.value = tempStyleName.value
  }
  showStylePicker.value = false
}

const beforeRead = (file) => {
  return validateImageFile(file)
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
  const uploadingFile = fileList.value.find(f => f.status === 'uploading')
  if (uploadingFile) {
    showToast('图片正在处理中，请稍候')
    return
  }
  const failedFile = fileList.value.find(f => f.status === 'failed')
  if (failedFile) {
    showToast('存在上传失败的图片，请重新上传')
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

.picker-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.picker-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #ebedf0;
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.picker-popup-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.picker-option-group {
  margin-bottom: 20px;
}

.picker-option-group:last-child {
  margin-bottom: 0;
}

.picker-option-group .group-title {
  font-size: 14px;
  font-weight: 600;
  color: #646566;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #07c160;
}

.picker-option-group .group-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.picker-option-group :deep(.van-tag) {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border-width: 1px;
}

.picker-option-group :deep(.van-tag--plain) {
  background: #f7f8fa;
  border-color: #ebedf0;
  color: #646566;
}

.picker-option-group :deep(.van-tag--primary.van-tag--plain) {
  background: #e8f3ea;
  border-color: #07c160;
  color: #07c160;
}

.tag-selected {
  background: #e8f3ea !important;
  border-color: #07c160 !important;
  color: #07c160 !important;
}

.shaping-section {
  margin: 12px 16px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.shaping-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  padding: 16px 16px 12px;
  border-left: 3px solid #07c160;
  margin: 0;
}

.shaping-section :deep(.van-cell) {
  padding: 12px 16px;
}

.shaping-section :deep(.van-field--textarea) {
  padding: 0;
}

.shaping-section :deep(.van-field--textarea .van-field__control) {
  padding: 12px 16px;
}

.shaping-section :deep(.van-cell::after) {
  left: 16px;
}

.shaping-section :deep(.van-cell:last-child::after) {
  display: none;
}
</style>
