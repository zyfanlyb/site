<script setup>
import { computed } from 'vue'
import LoginRequired from '../components/LoginRequired.vue'

const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY || 'user_token'
const rawLoginHref = import.meta.env.VITE_AGENT_LOGIN_HREF || '/auth/login'

function normalizeHref(href) {
  const s = String(href || '').trim()
  if (!s) return '/auth/login'
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  if (s.startsWith('//')) return s
  if (s.startsWith('/')) return s
  return '/' + s
}

const siteOrigin = typeof window === 'undefined' ? '' : window.location.origin
const loginHref = normalizeHref(rawLoginHref + '?redirectUrl=' + encodeURIComponent(siteOrigin))

const hasToken = computed(() => {
  if (typeof window === 'undefined') return false
  try {
    const raw = localStorage.getItem(tokenKey)
    return raw != null && raw !== '' && raw !== 'null' && raw !== 'undefined'
  } catch {
    return false
  }
})
</script>

<template>
  <div>
    <LoginRequired v-if="!hasToken" :loginHref="loginHref" />
    <div v-else style="padding: 16px; color: #2563eb; font-weight: 600;">智能体原页面（占位）</div>
  </div>
</template>

