<script setup>
import { computed } from 'vue'
import DefaultTheme from 'vitepress/theme'
import { useRoute } from 'vitepress'

import LoginRequired from '../components/LoginRequired.vue'

const { Layout: DefaultLayout } = DefaultTheme
const route = useRoute()

const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY || 'user_token'
const rawLoginHref = import.meta.env.VITE_AGENT_LOGIN_HREF || '/auth/login'

function normalizeHref(href) {
  const s = String(href || '').trim()
  if (!s) return '/auth/login'

  // 支持完整地址：http(s)://...
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  // 支持协议相对：//host/xxx
  if (s.startsWith('//')) return s

  // 支持站内路径：/xxx
  if (s.startsWith('/')) return s

  // 否则当作相对路径，补前导 /
  return '/' + s
}

const loginHref = normalizeHref(rawLoginHref+'?redirectUrl='+encodeURIComponent(window.location.origin))

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
</script>

<template>
  <div :class="{ 'auth-gate-hide-content': gate }">
    <DefaultLayout>
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
