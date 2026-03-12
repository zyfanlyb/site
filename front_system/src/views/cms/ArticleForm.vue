<template>
  <a-modal
    v-model:open="visible"
    :title="formData.id ? '编辑文章' : '新增文章'"
    width="800px"
    :confirmLoading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-form-item label="标题" name="title">
        <a-input v-model:value="formData.title" placeholder="请输入文章标题" />
      </a-form-item>
      <a-form-item label="分类" name="categoryId">
        <a-select v-model:value="formData.categoryId" placeholder="请选择分类" allowClear>
          <a-select-option v-for="item in categoryList" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="摘要" name="summary">
        <a-textarea v-model:value="formData.summary" :rows="3" placeholder="请输入文章摘要" />
      </a-form-item>
      <a-form-item label="内容" name="content">
        <a-textarea v-model:value="formData.content" :rows="10" placeholder="请输入文章内容" />
      </a-form-item>
      <a-form-item label="封面图" name="cover">
        <a-input v-model:value="formData.cover" placeholder="请输入封面图URL" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="0">草稿</a-radio>
          <a-radio :value="1">已发布</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'

const emit = defineEmits(['refresh'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const categoryList = ref([])

const formData = reactive({
  id: null,
  title: '',
  categoryId: null,
  summary: '',
  content: '',
  cover: '',
  status: 0
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.categoryId = null
  formData.summary = ''
  formData.content = ''
  formData.cover = ''
  formData.status = 0
  formRef.value?.resetFields()
}

const fetchCategoryList = async () => {
  try {
    const res = await post('/cms/category/list')
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchArticleInfo = async (id) => {
  try {
    loading.value = true
    const res = await post(`/cms/article/info/${id}`)
    if (res.data) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    console.error('获取文章信息失败:', error)
    message.error('获取文章信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (formData.id) {
      await post('/cms/article/update', { data: formData })
    } else {
      await post('/cms/article/insert', { data: formData })
    }
    
    message.success(formData.id ? '更新成功' : '新增成功')
    visible.value = false
    emit('refresh')
  } catch (error) {
    if (error.errorFields) {
      return
    }
    console.error('提交失败:', error)
    message.error('提交失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  resetForm()
}

const open = (id = null) => {
  resetForm()
  visible.value = true
  fetchCategoryList()
  if (id) {
    formData.id = id
    fetchArticleInfo(id)
  }
}

watch(visible, (val) => {
  if (!val) {
    resetForm()
  }
})

defineExpose({
  open
})
</script>

<style scoped>
</style>
