# front_app

这是对 `front_service`（VitePress）的一期重构版本：**Vue 3 + Vite + Vue Router**。

目标：**页面效果先与当前 VitePress 版本保持一致**，后续再逐步优化工程化与业务能力。

## 开发

```bash
npm install
npm run dev
```

## 构建

```bash
npm run build
```

## 说明

- **路由**：
  - `/`：首页（三屏滚动吸附 + reveal）
  - `/dynamic/blogs`：博客列表（复用 `BlogList`，请求 `/api/...`）
  - `/dynamic/projects`：项目经历列表（复用 `BlogList`）
  - `/dynamic/agent`：智能体页（localStorage token gate）
- **本地代理**：`/api` 默认转发到 `http://localhost:11007`（可用 `VITE_API_PROXY_TARGET` 覆盖）。
- **可选 iconfont**：设置 `VITE_ICONFONT_CSS` 后会自动注入对应的 `<link rel="stylesheet">`。

