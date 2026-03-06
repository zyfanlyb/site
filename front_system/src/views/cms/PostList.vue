<template>
  <div class="cms-post">
    <a-card title="内容管理（博客 / 项目）" :bordered="false">
      <div class="search-container">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="类型">
            <a-select v-model:value="searchForm.type" style="width: 140px" allowClear>
              <a-select-option value="BLOG">博客</a-select-option>
              <a-select-option value="PROJECT">项目</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="searchForm.status" style="width: 140px" allowClear>
              <a-select-option value="DRAFT">草稿</a-select-option>
              <a-select-option value="PUBLISHED">已发布</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="标题">
            <a-input v-model:value="searchForm.title" placeholder="模糊搜索" style="width: 220px" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="fetchData">查询</a-button>
              <a-button @click="resetSearch">重置</a-button>
              <a-button type="primary" @click="openEditor()" v-if="canEdit">新增</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :row-key="r => r.id"
        :pagination="false"
        :scroll="{ y: 'calc(100vh - 450px)' }"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="text === 'PUBLISHED' ? 'green' : 'orange'">
              {{ text === 'PUBLISHED' ? '已发布' : '草稿' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag :color="text === 'PROJECT' ? 'blue' : 'geekblue'">
              {{ text === 'PROJECT' ? '项目' : '博客' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" type="link" @click="openEditor(record.id)" v-if="canView">详情</a-button>
              <a-button size="small" type="link" @click="openEditor(record.id, true)" v-if="canEdit">编辑</a-button>
              <a-popconfirm
                v-if="canPublish && record.status !== 'PUBLISHED'"
                title="确认发布？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="publish(record.id)"
              >
                <a-button size="small" type="link" danger>发布</a-button>
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
    </a-card>

    <a-modal
      v-model:open="editorOpen"
      :title="editorModeTitle"
      width="860px"
      ok-text="保存草稿"
      cancel-text="关闭"
      @ok="saveDraft"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="类型">
              <a-select v-model:value="editor.type" :disabled="!editorEditable">
                <a-select-option value="BLOG">博客</a-select-option>
                <a-select-option value="PROJECT">项目</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="slug（URL 标识）">
              <a-input v-model:value="editor.slug" :disabled="!editorEditable" placeholder="例如 my-first-post" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-input :value="editor.status || 'DRAFT'" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="标题">
          <a-input v-model:value="editor.title" :disabled="!editorEditable" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea v-model:value="editor.summary" :disabled="!editorEditable" :auto-size="{ minRows: 2, maxRows: 4 }" />
        </a-form-item>
        <a-form-item label="封面图 URL（可选）">
          <a-input v-model:value="editor.coverUrl" :disabled="!editorEditable" />
        </a-form-item>
        <a-form-item label="正文（Markdown）">
          <a-textarea v-model:value="editor.contentMd" :disabled="!editorEditable" :auto-size="{ minRows: 10, maxRows: 18 }" />
        </a-form-item>
      </a-form>

      <template #footer>
        <a-space>
          <a-button @click="editorOpen = false">关闭</a-button>
          <a-button type="primary" :loading="saving" @click="saveDraft" v-if="canEdit && editorEditable">
            保存草稿
          </a-button>
          <a-button danger type="primary" :loading="publishing" @click="publish(editor.id)" v-if="canPublish && editor.id && editor.status !== 'PUBLISHED'">
            发布
          </a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePagination, resetPagination } from '@/utils/pagination'
import { usePermission } from '@/composables/usePermission'

const { checkPermission } = usePermission()
const canEdit = computed(() => checkPermission('cms:post:edit'))
const canView = computed(() => checkPermission('cms:post:view'))
const canPublish = computed(() => checkPermission('cms:post:publish'))

const pagination = usePagination()
const dataSource = ref([])
const searchForm = reactive({
  type: undefined,
  status: undefined,
  title: undefined,
})

const columns = computed(() => {
  const cols = [
    { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
    { title: '标题', dataIndex: 'title', key: 'title' },
    { title: 'slug', dataIndex: 'slug', key: 'slug', width: 220 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  ]
  if (canView.value || canEdit.value || canPublish.value) {
    cols.push({ title: '操作', key: 'action', width: 180 })
  }
  return cols
})

const fetchData = async () => {
  try {
    const res = await post('/cms/post/page', {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      data: {
        type: searchForm.type || undefined,
        status: searchForm.status || undefined,
        title: searchForm.title || undefined,
      }
    })
    if (Array.isArray(res.data)) {
      dataSource.value = res.data
      pagination.total = res.page?.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error(e)
    message.error('获取列表失败')
  }
}

const resetSearch = () => {
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.title = undefined
  resetPagination(pagination)
  fetchData()
}

const handleTableChange = (page, pageSize) => {
  if (page) pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const editorOpen = ref(false)
const editorEditable = ref(true)
const saving = ref(false)
const publishing = ref(false)

const editor = reactive({
  id: undefined,
  type: 'BLOG',
  slug: '',
  title: '',
  summary: '',
  coverUrl: '',
  contentMd: '',
  status: 'DRAFT',
})

const editorModeTitle = computed(() => {
  if (!editor.id) return '新增内容'
  return editorEditable.value ? '编辑内容' : '内容详情'
})

const resetEditor = () => {
  editor.id = undefined
  editor.type = 'BLOG'
  editor.slug = ''
  editor.title = ''
  editor.summary = ''
  editor.coverUrl = ''
  editor.contentMd = ''
  editor.status = 'DRAFT'
}

const openEditor = async (id, editable = true) => {
  editorEditable.value = editable
  if (!id) {
    resetEditor()
    editorOpen.value = true
    return
  }
  try {
    const res = await post(`/cms/post/${id}`)
    const p = res?.data
    resetEditor()
    if (p) {
      editor.id = p.id
      editor.type = p.type || 'BLOG'
      editor.slug = p.slug || ''
      editor.title = p.title || ''
      editor.summary = p.summary || ''
      editor.coverUrl = p.coverUrl || ''
      editor.contentMd = p.contentMd || ''
      editor.status = p.status || 'DRAFT'
    }
    editorOpen.value = true
  } catch (e) {
    console.error(e)
    message.error('获取详情失败')
  }
}

const saveDraft = async () => {
  if (!canEdit.value || !editorEditable.value) return
  if (!editor.slug || !editor.title) {
    message.error('slug 和 标题必填')
    return
  }
  saving.value = true
  try {
    const res = await post('/cms/post/saveDraft', { data: { ...editor } })
    message.success('草稿已保存')
    if (res?.data?.id) {
      editor.id = res.data.id
      editor.status = res.data.status || editor.status
    }
    fetchData()
  } catch (e) {
    console.error(e)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const publish = async (id) => {
  if (!canPublish.value || !id) return
  publishing.value = true
  try {
    const res = await post(`/cms/post/publish/${id}`)
    message.success('发布成功')
    editor.status = res?.data?.status || 'PUBLISHED'
    fetchData()
  } catch (e) {
    console.error(e)
    message.error('发布失败')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.cms-post {
  padding: 12px;
}
.search-container {
  margin-bottom: 12px;
}
.pagination-wrapper {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>

