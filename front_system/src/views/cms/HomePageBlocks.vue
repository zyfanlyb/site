<template>
  <div class="cms-page">
    <a-card title="首页模块配置（/）" :bordered="false">
      <a-alert
        type="info"
        show-icon
        message="这里编辑的是首页草稿 blocks，点击发布后前台通过 /noAuth/cms/page?path=/ 读取已发布 blocks 渲染。"
        style="margin-bottom: 12px"
      />

      <a-space style="margin-bottom: 12px">
        <a-button type="primary" @click="loadDraft">刷新草稿</a-button>
        <a-button type="primary" @click="addBlock" v-if="canEdit">新增模块</a-button>
        <a-button :loading="saving" type="primary" @click="saveDraft" v-if="canEdit">保存草稿</a-button>
        <a-button :loading="publishing" type="primary" danger @click="publish" v-if="canPublish">发布</a-button>
      </a-space>

      <a-table :columns="columns" :data-source="blocks" :row-key="r => r._k" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" type="link" @click="editBlock(record)" v-if="canEdit">编辑</a-button>
              <a-button size="small" type="link" danger @click="removeBlock(record)" v-if="canEdit">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="编辑模块" ok-text="确定" cancel-text="取消" @ok="applyBlock">
      <a-form layout="vertical">
        <a-form-item label="type">
          <a-select v-model:value="editing.type" style="width: 100%">
            <a-select-option value="HERO">HERO</a-select-option>
            <a-select-option value="MARKDOWN">MARKDOWN</a-select-option>
            <a-select-option value="POST_LIST">POST_LIST</a-select-option>
            <a-select-option value="PROJECT_LIST">PROJECT_LIST</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="sort（越小越靠前）">
          <a-input-number v-model:value="editing.sort" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="props_json（JSON）">
          <a-textarea v-model:value="editing.propsJson" :auto-size="{ minRows: 8, maxRows: 16 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePermission } from '@/composables/usePermission'

const { checkPermission } = usePermission()
const canEdit = computed(() => checkPermission('cms:page:edit'))
const canPublish = computed(() => checkPermission('cms:page:publish'))

const pagePath = '/'
const pageMeta = reactive({
  title: '',
  seoTitle: '',
  seoDescription: '',
})

const blocks = ref([])

const columns = computed(() => ([
  { title: 'type', dataIndex: 'type', key: 'type', width: 140 },
  { title: 'sort', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: 'props_json', dataIndex: 'propsJson', key: 'propsJson' },
  ...(canEdit.value ? [{ title: '操作', key: 'action', width: 120 }] : []),
]))

const saving = ref(false)
const publishing = ref(false)

const normalizeBlocks = (list) => {
  const arr = Array.isArray(list) ? list : []
  return arr.map((b, idx) => ({
    _k: `${Date.now()}_${idx}_${Math.random()}`,
    type: b.type || 'MARKDOWN',
    sort: b.sort ?? idx,
    propsJson: b.propsJson || '{}',
  })).sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
}

const loadDraft = async () => {
  try {
    const res = await post('/cms/page', { data: pagePath })
    pageMeta.title = res?.data?.page?.title || ''
    pageMeta.seoTitle = res?.data?.page?.seoTitle || ''
    pageMeta.seoDescription = res?.data?.page?.seoDescription || ''
    blocks.value = normalizeBlocks(res?.data?.blocks)
  } catch (e) {
    console.error(e)
    message.error('获取草稿失败')
  }
}

const saveDraft = async () => {
  if (!canEdit.value) return
  // validate json
  for (const b of blocks.value) {
    try {
      JSON.parse(b.propsJson || '{}')
    } catch (e) {
      message.error(`模块 ${b.type} 的 props_json 不是合法 JSON`)
      return
    }
  }
  saving.value = true
  try {
    await post('/cms/page/saveDraft', {
      data: {
        page: { path: pagePath, ...pageMeta },
        blocks: blocks.value.map(({ _k, ...rest }) => rest),
      }
    })
    message.success('草稿已保存')
    await loadDraft()
  } catch (e) {
    console.error(e)
    message.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

const publish = async () => {
  if (!canPublish.value) return
  publishing.value = true
  try {
    await post('/cms/page/publish', { data: pagePath })
    message.success('发布成功')
  } catch (e) {
    console.error(e)
    message.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const modalOpen = ref(false)
const editing = reactive({ _k: null, type: 'MARKDOWN', sort: 0, propsJson: '{}' })
const isNew = ref(false)

const addBlock = () => {
  isNew.value = true
  editing._k = null
  editing.type = 'MARKDOWN'
  editing.sort = blocks.value.length
  editing.propsJson = '{}'
  modalOpen.value = true
}

const editBlock = (record) => {
  isNew.value = false
  editing._k = record._k
  editing.type = record.type
  editing.sort = record.sort
  editing.propsJson = record.propsJson
  modalOpen.value = true
}

const applyBlock = () => {
  try {
    JSON.parse(editing.propsJson || '{}')
  } catch (e) {
    message.error('props_json 不是合法 JSON')
    return
  }
  if (isNew.value) {
    blocks.value.push({ _k: `${Date.now()}_${Math.random()}`, type: editing.type, sort: editing.sort, propsJson: editing.propsJson })
  } else {
    const idx = blocks.value.findIndex(b => b._k === editing._k)
    if (idx >= 0) {
      blocks.value[idx] = { ...blocks.value[idx], type: editing.type, sort: editing.sort, propsJson: editing.propsJson }
    }
  }
  blocks.value = normalizeBlocks(blocks.value)
  modalOpen.value = false
}

const removeBlock = (record) => {
  blocks.value = blocks.value.filter(b => b._k !== record._k)
}

onMounted(() => {
  loadDraft()
})
</script>

<style scoped>
.cms-page {
  padding: 12px;
}
</style>

