<template>
  <a-modal
    v-model:open="showModal"
    :title="title"
    :confirm-loading="confirmLoading"
    :footer="null"
    :width="700"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="用户名" name="username">
        <a-input 
          v-model:value="formState.username" 
          placeholder="请输入用户名"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item v-if="!isDetail" label="密码" name="password">
        <a-input-password 
          v-model:value="formState.password" 
          :placeholder="isEdit ? '修改密码' : '请输入密码'"
        />
      </a-form-item>

      <a-form-item label="昵称" name="nickName">
        <a-input 
          v-model:value="formState.nickName" 
          placeholder="请输入昵称"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="手机号" name="phone">
        <a-input 
          v-model:value="formState.phone" 
          placeholder="请输入手机号"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="邮箱" name="email">
        <a-input 
          v-model:value="formState.email" 
          placeholder="请输入邮箱"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="用户类型" name="userType">
        <a-radio-group 
          v-model:value="formState.userType"
          :disabled="isDetail"
        >
          <a-radio :value="false">内部用户</a-radio>
          <a-radio :value="true">外部用户</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-switch 
          v-model:checked="formState.status"
          checked-children="启用"
          un-checked-children="禁用"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="头像" name="avatar">
        <template v-if="isDetail">
          <a-image
            v-if="avatarPreviewUrl"
            :src="avatarPreviewUrl"
            :width="104"
            :height="104"
            class="avatar-preview-image"
          />
          <div v-else class="avatar-empty">暂无头像</div>
        </template>
        <template v-else>
          <a-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :custom-request="handleUpload"
            :before-upload="beforeUpload"
            @preview="handlePreview"
            @change="handleChange"
            @remove="handleRemove"
            :max-count="1"
            accept="image/*"
          >
            <div v-if="fileList.length < 1">
              <plus-outlined />
              <div style="margin-top: 8px">上传</div>
            </div>
          </a-upload>
        </template>
      </a-form-item>

      <div class="form-footer">
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button 
            type="primary" 
            @click="handleOk"
            v-if="!isDetail"
          >
            确定
          </a-button>
        </a-space>
      </div>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import service from '@/utils/request'
import { getAuthFilePreviewUrl } from '@/utils/file'

const showModal = ref(false)
const formRef = ref()
const confirmLoading = ref(false)
const fileList = ref([])
const avatarPreviewUrl = ref('')

const mode = ref('add')
const title = ref('新增用户')
const isDetail = computed(() => mode.value === 'detail')
const isEdit = computed(() => mode.value === 'edit')

const formState = reactive({
  id: null,
  username: '',
  password: '',
  nickName: '',
  phone: '',
  email: '',
  avatar: '',
  userType: false,
  status: true,
  createTime: '',
  updateTime: '',
  createBy: '',
  updateBy: ''
})

const rules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { 
      validator: (_, value) => {
        // 编辑模式下允许空值
        if (isEdit.value && !value) return Promise.resolve();
        // 新增模式下必填
        if (!isEdit.value && !value) return Promise.reject('请输入密码');
        // 校验复杂度
        if (!/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\S+$).{8,20}$/.test(value)) {
          return Promise.reject('密码需8-20位，包含大小写字母和数字');
        }
        return Promise.resolve();
      },
      trigger: 'blur' 
    }
  ],
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

const open = (id = null, modalMode = 'add') => {
  showModal.value = true
  mode.value = modalMode
  title.value = modalMode === 'detail' ? '用户详情' : modalMode === 'edit' ? '编辑用户' : '新增用户'
  
  if (id) {
    loadUserInfo(id)
  } else {
    formRef.value?.resetFields()
    formState.id = null
    formState.password = ''
    formState.status = true
    formState.userType = false
    formState.avatar = ''
    fileList.value = []
  }
}

const loadUserInfo = async (id) => {
  try {
    const res = await post(`/sysUser/info/${id}`)
    Object.assign(formState, res.data)
    formState.password = ''
    if (res.data.avatar) {
      // 使用预览URL显示头像（异步获取blob URL）
      const previewUrl = await getAuthFilePreviewUrl(res.data.avatar)
      avatarPreviewUrl.value = previewUrl
      fileList.value = [{
        uid: '-1',
        name: 'avatar',
        status: 'done',
        url: previewUrl,
        filePath: res.data.avatar // 保存原始文件路径
      }]
    } else {
      avatarPreviewUrl.value = ''
      fileList.value = []
    }
  } catch (error) {
    console.error(error)
    message.error('获取用户详情失败')
  }
}

// 文件上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false // 校验失败，阻止文件添加到列表
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB!')
    return false // 校验失败，阻止文件添加到列表
  }
  return true // 校验通过，允许文件添加到列表，customRequest会处理实际上传
}

/**
 * 头像仅本地预览；与 /sysUser/insert|update 一并 multipart 提交，避免未点确定就落 MinIO。
 */
const handleUpload = ({ file, onSuccess }) => {
  const previewUrl = URL.createObjectURL(file)
  const idx = fileList.value.findIndex((item) => item.uid === file.uid)
  const patch = {
    uid: file.uid,
    name: file.name || 'avatar',
    status: 'done',
    url: previewUrl,
    originFileObj: file
  }
  if (idx > -1) {
    fileList.value[idx] = { ...fileList.value[idx], ...patch }
  } else {
    fileList.value.push(patch)
  }
  formState.avatar = ''
  onSuccess({}, file)
}

// 文件列表变化
const handleChange = ({ fileList: newFileList }) => {
  fileList.value = newFileList
}

// 移除文件
const handleRemove = () => {
  formState.avatar = ''
  return true
}

// 预览图片
const handlePreview = async (file) => {
  // 优先按文件路径走 /file/preview，与头像显示逻辑一致
  const filePath = file.filePath || formState.avatar
  if (filePath) {
    const previewUrl = await getAuthFilePreviewUrl(filePath)
    if (previewUrl) {
      window.open(previewUrl, '_blank')
      return
    }
  }

  // 兜底：对于本地未上传文件，使用上传组件已有的临时地址
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

// 清空表单数据
const resetForm = () => {
  formRef.value?.resetFields()
  formState.id = null
  formState.username = ''
  formState.password = ''
  formState.nickName = ''
  formState.phone = ''
  formState.email = ''
  formState.avatar = ''
  formState.userType = false
  formState.status = true
  formState.createTime = ''
  formState.updateTime = ''
  formState.createBy = ''
  formState.updateBy = ''
  avatarPreviewUrl.value = ''
  fileList.value = []
}

const handleOk = async () => {
  try {
    await formRef.value.validate()
    confirmLoading.value = true

    const avatarItem = fileList.value[0]
    const newAvatarFile = avatarItem?.originFileObj

    const formData = { ...formState }
    if (isEdit.value && !formData.password) {
      delete formData.password
    }

    if (newAvatarFile) {
      formData.avatar = ''
    } else if (avatarItem?.filePath) {
      formData.avatar = avatarItem.filePath
    } else {
      formData.avatar = ''
    }

    const url = isEdit.value ? '/sysUser/update' : '/sysUser/insert'
    const body = { data: formData }

    if (newAvatarFile) {
      const fd = new FormData()
      fd.append('data', JSON.stringify(body))
      fd.append('avatarFile', newAvatarFile)
      await service.post(url, fd)
    } else {
      await post(url, body)
    }

    message.success('操作成功')
    showModal.value = false
    emits('refresh')
  } catch (error) {
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  showModal.value = false
}

// 监听模态框关闭，每次关闭时清空表单
watch(showModal, (newVal) => {
  if (!newVal) {
    // 模态框关闭时清空表单
    resetForm()
  }
})

defineExpose({
  open
})

const emits = defineEmits(['refresh'])
</script>

<style scoped>
.form-footer {
  text-align: center;
  margin-top: 24px;
}

.avatar-preview-image :deep(.ant-image-img) {
  border-radius: 8px;
  object-fit: cover;
}

.avatar-empty {
  width: 104px;
  height: 104px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
