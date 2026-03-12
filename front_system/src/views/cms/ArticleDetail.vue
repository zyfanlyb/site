<template>
  <a-modal
    v-model:open="visible"
    title="文章详情"
    width="800px"
  >
    <a-spin :spinning="loading">
      <a-form
        v-if="detail"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item label="标题">
          <a-input :value="detail.title" disabled />
        </a-form-item>
        <a-form-item label="分类">
          <a-input :value="detail.categoryName || '-'" disabled />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea :value="detail.summary || '-'" :rows="3" disabled />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea :value="detail.content || '-'" :rows="10" disabled class="detail-content" />
        </a-form-item>
        <a-form-item label="封面图">
          <a-input :value="detail.cover || ''" disabled>
            <template v-if="detail.cover" #addonAfter>
              <a :href="detail.cover" target="_blank" rel="noopener noreferrer">预览</a>
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group :value="detail.status" disabled>
            <a-radio :value="0">草稿</a-radio>
            <a-radio :value="1">已发布</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="排序">
          <a-input :value="String(detail.sort ?? '-')" disabled />
        </a-form-item>
        <a-form-item label="创建时间">
          <a-input :value="detail.createTime || '-'" disabled />
        </a-form-item>
        <a-form-item label="更新时间">
          <a-input :value="detail.updateTime || '-'" disabled />
        </a-form-item>
      </a-form>
      <a-empty v-else-if="!loading" description="暂无数据" />
    </a-spin>
    <template #footer>
      <a-button type="primary" @click="visible = false">关闭</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'

const visible = ref(false)
const loading = ref(false)
const detail = ref(null)

const fetchDetail = async (id) => {
  try {
    loading.value = true
    detail.value = null
    const res = await post(`/cms/article/info/${id}`)
    if (res.data) {
      detail.value = res.data
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    message.error('获取文章详情失败')
  } finally {
    loading.value = false
  }
}

const open = (id) => {
  visible.value = true
  if (id) {
    fetchDetail(id)
  }
}

watch(visible, (val) => {
  if (!val) {
    detail.value = null
  }
})

defineExpose({
  open
})
</script>

<style scoped>
.detail-content {
  min-height: 200px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  padding: 8px 11px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
}
</style>
