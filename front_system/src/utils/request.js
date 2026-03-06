import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 10000
})

import { useAuthStore } from '@/stores/auth'

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers['Authorization'] = `${token}`
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        // 如果是blob响应，直接返回response
        if (response.config.responseType === 'blob') {
            return response;
        }
        
        const res = response.data;
        if (res.code !== 200) {
            if (res.code && res.code==401) {
                const authStore = useAuthStore();
                var redirectUrl = authStore.redirectUrl;
                authStore.clearToken()
                window.location.href = (redirectUrl||import.meta.env.VITE_LOGIN_DOMAIN||window.location.origin)?redirectUrl+'?logout=true':'/auth/login?redirectUrl=' + encodeURIComponent(window.location.origin);
            }
            message.error(res.message || 'Error');
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        if (error.response && error.response.status==401) {
            const authStore = useAuthStore();
            var redirectUrl = (import.meta.env.VITE_LOGIN_DOMAIN || authStore.redirectUrl)
            authStore.clearToken()
            window.location.href = (redirectUrl||import.meta.env.VITE_LOGIN_DOMAIN||window.location.origin)?redirectUrl+'?logout=true':'/auth/login?redirectUrl=' + encodeURIComponent(window.location.origin);
        }
        return Promise.reject(error);
    }
)

// 封装post方法
export function post(url, data = {}) {
  return service({
    url,
    method: 'post',
    data
  })
}

export default service
