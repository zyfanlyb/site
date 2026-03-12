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
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="标题">
          <a-input v-model:value="searchForm.title" placeholder="请输入文章标题" style="width: 200px" />
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
          </a-space>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-wrapper">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :row-key="record => record.id"
        :pagination="pagination"
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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, FolderOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import { usePermission } from '@/composables/usePermission'
import ArticleForm from './ArticleForm.vue'
import ArticleDetail from './ArticleDetail.vue'
import CategoryConfig from './CategoryConfig.vue'

// 权限检查
const { checkPermission } = usePermission()
const hasAddPermission = computed(() => checkPermission('cms:article:add'))
const hasViewPermission = computed(() => checkPermission('cms:article:view'))
const hasEditPermission = computed(() => checkPermission('cms:article:edit'))
const hasDeletePermission = computed(() => checkPermission('cms:article:delete'))
const hasCategoryConfigPermission = computed(() => checkPermission('cms:category:page'))

const articleFormRef = ref(null)
const articleDetailRef = ref(null)
const categoryConfigRef = ref(null)
const loading = ref(false)
const dataSource = ref([])
const searchForm = reactive({
  title: '',
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
  onChange: (page, pageSize) => {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchData()
  }
})

// 操作列根据权限动态显示
const hasAnyActionPermission = computed(() => hasViewPermission.value || hasEditPermission.value || hasDeletePermission.value)

const columns = computed(() => {
  const baseColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', width: 120, ellipsis: true },
    { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
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
        title: searchForm.title || undefined,
        status: searchForm.status !== undefined ? Number(searchForm.status) : undefined
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
    console.error('获取文章列表失败:', error)
    message.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.title = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  articleFormRef.value?.open()
}

const handleConfigCategory = () => {
  categoryConfigRef.value?.open()
}

const handleCategoryUpdated = () => {
  articleFormRef.value?.refreshCategories?.()
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
  padding: 24px;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-container {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.table-wrapper {
  flex: 1;
  overflow: hidden;
}
</style>
