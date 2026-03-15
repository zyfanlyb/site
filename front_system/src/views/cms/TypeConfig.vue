<template>
  <a-modal
    v-model:open="visible"
    title="文章类型"
    width="660px"
    :footer="null"
    @cancel="handleCancel"
  >
    <!-- 搜索区域 -->
    <div class="search-bar">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="分类">
          <a-select v-model:value="searchForm.categoryId" placeholder="请选择分类" style="width: 180px" allowClear>
            <a-select-option v-for="item in categoryList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="类型名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入类型名称" style="width: 150px" allowClear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button v-if="hasAddPermission" type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增类型
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :row-key="record => record.id"
      :pagination="pagination"
      :loading="loading"
      size="small"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.key === 'categoryName'">
          {{ getCategoryName(record.categoryId) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="text ? 'green' : 'red'">
            {{ text ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space size="small">
            <a-button v-if="hasEditPermission" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button v-if="hasDeletePermission" type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑表单弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="formData.id ? '编辑类型' : '新增类型'"
      width="480px"
      :confirmLoading="saveLoading"
      okText="确认"
      cancelText="取消"
      @ok="handleSave"
      @cancel="handleFormCancel"
    >
      <a-form :model="formData" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="所属分类" required>
          <a-select v-model:value="formData.categoryId" placeholder="请选择分类" :disabled="!!formData.id">
            <a-select-option v-for="item in categoryList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="类型名称" required>
          <a-input v-model:value="formData.name" placeholder="请输入类型名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="formData.description" placeholder="请输入描述" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">启用</a-radio>
            <a-radio :value="false">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import { usePermission } from '@/composables/usePermission'

const emit = defineEmits(['updated'])

const { checkPermission } = usePermission()
const hasAddPermission = computed(() => checkPermission('cms:type:add'))
const hasEditPermission = computed(() => checkPermission('cms:type:edit'))
const hasDeletePermission = computed(() => checkPermission('cms:type:delete'))
const hasAnyActionPermission = computed(() => hasEditPermission.value || hasDeletePermission.value)

const visible = ref(false)
const formVisible = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const dataSource = ref([])
const categoryList = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const searchForm = reactive({
  categoryId: undefined,
  name: ''
})

const formData = reactive({
  id: null,
  categoryId: undefined,
  name: '',
  description: '',
  sort: 0,
  status: true
})

const columns = computed(() => {
  const baseColumns = [
    { title: '类型名称', dataIndex: 'name', key: 'name', width: 120 },
    { title: '所属分类', dataIndex: 'categoryId', key: 'categoryName', width: 120 },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80 }
  ]
  if (hasAnyActionPermission.value) {
    baseColumns.push({ title: '操作', key: 'action', width: 120 })
  }
  return baseColumns
})

const getCategoryName = (categoryId) => {
  if (!categoryId) return '-'
  const cat = categoryList.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

const fetchCategoryList = async () => {
  try {
    const res = await post('/cms/category/list')
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const resetForm = () => {
  formData.id = null
  formData.categoryId = undefined
  formData.name = ''
  formData.description = ''
  formData.sort = 0
  formData.status = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await post('/cms/type/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        categoryId: searchForm.categoryId,
        name: searchForm.name?.trim() || undefined
      }
    })
    // 与分类一致：后端可能返回 data 为数组，分页信息在 res.page
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
    console.error('获取类型列表失败:', error)
    message.error('获取类型列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.categoryId = undefined
  searchForm.name = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleAdd = () => {
  resetForm()
  formVisible.value = true
}

const handleEdit = (record) => {
  formData.id = record.id
  formData.categoryId = record.categoryId
  formData.name = record.name
  formData.description = record.description || ''
  formData.sort = record.sort ?? 0
  formData.status = record.status !== false
  formVisible.value = true
}

const handleFormCancel = () => {
  formVisible.value = false
  resetForm()
}

const handleSave = async () => {
  if (!formData.categoryId) {
    message.warning('请选择所属分类')
    return
  }
  if (!formData.name?.trim()) {
    message.warning('请输入类型名称')
    return
  }
  try {
    if (formData.id) {
      await post('/cms/type/update', { data: formData })
      message.success('更新成功')
    } else {
      await post('/cms/type/insert', { data: formData })
      message.success('新增成功')
    }
    formVisible.value = false
    resetForm()
    fetchData()
    emit('updated')
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  }
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除类型"${record.name}"吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await post(`/cms/type/remove/${record.id}`)
        message.success('删除成功')
        fetchData()
        emit('updated')
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

const handleCancel = () => {
  searchForm.categoryId = undefined
  searchForm.name = ''
  resetForm()
}

const open = () => {
  searchForm.categoryId = undefined
  searchForm.name = ''
  resetForm()
  formVisible.value = false
  visible.value = true
  fetchCategoryList()
  fetchData()
}

watch(visible, (val) => {
  if (!val) {
    searchForm.categoryId = undefined
    searchForm.name = ''
    resetForm()
    formVisible.value = false
  }
})

defineExpose({
  open
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}
.search-bar :deep(.ant-form-item) {
  margin-bottom: 0;
}
</style>
