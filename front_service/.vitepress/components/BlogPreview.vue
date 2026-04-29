<script setup>
import { ref, computed, onMounted } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const loading = ref(false)
const article = ref(null)
const error = ref('')

const idFromLocation = () => {
  if (typeof window === 'undefined') return ''
  try {
    const sp = new URLSearchParams(window.location.search || '')
    return (sp.get('id') || '').trim()
  } catch {
    return ''
  }
}

const fetchArticle = async (id) => {
  if (!id) {
    error.value = '缺少文章 id'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`/api/blogs/article/${encodeURIComponent(id)}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.message || '接口返回错误')
    article.value = json?.data ?? null
    if (!article.value) throw new Error('文章不存在或未发布')
  } catch (e) {
    console.error(e)
    article.value = null
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const contentMd = computed(() => {
  const c = article.value?.content
  if (c == null) return ''
  return String(c)
})

onMounted(() => {
  const id = idFromLocation()
  fetchArticle(id)
})
</script>

<template>
  <div class="wrap">
    <div v-if="loading" class="loading-wrap" aria-live="polite" aria-busy="true">
      <span class="spinner" aria-hidden="true"></span>
      <span class="loading-text">加载中...</span>
    </div>
    <div v-else-if="error" class="state state-error">{{ error }}</div>

    <article v-else-if="article && contentMd" class="article">
      <div class="content">
        <ClientOnly>
          <MdPreview :modelValue="contentMd" :language="'zh-CN'" />
        </ClientOnly>
      </div>
    </article>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 960px;
  margin: 0 auto;
  padding: 18px 16px 48px;
}
.content {
  padding-top: 0;
}
.loading-wrap {
  min-height: 52vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--vp-c-text-2);
}
.spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid color-mix(in srgb, var(--vp-c-brand-1) 25%, var(--vp-c-divider));
  border-top-color: var(--vp-c-brand-1);
  animation: spin 0.9s linear infinite;
}
.loading-text {
  font-size: 13px;
}
.state {
  padding: 24px 0;
  color: var(--vp-c-text-2);
}
.state-error {
  color: #d32f2f;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
