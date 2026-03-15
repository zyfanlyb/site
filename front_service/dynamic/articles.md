---
title: 动态文章列表示例
---

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const error = ref('')
const articles = ref([])

const fetchArticles = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/articles?pageNum=1&pageSize=10')
    const json = await res.json()
    if (json.code !== 200) {
      throw new Error(json.message || '接口返回错误')
    }
    // 后端 pageList 返回 IPage，data 为 { records, total, ... }，取 records
    const list = json.data?.records ?? json.data
    articles.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error(e)
    error.value = e.message || '请求失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

# 动态文章列表示例

这个页面会在**运行时**从后端接口 `/api/articles` 拉取数据并展示。

> 开发时需确保 `site_service` 已启动，VitePress 已配置 `/api` 代理到后端。

下面是从接口获取到的文章列表（实时）：

<div v-if="loading">加载中...</div>
<div v-else-if="error" style="color: red;">{{ error }}</div>

<div v-else>
  <div v-if="articles.length === 0">
    暂无文章，请先在 CMS 后台创建文章并发布。
  </div>
  <ul v-else>
    <li v-for="item in articles" :key="item.id" style="margin-bottom: 12px;">
      <strong>{{ item.title }}</strong>
      <span v-if="item.categoryName || item.typeName" style="margin-left: 8px; color: #888;">
        [{{ [item.categoryName, item.typeName].filter(Boolean).join(' / ') }}]
      </span>
      <div style="font-size: 13px; color: #666; margin-top: 4px;">
        {{ item.summary || '暂无摘要' }}
      </div>
    </li>
  </ul>
</div>

