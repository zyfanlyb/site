<template>
  <div class="user-container">
    <UserForm 
      ref="userFormRef"
      @refresh="handleRefresh"
    />
    <RoleSelectModal 
      ref="roleSelectModalRef"
      @success="handleRefresh"
    />
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="searchForm.nickName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" style="width: 120px" allowClear>
            <a-select-option value="1">启用</a-select-option>
            <a-select-option value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-wrapper">
      <a-table
          :columns="columns"
          :data-source="dataSource"
          :row-key="record => record.id"
          :pagination="false"
          :scroll="{ y: 'calc(100vh - 450px)' }"
      >
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="text ? 'green' : 'red'">
            {{ text ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'userType'">
          <span>{{ getUserTypeText(text) }}</span>
        </template>
        <template v-else-if="column.key === 'createTime'">
          <span>{{ text }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a 
              v-if="hasViewPermission" 
              style="white-space: nowrap;" 
              @click="handleView(record)"
            >
              详情
            </a>
            <a 
              v-if="hasBindRolePermission" 
              style="white-space: nowrap;" 
              @click="handleBindRoles(record)"
            >
              角色绑定
            </a>
          </a-space>
        </template>
      </template>
      </a-table>
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="false"
          :show-total="(total) => `共 ${total} 条`"
          @change="handleTableChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePagination, createPaginationHandler, resetPagination } from '@/utils/pagination'
import { usePermission } from '@/composables/usePermission'
import UserForm from './UserForm.vue'
import RoleSelectModal from './RoleSelectModal.vue'

// 权限检查
const { checkPermission } = usePermission()
const hasViewPermission = computed(() => checkPermission('system:user:view'))
const hasBindRolePermission = computed(() => checkPermission('system:user:bindRole'))

const userFormRef = ref()
const dataSource = ref([])
const pagination = usePagination()
const searchForm = reactive({
  username: undefined,
  nickName: undefined,
  status: undefined
})

// 检查是否有任何操作按钮权限
const hasAnyActionPermission = computed(() => {
  return hasViewPermission.value || hasBindRolePermission.value
})

const columns = computed(() => {
  const baseColumns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      ellipsis: true,
    },
    {
      title: '昵称',
      dataIndex: 'nickName',
      key: 'nickName',
      ellipsis: true,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      ellipsis: true,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      ellipsis: true,
    },
    {
      title: '用户类型',
      key: 'userType',
      dataIndex: 'userType',
      width: 100,
    },
    {
      title: '角色',
      key: 'roleNames',
      dataIndex: 'roleNames',
      ellipsis: true,
    },
    {
      title: '状态',
      key: 'status',
      dataIndex: 'status',
      width: 80,
    },
    {
      title: '创建时间', 
      key: 'createTime',
      dataIndex: 'createTime',
      width: 200,
      ellipsis: false,
    },
  ]
  
  // 只有当至少有一个操作按钮有权限时才添加操作列
  if (hasAnyActionPermission.value) {
    baseColumns.push({
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
    })
  }
  
  return baseColumns
})

const getUserTypeText = (type) => {
  if (type === false || type === 0 || type === '0') {
    return '内部用户'
  } else if (type === true || type === 1 || type === '1') {
    return '外部用户'
  }
  return '-'
}

const fetchData = async () => {
  try {
    const res = await post('/sysUser/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        username: searchForm.username,
        nickName: searchForm.nickName,
        status: searchForm.status ? Boolean(Number(searchForm.status)) : undefined
      }
    })
    // 统一响应格式：data为数组，page包含分页信息
    if (Array.isArray(res.data)) {
      dataSource.value = res.data
      pagination.total = res.page?.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error(error)
    message.error('获取用户列表失败')
  }
}

const resetSearch = () => {
  searchForm.username = undefined
  searchForm.nickName = undefined
  searchForm.status = undefined
  resetPagination(pagination)
  fetchData()
}

const handleTableChange = (page, pageSize) => {
  if (page) pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const handleView = async (record) => {
  userFormRef.value?.open(record.id, 'detail')
}

const handleRefresh = () => {
  fetchData()
}

const roleSelectModalRef = ref()

const handleBindRoles = async (record) => {
  try {
    // 获取用户已绑定的角色
    const res = await post('/sysUserRole/page', {
      pageNum: 1,
      pageSize: 1000,
      data: { 
        userId: record.id 
      }
    })
    const selectedRoleIds = res.data.map(item => item.roleId)
    roleSelectModalRef.value?.open(record.id, selectedRoleIds)
  } catch (error) {
    console.error(error)
    message.error('获取用户角色失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.user-container {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.search-container {
  height: 12%;
  min-height: 80px;
  max-height: 120px;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  flex-shrink: 0;
  overflow-y: auto;
  border-radius: var(--sky-radius-md);
}

.table-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
  background: #fff;
  border-radius: var(--sky-radius-md);
}

.user-container :deep(.ant-table-wrapper) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
  padding-bottom: 0;
}

.user-container :deep(.ant-table) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 100%;
  table-layout: fixed;
}

.user-container :deep(.ant-table-container) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.user-container :deep(.ant-table-body) {
  overflow-y: auto !important;
  overflow-x: auto !important;
  margin-bottom: 0 !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.user-container :deep(.ant-table-body)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.user-container :deep(.ant-table-thead) {
  flex-shrink: 0;
}

.user-container :deep(.ant-table-tbody) {
  overflow-y: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.user-container :deep(.ant-table-tbody)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.pagination-wrapper {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #fff;
}
</style>
