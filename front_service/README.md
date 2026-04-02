# VitePress 网站前端

基于 VitePress 构建的网站前端项目。

## 目录结构（主题）

```
front_service/
├── .vitepress/
│   ├── config.js
│   ├── components/          # 全局注册的 Markdown 内嵌组件
│   └── theme/
│       ├── index.js         # 主题入口：注册组件 + 挂载首页滚动增强
│       ├── AuthGateLayout.vue
│       ├── homeScroll.client.js   # 首页三屏吸附 / reveal（纯浏览器端，与内联版逻辑一致）
│       ├── utils/
│       │   └── normalizeLoginHref.js
│       ├── custom.css
│       └── …
├── dynamic/                 # 动态路由 Markdown（博客、项目、智能体等）
├── index.md
└── …
```

## 开发

```bash
npm run dev
```

## 构建

```bash
npm run build
```

## 预览

```bash
npm run preview
```
