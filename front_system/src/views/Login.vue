<template>
  <div class="auth-callback-container">
    <WelcomeAnimation 
      :show="showWelcome" 
      :ready="loginReady"
      :duration="2000"
      @complete="handleWelcomeComplete"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import api from '@/utils/request.js'
import WelcomeAnimation from '@/components/WelcomeAnimation.vue'
import { useAuthStore } from '@/stores/auth.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showWelcome = ref(false)
const loginReady = ref(false)
// 登录接口
const login = async (code) => {
  try {
    // 调用后端登录接口
    const response = await api.post('/noAuth/login', {
      data: code
    })
    
    // 假设返回的数据格式为 { token: 'xxx', ... }
    const token = response.data.userToken
    
    if (token) {
      // 存储 token 到 localStorage
      localStorage.setItem('user_token', token)
      return true
    } else {
      throw new Error('登录失败：未获取到 token')
    }
  } catch (error) {
    // 登录失败，跳转到错误页面或登录页
    const callbackUrl = window.location.origin + `${import.meta.env.VITE_PRE_PATH}/login`
    const authUrl = (import.meta.env.VITE_LOGIN_DOMAIN || window.location.origin)+`/auth/login?redirectUrl=${encodeURIComponent(callbackUrl)}&error=${encodeURIComponent(error.message)}`
    window.location.href = authUrl
    return false
  }
}

// 处理欢迎动画完成
const handleWelcomeComplete = () => {
  // 从查询参数中获取原始要跳转的路径，如果没有则跳转到 /site
  const redirect = route.query.redirect || '/site'
  router.push(redirect)
}

onMounted(async () => {
  // 从 URL 查询参数中获取授权码
  const code = route.query.code

  if (!code) {
    // 如果没有授权码，跳转到登录页
    message.error('缺少授权码')
    // 优先使用环境变量，如果没有配置则使用 window.location.origin
    const callbackUrl = window.location.origin+`${import.meta.env.VITE_PRE_PATH}/login`
    const authUrl = (import.meta.env.VITE_LOGIN_DOMAIN || window.location.origin)+`/auth/login?redirectUrl=${encodeURIComponent(callbackUrl)}`
    window.location.href = authUrl
    return
  }
  
  // 先调用登录接口
  const loginSuccess = await login(code)
  
  // 登录成功后才显示欢迎动画
  if (loginSuccess) {
    try {
      // 登录成功后初始化菜单、用户信息并添加动态路由
      await authStore.initAfterLogin()
    } catch (e) {
      console.error('登录后初始化失败:', e)
    }
    showWelcome.value = true
    loginReady.value = true
  }
})
</script>

<style scoped>
.auth-callback-container {
  width: 100%;
  height: 100vh;
  position: relative;
}
</style>
