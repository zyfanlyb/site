<template>
  <a-config-provider :locale="zhCN">
  <a-modal
    v-model:open="showModal"
    :title="title"
    :confirm-loading="confirmLoading"
    :footer="null"
    :width="600"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="角色名称" name="roleName">
        <a-input 
          v-model:value="formState.roleName"
          placeholder="请输入角色名称"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-switch 
          v-model:checked="formState.status"
          checked-children="启用"
          un-checked-children="禁用"
          :disabled="isDetail"
        />
      </a-form-item>

      <a-form-item label="角色描述" name="description">
        <a-textarea 
          v-model:value="formState.description"
          placeholder="请输入角色描述"
          :disabled="isDetail"
          :rows="4"
        />
      </a-form-item>

      <template v-if="isDetail">
        <a-form-item label="创建时间">
          <div class="detail-value">{{ formState.createTime || '-' }}</div>
        </a-form-item>
        <a-form-item label="更新时间">
          <div class="detail-value">{{ formState.updateTime || '-' }}</div>
        </a-form-item>
        <a-form-item label="创建人">
          <div class="detail-value">{{ formState.createBy || '-' }}</div>
        </a-form-item>
        <a-form-item label="更新人">
          <div class="detail-value">{{ formState.updateBy || '-' }}</div>
        </a-form-item>
      </template>

      <div class="form-footer" v-if="!isDetail">
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" style="margin-left: 10px" @click="handleOk">确定</a-button>
      </div>
    </a-form>
    </a-modal>
  </a-config-provider>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { message, ConfigProvider } from 'ant-design-vue'
import { post } from '@/utils/request'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

const showModal = ref(false)
const formRef = ref()
const confirmLoading = ref(false)

const mode = ref('add')
const title = ref('新增角色')
const isDetail = computed(() => mode.value === 'detail')

const formState = reactive({
  id: null,
  roleName: '',
  description: '',
  status: true,
  createTime: '',
  updateTime: '',
  createBy: '',
  updateBy: ''
})

const rules = reactive({
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
})

const open = (id = null, modalMode = 'add') => {
  showModal.value = true
  mode.value = modalMode
  title.value = modalMode === 'detail' ? '角色详情' : modalMode === 'edit' ? '编辑角色' : '新增角色'
  
  if (id) {
    loadRoleInfo(id)
  } else {
    formRef.value?.resetFields()
    formState.id = null
    formState.status = true
  }
}

const loadRoleInfo = async (id) => {
  try {
    const res = await post(`/sysRole/info/${id}`)
    Object.assign(formState, res.data)
  } catch (error) {
    console.error(error)
    message.error('获取角色详情失败')
  }
}

// 清空表单数据
const resetForm = () => {
  formRef.value?.resetFields()
  formState.id = null
  formState.roleName = ''
  formState.description = ''
  formState.status = true
  formState.createTime = ''
  formState.updateTime = ''
  formState.createBy = ''
  formState.updateBy = ''
}

const handleOk = async () => {
  try {
    await formRef.value.validateFields()
    confirmLoading.value = true
    
    // 根据是否有ID调用不同接口
    const url = formState.id ? '/sysRole/update' : '/sysRole/insert'
    const response = await post(url, { data: formState })
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
.detail-value {
  padding: 8px 0;
  color: rgba(0, 0, 0, 0.85);
}
</style>
