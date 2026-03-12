---
title: 动态文章列表示例
---

# 动态文章列表示例

这个页面会在**运行时**从后端接口 `/api/articles` 拉取数据并展示。

> 为了避免跨域，建议在开发环境通过反向代理把 `/api` 代理到你的 `site_service` 服务。

```ts
<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const error = ref('')
const articles = ref([])

const fetchArticles = async () => {
  loading.value = true
  error.value = ''
  try {
    // 这里使用相对路径 /api/articles
    // 开发时可以在 VitePress 配置 dev 服务器代理到后端
    const res = await fetch('/api/articles?pageNum=1&pageSize=10')
    const json = await res.json()

    if (json.code !== 200) {
      throw new Error(json.message || '接口返回错误')
    }

    // 后端 ResponseVo<List<CmsArticle>>，data 是数组
    articles.value = json.data || []
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
```

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
      <span v-if="item.categoryName" style="margin-left: 8px; color: #888;">
        [{{ item.categoryName }}]
      </span>
      <div style="font-size: 13px; color: #666; margin-top: 4px;">
        {{ item.summary || '暂无摘要' }}
      </div>
    </li>
  </ul>
</div>

