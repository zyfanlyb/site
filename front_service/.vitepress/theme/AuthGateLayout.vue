<script setup>
import { computed, onMounted, watch } from 'vue'
import DefaultTheme from 'vitepress/theme'
import { useRoute } from 'vitepress'

import LoginRequired from '../components/LoginRequired.vue'
import HomeShowcaseStrip from './HomeShowcaseStrip.vue'
import { normalizeLoginHref } from './utils/normalizeLoginHref.js'

const { Layout: DefaultLayout } = DefaultTheme
const route = useRoute()

const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY || 'user_token'
const rawLoginHref = import.meta.env.VITE_AGENT_LOGIN_HREF || '/auth/login'

// SSR: window 不存在，因此 redirectUrl 需要做保护
const siteOrigin = typeof window === 'undefined' ? '' : window.location.origin
const loginHref = normalizeLoginHref(
  rawLoginHref + '?redirectUrl=' + encodeURIComponent(siteOrigin),
)

const isAgentPage = computed(() => {
  // route.path 是响应式的，能覆盖“点击 tab 后不刷新”的情况
  let p = route.path || ''
  p = p.replace(/\/+$/, '')
  return p === '/dynamic/agent' || p === '/dynamic/agent.html'
})

const hasToken = computed(() => {
  // SSR：无法读取 localStorage，默认按“未登录”处理；客户端 hydrate 后会立即更新
  if (typeof window === 'undefined') return false
  try {
    const raw = localStorage.getItem(tokenKey)
    return raw != null && raw !== '' && raw !== 'null' && raw !== 'undefined'
  } catch (e) {
    return false
  }
})

const gate = computed(() => isAgentPage.value && !hasToken.value)

function kickHomeRevealSync() {
  if (typeof window === 'undefined') return
  window.requestAnimationFrame(() => {
    window.dispatchEvent(new CustomEvent('vp-home-reveal-kick'))
  })
}

onMounted(kickHomeRevealSync)
watch(
  () => route.path,
  () => kickHomeRevealSync(),
)
</script>

<template>
  <div :class="{ 'auth-gate-hide-content': gate }">
    <DefaultLayout>
      <!-- tagline 之后：三栏装饰卡片（无链接）；插在 home-hero-info-after 更稳 -->
      <template #home-hero-info-after>
        <div class="home-hero-actions-extra">
          <HomeShowcaseStrip />
        </div>
      </template>
      <!-- 仅在未登录时插入提示；正文仍由 VitePress 渲染，但会被下方样式隐藏 -->
      <template v-if="gate" #doc-before>
        <LoginRequired :loginHref="loginHref" />
      </template>
    </DefaultLayout>
  </div>
</template>

<style scoped>
.auth-gate-hide-content :deep(main .vp-doc) {
  display: none !important;
}
</style>
