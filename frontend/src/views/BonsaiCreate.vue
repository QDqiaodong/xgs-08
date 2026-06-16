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

    <van-popup v-model:show="showSpeciesPicker" position="bottom">
      <van-picker
        :columns="speciesColumns"
        title="选择树种"
        @confirm="onSpeciesConfirm"
        @cancel="showSpeciesPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
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
const speciesColumns = ref([])
const selectedSpeciesName = ref('')

const loadCategories = async () => {
  try {
    speciesList.value = await getCategoriesByType('species')
    speciesColumns.value = speciesList.value.map(s => ({ text: s.name, value: s.id }))
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const onSpeciesConfirm = ({ selectedOptions }) => {
  form.speciesId = selectedOptions[0].value
  selectedSpeciesName.value = selectedOptions[0].text
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
</style>
