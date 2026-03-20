<script setup>
const props = defineProps({
  messagePrefix: { type: String, default: '该页面需要登录访问，' },
  loginText: { type: String, default: '去登录' },
  loginHref: { type: String, default: '/site/login' }
})

const handleLoginClick = () => {
  if (typeof window === 'undefined') return
  // 用顶层跳转，避免被 SPA/路由拦截成内嵌行为
  window.location.href = props.loginHref
}
</script>

<template>
  <div class="login-required">
    <div class="card">
      <p class="text">
        {{ messagePrefix }}
        <a class="link" :href="loginHref" @click.prevent="handleLoginClick">{{ loginText }}</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-required {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.card {
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 24px 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px);
}

.text {
  margin: 0;
  font-size: 16px;
  color: rgba(0, 0, 0, 0.75);
}

.link {
  color: #2563eb;
  text-decoration: underline;
}

.link:hover {
  color: #1d4ed8;
}
</style>

