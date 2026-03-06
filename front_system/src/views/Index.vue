<!--
  通用化后台管理系统首页（欢迎页）
  - 只包含欢迎区域和一些柔和的装饰动效
  - 不依赖任何业务接口，可在任意后台首页复用
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { ClockCircleOutlined } from '@ant-design/icons-vue'

const authStore = useAuthStore()
const { userInfo } = storeToRefs(authStore)

const welcomeText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

const todayText = computed(() =>
  new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
)
</script>

<template>
  <div class="hero-main">
    <h1 class="hero-title">
      {{ welcomeText }}，{{ userInfo?.nickname || '用户' }}
    </h1>
    <p class="hero-subtitle">
      <ClockCircleOutlined class="hero-icon" />
      <span>{{ todayText }}</span>
    </p>
  </div>
<!--  <div class="hero-orbits">-->
<!--    <div class="orbit orbit-lg"></div>-->
<!--    <div class="orbit orbit-md"></div>-->
<!--    <div class="orbit orbit-sm"></div>-->
<!--  </div>-->
</template>

<style scoped>

.hero-main {
  position: relative;
  z-index: 2;
  max-width: 60%;
}

.hero-title {
  margin: 0 0 10px 0;
  font-size: 26px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.94);
}

.hero-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(15, 23, 42, 0.7);
  display: flex;
  align-items: center;
  gap: 6px;
}

.hero-icon {
  font-size: 16px;
  color: #2563eb;
}

@keyframes float {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(-8px, 6px, 0) scale(1.04);
  }
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
}
</style>
