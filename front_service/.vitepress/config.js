import { defineConfig } from 'vitepress'

const siteUrl = 'https://www.zyfan.top'

export default defineConfig({
  title: 'zyfan',
  description: 'zyfan的个人小站',
  
  // 主题配置
  themeConfig: {
    // 网站logo
    logo: '/logo.png',
    
    // 导航栏
    nav: [
      { text: '首页', link: '/' },
      { text: '博客', link: '/dynamic/blogs' },
      { text: '项目经历', link: '/dynamic/projects' },
      { text: '智能体', link: '/dynamic/agent' }
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

    
    // 页脚
    footer: {
      message: 'zyfan · 全栈工程师 · <a href="mailto:2938668548@qq.com">2938668548@qq.com</a>',
      copyright: '© 2026 版权与声明：本站内容仅代表个人观点，转载/引用请注明出处，如有侵权请邮件联系删除。'
    },
  },
  
  // 基础路径
  base: '/',
  
  // 语言
  lang: 'zh-CN',
  
  // 最后更新时间
  lastUpdated: false,

  // Sitemap：帮助搜索引擎更快发现页面
  sitemap: {
    hostname: siteUrl
  },

  // 基础 SEO：canonical / OG / 结构化数据
  head: [
    ['link', { rel: 'canonical', href: `${siteUrl}/` }],
    ['meta', { property: 'og:title', content: 'zyfan' }],
    ['meta', { property: 'og:description', content: 'zyfan的个人小站' }],
    ['meta', { property: 'og:url', content: `${siteUrl}/` }],
    ['meta', { name: 'twitter:card', content: 'summary' }],
    [
      'script',
      { type: 'application/ld+json' },
      JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'Person',
        name: 'zyfan',
        url: `${siteUrl}/`,
        email: '2938668548@qq.com',
        jobTitle: '全栈工程师'
      })
    ]
  ],
  
  // 开发环境 API 代理到 site_service
  vite: {
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:11007',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})
