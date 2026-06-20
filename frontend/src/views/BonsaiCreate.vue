<template>
  <div class="bonsai-create-page">
    <van-nav-bar :title="isEdit ? '编辑盆景' : '新增盆景'" left-arrow @click-left="goBack" fixed placeholder />

    <div v-if="loading" class="loading-wrapper">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <div v-else class="content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            name="name"
            label="名称"
            placeholder="给这盆盆景起个名字"
            :rules="[{ required: true, message: '请填写盆景名称' }]"
            maxlength="50"
            show-word-limit
          />

          <van-field
            :model-value="selectedSpeciesName"
            name="speciesId"
            label="树种"
            placeholder="请选择树种"
            is-link
            readonly
            @click="showSpeciesPicker = true"
          />

          <van-field
            :model-value="selectedStyleName"
            name="styleId"
            label="造型风格"
            placeholder="请选择造型风格"
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
            placeholder="如：紫砂圆盆、长方形陶盆"
          />

          <van-field
            v-model="form.acquireDate"
            name="acquireDate"
            label="入手日期"
            type="date"
            placeholder="选择入手日期"
          />

          <van-field
            v-model="form.description"
            name="description"
            label="简介"
            type="textarea"
            placeholder="简单介绍一下这盆盆景..."
            :autosize="{ minHeight: 80 }"
            maxlength="500"
            show-word-limit
          />
        </van-cell-group>

        <div v-if="validationResult.errors.length > 0 || validationResult.warnings.length > 0" class="validation-section">
          <div v-if="validationResult.errors.length > 0" class="validation-block error-block">
            <div class="validation-header">
              <van-icon name="cross-circle" color="#ee0a24" />
              <span class="validation-title">需要修正的问题（{{ validationResult.errors.length }}）</span>
            </div>
            <ul class="validation-list">
              <li v-for="(error, idx) in validationResult.errors" :key="'e'+idx">{{ error }}</li>
            </ul>
          </div>
          <div v-if="validationResult.warnings.length > 0" class="validation-block warning-block">
            <div class="validation-header">
              <van-icon name="warning-o" color="#ff976a" />
              <span class="validation-title">温馨提示（{{ validationResult.warnings.length }}）</span>
            </div>
            <ul class="validation-list">
              <li v-for="(warning, idx) in validationResult.warnings" :key="'w'+idx">{{ warning }}</li>
            </ul>
          </div>
        </div>

        <div class="outline-section">
          <div class="section-title">
            <van-icon name="eye-o" />
            <span>树形轮廓</span>
          </div>
          <van-cell-group inset>
            <van-field
              v-model="form.trunkShape"
              name="trunkShape"
              label="干型"
              placeholder="如：直干、斜干、曲干等"
              maxlength="200"
            />
            <van-field
              v-model="form.branchSupport"
              name="branchSupport"
              label="枝托"
              placeholder="如：第一托出枝位置、枝片分布等"
              maxlength="200"
            />
            <van-field
              v-model="form.crownWidth"
              name="crownWidth"
              label="冠幅"
              placeholder="如：树冠宽度、高度比例等"
              maxlength="200"
            />
            <van-field
              v-model="form.potSurface"
              name="potSurface"
              label="盆面"
              placeholder="如：盆面布局、点缀植物、铺面石等"
              maxlength="200"
            />
          </van-cell-group>
        </div>

        <div class="upload-section">
          <div class="section-title">封面图片</div>
          <van-uploader
            v-model="fileList"
            :max-count="1"
            :before-read="beforeRead"
            :after-read="afterRead"
            upload-text="上传封面"
          />
        </div>

        <div v-if="isEdit" class="stage-section">
          <div class="section-title">
            <van-icon name="photos" />
            <span>养护阶段图片管理</span>
          </div>
          <p class="stage-hint">记录盆景在不同养护阶段的变化，见证长期养护成果</p>

          <div v-for="stage in STAGE_LIST" :key="stage.value" class="stage-block">
            <div class="stage-header" :style="{ borderLeftColor: stage.color }">
              <span class="stage-icon">{{ stage.icon }}</span>
              <div class="stage-info">
                <div class="stage-name" :style="{ color: stage.color }">{{ stage.label }}</div>
                <div class="stage-desc">{{ stage.description }}</div>
              </div>
            </div>
            <div class="stage-uploader-wrapper">
              <van-uploader
                v-model="stageFileList[stage.value]"
                :max-count="6"
                :before-read="(file) => beforeStageRead(file, stage.value)"
                :after-read="(file) => afterStageRead(file, stage.value)"
                upload-text="添加图片"
                multiple
                :preview-size="80"
              />
            </div>
          </div>
        </div>

        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            保存
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import {
  createBonsai,
  updateBonsai,
  getBonsaiById,
  addStageImagesBatch,
  getStageImages
} from '@/api/bonsai'
import { getCategoriesByType } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { validateImageFile, uploadSingleImage } from '@/api/upload'
import { getImageWithFallback } from '@/utils/image'
import { validateBonsai as validateBonsaiData, STAGE_LIST } from '@/utils/bonsaiValidator'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)

const form = reactive({
  id: null,
  name: '',
  speciesId: null,
  styleId: null,
  treeAge: null,
  potType: '',
  acquireDate: new Date().toISOString().split('T')[0],
  description: '',
  coverImage: '',
  trunkShape: '',
  branchSupport: '',
  crownWidth: '',
  potSurface: ''
})

const fileList = ref([])
const submitting = ref(false)
const showSpeciesPicker = ref(false)
const showStylePicker = ref(false)
const speciesList = ref([])
const styleList = ref([])
const tempSpeciesId = ref(null)
const tempSpeciesName = ref('')
const selectedSpeciesName = ref('')
const tempStyleId = ref(null)
const tempStyleName = ref('')
const selectedStyleName = ref('')

const validationResult = reactive({
  errors: [],
  warnings: []
})

const stageFileList = reactive({})

STAGE_LIST.forEach(stage => {
  stageFileList[stage.value] = []
})

const runValidation = () => {
  const result = validateBonsaiData(form, selectedSpeciesName.value, selectedStyleName.value)
  validationResult.errors = result.errors
  validationResult.warnings = result.warnings
}

watch(
  () => [form.treeAge, form.potType, form.description, form.name, form.potSurface, form.acquireDate, selectedSpeciesName.value, selectedStyleName.value],
  () => {
    runValidation()
  },
  { deep: true }
)

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

const afterRead = async (file) => {
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

const beforeStageRead = (file) => {
  return validateImageFile(file)
}

const afterStageRead = async (file, stage) => {
  const files = Array.isArray(file) ? file : [file]
  for (const f of files) {
    f.status = 'uploading'
    f.message = '上传中...'
    try {
      await uploadSingleImage(f)
      f.status = 'done'
      f.message = ''
    } catch (e) {
      f.status = 'failed'
      f.message = '上传失败'
      showToast(e?.response?.data?.message || e?.message || '图片上传失败')
    }
  }
}

const loadExistingStageImages = async (bonsaiId) => {
  try {
    const images = await getStageImages(bonsaiId)
    if (images && images.length > 0) {
      const grouped = {}
      STAGE_LIST.forEach(stage => {
        grouped[stage.value] = []
      })
      images.forEach(img => {
        if (grouped[img.stage]) {
          grouped[img.stage].push({
            id: img.id,
            url: getImageWithFallback(img.imageUrl),
            uploadedUrl: img.imageUrl,
            note: img.note,
            status: 'done'
          })
        }
      })
      Object.keys(grouped).forEach(key => {
        stageFileList[key] = grouped[key]
      })
    }
  } catch (e) {
    console.warn('加载阶段图片失败', e)
  }
}

const saveStageImages = async (bonsaiId) => {
  const imagesToSave = []
  STAGE_LIST.forEach(stage => {
    const files = stageFileList[stage.value] || []
    files.forEach((file, idx) => {
      if (file.uploadedUrl && !file.id) {
        imagesToSave.push({
          stage: stage.value,
          imageUrl: file.uploadedUrl,
          note: file.note || '',
          sortOrder: idx
        })
      }
    })
  })
  if (imagesToSave.length > 0) {
    try {
      await addStageImagesBatch(bonsaiId, imagesToSave)
    } catch (e) {
      console.warn('保存阶段图片失败', e)
    }
  }
}

const loadBonsai = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const bonsai = await getBonsaiById(route.params.id)
    if (bonsai) {
      form.id = bonsai.id
      form.name = bonsai.name || ''
      form.speciesId = bonsai.speciesId || null
      form.styleId = bonsai.styleId || null
      form.treeAge = bonsai.treeAge || null
      form.potType = bonsai.potType || ''
      form.acquireDate = bonsai.acquireDate || ''
      form.description = bonsai.description || ''
      form.coverImage = bonsai.coverImage || ''
      form.trunkShape = bonsai.trunkShape || ''
      form.branchSupport = bonsai.branchSupport || ''
      form.crownWidth = bonsai.crownWidth || ''
      form.potSurface = bonsai.potSurface || ''

      if (bonsai.species) {
        selectedSpeciesName.value = bonsai.species.name
        tempSpeciesId.value = bonsai.speciesId
        tempSpeciesName.value = bonsai.species.name
      }

      if (bonsai.style) {
        selectedStyleName.value = bonsai.style.name
        tempStyleId.value = bonsai.styleId
        tempStyleName.value = bonsai.style.name
      }

      if (bonsai.coverImage) {
        fileList.value = [{
          url: getImageWithFallback(bonsai.coverImage),
          status: 'done',
          uploadedUrl: bonsai.coverImage
        }]
      }

      await loadExistingStageImages(bonsai.id)
      runValidation()
    }
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const onSubmit = async () => {
  const uploadingFile = fileList.value.find(f => f.status === 'uploading')
  if (uploadingFile) {
    showToast('图片正在上传中，请稍候')
    return
  }
  const failedFile = fileList.value.find(f => f.status === 'failed')
  if (failedFile) {
    showToast('存在上传失败的图片，请重新上传')
    return
  }

  for (const stage of STAGE_LIST) {
    const stageUploading = (stageFileList[stage.value] || []).find(f => f.status === 'uploading')
    if (stageUploading) {
      showToast(`${stage.label}图片正在上传中，请稍候`)
      return
    }
  }

  runValidation()
  if (validationResult.errors.length > 0) {
    showToast('请先修正表单中的错误')
    return
  }

  submitting.value = true
  try {
    if (fileList.value.length > 0 && fileList.value[0].uploadedUrl) {
      form.coverImage = fileList.value[0].uploadedUrl
    }

    const data = {
      userId: userStore.currentUser.id,
      ...form
    }

    let result
    if (isEdit.value) {
      result = await updateBonsai(data)
      await saveStageImages(result.id)
      showToast('保存成功')
    } else {
      result = await createBonsai(data)
      showToast('添加成功')
    }
    setTimeout(() => {
      router.replace(`/bonsais/${result.id}`)
    }, 1000)
  } catch (e) {
    showToast(e?.response?.data?.message || e?.message || (isEdit.value ? '保存失败' : '添加失败'))
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadCategories()
  loadBonsai()
})
</script>

<style scoped>
.bonsai-create-page {
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
  display: flex;
  align-items: center;
  gap: 4px;
}

.outline-section {
  margin: 16px;
}

.outline-section .section-title {
  color: #323233;
  font-size: 16px;
  font-weight: 600;
  padding-left: 8px;
  border-left: 3px solid #07c160;
  margin-bottom: 12px;
}

.validation-section {
  margin: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.validation-block {
  border-radius: 8px;
  padding: 12px 16px;
}

.error-block {
  background: #feecec;
  border: 1px solid #fcd3d3;
}

.warning-block {
  background: #fff7e8;
  border: 1px solid #ffe7b8;
}

.validation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.validation-title {
  font-size: 14px;
  font-weight: 600;
  color: #323233;
}

.validation-list {
  margin: 0;
  padding-left: 20px;
}

.validation-list li {
  font-size: 13px;
  line-height: 1.6;
  color: #646566;
  margin-bottom: 4px;
}

.error-block .validation-list li {
  color: #c8c9cc;
  color: #ee0a24;
}

.warning-block .validation-list li {
  color: #ff976a;
}

.stage-section {
  margin: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.stage-section .section-title {
  color: #323233;
  font-size: 16px;
  font-weight: 600;
  padding-left: 8px;
  border-left: 3px solid #409eff;
  margin-bottom: 8px;
}

.stage-hint {
  font-size: 12px;
  color: #969799;
  margin: 0 0 16px 11px;
}

.stage-block {
  margin-bottom: 16px;
}

.stage-block:last-child {
  margin-bottom: 0;
}

.stage-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f7f8fa;
  border-radius: 6px;
  border-left: 4px solid;
}

.stage-icon {
  font-size: 24px;
}

.stage-info {
  flex: 1;
}

.stage-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
}

.stage-desc {
  font-size: 12px;
  color: #969799;
}

.stage-uploader-wrapper {
  padding-left: 8px;
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
</style>
