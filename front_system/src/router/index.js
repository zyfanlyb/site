import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'
import {storeToRefs} from "pinia";

// 使用 import.meta.glob 预加载所有视图组件，确保在生产环境中正确加载
const componentModules = import.meta.glob('../views/**/*.vue');


/**
 * 递归确保所有路由（包括children）的meta.requiresAuth都设置为true
 * @param {Array} routes - 路由配置数组
 */
function ensureRequiresAuth(routes) {
    routes.forEach(route => {
        // 确保meta对象存在
        if (!route.meta) {
            route.meta = {};
        }
        // 强制设置requiresAuth为true
        route.meta.requiresAuth = true;
        // 如果有children，递归处理
        if (route.children && route.children.length > 0) {
            ensureRequiresAuth(route.children);
        }
    });
}

/**
 * 将menu数组转换为路由配置
 * @param {Array} menus - 菜单数组
 * @param {String} parentPath - 父路径前缀
 * @returns {Array} 路由配置数组
 */
function generateRoutesFromMenus(menus, parentPath = '/cms') {
    const routes = [];
    
    menus.forEach(menu => {
        // 只处理目录(type=0)和菜单(type=1)，跳过按钮(type=2)
        if (menu.type === '2') {
            return;
        }
        
        // 如果是目录类型(type=0)，直接递归处理子菜单，目录本身不创建路由
        if (menu.type === '0') {
            if (menu.children && menu.children.length > 0) {
                // 递归处理子菜单
                const childRoutes = generateRoutesFromMenus(menu.children, parentPath);
                routes.push(...childRoutes);
            }
            return;
        }
        
        // 处理菜单类型(type=1)
        // 构建路由路径
        let routePath = menu.path;
        if (!routePath) {
            return; // 如果没有path，跳过
        }
        
        // 处理路径：如果path是绝对路径（以/cms开头），直接使用；否则拼接父路径
        if (routePath.startsWith('/cms')) {
            // 已经是绝对路径，直接使用
        } else if (routePath.startsWith('/')) {
            // 以/开头但不是/cms，需要加上/cms前缀
            routePath = '/cms' + routePath;
        } else {
            // 相对路径，拼接父路径
            routePath = parentPath + '/' + routePath;
        }
        
        // 清理路径，防止重复的/cms
        routePath = routePath.replace(/\/cms\/cms/g, '/cms').replace(/\/+/g, '/');
        
        // 转换为相对于/cms的相对路径（用于router.addRoute）
        let routerPath = routePath.replace('/cms', '').replace(/^\//, '') || 'index';
        
        // 构建路由配置
        const route = {
            path: routerPath,
            // 使用menu.id确保唯一性，如果id不存在则使用path或name
            name: menu.id ? `menu_${menu.id}` : (menu.path || menu.name || `route_${Date.now()}`),
            meta: {
                title: menu.name,
                requiresAuth: true,
                menuId: menu.id,
                perms: menu.perms
            }
        };
        
        // 菜单类型，需要设置component
        if (menu.component) {
            // 将component路径转换为实际的Vue组件导入路径
            // 例如: "system/MenuList" -> "../views/system/MenuList.vue"
            // 或者: "@/views/system/MenuList" -> "../views/system/MenuList.vue"
            let componentPath = menu.component;
            
            // 移除@/前缀（如果存在）
            if (componentPath.startsWith('@/')) {
                componentPath = componentPath.substring(2);
            }
            
            // 移除views/前缀（如果存在），因为我们要添加../views/
            if (componentPath.startsWith('views/')) {
                componentPath = componentPath.substring(6);
            }
            
            // 确保有.vue后缀
            if (!componentPath.endsWith('.vue')) {
                componentPath = componentPath + '.vue';
            }
            
            // 转换为相对路径（从router/index.js到views目录）
            const relativePath = `../views/${componentPath}`;
            
            // 使用 import.meta.glob 预加载的模块，确保在生产环境中正确工作
            route.component = () => {
                const moduleLoader = componentModules[relativePath];
                if (moduleLoader) {
                    return moduleLoader();
                } else {
                    console.warn(`Component not found: ${relativePath}, using default Index component`);
                    // 如果组件不存在，使用默认的Index组件
                    return componentModules['../views/Index.vue']();
                }
            };
        } else {
            // 如果没有component，使用默认的Index组件
            route.component = () => componentModules['../views/Index.vue']();
        }
        
        routes.push(route);
    });

    // 固定将首页动态路由放在第一个，其他路由保持原有顺序
    const isHomeRoute = (route) => {
        if (!route) {
            return false;
        }
        const path = (route.path || '').replace(/^\/+|\/+$/g, '').toLowerCase();
        const title = ((route.meta && route.meta.title) || '').trim();
        return path === 'index' || path === '' || title === '首页';
    };

    const homeRouteIndex = routes.findIndex(isHomeRoute);
    if (homeRouteIndex > 0) {
        const [homeRoute] = routes.splice(homeRouteIndex, 1);
        routes.unshift(homeRoute);
    }

    return routes;
}

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/cms',
            name: 'cms',
            redirect: (to) => ({
                path: '/cms/index',
                query: to.query,
                hash: to.hash
            }),
            component: () => import('@/views/Home.vue'),
            meta: {requiresAuth: true},
            children: [
                {
                    name: 'index',
                    path: 'index',
                    component: () => import('@/views/Index.vue'),
                    meta: {requiresAuth: true, title: '首页'}
                },
                {
                    name: 'profile',
                    path: 'profile',
                    component: () => import('@/views/system/Profile.vue'),
                    meta: {requiresAuth: true, title: '个人中心'}
                },
            ]
        },
        {
            path: '/cms/login',
            name: 'login',
            component: ()=>import("@/views/Login.vue"),
            meta: {
                requiresAuth: false // 回调页面不需要 token 检查
            }
        },
        {
            path: '/',
            redirect: '/cms',
        }
    ]
})

/**
 * 恢复动态路由
 * @param {Array} menus - 菜单数组
 * @param {Boolean} force - 是否强制重新添加（即使已添加过）
 */
export function restoreDynamicRoutes(menus, force = false) {
    if (!menus || menus.length === 0) {
        return;
    }
    
    const dynamicRoutes = generateRoutesFromMenus(menus);
    ensureRequiresAuth(dynamicRoutes);
    
    const authStore = useAuthStore();
    
    // 如果强制重新添加，先重置标志
    if (force) {
        authStore.$patch({ dynamicRoutesAdded: false });
    }
    
    // 检查路由是否已添加，避免重复添加
    if (!authStore.dynamicRoutesAdded) {
        dynamicRoutes.forEach(route => {
            router.addRoute('cms', route);
        });
        authStore.$patch({ dynamicRoutesAdded: true });
    }
}

router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();
    const token = localStorage.getItem('user_token');
    const iscmsPath = to.path.startsWith('/cms');
    const isLoginRoute = to.path === '/cms/login' || to.name === 'login';
    
    // 如果访问 /auth 路径且有uuid参数，则尝试从uuid获取token并保存redirectUrl
    // 不再限制“没有token”的情况，允许覆盖现有会话，按最新uuid为准
    if (to.path.startsWith('/cms') && to.query.uuid) {
        try {
            const service = (await import('@/utils/request.js')).default;
            const formData = new URLSearchParams();
            formData.append('uuid', to.query.uuid);
            const response = await service.post('/noAuth/getCachedTokenAndRedirectUrl', formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });
            
            if (response.data && response.data.userToken && response.data.redirectUrl) {
                // 保存userToken到localStorage
                localStorage.setItem('user_token', response.data.userToken);

                // 保存redirectUrl到store
                authStore.setRedirectUrl(response.data.redirectUrl);
                
                // 初始化：获取菜单、用户信息并添加动态路由
                try {
                    await authStore.initAfterLogin();
                    
                    // 移除URL中的uuid参数，重新导航
                    const newQuery = { ...to.query };
                    delete newQuery.uuid;
                    // 兼容后端重定向到 /cms/（尾斜杠）导致的路由不匹配风险
                    next({ path: '/cms/index', query: newQuery, replace: true });
                    return;
                } catch (initError) {
                    console.error('初始化失败:', initError);
                    // 初始化失败，清除token，跳转到登录页
                    localStorage.removeItem('user_token');
                    next({name: 'login'});
                    return;
                }
            }
        } catch (error) {
            console.error('获取缓存的token和redirectUrl失败:', error);
            next();
            return;
        }
    }
    
    // 处理所有 /cms 下的路由（除了登录页）
    if (iscmsPath && !isLoginRoute) {
        // 如果没有token，跳转到登录页
        if (!token) {
            const savedRedirectUrl = authStore.redirectUrl;
            if (savedRedirectUrl) {
                window.location.href = savedRedirectUrl+'?logout=true';
            } else {
                const callbackUrl = window.location.origin + `${import.meta.env.VITE_PRE_PATH}/login`
                const authUrl = (import.meta.env.VITE_LOGIN_DOMAIN || window.location.origin)+`/auth/login?redirectUrl=${encodeURIComponent(callbackUrl)}`
                window.location.href = authUrl
            }
            return;
        }
        
        const { menus, userInfo } = storeToRefs(authStore);

        // 动态路由未加载时，不管从哪个 /cms 路径进入都优先恢复动态路由。
        let routeExists = router.resolve(to.path).matched.length > 0;
        const shouldRestoreDynamicRoutes = !routeExists || !authStore.dynamicRoutesAdded;

        if (shouldRestoreDynamicRoutes) {
            try {
                if (menus.value.length === 0) {
                    await authStore.fetchUserMenus();
                }

                if (!userInfo.value) {
                    await authStore.fetchUserInfo();
                }

                if (menus.value.length > 0) {
                    // 刷新或首次进入时路由实例是新的，强制重新注入最稳妥
                    restoreDynamicRoutes(menus.value, true);
                    routeExists = router.resolve(to.path).matched.length > 0;
                }

                if (routeExists) {
                    next({ ...to, replace: true });
                    return;
                }

                // 菜单里没有目标路径，兜底回首页
                console.warn(`Route ${to.path} not found in menus, redirecting to home`);
                next({ name: 'cms' });
                return;
            } catch (error) {
                console.error('Failed to restore routes:', error);
                next({ name: 'cms' });
                return;
            }
        }
        
        // 路由存在，允许通过
        // 注意：如果路由存在，说明它要么是静态路由，要么是动态路由且已正确添加
    }
    
    // 需要认证的路由（非 /auth 下的路由）
    if (to.meta && to.meta.requiresAuth) {
        if (!token) {
            next({name: 'login'});
            return;
        }
    }
    
    next();
})


export default router