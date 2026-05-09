import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { loadEnv } from 'vite'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

const siteUrl = 'https://www.zyfan.top'
const defaultTitle = 'zyfan'
const defaultDescription = 'zyfan的个人小站 · Web全栈与智能体'

/** 与 VitePress 生成 sitemap 的路径规则一致（cleanUrls 为 false 时带 .html） */
function mdFileToPathname(pageMd, cleanUrls) {
  let url = pageMd.replace(/(^|\/)index\.md$/, '$1')
  url = url.replace(/\.md$/, cleanUrls ? '' : '.html')
  if (!url || url === '/') return '/'
  return url.startsWith('/') ? url : `/${url}`
}

const sharedHead = [
  ['link', { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' }],
  ['meta', { name: 'keywords', content: 'zyfan,全栈,智能体,Java,Spring Boot,Vue,VitePress' }],
  ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
  ['meta', { property: 'og:site_name', content: defaultTitle }],
  ['meta', { property: 'og:image', content: `${siteUrl}/logo.png` }],
  ['meta', { property: 'og:locale', content: 'zh_CN' }],
  ['meta', { property: 'og:type', content: 'website' }],
]

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, path.resolve(__dirname, '..'), '')
  const iconfontCss = (env.VITE_ICONFONT_CSS || '').trim()
  const head = [...sharedHead]
  if (iconfontCss) {
    head.push(['link', { rel: 'stylesheet', href: iconfontCss }])
  }

  return {
    title: defaultTitle,
    description: defaultDescription,

    themeConfig: {
      logo: '/logo.png',

      nav: [
        { text: '首页', link: '/' },
        { text: '博客', link: '/dynamic/blogs.html' },
        { text: '项目经历', link: '/dynamic/projects.html' },
      ],

      sidebar: {
        '/guide/': [
          {
            text: '开始',
            items: [
              { text: '介绍', link: '/guide/' },
              { text: '快速开始', link: '/guide/getting-started' },
            ],
          },
        ],
      },

      footer: {
        message:
          'zyfan · 全栈工程师 · <a href="mailto:2938668548@qq.com">2938668548@qq.com</a>',
        copyright:
          '© 2026 版权与声明：本站内容仅代表个人观点，转载/引用请注明出处，如有侵权请邮件联系删除。',
      },
    },

    base: '/',

    lang: 'zh-CN',

    lastUpdated: false,

    sitemap: {
      hostname: siteUrl,
      transformItems(items) {
        return items.filter((item) => {
          if (!item?.url) return true
          try {
            const p = new URL(item.url, siteUrl).pathname
            if (p.replace(/\/$/, '').toLowerCase() === '/readme.html') return false
            return true
          } catch {
            return true
          }
        })
      },
    },

    head,

    transformHead({ page, pageData, title, description, siteConfig }) {
      if (pageData.isNotFound || page === '404.md') return []

      const pathname = mdFileToPathname(page, siteConfig.cleanUrls)
      const canonicalUrl =
        pathname === '/' ? `${siteUrl}/` : `${siteUrl.replace(/\/$/, '')}${pathname}`

      const desc = description || defaultDescription
      const pageTitle = title || defaultTitle

      const tags = [
        ['link', { rel: 'canonical', href: canonicalUrl }],
        ['meta', { property: 'og:title', content: pageTitle }],
        ['meta', { property: 'og:description', content: desc }],
        ['meta', { property: 'og:url', content: canonicalUrl }],
        ['meta', { name: 'twitter:title', content: pageTitle }],
        ['meta', { name: 'twitter:description', content: desc }],
        ['meta', { name: 'twitter:image', content: `${siteUrl}/logo.png` }],
      ]

      if (page === 'index.md') {
        tags.push([
          'script',
          { type: 'application/ld+json' },
          JSON.stringify({
            '@context': 'https://schema.org',
            '@type': 'Person',
            name: 'zyfan',
            url: `${siteUrl}/`,
            email: '2938668548@qq.com',
            jobTitle: '全栈工程师',
            description: defaultDescription,
          }),
        ])
        tags.push([
          'script',
          { type: 'application/ld+json' },
          JSON.stringify({
            '@context': 'https://schema.org',
            '@type': 'WebSite',
            name: defaultTitle,
            url: `${siteUrl}/`,
            description: defaultDescription,
          }),
        ])
      }

      return tags
    },

    vite: {
      publicDir: '.vitepress/public',
      server: {
        proxy: {
          '/api': {
            target: 'http://localhost:11007',
            changeOrigin: true,
            rewrite: (p) => p.replace(/^\/api/, ''),
          },
        },
      },
    },
  }
})
