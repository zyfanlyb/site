import { defineStore } from 'pinia'
import axios from '@/utils/request'
import { post } from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    menus: [],
    buttons: [],
    userInfo: null,
    redirectUrl: '',
    dynamicRoutesAdded: false // 动态路由是否已添加
  }),

  persist: {
    enabled: true,
    strategies: [
      {
        key: 'auth-store',
        storage: localStorage,
        paths: ['menus', 'buttons', 'userInfo', 'dynamicRoutesAdded', 'redirectUrl']
      }
    ]
  },

  actions: {

    async fetchUserMenus() {
      try {
        const response = await axios.get('/sysMenu/userMenus')
        const flatMenus = response.data || []

        // 提取所有按钮（type=2）
        const buttons = flatMenus.filter(menu => menu.type === '2' || menu.type === 2)

        // 从扁平结构构建树形结构
        const buildMenuTree = (menus) => {
          if (!menus || !Array.isArray(menus)) {
            return []
          }

          // 过滤出菜单和目录类型（type=0或1）
          const menuList = menus.filter(menu => menu.type === '0' || menu.type === 0 || menu.type === '1' || menu.type === 1)

          // 创建菜单映射表，方便查找
          const menuMap = new Map()
          menuList.forEach(menu => {
            menuMap.set(menu.id, { ...menu, children: [] })
          })

          // 构建树形结构
          const tree = []
          menuList.forEach(menu => {
            const menuItem = menuMap.get(menu.id)
            const parentId = menu.parentId

            if (!parentId || parentId === 0 || parentId === '0') {
              // 顶级菜单
              tree.push(menuItem)
            } else {
              // 子菜单，添加到父菜单的children中
              const parent = menuMap.get(parentId)
              if (parent) {
                parent.children.push(menuItem)
              } else {
                // 如果找不到父菜单，也作为顶级菜单处理（可能是数据不完整）
                tree.push(menuItem)
              }
            }
          })

          // 递归排序子菜单
          const sortMenuTree = (menuTree) => {
            menuTree.sort((a, b) => {
              const sortA = a.sort || 0
              const sortB = b.sort || 0
              if (sortA !== sortB) {
                return sortA - sortB
              }
              // 如果sort相同，按创建时间倒序
              const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
              const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
              return timeB - timeA
            })
            menuTree.forEach(menu => {
              if (menu.children && menu.children.length > 0) {
                sortMenuTree(menu.children)
              }
            })
          }

          sortMenuTree(tree)

          tree.unshift({
            id: 'fixed_home',
            name: '首页',
            path: 'index',
            type: '1',
            icon: 'HomeOutlined',
            children: []
          })

          return tree
        }

        const menuTree = buildMenuTree(flatMenus)

        this.$patch({
          menus: menuTree,
          buttons: buttons
        })
        return this.menus
      } catch (error) {
        throw error
      }
    },

    async fetchUserInfo() {
      try {
        const response = await post('/user/info')
        this.$patch({
          userInfo: response.data
        })
        return this.userInfo
      } catch (error) {
        throw error
      }
    },

    setMenus(menus) {
      // 使用 $patch 进行状态更新
      this.$patch({
        menus: menus
      })
    },

    setRedirectUrl(url) {
      this.$patch({
        redirectUrl: url
      })
    },

    clearToken() {
      localStorage.removeItem('user_token');
      this.$patch({
        menus: [],
        buttons: [],
        userInfo: null,
        redirectUrl: '',
        dynamicRoutesAdded: false // 重置动态路由标志
      })
    },

    /**
     * 登录后初始化：获取菜单、用户信息并添加动态路由
     */
    async initAfterLogin() {
      try {
        
        // 重置动态路由标志，确保重新添加
        this.$patch({ dynamicRoutesAdded: false });
        
        // 获取菜单和用户信息
        await Promise.all([
          this.fetchUserMenus(),
          this.fetchUserInfo()
        ]);
        
        // 动态导入路由恢复函数并添加路由（强制重新添加）
        const { restoreDynamicRoutes } = await import('@/router/index.js');
        if (this.menus && this.menus.length > 0) {
          restoreDynamicRoutes(this.menus, true); // 强制重新添加
        } else {
          console.warn('未获取到菜单数据');
        }
        
        return {
          menus: this.menus,
          userInfo: this.userInfo
        };
      } catch (error) {
        console.error('初始化失败:', error);
        // 重置标志，以便下次重试
        this.$patch({ dynamicRoutesAdded: false });
        throw error;
      }
    }
  }
})
