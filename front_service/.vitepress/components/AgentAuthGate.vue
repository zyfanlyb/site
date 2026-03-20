<script setup>
import { computed, ref } from 'vue'
import LoginRequired from './LoginRequired.vue'

const props = defineProps({
  tokenKey: { type: String, default: 'user_token' },
  loginText: { type: String, default: '去登录' },
  loginHref: { type: String, default: '/site/login' }
})

const hasToken = ref(false)

// 关键：避免 SSR/初始渲染时 blank。
// 服务端无法访问 localStorage，因此默认先展示“去登录”提示；
// 在浏览器端再同步读取 localStorage，有 token 时切回 slot 内容。
if (typeof window !== 'undefined') {
  try {
    hasToken.value = !!localStorage.getItem(props.tokenKey)
  } catch (e) {
    hasToken.value = false
  }
}

const showContent = computed(() => hasToken.value)
const showLoginRequired = computed(() => !hasToken.value)
</script>

<template>
  <div>
    <template v-if="showContent">
      <slot />
    </template>

    <template v-else-if="showLoginRequired">
      <LoginRequired :loginText="loginText" :loginHref="loginHref" />
    </template>
  </div>
</template>

