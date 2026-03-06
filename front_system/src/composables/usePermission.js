import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { hasPermission } from '@/utils/permission'

/**
 * 按钮权限 Composable
 * 用于在组件中检查按钮权限
 */
export function usePermission() {
  const authStore = useAuthStore()
  
  /**
   * 检查是否有权限
   * @param {string|string[]} permission - 权限标识
   * @returns {boolean}
   */
  const checkPermission = (permission) => {
    return hasPermission(permission)
  }
  
  /**
   * 创建权限检查的计算属性
   * @param {string|string[]} permission - 权限标识
   * @returns {import('vue').ComputedRef<boolean>}
   */
  const createPermissionChecker = (permission) => {
    return computed(() => hasPermission(permission))
  }
  
  /**
   * 获取所有按钮权限列表
   * @returns {string[]}
   */
  const getButtonList = () => {
    if (authStore.userInfo && authStore.userInfo.buttons && Array.isArray(authStore.userInfo.buttons)) {
      return authStore.userInfo.buttons
    } else if (authStore.buttons && Array.isArray(authStore.buttons)) {
      return authStore.buttons
        .filter(btn => btn && btn.perms)
        .map(btn => btn.perms)
    }
    return []
  }
  
  return {
    checkPermission,
    createPermissionChecker,
    getButtonList,
    hasPermission // 导出函数，方便直接使用
  }
}
