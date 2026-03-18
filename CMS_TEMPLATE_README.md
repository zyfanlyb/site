# CMS内容管理模板使用说明

## 概述

这是一个完整的CMS内容管理模板，包含后端API和前端管理页面。

## 已实现的功能

### 后端 (site_cms)

1. **实体类**
   - `CmsArticle` - 文章实体
   - `CmsCategory` - 分类实体

2. **Mapper接口**
   - `CmsArticleMapper` - 文章数据访问
   - `CmsCategoryMapper` - 分类数据访问

3. **Service层**
   - `ICmsArticleService` / `CmsArticleServiceImpl` - 文章业务逻辑
   - `ICmsCategoryService` / `CmsCategoryServiceImpl` - 分类业务逻辑

4. **Controller层**
   - `CmsArticleController` - 文章管理API (`/cms/article/*`)
   - `CmsCategoryController` - 分类管理API (`/cms/category/*`)

### 前端 (front_system)

1. **管理页面**
   - `ArticleList.vue` - 文章列表页面
   - `ArticleForm.vue` - 文章表单页面（新增/编辑）

### 网站服务 (site_service)

1. **前端API接口**
   - `SiteController` - 提供给VitePress等前端网站使用的API
     - `GET /blogs/articles` - 获取文章列表
     - `GET /blogs/article/{id}` - 获取文章详情
     - `GET /blogs/categories` - 获取分类列表
     - `GET /blogs/category/{categoryId}/articles` - 根据分类获取文章

## 数据库初始化

执行 `site_cms/src/main/resources/sql/cms_tables.sql` 创建数据库表。

## API接口说明

### CMS管理后台API

#### 文章管理
- `POST /cms/article/page` - 分页查询文章列表
- `POST /cms/article/list` - 查询文章列表（不分页）
- `POST /cms/article/insert` - 新增文章
- `POST /cms/article/update` - 更新文章
- `POST /cms/article/info/{id}` - 获取文章详情
- `POST /cms/article/remove/{id}` - 删除文章

#### 分类管理
- `POST /cms/category/list` - 启用分类列表（不分页，文章表单下拉用）
- `POST /cms/category/page` - 分页查询分类（配置弹窗用，支持按名称筛选）
- `POST /cms/category/insert` - 新增分类
- `POST /cms/category/update` - 更新分类
- `POST /cms/category/info/{id}` - 获取分类详情
- `POST /cms/category/remove/{id}` - 删除分类

### 网站前端API

- `GET /blogs/articles?categoryId=1&pageNum=1&pageSize=10` - 获取已发布的文章列表
- `GET /blogs/article/{id}` - 获取文章详情
- `GET /blogs/categories` - 获取所有分类
- `GET /blogs/category/{categoryId}/articles?pageNum=1&pageSize=10` - 根据分类获取文章

## 权限配置

需要在菜单管理中配置以下权限：

- `cms:article:page` - 文章列表查询
- `cms:article:list` - 文章列表查询（不分页）
- `cms:article:add` - 新增文章
- `cms:article:edit` - 编辑文章
- `cms:article:view` - 查看文章
- `cms:article:delete` - 删除文章
- `cms:category:list` - 分类配置（文章分类按钮）
- `cms:category:list` - 分类列表查询
- `cms:category:add` - 新增分类
- `cms:category:edit` - 编辑分类
- `cms:category:view` - 查看分类
- `cms:category:delete` - 删除分类

## 前端页面路由配置

在菜单管理中配置文章管理页面：

- 路径：`cms/article`
- 组件：`cms/ArticleList`
- 权限：`cms:article:page`

## 使用示例

### 1. 在VitePress中调用API

```javascript
// 获取文章列表
const response = await fetch('http://your-api-domain/api/articles?pageNum=1&pageSize=10')
const data = await response.json()

// 获取文章详情
const article = await fetch('http://your-api-domain/api/article/1')
const articleData = await article.json()
```

### 2. 扩展功能

可以基于此模板扩展：
- 标签管理
- 评论功能
- 富文本编辑器集成
- 文件上传功能
- SEO优化
- 文章搜索

## 注意事项

1. 确保数据库表已创建
2. 确保权限已配置
3. 确保菜单路由已配置
4. `site_service` 需要能够访问 `site_cms` 的服务（通过Nacos服务发现或直接依赖）

## 下一步开发建议

1. 添加分类管理页面（参考ArticleList和ArticleForm）
2. 集成富文本编辑器（如TinyMCE、Quill）
3. 添加文件上传功能
4. 在VitePress中集成文章展示页面
5. 添加文章搜索功能
6. 添加标签功能
