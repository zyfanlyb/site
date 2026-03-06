package com.zyfan.cms.controller;

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
import com.zyfan.pojo.web.ResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CMS 公开访问接口控制器
 * 提供前台网站读取已发布内容的接口，所有接口都无需登录认证
 * 注意：路径必须放在 /noAuth/** 下，以绕过 AuthInterceptor 的拦截
 * 
 * @author system
 */
@RestController
@RequestMapping("/noAuth/cms")
public class CmsPublicController {

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
     * 用于前台网站加载全站配置信息，如导航菜单、页脚、主题色等
     * 
     * @return 站点设置对象，如果不存在则自动创建并返回默认值
     */
    @GetMapping("/siteSetting")
    public ResponseVo<CmsSiteSetting> siteSetting() {
        return ResponseVo.success(siteSettingService.getOrInit());
    }

    /**
     * 获取站点设置（POST方式）
     * 用于前台网站加载全站配置信息，兼容前端统一使用 POST 请求
     * 
     * @return 站点设置对象，如果不存在则自动创建并返回默认值
     */
    @PostMapping("/siteSetting")
    public ResponseVo<CmsSiteSetting> siteSettingPost() {
        return ResponseVo.success(siteSettingService.getOrInit());
    }

    /**
     * 获取已发布的页面内容（GET方式）
     * 用于前台网站根据路径加载页面信息和已发布的模块块
     * 只返回状态为 PUBLISHED 的页面和模块块，草稿内容不会返回
     * 
     * @param path 页面路径，例如 "/" 表示首页，"/about" 表示关于页。如果为空则默认为 "/"
     * @return 页面视图对象，包含页面基本信息和已发布状态的模块块列表（按 sort 排序）
     */
    @GetMapping("/page")
    public ResponseVo<CmsPageVo> page(@RequestParam(value = "path", required = false) String path) {
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = pageService.getOrInitByPath(path);
        CmsPageVo vo = new CmsPageVo();
        vo.setPage(page);
        List<CmsPageBlock> blocks = pageBlockService.listBlocks(page.getId(), CmsStatus.PUBLISHED.name());
        vo.setBlocks(blocks);
        return ResponseVo.success(vo);
    }

    /**
     * 获取已发布的页面内容（POST方式）
     * 用于前台网站根据路径加载页面信息和已发布的模块块，兼容前端统一使用 POST 请求
     * 只返回状态为 PUBLISHED 的页面和模块块，草稿内容不会返回
     * 
     * @param requestVo 请求对象，data 字段为页面路径字符串
     * @return 页面视图对象，包含页面基本信息和已发布状态的模块块列表（按 sort 排序）
     */
    @PostMapping("/page")
    public ResponseVo<CmsPageVo> pagePost(@RequestBody(required = false) com.zyfan.pojo.web.RequestVo<String> requestVo) {
        String path = requestVo != null ? requestVo.getData() : null;
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = pageService.getOrInitByPath(path);
        CmsPageVo vo = new CmsPageVo();
        vo.setPage(page);
        List<CmsPageBlock> blocks = pageBlockService.listBlocks(page.getId(), CmsStatus.PUBLISHED.name());
        vo.setBlocks(blocks);
        return ResponseVo.success(vo);
    }

    /**
     * 根据 slug 获取已发布的文章/项目详情
     * 用于前台网站展示文章或项目的详细内容
     * 只返回状态为 PUBLISHED 的内容，草稿内容不会返回
     * 
     * @param slug 内容的 URL 友好标识符，例如 "my-first-article"
     * @return 内容对象，包含标题、摘要、封面、Markdown 内容等信息
     */
    @GetMapping("/post/{slug}")
    public ResponseVo<CmsPost> post(@PathVariable("slug") String slug) {
        return ResponseVo.success(postService.getBySlug(slug, true));
    }
}

