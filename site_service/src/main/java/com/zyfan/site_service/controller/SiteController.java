package com.zyfan.site_service.controller;

import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.site_service.cms.entity.CmsArticle;
import com.zyfan.site_service.cms.entity.CmsCategory;
import com.zyfan.site_service.cms.service.ICmsArticleService;
import com.zyfan.site_service.cms.service.ICmsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网站前端API接口
 * 提供给VitePress等前端网站使用
 */
@RestController
@RequestMapping("/api")
public class SiteController {

    @Autowired
    private ICmsArticleService cmsArticleService;

    @Autowired
    private ICmsCategoryService cmsCategoryService;

    /**
     * 获取文章列表（已发布的）
     */
    @GetMapping("/articles")
    public ResponseVo<List<CmsArticle>> getArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        RequestVo<CmsArticle> requestVo = new RequestVo<>();
        requestVo.setPageNum((long) pageNum);
        requestVo.setPageSize((long) pageSize);
        
        CmsArticle params = new CmsArticle();
        params.setStatus(1); // 只查询已发布的
        if (categoryId != null) {
            params.setCategoryId(categoryId);
        }
        requestVo.setData(params);
        
        return ResponseVo.success(cmsArticleService.pageList(requestVo));
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public ResponseVo<CmsArticle> getArticle(@PathVariable Long id) {
        CmsArticle article = cmsArticleService.info(id);
        // 只有已发布的文章才能被访问
        if (article != null && article.getStatus() == 1) {
            return ResponseVo.success(article);
        }
        return ResponseVo.failed("文章不存在或未发布");
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public ResponseVo<List<CmsCategory>> getCategories() {
        return ResponseVo.success(cmsCategoryService.getAllCategories());
    }

    /**
     * 根据分类ID获取文章列表
     */
    @GetMapping("/category/{categoryId}/articles")
    public ResponseVo<List<CmsArticle>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        RequestVo<CmsArticle> requestVo = new RequestVo<>();
        requestVo.setPageNum((long) pageNum);
        requestVo.setPageSize((long) pageSize);
        
        CmsArticle params = new CmsArticle();
        params.setStatus(1); // 只查询已发布的
        params.setCategoryId(categoryId);
        requestVo.setData(params);
        
        return ResponseVo.success(cmsArticleService.pageList(requestVo));
    }

}
