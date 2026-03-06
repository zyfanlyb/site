package com.zyfan.cms.controller;

import com.zyfan.anno.Permission;
import com.zyfan.cms.entity.CmsPage;
import com.zyfan.cms.entity.CmsPageBlock;
import com.zyfan.cms.entity.CmsPost;
import com.zyfan.cms.entity.CmsSiteSetting;
import com.zyfan.cms.enums.CmsStatus;
import com.zyfan.cms.service.ICmsPageBlockService;
import com.zyfan.cms.service.ICmsPageService;
import com.zyfan.cms.service.ICmsPostService;
import com.zyfan.cms.service.ICmsSiteSettingService;
import com.zyfan.cms.vo.CmsPageVo;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CMS 后台管理接口控制器
 * 提供站点设置、页面管理、内容管理的后台操作接口
 * 所有接口都需要登录认证，并通过 @Permission 注解进行权限控制
 * 
 * @author system
 */
@RestController
@RequestMapping("/cms")
public class CmsAdminController {

    @Autowired
    private ICmsSiteSettingService siteSettingService;

    @Autowired
    private ICmsPageService pageService;

    @Autowired
    private ICmsPageBlockService pageBlockService;

    @Autowired
    private ICmsPostService postService;

    /**
     * 获取站点设置（GET方式）
     * 用于后台管理页面加载站点配置信息
     * 
     * @return 站点设置对象，如果不存在则自动创建并返回默认值
     */
    @Permission("cms:site:view")
    @GetMapping("/siteSetting")
    public ResponseVo<CmsSiteSetting> siteSetting() {
        return ResponseVo.success(siteSettingService.getOrInit());
    }

    /**
     * 获取站点设置（POST方式）
     * 用于后台管理页面加载站点配置信息，兼容前端统一使用 POST 请求
     * 
     * @return 站点设置对象，如果不存在则自动创建并返回默认值
     */
    @Permission("cms:site:view")
    @PostMapping("/siteSetting")
    public ResponseVo<CmsSiteSetting> siteSettingPost() {
        return ResponseVo.success(siteSettingService.getOrInit());
    }

    /**
     * 更新站点设置
     * 保存全站配置信息，包括站点标题、logo、导航菜单、页脚、主题色等
     * 
     * @param requestVo 请求对象，data 字段为 JSON 格式的配置字符串
     * @return 更新是否成功
     */
    @Permission("cms:site:edit")
    @PostMapping("/siteSetting/update")
    public ResponseVo<Boolean> updateSiteSetting(@RequestBody RequestVo<String> requestVo) {
        siteSettingService.updateSettingsJson(requestVo.getData());
        return ResponseVo.success(true);
    }

    /**
     * 获取页面草稿内容（GET方式）
     * 用于后台编辑页面时加载草稿版本的数据（包括页面信息和所有模块块）
     * 
     * @param path 页面路径，例如 "/" 表示首页，"/about" 表示关于页。如果为空则默认为 "/"
     * @return 页面视图对象，包含页面基本信息和草稿状态的模块块列表
     */
    @Permission("cms:page:view")
    @GetMapping("/page")
    public ResponseVo<CmsPageVo> pageDraft(@RequestParam(value = "path", required = false) String path) {
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = pageService.getOrInitByPath(path);
        CmsPageVo vo = new CmsPageVo();
        vo.setPage(page);
        vo.setBlocks(pageBlockService.listBlocks(page.getId(), CmsStatus.DRAFT.name()));
        return ResponseVo.success(vo);
    }

    /**
     * 获取页面草稿内容（POST方式）
     * 用于后台编辑页面时加载草稿版本的数据，兼容前端统一使用 POST 请求
     * 
     * @param requestVo 请求对象，data 字段为页面路径字符串
     * @return 页面视图对象，包含页面基本信息和草稿状态的模块块列表
     */
    @Permission("cms:page:view")
    @PostMapping("/page")
    public ResponseVo<CmsPageVo> pageDraftPost(@RequestBody(required = false) RequestVo<String> requestVo) {
        String path = requestVo != null ? requestVo.getData() : null;
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = pageService.getOrInitByPath(path);
        CmsPageVo vo = new CmsPageVo();
        vo.setPage(page);
        vo.setBlocks(pageBlockService.listBlocks(page.getId(), CmsStatus.DRAFT.name()));
        return ResponseVo.success(vo);
    }

    /**
     * 保存页面草稿
     * 保存页面的编辑内容为草稿状态，不会影响前台已发布的内容
     * 支持更新页面基本信息（标题、SEO信息）和所有模块块的内容
     * 
     * @param requestVo 请求对象，data 字段为页面视图对象，包含页面信息和模块块列表
     * @return 保存是否成功
     */
    @Permission("cms:page:edit")
    @PostMapping("/page/saveDraft")
    public ResponseVo<Boolean> saveDraft(@RequestBody RequestVo<CmsPageVo> requestVo) {
        CmsPageVo vo = requestVo.getData();
        if (vo == null || vo.getPage() == null || StringUtils.isBlank(vo.getPage().getPath())) {
            return ResponseVo.success(false);
        }
        CmsPage page = pageService.getOrInitByPath(vo.getPage().getPath());
        // update page meta (keep status as-is)
        if (StringUtils.isNotBlank(vo.getPage().getTitle())) {
            page.setTitle(vo.getPage().getTitle());
        } else {
            page.setTitle(vo.getPage().getTitle());
        }
        page.setSeoTitle(vo.getPage().getSeoTitle());
        page.setSeoDescription(vo.getPage().getSeoDescription());
        pageService.updateById(page);

        List<CmsPageBlock> blocks = vo.getBlocks();
        pageBlockService.replaceDraftBlocks(page.getId(), blocks);
        return ResponseVo.success(true);
    }

    /**
     * 发布页面
     * 将页面的草稿内容发布到前台，前台将显示最新发布的内容
     * 发布操作会将页面状态和所有模块块状态从 DRAFT 改为 PUBLISHED
     * 
     * @param requestVo 请求对象，data 字段为页面路径字符串
     * @return 发布是否成功
     */
    @Permission("cms:page:publish")
    @PostMapping("/page/publish")
    public ResponseVo<Boolean> publish(@RequestBody RequestVo<String> requestVo) {
        String path = requestVo.getData();
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = pageService.publishByPath(path);
        pageBlockService.publishBlocks(page.getId());
        return ResponseVo.success(true);
    }

    /**
     * 分页查询文章/项目列表
     * 用于后台管理页面展示所有内容（包括草稿和已发布）
     * 支持按类型、状态、标题等条件筛选
     * 
     * @param requestVo 请求对象，包含分页参数和查询条件
     * @return 分页结果，包含内容列表和分页信息
     */
    @Permission("cms:post:page")
    @PostMapping("/post/page")
    public ResponseVo<?> postPage(@RequestBody RequestVo<CmsPost> requestVo) {
        return ResponseVo.success(postService.pageList(requestVo));
    }

    /**
     * 获取文章/项目详情（GET方式）
     * 用于后台编辑内容时加载详细信息
     * 
     * @param id 内容ID
     * @return 内容对象
     */
    @Permission("cms:post:view")
    @GetMapping("/post/{id}")
    public ResponseVo<CmsPost> postInfo(@PathVariable("id") Long id) {
        return ResponseVo.success(postService.getById(id));
    }

    /**
     * 获取文章/项目详情（POST方式）
     * 用于后台编辑内容时加载详细信息，兼容前端统一使用 POST 请求
     * 
     * @param id 内容ID
     * @return 内容对象
     */
    @Permission("cms:post:view")
    @PostMapping("/post/{id}")
    public ResponseVo<CmsPost> postInfoPost(@PathVariable("id") Long id) {
        return ResponseVo.success(postService.getById(id));
    }

    /**
     * 保存文章/项目草稿
     * 保存内容的编辑为草稿状态，不会影响前台已发布的内容
     * 如果 slug 已存在且不是当前记录，会自动生成新的 slug
     * 
     * @param requestVo 请求对象，data 字段为内容对象
     * @return 保存后的内容对象（包含自动生成的ID和slug）
     */
    @Permission("cms:post:edit")
    @PostMapping("/post/saveDraft")
    public ResponseVo<CmsPost> savePostDraft(@RequestBody RequestVo<CmsPost> requestVo) {
        return ResponseVo.success(postService.saveDraft(requestVo.getData()));
    }

    /**
     * 发布文章/项目
     * 将内容的草稿状态发布到前台，前台将显示最新发布的内容
     * 发布操作会将内容状态从 DRAFT 改为 PUBLISHED，并设置 published_at 为当前时间
     * 
     * @param id 内容ID
     * @return 发布后的内容对象
     */
    @Permission("cms:post:publish")
    @PostMapping("/post/publish/{id}")
    public ResponseVo<CmsPost> publishPost(@PathVariable("id") Long id) {
        return ResponseVo.success(postService.publish(id));
    }
}

