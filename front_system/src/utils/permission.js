import { useAuthStore } from '@/stores/auth'

/**
 * 检查是否有按钮权限
 * @param {string|string[]} permission - 权限标识，可以是单个字符串或字符串数组
 * @returns {boolean} - 是否有权限
 */
export function hasPermission(permission) {
  if (!permission) {
    return true // 如果没有指定权限，默认允许
  }
  
  const authStore = useAuthStore()
  
  // 优先从 userInfo.buttons 中获取权限列表
  let buttonList = []
  if (authStore.userInfo && authStore.userInfo.buttons && Array.isArray(authStore.userInfo.buttons)) {
    buttonList = authStore.userInfo.buttons
  } else if (authStore.buttons && Array.isArray(authStore.buttons)) {
    // 兼容旧的方式：从按钮菜单中提取 perms
    buttonList = authStore.buttons
      .filter(btn => btn && btn.perms)
      .map(btn => btn.perms)
  }
  
  if (buttonList.length === 0) {
    return false // 如果没有按钮权限列表，默认不允许
  }
  
  // 如果 permission 是数组，检查是否至少有一个权限
  if (Array.isArray(permission)) {
    return permission.some(perm => buttonList.includes(perm))
  }
  
  // 单个权限检查
  return buttonList.includes(permission)
}
