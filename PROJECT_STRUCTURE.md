# 项目结构说明

## 项目架构

本项目采用前后端分离的架构，包含以下模块：

### 前端模块

#### 1. `front_system` - CMS管理后台前端
- **技术栈**: Vue 3 + Vite + Ant Design Vue
- **功能**: 
  - 系统管理（用户、角色、菜单）
  - CMS内容管理（文章管理等）
- **启动**: 
  ```bash
  cd front_system
  npm install
  npm run dev
  ```

#### 2. `front_service` - VitePress网站前端
- **技术栈**: VitePress
- **功能**: 静态文档网站
- **启动**:
  ```bash
  cd front_service
  npm install
  npm run dev
  ```

### 后端模块

#### 3. `site_cms` - CMS后台管理后端
- **技术栈**: Spring Boot 3 + MyBatis Plus + MySQL
- **功能**: 
  - 系统管理API（用户、角色、菜单）
  - CMS内容管理API（待实现）
- **启动**: 
  ```bash
  mvn spring-boot:run
  ```

#### 4. `site_service` - 网站后端服务
- **技术栈**: Spring Boot 3
- **功能**: 从CMS后台获取数据，提供给前端网站使用
- **启动**: 
  ```bash
  mvn spring-boot:run
  ```

## 数据流向

```
CMS管理后台 (front_system) 
    ↓
CMS后端 (site_cms) 
    ↓ (管理数据)
数据库
    ↓ (读取数据)
网站后端 (site_service)
    ↓
VitePress网站 (front_service)
```

## 开发流程

1. **CMS管理后台开发**
   - 在 `front_system` 中开发管理页面
   - 在 `site_cms` 中开发对应的后端API
   - 通过菜单管理配置页面路由

2. **网站前端开发**
   - 在 `front_service` 中编写VitePress文档
   - 通过 `site_service` 获取CMS数据
   - 渲染到VitePress页面中

## 目录结构

```
site/
├── front_system/          # CMS管理后台前端
│   ├── src/
│   │   ├── views/
│   │   │   ├── system/    # 系统管理页面
│   │   │   └── cms/       # CMS内容管理页面
│   │   └── ...
│   └── package.json
├── front_service/         # VitePress网站前端
│   ├── .vitepress/       # VitePress配置
│   ├── guide/            # 文档页面
│   ├── about/            # 关于页面
│   └── package.json
├── site_cms/             # CMS后台管理后端
│   └── src/main/java/
│       └── com/zyfan/
│           ├── system/   # 系统管理
│           └── cms/      # CMS内容管理（待实现）
└── site_service/         # 网站后端服务
    └── src/main/java/
```

## 下一步开发建议

1. **完善CMS内容管理**
   - 在 `site_cms` 后端实现文章、分类等实体和API
   - 在 `front_system` 中完善文章管理页面，对接后端API
   - 添加分类管理、标签管理等页面

2. **网站数据对接**
   - 在 `site_service` 中实现从CMS获取数据的接口
   - 在 `front_service` 中通过API获取数据并渲染

3. **功能扩展**
   - 添加富文本编辑器（如TinyMCE、Quill）
   - 添加文件上传功能
   - 添加SEO优化功能
