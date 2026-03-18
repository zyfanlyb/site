<template>
  <!-- 主表单模态框 -->
  <a-modal
    v-model:open="visible"
    :title="formData.id ? '编辑文章' : '新增文章'"
    width="960px"
    :confirmLoading="loading"
    okText="确认"
    cancelText="取消"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
      autocomplete="off"
    >
      <a-form-item label="标题" name="title">
        <a-input v-model:value="formData.title" placeholder="请输入文章标题" autocomplete="off" />
      </a-form-item>
      <a-form-item label="分类" name="categoryId">
        <a-select v-model:value="formData.categoryId" placeholder="请选择分类" allowClear @change="onCategoryChange">
          <a-select-option v-for="item in categoryList" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="类型" name="typeId">
        <a-select v-model:value="formData.typeId" placeholder="请先选择分类" allowClear :disabled="!formData.categoryId">
          <a-select-option v-for="item in typeList" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="摘要" name="summary">
        <a-textarea v-model:value="formData.summary" :rows="3" placeholder="请输入文章摘要" autocomplete="off" />
      </a-form-item>
      <a-form-item label="内容" name="content">
        <div class="content-editor-wrap">
          <div class="content-toolbar">
            <span class="label">支持在线编辑 Markdown，或从本地上传 .md 文件</span>
            <a-space>
              <a-button type="link" size="small" @click="triggerMdUpload">
                <template #icon><UploadOutlined /></template>
                上传 Markdown 文件
              </a-button>
              <a-button type="link" size="small" @click="openFullscreenEditor">
                <template #icon><FullscreenOutlined /></template>
                放大编辑
              </a-button>
            </a-space>
            <input
              ref="mdFileInputRef"
              type="file"
              accept=".md,text/markdown,text/plain"
              class="md-file-input"
              @change="onMdFileChange"
            />
          </div>
          <MdEditor v-model="formData.content" :preview="true" :language="'zh-CN'" class="md-editor-inner" />
        </div>
      </a-form-item>
      <a-form-item label="封面图片" name="coverImages">
        <a-upload
          v-model:file-list="coverFileList"
          list-type="picture-card"
          :custom-request="handleCoverUpload"
          :before-upload="beforeCoverUpload"
          @remove="handleCoverRemove"
          :max-count="6"
          accept="image/*"
        >
          <div v-if="coverFileList.length < 6">
            <PlusOutlined />
            <div style="margin-top: 8px">上传封面</div>
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="0">草稿</a-radio>
          <a-radio :value="1">已发布</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 放大编辑模态框：Teleport 到 body，并禁用编辑器自带全屏 -->
  <Teleport to="body">
    <a-modal
      v-model:open="fullscreenEditorVisible"
      title="文章内容 - 放大编辑"
      width="90vw"
      :footer="null"
      wrap-class-name="fullscreen-editor-modal"
      class="fullscreen-editor-modal-content"
      :body-style="fullscreenBodyStyle"
      @cancel="fullscreenEditorVisible = false"
    >
      <!-- 固定悬浮的退出按钮，始终在最上层可点 -->
      <div class="exit-float-btn" @click="fullscreenEditorVisible = false">
        <CloseOutlined />
        <span>退出放大</span>
      </div>
      <div class="fullscreen-editor-toolbar">
        <a-space>
          <a-button type="link" size="small" @click="triggerMdUploadInFullscreen">
            <template #icon><UploadOutlined /></template>
            上传 Markdown 文件
          </a-button>
        </a-space>
        <a-button type="primary" size="small" @click="fullscreenEditorVisible = false">
          <template #icon><CloseOutlined /></template>
          退出放大
        </a-button>
        <input
          ref="mdFileInputFullscreenRef"
          type="file"
          accept=".md,text/markdown,text/plain"
          class="md-file-input"
          @change="onMdFileChange"
        />
      </div>
      <div class="fullscreen-editor-body">
        <MdEditor
          v-model="formData.content"
          :preview="true"
          :language="'zh-CN'"
          :toolbars-exclude="['fullscreen', 'maximize']"
          :page-fullscreen="false"
          class="md-editor-fullscreen"
        />
      </div>
      <div class="fullscreen-editor-footer">
        <span class="footer-tip">内容已自动同步</span>
        <a-button type="primary" @click="fullscreenEditorVisible = false">完成并退出</a-button>
      </div>
    </a-modal>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined, FullscreenOutlined, CloseOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import service from '@/utils/request'
import { getCmsFilePreviewUrl } from '@/utils/file'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const emit = defineEmits(['refresh'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const mdFileInputRef = ref(null)
const mdFileInputFullscreenRef = ref(null)
const fullscreenEditorVisible = ref(false)
const coverFileList = ref([])
const fullscreenBodyStyle = {
  padding: 0,
  height: '80vh',
  display: 'flex',
  flexDirection: 'column',
  overflow: 'hidden'
}
const categoryList = ref([])
const typeList = ref([])

const formData = reactive({
  id: null,
  title: '',
  categoryId: null,
  typeId: null,
  summary: '',
  content: '',
  cover: '',
  coverImages: [],
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
  formData.typeId = null
  formData.summary = ''
  formData.content = ''
  formData.cover = ''
  formData.coverImages = []
  formData.status = 0
  typeList.value = []
  coverFileList.value = []
  formRef.value?.resetFields()
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    message.error('封面图不能超过 5MB')
    return false
  }
  return true
}

const handleCoverUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const fd = new FormData()
    fd.append('file', file)
    const response = await service.post('/file/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (response.code === 200 && response.data) {
      // 以 coverImages 为准；同时兼容旧字段 cover（取第一张）
      if (!Array.isArray(formData.coverImages)) formData.coverImages = []
      formData.coverImages.push(response.data)
      formData.cover = formData.coverImages[0] || ''

      const idx = coverFileList.value.findIndex((item) => item.uid === file.uid)
      const previewUrl = await getCmsFilePreviewUrl(response.data)
      const patch = {
        uid: file.uid,
        name: file.name || 'cover',
        status: 'done',
        url: previewUrl,
        filePath: response.data
      }
      if (idx > -1) coverFileList.value[idx] = { ...coverFileList.value[idx], ...patch }
      else coverFileList.value.push(patch)

      onSuccess(response.data, file)
      message.success('封面上传成功')
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    message.error('封面上传失败: ' + (error.message || '未知错误'))
    onError(error)
  }
}

const handleCoverRemove = (file) => {
  const filePath = file?.filePath
  if (filePath && Array.isArray(formData.coverImages)) {
    formData.coverImages = formData.coverImages.filter((x) => x !== filePath)
  }
  formData.cover = Array.isArray(formData.coverImages) ? (formData.coverImages[0] || '') : ''
  return true
}

const fetchCategoryList = async () => {
  try {
    const res = await post('/cms/category/list')
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchTypeList = async (categoryId) => {
  if (!categoryId) {
    typeList.value = []
    return
  }
  try {
    const res = await post('/cms/type/list', { data: { categoryId } })
    typeList.value = res.data || []
  } catch (error) {
    console.error('获取类型列表失败:', error)
    typeList.value = []
  }
}

const onCategoryChange = () => {
  formData.typeId = null
  fetchTypeList(formData.categoryId)
}

const triggerMdUpload = () => {
  mdFileInputRef.value?.click()
}

const openFullscreenEditor = () => {
  fullscreenEditorVisible.value = true
}

const triggerMdUploadInFullscreen = () => {
  mdFileInputFullscreenRef.value?.click()
}

const onMdFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    const text = ev.target?.result
    if (typeof text === 'string') {
      formData.content = text
      message.success('已加载 Markdown 文件内容')
    }
  }
  reader.readAsText(file, 'UTF-8')
  e.target.value = ''
}

const fetchArticleInfo = async (id) => {
  try {
    loading.value = true
    const res = await post(`/cms/article/info/${id}`)
    if (res.data) {
      Object.assign(formData, res.data)
      fetchTypeList(formData.categoryId)
      // 回显封面：优先 coverImages，其次 cover
      const coversRaw = res.data.coverImages ?? (res.data.cover ? [res.data.cover] : [])
      const covers = Array.isArray(coversRaw) ? coversRaw : []
      formData.coverImages = covers
        .map((x) => (x == null ? '' : String(x)).trim())
        .filter(Boolean)
      formData.cover = formData.coverImages[0] || ''

      if (formData.coverImages.length) {
        const urls = await Promise.all(formData.coverImages.map((obj) => getCmsFilePreviewUrl(obj)))
        coverFileList.value = formData.coverImages.map((obj, i) => ({
          uid: String(i + 1),
          name: 'cover',
          status: 'done',
          url: urls[i],
          filePath: obj
        }))
      } else {
        coverFileList.value = []
      }
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

    // 提交前保证 coverImages / cover 一致
    if (!Array.isArray(formData.coverImages)) formData.coverImages = []
    formData.coverImages = formData.coverImages.map((x) => (x == null ? '' : String(x)).trim()).filter(Boolean)
    formData.cover = formData.coverImages[0] || ''
    
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

const refreshCategories = () => {
  fetchCategoryList()
}

const refreshTypes = () => {
  if (formData.categoryId) {
    fetchTypeList(formData.categoryId)
  }
}

defineExpose({
  open,
  refreshCategories,
  refreshTypes
})
</script>

<style scoped>
.content-editor-wrap {
  width: 100%;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}
.content-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #d9d9d9;
}
.content-toolbar .label {
  color: #666;
  font-size: 12px;
}
.md-file-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}
.md-editor-inner {
  min-height: 360px;
}
:deep(.md-editor-inner .md-editor-preview-wrapper) {
  padding: 12px;
}

/* 放大编辑模态框 */
.fullscreen-editor-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.fullscreen-editor-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 16px;
}
.fullscreen-editor-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
  box-shadow: 0 -1px 2px rgba(0,0,0,0.05);
}
.fullscreen-editor-footer .footer-tip {
  color: #888;
  font-size: 12px;
}
.md-editor-fullscreen {
  min-height: 400px;
}
:deep(.md-editor-fullscreen) {
  min-height: 400px;
}
:deep(.md-editor-fullscreen .md-editor-preview-wrapper) {
  padding: 16px;
}

/* 固定悬浮的退出按钮，防止被编辑器全屏挡住 */
.exit-float-btn {
  position: fixed;
  top: 72px;
  right: 32px;
  z-index: 10002;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.exit-float-btn:hover {
  background: #40a9ff;
  color: #fff;
}
</style>

<style>
/* 放大模态框宽度，非 scoped 以便作用于 Modal 的 wrap */
.fullscreen-editor-modal.ant-modal-wrap .ant-modal {
  max-width: 90vw;
}
.fullscreen-editor-modal .ant-modal-body {
  display: flex;
  flex-direction: column;
}
</style>
