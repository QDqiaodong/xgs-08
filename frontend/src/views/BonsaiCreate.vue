<template>
  <div class="bonsai-create-page">
    <van-nav-bar title="新增盆景" left-arrow @click-left="goBack" fixed placeholder />

    <div class="content">
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
            :value="selectedSpeciesName"
            name="speciesId"
            label="树种"
            placeholder="请选择树种"
            is-link
            readonly
            @click="showSpeciesPicker = true"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createBonsai } from '@/api/bonsai'
import { getCategoriesByType } from '@/api/category'
import { useUserStore } from '@/stores/user'
import { validateImageFile, uploadSingleImage } from '@/api/upload'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  name: '',
  speciesId: null,
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
const speciesList = ref([])
const tempSpeciesId = ref(null)
const tempSpeciesName = ref('')
const selectedSpeciesName = ref('')

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

const loadCategories = async () => {
  try {
    speciesList.value = await getCategoriesByType('species')
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

  submitting.value = true
  try {
    if (fileList.value.length > 0 && fileList.value[0].uploadedUrl) {
      form.coverImage = fileList.value[0].uploadedUrl
    }

    const data = {
      userId: userStore.currentUser.id,
      ...form
    }

    const result = await createBonsai(data)
    showToast('添加成功')
    setTimeout(() => {
      router.replace(`/bonsais/${result.id}`)
    }, 1000)
  } catch (e) {
    showToast(e?.response?.data?.message || e?.message || '添加失败')
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
