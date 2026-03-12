import { defineConfig } from 'vitepress'

export default defineConfig({
  title: '网站文档',
  description: '基于VitePress的网站前端',
  
  // 主题配置
  themeConfig: {
    // 网站logo
    logo: '/logo.png',
    
    // 导航栏
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/' },
      { text: '关于', link: '/about/' },
      { text: '动态文章', link: '/dynamic/articles' }
    ],
    
    // 侧边栏
    sidebar: {
      '/guide/': [
        {
          text: '开始',
          items: [
            { text: '介绍', link: '/guide/' },
            { text: '快速开始', link: '/guide/getting-started' }
          ]
        }
      ]
    },
    
    // 社交链接
    socialLinks: [
      { icon: 'github', link: 'https://github.com' }
    ],
    
    // 页脚
    footer: {
      message: '基于 VitePress 构建',
      copyright: 'Copyright © 2024'
    },
    
    // 搜索
    search: {
      provider: 'local'
    }
  },
  
  // 基础路径
  base: '/',
  
  // 语言
  lang: 'zh-CN',
  
  // 最后更新时间
  lastUpdated: true,
  
  // 开发环境 API 代理到 site_service
  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:11007',
          changeOrigin: true
        }
      }
    }
  }
})
