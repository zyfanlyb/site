<template>
  <a-modal
    v-model:open="visible"
    title="角色绑定" 
    width="800px"
    :confirm-loading="confirmLoading"
    ok-text="确认"
    cancel-text="取消"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-table
      :columns="columns"
      :data-source="roleList"
      :row-key="record => record.id"
      :row-selection="rowSelection"
      :pagination="false"
    >
      <template #bodyCell="{ column, text }">
        <template v-if="column.key === 'status'">
          <a-tag :color="text ? 'green' : 'red'">
            {{ text ? '启用' : '禁用' }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'

const emit = defineEmits(['success'])
const visible = ref(false)
const confirmLoading = ref(false)
const selectedRoleIds = ref([])
const roleList = ref([])
const currentUserId = ref(null)

const columns = reactive([
  {
    title: '角色名称',
    dataIndex: 'roleName',
    key: 'roleName',
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
  },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
  },
])

const rowSelection = computed(() => {
  return {
    selectedRowKeys: selectedRoleIds.value,
    onChange: (selectedRowKeys) => {
      selectedRoleIds.value = selectedRowKeys
    },
    type: 'checkbox'
  }
})

const fetchRoleList = async () => {
  try {
    const res = await post('/sysRole/page', {
      pageNum: 1,
      pageSize: 1000,
      data: {}
    })
    roleList.value = res.data
  } catch (error) {
    console.error(error)
    message.error('获取角色列表失败')
  }
}

const open = (userId, selectedIds = []) => {
  currentUserId.value = userId
  selectedRoleIds.value = selectedIds
  visible.value = true
  fetchRoleList()
}

const handleOk = async () => {
  confirmLoading.value = true
  try {
    await post('/sysUserRole/update', {
      data: {
        userId: currentUserId.value, 
        roleIds: selectedRoleIds.value
      }
    })
    visible.value = false
    emit('success')
    message.success('角色绑定成功')
  } catch (error) {
    console.error(error)
    message.error('角色绑定失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
}

defineExpose({ open })
</script>
