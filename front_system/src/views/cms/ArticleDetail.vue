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
        <a-form-item label="类型">
          <a-input :value="detail.typeName || '-'" disabled />
        </a-form-item>
        <a-form-item label="摘要">
          <a-textarea :value="detail.summary || '-'" :rows="3" disabled />
        </a-form-item>
        <a-form-item label="内容">
          <div ref="contentPreviewWrapRef" class="detail-content">
            <MdPreview
              :modelValue="detail.content || ''"
              :language="'zh-CN'"
              @onHtmlChanged="handleContentHtmlChanged"
            />
            <div v-if="!detail.content" class="detail-empty">-</div>
          </div>
        </a-form-item>
        <a-form-item label="封面图片">
          <template v-if="coverPreviewUrls.length">
            <div class="cover-grid">
              <a
                v-for="(u, i) in coverPreviewUrls"
                :key="`cover-${i}`"
                :href="u"
                target="_blank"
                rel="noopener noreferrer"
                class="cover-link"
              >
                <img :src="u" alt="封面" class="cover-img" />
              </a>
            </div>
          </template>
          <span v-else>-</span>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group :value="detail.status" disabled>
            <a-radio :value="0">草稿</a-radio>
            <a-radio :value="1">已发布</a-radio>
          </a-radio-group>
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
import { nextTick, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { getCmsFilePreviewUrl } from '@/utils/file'
import { hydrateCmsPreviewImages } from '@/utils/cmsPreviewImages'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const visible = ref(false)
const loading = ref(false)
const detail = ref(null)
const coverPreviewUrls = ref([])
const contentPreviewWrapRef = ref(null)

let contentHydrateTimer = null
const handleContentHtmlChanged = () => {
  // 预览 HTML 更新后，异步把图片 src 替换成 blob URL
  if (contentHydrateTimer) window.clearTimeout(contentHydrateTimer)
  contentHydrateTimer = window.setTimeout(async () => {
    await nextTick()
    hydrateCmsPreviewImages(contentPreviewWrapRef.value)
  }, 50)
}

const fetchDetail = async (id) => {
  try {
    loading.value = true
    detail.value = null
    coverPreviewUrls.value = []
    const res = await post(`/cms/article/info/${id}`)
    if (res.data) {
      detail.value = res.data
      handleContentHtmlChanged()
      const coversRaw = res.data.coverImages ?? (res.data.cover ? [res.data.cover] : [])
      const covers = Array.isArray(coversRaw) ? coversRaw : []
      const objectNames = covers
        .map((x) => (x == null ? '' : String(x)).trim())
        .filter(Boolean)
      if (objectNames.length) {
        const urls = await Promise.all(objectNames.map((obj) => getCmsFilePreviewUrl(obj)))
        coverPreviewUrls.value = urls.filter(Boolean)
      }
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
    coverPreviewUrls.value = []
  }
})

defineExpose({
  open
})
</script>

<style scoped>
.detail-content {
  min-height: 200px;
  max-height: 420px;
  overflow-y: auto;
  padding: 8px 11px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
}
.detail-empty {
  color: rgba(0, 0, 0, 0.45);
  padding: 8px 0;
}
.cover-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cover-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}
.cover-link {
  display: block;
}
.cover-img {
  width: 100%;
  height: 96px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.cover-path {
  color: #666;
  font-size: 12px;
}
</style>
