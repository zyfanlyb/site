<template>
  <a-layout class="layout-root">
    <a-layout-header class="header slide-down">
        <a-flex justify="flex-end" align="center" :style="{ width: '100%', height: '100%' }">
        <a-flex align="center" gap="middle">
          <!-- 如果持久化的redirectUrl有值，显示返回按钮 -->
          <a-button
            v-if="redirectUrl"
            @click="handleBack"
            class="back-button"
          >
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            <span>返回</span>
          </a-button>
          <a-dropdown v-model:open="dropdownOpen" :trigger="['click']">
            <template #overlay>
              <a-menu @click="handleMenuClick" class="user-dropdown-menu">
                <a-menu-item key="profile">
                  <ProfileOutlined />
                  <span>个人中心</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" class="logout-item" style="color: #ff4d4f;">
                  <LogoutOutlined style="color: #ff4d4f;" />
                  <span style="color: #ff4d4f;">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
            <a-button type="text" style="padding: 0; height: auto; display: flex; align-items: center; gap: 8px; border: 0px;margin-right: 50px;">
              <a-avatar size="large" :src="avatarUrl">
                <template #icon v-if="!avatarUrl">
                  <UserOutlined/>
                </template>
              </a-avatar>
              <span v-if="userInfo?.nickname">{{ userInfo.nickname }}</span>
            </a-button>
          </a-dropdown>
        </a-flex>
      </a-flex>
    </a-layout-header>
    <a-layout class="main-layout">
      <a-layout-sider width="240" class="sider slide-in-left">
        <div class="sider-inner">
          <a-menu
              v-model:openKeys="state.openKeys"
              v-model:selectedKeys="state.selectedKeys"
              :inline-collapsed="state.collapsed"
              :items="menuItems"
              mode="inline"
              @click="openMenuItem"
              class="custom-menu"
          >
          </a-menu>
        </div>
      </a-layout-sider>
      <a-layout-content class="content-wrapper expand-content">
        <div class="content">
          <router-view v-slot="{ Component, route }">
            <transition name="page-slide" mode="out-in">
              <div v-if="Component" :key="route.path" class="page-wrapper">
                <component :is="Component" />
              </div>
            </transition>
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script lang="ts" setup>
import {ref, h, computed, watchEffect, watch, onMounted} from 'vue';
import * as Icons from '@ant-design/icons-vue';
import {UserOutlined, AppstoreOutlined, LogoutOutlined, ProfileOutlined, ArrowLeftOutlined} from '@ant-design/icons-vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import {storeToRefs} from "pinia";
import service from '@/utils/request';
import { message } from 'ant-design-vue';
import { getFilePreviewUrl } from '@/utils/file';

const state = ref({
  collapsed: false,
  selectedKeys: [],
  openKeys: [],
  preOpenKeys: [],
});

const dropdownOpen = ref(false);

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const { userInfo, redirectUrl } = storeToRefs(authStore);

// 头像预览URL（使用ref存储，因为需要异步加载）
const avatarUrl = ref('');

// 监听userInfo变化，异步加载头像URL
watchEffect(async () => {
  if (userInfo.value?.avatar) {
    avatarUrl.value = await getFilePreviewUrl(userInfo.value.avatar);
  } else {
    avatarUrl.value = '';
  }
});

// Convert menu data to Ant Design Menu items format
const menuItems = computed(() => {
  const { menus } = storeToRefs(authStore);
  const filter = flattenMenus(menus.value.filter(menu => {
    return menu.type == "0" || menu.type == "1"
  }));
  return ensureHomeMenuFirst(filter);
});

function isHomeMenuData(menu) {
  if (!menu) {
    return false;
  }
  const rawPath = String(menu.path || menu.key || '').replace(/^\/+|\/+$/g, '').toLowerCase();
  const normalizedPath = rawPath.replace(/^site\//, '');
  const name = String(menu.name || menu.label || menu.title || '').trim();
  return name === '首页' || rawPath === 'site/index' || normalizedPath === 'index';
}

function ensureHomeMenuFirst(items) {
  const menuList = Array.isArray(items) ? [...items] : [];
  const isHomeMenuItem = (item) => {
    if (!item) {
      return false;
    }
    const key = String(item.key || '').replace(/^\/+|\/+$/g, '').toLowerCase();
    const normalizedKey = key.replace(/^site\//, '');
    const label = String(item.label || '').trim();
    return label === '首页' || key === 'site/index' || normalizedKey === 'index';
  };

  // 移除所有首页菜单（避免重复）
  const filteredList = menuList.filter(item => !isHomeMenuItem(item));

  // 在最前面插入固定的首页菜单
  filteredList.unshift({
    key: '/site/index',
    label: '首页',
    title: '首页',
    icon: () => h(Icons.HomeOutlined)
  });

  return filteredList;
}

// 根据图标名称获取图标组件
function getIconComponent(iconName) {
  // 如果图标名称为空或无效，返回null
  if (!iconName || typeof iconName !== 'string' || iconName.trim() === '') {
    return null;
  }
  // 如果图标名称存在，返回对应的图标组件
  const IconComponent = Icons[iconName];
  // 如果找不到，返回null（不使用默认图标，避免显示错误的图标）
  return IconComponent || null;
}

// Flatten nested menu structure and convert to Ant Design format
// 考虑菜单层级关系，将子菜单作为内联菜单
function flattenMenus(menus) {
  if (!menus || menus.length === 0) {
    return [];
  }

  return menus.map(menu => {
    // 处理路径：确保路径格式与路由path匹配
    let menuPath = menu.path;
    if (menuPath) {
      // 如果path不是以/site开头，需要加上/site前缀（与路由生成逻辑保持一致）
      if (!menuPath.startsWith('/site')) {
        if (menuPath.startsWith('/')) {
          // 以/开头但不是/site，加上/site前缀
          menuPath = '/site' + menuPath;
        } else {
          // 相对路径，加上/site/前缀
          menuPath = '/site/' + menuPath;
        }
      }
    } else {
      // 如果没有path，使用默认路径（对于目录类型，使用id作为key）
      menuPath = menu.key || `/site/menu_${menu.id}`;
    }

    // 首页图标固定为 HomeOutlined
    const iconName = isHomeMenuData(menu) ? 'HomeOutlined' : menu.icon;
    const IconComponent = getIconComponent(iconName);

    // 递归处理子菜单，确保子菜单也以内联方式显示
    let children = undefined;
    if (menu.children && menu.children.length > 0) {
      // 过滤子菜单，只包含目录和菜单类型，排除按钮类型
      const validChildren = menu.children.filter(child => child.type === "0" || child.type === "1");
      if (validChildren.length > 0) {
        children = flattenMenus(validChildren);
      }
    }

    // 构建菜单项
    const menuItem = {
      key: menuPath,
      label: menu.name,
      title: menu.name,
      children: children,
      icon: undefined,
    };

    // 只有当图标组件存在时才设置icon属性
    if (IconComponent) {
      menuItem.icon = () => h(IconComponent);
    }

    return menuItem;
  });
}


function openMenuItem({ item, key, keyPath }){
  // key已经是完整的路径，直接使用
  router.push({path: key})
};

// 根据当前路由路径，查找对应的菜单项及其所有父菜单的key
function findMenuKeysByPath(path, menus, parentKeys = []) {
  for (const menu of menus) {
    let menuPath = menu.path;
    if (menuPath) {
      if (!menuPath.startsWith('/site')) {
        if (menuPath.startsWith('/')) {
          menuPath = '/site' + menuPath;
        } else {
          menuPath = '/site/' + menuPath;
        }
      }
    } else {
      menuPath = menu.key || `/site/menu_${menu.id}`;
    }

    const currentKeys = [...parentKeys, menuPath];

    // 如果路径匹配，返回当前菜单的key和所有父菜单的key
    if (menuPath === path) {
      return {
        selectedKey: menuPath,
        openKeys: parentKeys
      };
    }

    // 如果有子菜单，递归查找
    if (menu.children && menu.children.length > 0) {
      const validChildren = menu.children.filter(child => child.type === "0" || child.type === "1");
      const result = findMenuKeysByPath(path, validChildren, currentKeys);
      if (result) {
        return result;
      }
    }
  }
  return null;
}

// 监听路由变化，更新菜单的选中状态和展开状态
watch(() => route.path, (newPath) => {
  const { menus } = storeToRefs(authStore);
  const validMenus = menus.value.filter(menu => menu.type === "0" || menu.type === "1");
  const menuKeys = findMenuKeysByPath(newPath, validMenus);

  if (menuKeys) {
    state.value.selectedKeys = [menuKeys.selectedKey];
    // 合并现有的openKeys和新的openKeys，避免关闭已展开的菜单
    const newOpenKeys = [...new Set([...state.value.openKeys, ...menuKeys.openKeys])];
    state.value.openKeys = newOpenKeys;
  }
}, { immediate: true });

// 组件挂载时，初始化菜单状态
onMounted(() => {
  const currentPath = route.path;
  const { menus } = storeToRefs(authStore);
  const validMenus = menus.value.filter(menu => menu.type === "0" || menu.type === "1");
  const menuKeys = findMenuKeysByPath(currentPath, validMenus);

  if (menuKeys) {
    state.value.selectedKeys = [menuKeys.selectedKey];
    state.value.openKeys = menuKeys.openKeys;
  }
});

// 处理下拉菜单点击
const handleMenuClick = async ({ key }) => {
  if (key === 'logout') {
    await handleLogout();
  } else if (key === 'profile') {
    // 跳转到个人中心页面
    dropdownOpen.value = false;
    router.push('/site/profile');
  }
};

// 返回到外部系统（根据持久化的 redirectUrl）
const handleBack = () => {
  const savedRedirectUrl = redirectUrl.value;
  if (savedRedirectUrl) {
    window.location.href = savedRedirectUrl;
  }
};

// 退出登录
const handleLogout = async () => {
  // 在清除token之前先保存 redirectUrl，因为 clearToken() 会清空它
  const savedRedirectUrl = authStore.redirectUrl;

  try {
    await service.post('/logout');
  } catch (error) {
    console.error('退出登录接口调用失败:', error);
  } finally {
    // 无论接口是否成功，都清除本地token和用户信息
    authStore.clearToken();

    // 根据保存的 redirectUrl 决定跳转位置
    if (savedRedirectUrl) {
      window.location.href = savedRedirectUrl+'?logout=true';
    } else {
      const url = `${(import.meta.env.VITE_LOGIN_DOMAIN || window.location.origin)}/auth/login?redirectUrl=${encodeURIComponent(window.location.origin+import.meta.env.VITE_PRE_PATH+'/login')}`
      window.location.href = url
    }
  }
};
</script>
<style scoped>
.layout {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}

.header {
  background: #ffffff;
  padding: 0 32px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

/* 菜单样式调整 - 增大菜单项 */
:deep(.custom-menu) {
  font-size: var(--sky-font-size-base);
}

:deep(.custom-menu .ant-menu-item) {
  height: 48px !important;
  line-height: 48px !important;
  font-size: var(--sky-font-size-base) !important;
  padding-left: 20px !important;
  margin: 4px 0 !important;
}

:deep(.custom-menu .ant-menu-submenu-title) {
  height: 48px !important;
  line-height: 48px !important;
  font-size: var(--sky-font-size-base) !important;
  padding-left: 20px !important;
  margin: 4px 0 !important;
}

:deep(.custom-menu .ant-menu-item-icon) {
  font-size: 18px !important;
  width: 18px !important;
  height: 18px !important;
}

:deep(.custom-menu .ant-menu-submenu-title .ant-menu-item-icon) {
  font-size: 18px !important;
  width: 18px !important;
  height: 18px !important;
}

:deep(.custom-menu .ant-menu-submenu .ant-menu-item) {
  padding-left: 40px !important;
  height: 44px !important;
  line-height: 44px !important;
  font-size: var(--sky-font-size-base) !important;
}

:deep(.custom-menu .ant-menu-submenu .ant-menu-item .ant-menu-item-icon) {
  font-size: 16px !important;
  width: 16px !important;
  height: 16px !important;
}

/* 菜单切换动画 */
:deep(.custom-menu .ant-menu-item),
:deep(.custom-menu .ant-menu-submenu-title) {
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible;
}

/* 菜单项点击动画 */
:deep(.custom-menu .ant-menu-item:active) {
  transform: scale(0.98);
  transition: transform 0.15s ease;
}

:deep(.custom-menu .ant-menu-submenu-title:active) {
  transform: scale(0.98);
  transition: transform 0.15s ease;
}

/* 页面包装器 */
.page-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform-origin: center center;
  will-change: transform, opacity;
  backface-visibility: hidden;
  perspective: 1000px;
}

/* 页面切换动画 - 现代化流畅效果 */
.page-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.page-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.55, 0.06, 0.68, 0.19);
  z-index: 1;
}

.page-slide-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.96);
  filter: blur(4px);
}

.page-slide-enter-to {
  opacity: 1;
  transform: translateX(0) scale(1);
  filter: blur(0);
}

.page-slide-leave-from {
  opacity: 1;
  transform: translateX(0) scale(1);
  filter: blur(0);
}

.page-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px) scale(0.98);
  filter: blur(2px);
}

/* 用户下拉菜单样式优化 */
:deep(.user-dropdown-menu) {
  min-width: 180px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 8px 4px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
}

:deep(.user-dropdown-menu .ant-menu-item) {
  height: 44px;
  line-height: 44px;
  padding: 0 20px;
  margin: 2px 4px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  min-height: 44px;
}

:deep(.user-dropdown-menu .ant-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.user-dropdown-menu .ant-menu-item span) {
  font-weight: 400;
  letter-spacing: 0.01em;
}

:deep(.user-dropdown-menu .ant-menu-item-icon) {
  font-size: 17px;
  width: 17px;
  height: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.user-dropdown-menu .logout-item) {
  color: #ff4d4f !important;
}

:deep(.user-dropdown-menu .logout-item:hover) {
  background-color: #fff2f0 !important;
  color: #ff4d4f !important;
}

:deep(.user-dropdown-menu .logout-item .ant-menu-item-icon) {
  color: #ff4d4f !important;
}

:deep(.user-dropdown-menu .logout-item span) {
  color: #ff4d4f !important;
  font-weight: 500;
}

:deep(.user-dropdown-menu .logout-item:hover span) {
  color: #ff4d4f !important;
}

:deep(.user-dropdown-menu .ant-menu-item-selected) {
  background-color: transparent;
}

:deep(.user-dropdown-menu .logout-item.ant-menu-item-selected) {
  background-color: #fff2f0 !important;
}

/* 下拉菜单分隔线样式 */
:deep(.user-dropdown-menu .ant-menu-divider) {
  margin: 8px 8px;
  border-color: rgba(0, 0, 0, 0.08);
  height: 1px;
}

/* 返回按钮样式 */
.back-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  margin-right: 24px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  background: #ffffff;
  color: rgba(0, 0, 0, 0.88);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.back-button:hover {
  border-color: #4096ff;
  color: #4096ff;
  background: #f0f7ff;
  box-shadow: 0 2px 8px rgba(64, 150, 255, 0.15);
  transform: translateY(-1px);
}

.back-button:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.back-button .anticon {
  font-size: 14px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover .anticon {
  transform: translateX(-2px);
}

.back-button span {
  line-height: 1;
}

/* 菜单从左边切入动画 */
.slide-in-left {
  animation: slideInLeft 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  transform: translateX(-100%);
  will-change: transform, opacity;
}

@keyframes slideInLeft {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 导航条向下切入动画 */
.slide-down {
  animation: slideDown 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  transform: translateY(-100%);
  opacity: 0;
  will-change: transform, opacity;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 内容块膨胀动画 */
.expand-content {
  animation: expandContent 0.9s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  transform: scale(0.8);
  opacity: 0;
  will-change: transform, opacity;
}

@keyframes expandContent {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
