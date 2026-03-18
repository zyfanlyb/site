<script setup>
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  // CMS 分类名：博客/项目经历等
  categoryName: { type: String, default: '博客' },
  // 搜索框占位
  searchPlaceholder: { type: String, default: '搜索文章（标题/摘要/内容）' }
})

const loading = ref(false)
const error = ref('')
const articles = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = 10
const hasMore = ref(true)
const isFetchingMore = ref(false)
const loadMoreEl = ref(null)
const listEl = ref(null)
let io = null

const escapeRegExp = (s) => String(s).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
const escapeHtml = (s) =>
  String(s)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

const highlightText = (text) => {
  const raw = text ?? ''
  const safe = escapeHtml(raw)
  const kw = (keyword.value || '').trim()
  if (!kw) return safe
  try {
    const re = new RegExp(escapeRegExp(kw), 'gi')
    return safe.replace(re, (m) => `<mark class="hl">${m}</mark>`)
  } catch {
    return safe
  }
}

const safeArticles = computed(() => (Array.isArray(articles.value) ? articles.value : []).filter(Boolean))
const showLoading = computed(() => loading.value)
const showError = computed(() => !loading.value && !!error.value)
const showContent = computed(() => !loading.value && !error.value)

// 轻量虚拟渲染：减少 DOM 数量，服务器请求不变
const virtualEnabled = ref(true)
const estimatedItemHeight = ref(210) // 估算卡片高度（用于计算占位高度）
const overscan = 8 // 视口上下额外渲染条数
const startIndex = ref(0)
const endIndex = ref(0)
let scrollRaf = 0

const toImgUrl = (url) => {
  if (!url) return ''
  return String(url).trim()
}

const coverThumbs = (article) => {
  const pick = (v) => {
    if (v == null) return ''
    if (typeof v === 'string') return v.trim()
    if (typeof v === 'number' || typeof v === 'boolean') return String(v)
    if (typeof v === 'object') {
      const obj = v
      return String(obj.objectName ?? obj.url ?? obj.path ?? obj.name ?? '').trim()
    }
    return String(v).trim()
  }

  const normalizeArray = (arr) => arr.map(pick).filter(Boolean)

  const imgs = article?.coverImages
  if (Array.isArray(imgs)) return normalizeArray(imgs)

  return []
}

const formatDate = (val) => {
  if (!val) return ''
  if (typeof val === 'string') return val.slice(0, 10)
  try {
    const d = new Date(val)
    if (Number.isNaN(d.getTime())) return ''
    return d.toISOString().slice(0, 10)
  } catch {
    return ''
  }
}

const clamp = (n, min, max) => Math.max(min, Math.min(max, n))

const updateVirtualRange = () => {
  if (!virtualEnabled.value) return
  const el = listEl.value
  const total = safeArticles.value.length
  if (!el || total === 0) {
    startIndex.value = 0
    endIndex.value = total
    return
  }

  const rect = el.getBoundingClientRect()
  const listTop = rect.top + window.scrollY
  const scrollY = window.scrollY
  const viewH = window.innerHeight || 800
  const h = Math.max(120, Number(estimatedItemHeight.value) || 210)

  const topInList = Math.max(0, scrollY - listTop)
  const first = Math.floor(topInList / h) - overscan
  const visibleCount = Math.ceil(viewH / h) + overscan * 2
  const lastExclusive = first + visibleCount

  startIndex.value = clamp(first, 0, total)
  endIndex.value = clamp(lastExclusive, startIndex.value, total)
}

const scheduleVirtualRange = () => {
  if (!virtualEnabled.value) return
  if (scrollRaf) return
  scrollRaf = requestAnimationFrame(() => {
    scrollRaf = 0
    updateVirtualRange()
  })
}

const visibleArticles = computed(() => {
  if (!virtualEnabled.value) return safeArticles.value
  return safeArticles.value.slice(startIndex.value, endIndex.value)
})

const topSpacerHeight = computed(() => {
  if (!virtualEnabled.value) return 0
  return startIndex.value * estimatedItemHeight.value
})

const bottomSpacerHeight = computed(() => {
  if (!virtualEnabled.value) return 0
  const total = safeArticles.value.length
  return Math.max(0, (total - endIndex.value) * estimatedItemHeight.value)
})

const setCardRef = (globalIdx) => (el) => {
  // 采样更新估算高度，让虚拟滚动更贴近真实
  if (!virtualEnabled.value) return
  if (!el) return
  if (globalIdx !== startIndex.value) return
  const h = el.offsetHeight
  if (!h || h < 80) return
  // 平滑更新：避免估算高度抖动
  estimatedItemHeight.value = Math.round(estimatedItemHeight.value * 0.8 + h * 0.2)
}

const readListFromResponse = (json) => {
  const data = json?.data
  const list = data?.records ?? data
  return Array.isArray(list) ? list.filter(Boolean) : []
}

const buildListUrl = (page) => {
  const q = new URLSearchParams()
  const category = (props.categoryName && props.categoryName.trim()) ? props.categoryName.trim() : '博客'
  q.set('categoryName', category)
  q.set('pageNum', String(page))
  q.set('pageSize', String(pageSize))
  const kw = (keyword.value || '').trim()
  if (kw) q.set('keyword', kw)
  return `/api/blogs/articles?${q.toString()}`
}

const fetchFirstPage = async () => {
  loading.value = true
  error.value = ''
  pageNum.value = 1
  hasMore.value = true
  try {
    const res = await fetch(buildListUrl(pageNum.value))
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '接口返回错误')
    const list = readListFromResponse(json)
    articles.value = list
    hasMore.value = list.length === pageSize
    pageNum.value = 2
    nextTick(() => {
      updateVirtualRange()
    })
  } catch (e) {
    console.error(e)
    error.value = e?.message || '请求失败'
  } finally {
    loading.value = false
  }
}

const fetchNextPage = async () => {
  if (!hasMore.value) return
  if (loading.value) return
  if (isFetchingMore.value) return
  isFetchingMore.value = true
  error.value = ''
  try {
    const res = await fetch(buildListUrl(pageNum.value))
    const json = await res.json()
    if (json.code !== 200) throw new Error(json.message || '接口返回错误')
    const list = readListFromResponse(json)
    if (!list.length) {
      hasMore.value = false
      return
    }
    articles.value = [...safeArticles.value, ...list]
    hasMore.value = list.length === pageSize
    pageNum.value += 1
    nextTick(() => {
      updateVirtualRange()
    })
  } catch (e) {
    console.error(e)
    error.value = e?.message || '请求失败'
  } finally {
    isFetchingMore.value = false
  }
}

const doSearch = async () => {
  if (loading.value) return
  // 虚拟列表在滚动到中间/底部时，如果筛选后结果变少，可能出现“空白”
  // 这里主动回到顶部并重置虚拟窗口，保证筛选体验稳定
  startIndex.value = 0
  endIndex.value = 0
  try {
    window.scrollTo({ top: 0, behavior: 'auto' })
  } catch {
    window.scrollTo(0, 0)
  }
  await fetchFirstPage()
}

const keyOf = (article, idx) => String(article?.id ?? idx)
const galleryEls = new Map()
const scrollable = reactive({})
const canLeft = reactive({})
const canRight = reactive({})

const rafJobs = new Map()
const scheduleUpdateGalleryState = (k) => {
  if (!k) return
  if (rafJobs.has(k)) return
  const id = requestAnimationFrame(() => {
    rafJobs.delete(k)
    updateGalleryState(k)
  })
  rafJobs.set(k, id)
}

const updateGalleryState = (k) => {
  const el = galleryEls.get(k)
  if (!el) return
  const isScrollable = el.scrollWidth > el.clientWidth + 1
  scrollable[k] = isScrollable
  canLeft[k] = el.scrollLeft > 0
  canRight[k] = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
}

const setGalleryRef = (k) => (el) => {
  if (el) {
    galleryEls.set(k, el)
    nextTick(() => scheduleUpdateGalleryState(k))
  } else {
    galleryEls.delete(k)
    delete scrollable[k]
    delete canLeft[k]
    delete canRight[k]
  }
}

const scrollGallery = (k, dir) => {
  const el = galleryEls.get(k)
  if (!el) return
  const delta = Math.max(220, Math.floor(el.clientWidth * 0.8))
  el.scrollBy({ left: dir * delta, behavior: 'smooth' })
}

const onResize = () => {
  for (const k of galleryEls.keys()) scheduleUpdateGalleryState(k)
}

onMounted(async () => {
  await fetchFirstPage()
  nextTick(onResize)
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', scheduleVirtualRange, { passive: true })
  nextTick(() => {
    updateVirtualRange()
  })

  io = new IntersectionObserver(
    (entries) => {
      const entry = entries?.[0]
      if (!entry?.isIntersecting) return
      fetchNextPage()
    },
    { root: null, rootMargin: '260px 0px', threshold: 0 }
  )
  if (loadMoreEl.value) io.observe(loadMoreEl.value)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', scheduleVirtualRange)
  for (const id of rafJobs.values()) cancelAnimationFrame(id)
  rafJobs.clear()
  if (scrollRaf) cancelAnimationFrame(scrollRaf)
  scrollRaf = 0
  if (io) io.disconnect()
  io = null
})
</script>

<template>
  <div class="page-wrap">
    <section class="hero">
      <div class="hero-main">
        <div class="hero-top">
          <div class="hero-stats">共 {{ safeArticles.length }} 篇</div>
        </div>
        <div class="search-row">
          <div class="search-box">
            <input
              v-model="keyword"
              class="search-input"
              type="search"
              :placeholder="props.searchPlaceholder"
              @keydown.enter.prevent="doSearch"
            />
            <button class="search-btn" type="button" @click="doSearch">搜索</button>
            <button
              v-if="keyword.trim()"
              class="search-btn search-btn-ghost"
              type="button"
              @click="
                () => {
                  keyword = ''
                  doSearch()
                }
              "
            >
              清空
            </button>
          </div>
        </div>
      </div>
    </section>

    <div v-if="showLoading" class="state">
      <div class="skeleton">
        <div class="sk-title"></div>
        <div class="sk-line"></div>
        <div class="sk-line"></div>
      </div>
      <div class="skeleton">
        <div class="sk-title"></div>
        <div class="sk-line"></div>
        <div class="sk-line"></div>
      </div>
      <div class="skeleton">
        <div class="sk-title"></div>
        <div class="sk-line"></div>
        <div class="sk-line"></div>
      </div>
    </div>

    <div v-else-if="showError" class="state state-error">
      <div class="state-title">加载失败</div>
    </div>

    <div v-else-if="showContent">
      <div v-if="!safeArticles.length" class="state state-empty">
        <div class="empty-title">还没有文章</div>
      </div>

      <ul v-if="safeArticles.length" ref="listEl" class="blog-list" role="list">
        <li v-if="topSpacerHeight" class="spacer" :style="{ height: topSpacerHeight + 'px' }"></li>

        <li
          v-for="(article, localIdx) in visibleArticles"
          :key="keyOf(article, startIndex + localIdx)"
          class="blog-card"
          :ref="setCardRef(startIndex + localIdx)"
        >
          <div class="card-head">
            <h2 class="card-title" v-html="highlightText(article?.title || '未命名文章')"></h2>
          </div>

          <div v-if="coverThumbs(article).length === 1" class="one-cover">
            <div class="one-cover-img">
              <img :src="toImgUrl(coverThumbs(article)[0])" alt="封面" loading="lazy" decoding="async" />
            </div>
            <p class="one-cover-summary" v-html="highlightText(article?.summary || '无摘要')"></p>
          </div>

          <div v-else class="multi-cover">
            <p class="card-summary" v-html="highlightText(article?.summary || '无摘要')"></p>
            <div v-if="coverThumbs(article).length" class="gallery-wrap">
              <button
                v-if="scrollable[keyOf(article, startIndex + localIdx)]"
                class="gallery-btn gallery-btn-left"
                type="button"
                :disabled="!canLeft[keyOf(article, startIndex + localIdx)]"
                @click="scrollGallery(keyOf(article, startIndex + localIdx), -1)"
                v-text="'‹'"
              ></button>

              <div
                class="card-covers card-covers-scroll"
                :ref="setGalleryRef(keyOf(article, startIndex + localIdx))"
                @scroll.passive="scheduleUpdateGalleryState(keyOf(article, startIndex + localIdx))"
              >
                <div
                  v-for="(img, i) in coverThumbs(article)"
                  :key="keyOf(article, startIndex + localIdx) + '-img-' + i"
                  class="cover-thumb"
                >
                  <img class="cover-thumb-img" :src="toImgUrl(img)" alt="封面" loading="lazy" decoding="async" />
                </div>
              </div>

              <button
                v-if="scrollable[keyOf(article, startIndex + localIdx)]"
                class="gallery-btn gallery-btn-right"
                type="button"
                :disabled="!canRight[keyOf(article, startIndex + localIdx)]"
                @click="scrollGallery(keyOf(article, startIndex + localIdx), 1)"
                v-text="'›'"
              ></button>
            </div>
          </div>

          <div class="card-meta">
            <span v-if="article?.typeName" class="tag tag-type">{{ article.typeName }}</span>
            <time v-if="article?.createTime" class="card-date">{{ formatDate(article.createTime) }}</time>
          </div>
        </li>

        <li v-if="bottomSpacerHeight" class="spacer" :style="{ height: bottomSpacerHeight + 'px' }"></li>
      </ul>

      <div v-if="safeArticles.length" class="load-more">
        <div ref="loadMoreEl" class="load-more-sentinel"></div>
        <div v-if="isFetchingMore" class="load-more-text">加载更多...</div>
        <div v-else-if="!hasMore" class="load-more-text">没有更多了</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrap { max-width: none; margin: 0 auto; padding: 24px 0 52px; display: flex; flex-direction: column; align-items: center; }
.hero { margin: 10px 0 18px; width: 70vw; }
.hero-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.hero-stats { margin-top: 10px; font-size: 12px; color: var(--vp-c-text-2); }
.search-row { margin-top: 10px; display: flex; align-items: center; justify-content: flex-start; }
.search-box { width: 100%; max-width: 520px; display: flex; gap: 8px; align-items: center; }
.search-input {
  flex: 1;
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg);
  color: var(--vp-c-text-1);
  outline: none;
}
.search-input:focus { border-color: color-mix(in srgb, var(--vp-c-brand-1) 55%, var(--vp-c-divider)); box-shadow: 0 0 0 3px color-mix(in srgb, var(--vp-c-brand-1) 20%, transparent); }
.search-btn {
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font-weight: 600;
  cursor: pointer;
}
.search-btn:hover { background: color-mix(in srgb, var(--vp-c-bg-soft) 70%, var(--vp-c-bg)); }
.search-btn-ghost { background: transparent; }
.hl { background: #ffeb3b; color: inherit; padding: 0 2px; border-radius: 3px; }
.state { padding: 22px 0; font-size: 14px; width: 70vw; }
.state-title { font-size: 16px; font-weight: 700; color: var(--vp-c-text-1); margin-bottom: 6px; }
.state-desc { color: var(--vp-c-text-2); line-height: 1.7; }
.state-error { padding: 10px 0 0; }
.state-error .state-title { color: var(--vp-c-text-2); }
.state-empty { padding: 10px 0 0; }
.empty-title { font-size: 14px; font-weight: 600; color: var(--vp-c-text-2); }
.blog-list { list-style: none; padding: 0; margin: 0 auto; display: grid; grid-template-columns: 1fr; gap: 12px; width: 70vw; }
/* 去掉卡片感：无独立背景/边框/圆角，改为列表分隔线 */
.blog-card { border: 0; background: transparent; border-radius: 0; padding: 8px 0; display: flex; flex-direction: column; gap: 10px; }
.spacer { border: 0; background: transparent; padding: 0; margin: 0; }
.card-head { display: flex; align-items: baseline; gap: 10px; min-width: 0; padding-bottom: 8px; border-bottom: 1px solid var(--vp-c-divider); }
.card-title { margin: 0; font-size: 18px; font-weight: 700; color: var(--vp-c-brand-1); line-height: 1.35; letter-spacing: -0.01em; border-top: 0 !important; padding-top: 0 !important; flex: 1; min-width: 0; }
.card-summary { margin: 0; font-size: 13px; color: var(--vp-c-text-2); line-height: 1.6; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3; line-clamp: 3; min-height: calc(1.6em * 3); }
.card-meta { display: flex; justify-content: space-between; align-items: center; }
.card-date { font-size: 12px; color: var(--vp-c-text-3); }
.tag { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 999px; font-size: 11px; line-height: 1.6; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg); color: var(--vp-c-text-2); }
.tag-type { border-color: color-mix(in srgb, var(--vp-c-brand-1) 40%, var(--vp-c-divider)); color: color-mix(in srgb, var(--vp-c-brand-1) 80%, var(--vp-c-text-1)); }
.one-cover { display: grid; grid-template-columns: 160px 1fr; gap: 12px; align-items: start; }
.one-cover-img { width: 160px; height: 96px; border-radius: 10px; overflow: hidden; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg); }
.one-cover-img img { width: 100%; height: 100%; object-fit: cover; display: block; }
.one-cover-summary { margin: 0; font-size: 13px; color: var(--vp-c-text-2); line-height: 1.6; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 3; line-clamp: 3; min-height: calc(1.6em * 3); }
.card-covers { display: flex; gap: 10px; flex-wrap: nowrap; overflow: hidden; }
.card-covers-scroll { overflow-x: auto; scroll-behavior: smooth; padding-bottom: 2px; scrollbar-width: none; }
.card-covers-scroll::-webkit-scrollbar { display: none; }
.gallery-wrap { position: relative; }
.gallery-btn { position: absolute; top: 50%; transform: translateY(-50%); width: 28px; height: 28px; border-radius: 999px; border: 1px solid var(--vp-c-divider); background: color-mix(in srgb, var(--vp-c-bg) 85%, transparent); color: var(--vp-c-text-1); cursor: pointer; z-index: 2; display: inline-flex; align-items: center; justify-content: center; padding: 0; }
.gallery-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.gallery-btn-left { left: -8px; }
.gallery-btn-right { right: -8px; }
.cover-thumb { position: relative; width: 132px; height: 78px; border-radius: 10px; overflow: hidden; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); flex: 0 0 auto; }
.cover-thumb-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.btn { margin-top: 12px; display: inline-flex; align-items: center; justify-content: center; height: 34px; padding: 0 14px; border-radius: 10px; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg); color: var(--vp-c-text-1); font-weight: 600; cursor: pointer; }
.load-more { width: 70vw; margin: 14px auto 0; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.load-more-sentinel { width: 100%; height: 1px; }
.load-more-text { font-size: 12px; color: var(--vp-c-text-3); }
.skeleton { border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); border-radius: 16px; padding: 14px 14px; display: grid; gap: 10px; }
.sk-title { height: 16px; width: 52%; border-radius: 999px; background: color-mix(in srgb, var(--vp-c-divider) 70%, var(--vp-c-bg)); }
.sk-line { height: 12px; width: 100%; border-radius: 999px; background: color-mix(in srgb, var(--vp-c-divider) 70%, var(--vp-c-bg)); }
@media (max-width: 640px) {
  .blog-list, .hero, .state, .load-more { width: 100%; margin: 0; }
  .page-wrap { padding: 16px 0 40px; align-items: stretch; }
  .search-box { max-width: none; flex-wrap: wrap; }
  .search-input { width: 100%; flex: 1 1 100%; }
  .one-cover { grid-template-columns: 120px 1fr; }
  .one-cover-img { width: 120px; height: 72px; }
  .card-summary, .one-cover-summary { -webkit-line-clamp: 2; line-clamp: 2; min-height: calc(1.6em * 2); }
}
</style>

