import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import antDesign from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import './assets/theme.css';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import locale from 'ant-design-vue/es/locale/zh_CN';
import App from '@/App.vue'
import router, { restoreDynamicRoutes } from "@/router/index.js";
import { useAuthStore } from '@/stores/auth'

// 完整配置中文语言环境
dayjs.locale('zh-cn');
locale.dayjsLocale = 'zh-cn';

// 覆盖分页组件文案，把 "page" 改成 "页"
if (zhCN.Pagination) {
  zhCN.Pagination = {
    ...zhCN.Pagination,
    items_per_page: '页',
  };
}

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 应用中文配置
app.config.globalProperties.$dayjs = dayjs;
app.config.globalProperties.$locale = locale;

app.use(pinia)
    .use(router)
    .use(antDesign, { 
        locale: zhCN,
        dayjsLocale: 'zh-cn'
    })
    .mount('#app')

// 应用启动时恢复动态路由
router.isReady().then(() => {
    const authStore = useAuthStore()
    const token = localStorage.getItem('user_token')
    if (token && authStore.menus && authStore.menus.length > 0) {
        // 如果有 token 和持久化的菜单，强制恢复动态路由（刷新后路由实例是新的，需要重新添加）
        restoreDynamicRoutes(authStore.menus, true)
    }
})
