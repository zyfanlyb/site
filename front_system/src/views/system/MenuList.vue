<template>
  <div class="menu-container">
    <MenuForm 
      ref="menuFormRef"
      :menu-tree-data="menuTreeData"
      @refresh="handleRefresh"
    />
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="菜单名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入菜单名称" />
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
            <a-button 
              v-if="hasAddPermission" 
              type="primary" 
              @click="handleAdd"
            >
              新增菜单
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <div class="table-wrapper">
      <a-table
          :columns="columns"
          :data-source="dataSource"
          :row-key="record => record.id"
          :childrenColumnName="'children'"
          :defaultExpandAllRows="true"
          :pagination="false"
          :scroll="{ y: 'calc(100vh - 450px)' }"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="text ? 'green' : 'red'">
              {{ text ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'type'">
            <span>{{ getTypeText(text) }}</span>
          </template>
          <template v-else-if="column.key === 'perms'">
            <span v-if="record.type === '1' || record.type === '2'" style="color: #1890ff; white-space: nowrap;">
              {{ text || '-' }}
            </span>
            <span v-else style="color: #bfbfbf;">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button 
                v-if="hasViewPermission"
                size="small" 
                type="link" 
                @click="handleView(record)"
              >
                详情
              </a-button>
              <a-button 
                v-if="hasEditPermission"
                size="small" 
                type="link" 
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                  v-if="hasDeletePermission"
                  title="确认删除该菜单?"
                  ok-text="确认"
                  cancel-text="取消"
                  @confirm="handleDelete(record.id)"
              >
                <a-button size="small" type="link" danger>删除</a-button>
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
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePagination, resetPagination } from '@/utils/pagination'
import { usePermission } from '@/composables/usePermission'
import MenuForm from './MenuForm.vue'

// 权限检查
const { checkPermission } = usePermission()
const hasAddPermission = computed(() => checkPermission('system:menu:add'))
const hasViewPermission = computed(() => checkPermission('system:menu:view'))
const hasEditPermission = computed(() => checkPermission('system:menu:edit'))
const hasDeletePermission = computed(() => checkPermission('system:menu:delete'))

// 检查是否有任何操作按钮权限
const hasAnyActionPermission = computed(() => {
  return hasViewPermission.value || hasEditPermission.value || hasDeletePermission.value
})

const menuFormRef = ref()
const menuTreeData = ref([])
const dataSource = ref([])
const pagination = usePagination()
const searchForm = reactive({
  name: undefined,
  status: undefined
})

const columns = computed(() => {
  const baseColumns = [
    {
      title: '菜单名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '菜单路径',
      dataIndex: 'path',
      key: 'path',
    },
    {
      title: '类型',
      key: 'type',
      dataIndex: 'type',
    },
    {
      title: '接口权限',
      key: 'perms',
      dataIndex: 'perms',
      width: 300,
    },
    {
      title: '状态',
      key: 'status',
      dataIndex: 'status',
    },
    {
      title: '创建时间', 
      key: 'createTime',
      dataIndex: 'createTime',
    },
  ]
  
  // 只有当至少有一个操作按钮有权限时才添加操作列
  if (hasAnyActionPermission.value) {
    baseColumns.push({
      title: '操作',
      key: 'action',
    })
  }
  
  return baseColumns
})

const getTypeText = (type) => {
  const map = {
    '0': '目录',
    '1': '菜单',
    '2': '按钮'
  }
  return map[type] || '-'
}

const fetchData = async () => {
  try {
    const res = await post('/sysMenu/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        name: searchForm.name || undefined,
        status: searchForm.status ? Boolean(Number(searchForm.status)) : undefined,
      },
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
    message.error('获取菜单列表失败')
  }
}

const resetSearch = () => {
  searchForm.name = undefined
  searchForm.status = undefined
  resetPagination(pagination)
  fetchData()
}

const handleTableChange = (page, pageSize) => {
  if (page) pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const getMenuTree = async () => {
  try {
    const res = await post('/sysMenu/page', { pageSize: 1000 })
    menuTreeData.value = res.data
  } catch (error) {
    console.error(error)
    message.error('获取菜单树失败')
  }
}

const handleAdd = async () => {
  menuFormRef.value?.open()
}

const handleView = async (record) => {
  await getMenuTree()
  menuFormRef.value?.open(record.id, 'detail')
}

const handleEdit = async (record) => {
  await getMenuTree()
  menuFormRef.value?.open(record.id, 'edit')
}

const handleRefresh = () => {
  fetchData()
}

const handleDelete = async (id) => {
  try {
    await post(`/sysMenu/remove/${id}`)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.menu-container {
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

.menu-container :deep(.ant-table-wrapper) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.menu-container :deep(.ant-table) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 100%;
  table-layout: fixed;
}

.menu-container :deep(.ant-table-container) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.menu-container :deep(.ant-table-body) {
  overflow-y: auto !important;
  overflow-x: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.menu-container :deep(.ant-table-body)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.menu-container :deep(.ant-table-thead) {
  flex-shrink: 0;
}

.menu-container :deep(.ant-table-tbody) {
  overflow-y: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.menu-container :deep(.ant-table-tbody)::-webkit-scrollbar {
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
