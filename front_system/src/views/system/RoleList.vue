<template>
  <div class="role-container">
    <RoleForm 
      ref="roleFormRef"
      @refresh="handleRefresh"
    />
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="角色名称">
          <a-input v-model:value="searchForm.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" style="width: 120px" allowClear>
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="fetchData">查询</a-button>
          <a-button style="margin-left: 10px" @click="resetSearch">重置</a-button>
          <a-button 
            v-if="hasAddPermission"
            type="primary" 
            style="margin-left: 10px" 
            @click="handleAdd"
          >
            新增角色
          </a-button>
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
        <template v-else-if="column.key === 'action'">
          <a-space :size="8">
            <a-button 
              v-if="hasViewPermission"
              type="link" 
              style="padding: 0" 
              @click="handleView(record)"
            >
              详情
            </a-button>
            <a-button 
              v-if="hasEditPermission"
              type="link" 
              style="padding: 0" 
              @click="handleEdit(record)"
            >
              编辑
            </a-button>
            <a-button 
              v-if="hasBindMenuPermission"
              type="link" 
              style="padding: 0" 
              @click="handleBindMenus(record)"
            >
              绑定菜单
            </a-button>
            <a-popconfirm
              v-if="hasDeletePermission"
              title="确认删除该角色?"
              ok-text="确认"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger style="padding: 0">删除</a-button>
            </a-popconfirm>
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
<MenuSelectModal ref="menuSelectModalRef" @success="handleRefresh" />
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePagination, resetPagination } from '@/utils/pagination'
import { usePermission } from '@/composables/usePermission'
import RoleForm from './RoleForm.vue'
import MenuSelectModal from './MenuSelectModal.vue'

// 权限检查
const { checkPermission } = usePermission()
const hasAddPermission = computed(() => checkPermission('system:role:add'))
const hasViewPermission = computed(() => checkPermission('system:role:view'))
const hasEditPermission = computed(() => checkPermission('system:role:edit'))
const hasDeletePermission = computed(() => checkPermission('system:role:delete'))
const hasBindMenuPermission = computed(() => checkPermission('system:role:bindMenu'))

const roleFormRef = ref()
const menuSelectModalRef = ref()
const dataSource = ref([])
const pagination = usePagination()
const searchForm = reactive({
  roleName: undefined,
  status: undefined
})

// 检查是否有任何操作按钮权限
const hasAnyActionPermission = computed(() => {
  return hasViewPermission.value || hasEditPermission.value || hasBindMenuPermission.value || hasDeletePermission.value
})

const columns = computed(() => {
  const baseColumns = [
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName'
    },
    {
      title: '角色描述',
      key: 'description',
      dataIndex: 'description'
    },
    {
      title: '状态',
      key: 'status',
      dataIndex: 'status'
    },
    {
      title: '创建时间', 
      key: 'createTime',
      dataIndex: 'createTime'
    },
  ]
  
  // 只有当至少有一个操作按钮有权限时才添加操作列
  if (hasAnyActionPermission.value) {
    baseColumns.push({
      title: '操作',
      key: 'action',
      width: 280
    })
  }
  
  return baseColumns
})

const fetchData = async () => {
  try {
    const res = await post('/sysRole/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        roleName: searchForm.roleName,
        status: searchForm.status
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
    message.error('获取角色列表失败')
  }
}

const resetSearch = () => {
  searchForm.roleName = undefined
  searchForm.status = undefined
  resetPagination(pagination)
  fetchData()
}

const handleTableChange = (page, pageSize) => {
  if (page) pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const handleAdd = async () => {
  roleFormRef.value?.open()
}

const handleView = async (record) => {
  roleFormRef.value?.open(record.id, 'detail')
}

const handleEdit = async (record) => {
  roleFormRef.value?.open(record.id, 'edit')
}

const handleRefresh = () => {
  fetchData()
}

const handleDelete = async (id) => {
  try {
    await post(`/sysRole/remove/${id}`)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
    message.error('删除失败')
  }
}

const handleBindMenus = async (record) => {
  try {
    const res = await post('/sysRoleMenu/page', {
      pageNum: 1,
      pageSize: 1000,
      data: { roleId: record.id }
    })
    const selectedMenuIds = res.data.map(item => item.menuId)
    menuSelectModalRef.value?.open(record.id, selectedMenuIds)
  } catch (error) {
    console.error(error)
    message.error('获取角色菜单失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.role-container {
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

.role-container :deep(.ant-table-wrapper) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.role-container :deep(.ant-table) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 100%;
  table-layout: fixed;
}

.role-container :deep(.ant-table-container) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.role-container :deep(.ant-table-body) {
  overflow-y: auto !important;
  overflow-x: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.role-container :deep(.ant-table-body)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.role-container :deep(.ant-table-thead) {
  flex-shrink: 0;
}

.role-container :deep(.ant-table-tbody) {
  overflow-y: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.role-container :deep(.ant-table-tbody)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
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
