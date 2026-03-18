<template>
  <div class="article-container">
    <ArticleForm 
      ref="articleFormRef"
      @refresh="handleRefresh"
    />
    <ArticleDetail ref="articleDetailRef" />
    <CategoryConfig
      ref="categoryConfigRef"
      @updated="handleCategoryUpdated"
    />
    <TypeConfig
      ref="typeConfigRef"
      @updated="handleTypeUpdated"
    />
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="关键词">
          <a-input v-model:value="searchForm.keyword" placeholder="模糊搜索标题/摘要/内容" style="width: 220px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" style="width: 120px" allowClear>
            <a-select-option value="1">已发布</a-select-option>
            <a-select-option value="0">草稿</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
            <a-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增
            </a-button>
            <a-button v-if="hasCategoryConfigPermission" @click="handleConfigCategory">
              <template #icon><FolderOutlined /></template>
              文章分类
            </a-button>
            <a-button v-if="hasTypeConfigPermission" @click="handleConfigType">
              <template #icon><TagsOutlined /></template>
              文章类型
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
        :pagination="false"
        :loading="loading"
        :scroll="{ y: 'calc(100vh - 450px)' }"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="text ? 'green' : 'orange'">
              {{ text ? '已发布' : '草稿' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            <span>{{ formatDate(text) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="hasViewPermission" type="link" size="small" @click="handleDetail(record)">详情</a-button>
              <a-button v-if="hasEditPermission" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button v-if="hasDeletePermission" type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, FolderOutlined, TagsOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import { usePermission } from '@/composables/usePermission'
import { usePagination, resetPagination } from '@/utils/pagination'
import ArticleForm from './ArticleForm.vue'
import ArticleDetail from './ArticleDetail.vue'
import CategoryConfig from './CategoryConfig.vue'
import TypeConfig from './TypeConfig.vue'

// 权限检查
const { checkPermission } = usePermission()
const hasAddPermission = computed(() => checkPermission('cms:article:add'))
const hasViewPermission = computed(() => checkPermission('cms:article:view'))
const hasEditPermission = computed(() => checkPermission('cms:article:edit'))
const hasDeletePermission = computed(() => checkPermission('cms:article:delete'))
const hasCategoryConfigPermission = computed(() => checkPermission('cms:category:page'))
const hasTypeConfigPermission = computed(() => checkPermission('cms:type:page'))

const articleFormRef = ref(null)
const articleDetailRef = ref(null)
const categoryConfigRef = ref(null)
const typeConfigRef = ref(null)
const loading = ref(false)
const dataSource = ref([])
const searchForm = reactive({
  keyword: '',
  status: undefined
})

const pagination = usePagination()

// 操作列根据权限动态显示
const hasAnyActionPermission = computed(() => hasViewPermission.value || hasEditPermission.value || hasDeletePermission.value)

const columns = computed(() => {
  const baseColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', width: 120, ellipsis: true },
    { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
    { title: '类型', dataIndex: 'typeName', key: 'typeName', width: 100 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
  ]
  if (hasAnyActionPermission.value) {
    baseColumns.push({ title: '操作', key: 'action', width: 130, fixed: 'right' })
  }
  return baseColumns
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await post('/cms/article/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        keyword: searchForm.keyword || undefined,
        status: searchForm.status !== undefined ? Number(searchForm.status) : undefined
      }
    })
    // 兼容两种返回：data 为数组时用 res.page?.total；data 为分页对象时用 res.data.records 与 res.data.total
    if (Array.isArray(res.data)) {
      dataSource.value = res.data
      pagination.total = res.page?.total ?? res.data.length
    } else if (res.data && Array.isArray(res.data.records)) {
      dataSource.value = res.data.records
      pagination.total = res.data.total ?? 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
    message.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (page, pageSize) => {
  if (page) pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  resetPagination(pagination)
  fetchData()
}

const handleAdd = () => {
  articleFormRef.value?.open()
}

const handleConfigCategory = () => {
  categoryConfigRef.value?.open()
}

const handleConfigType = () => {
  typeConfigRef.value?.open()
}

const handleCategoryUpdated = () => {
  articleFormRef.value?.refreshCategories?.()
}

const handleTypeUpdated = () => {
  articleFormRef.value?.refreshTypes?.()
}

const handleDetail = (record) => {
  articleDetailRef.value?.open(record.id)
}

const handleEdit = (record) => {
  articleFormRef.value?.open(record.id)
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文章"${record.title}"吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await post(`/cms/article/remove/${record.id}`)
        message.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

const handleRefresh = () => {
  fetchData()
}

const formatDate = (date) => {
  if (!date) return '-'
  return date
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.article-container {
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

.article-container :deep(.ant-table-wrapper) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.article-container :deep(.ant-table) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 100%;
  table-layout: fixed;
}

.article-container :deep(.ant-table-container) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.article-container :deep(.ant-table-body) {
  overflow-y: auto !important;
  overflow-x: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.article-container :deep(.ant-table-body)::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.article-container :deep(.ant-table-thead) {
  flex-shrink: 0;
}

.article-container :deep(.ant-table-tbody) {
  overflow-y: auto !important;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.article-container :deep(.ant-table-tbody)::-webkit-scrollbar {
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
