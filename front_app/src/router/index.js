import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '../views/HomePage.vue'
import BlogsPage from '../views/BlogsPage.vue'
import ProjectsPage from '../views/ProjectsPage.vue'
import AgentPage from '../views/AgentPage.vue'
import AboutPage from '../views/AboutPage.vue'

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    { path: '/', name: 'home', component: HomePage, meta: { title: 'zyfan' } },
    { path: '/dynamic/blogs', name: 'blogs', component: BlogsPage, meta: { title: '博客 · zyfan' } },
    { path: '/dynamic/projects', name: 'projects', component: ProjectsPage, meta: { title: '项目经历 · zyfan' } },
    { path: '/dynamic/agent', name: 'agent', component: AgentPage, meta: { title: '智能体 · zyfan' } },
    { path: '/about', name: 'about', component: AboutPage, meta: { title: '关于 · zyfan' } },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    // 首页滚动/吸附由脚本接管，不在这里强制归顶
    if (to.path === '/' && from.path === '/') return false
    return { top: 0, left: 0 }
  },
})

router.afterEach((to) => {
  if (typeof document === 'undefined') return
  const title = to?.meta?.title
  if (typeof title === 'string' && title.trim()) document.title = title
})

export default router

