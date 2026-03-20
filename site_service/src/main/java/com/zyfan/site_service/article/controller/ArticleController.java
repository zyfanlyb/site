package com.zyfan.site_service.article.controller;

import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;
import com.zyfan.vo.CmsTypeVo;
import com.zyfan.site_service.article.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 获取文章列表（已发布的）
     */
    @GetMapping("/articles")
    public ResponseVo<List<CmsArticleVo>> getArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<Long> typeIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.getArticles(categoryId, categoryName, typeIds, keyword, pageNum, pageSize);
    }

    /**
     * VitePress 站点端路由：与前端 /api/blogs/* 保持一致
     */
    @GetMapping("/blogs/articles")
    public ResponseVo<List<CmsArticleVo>> getBlogArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<Long> typeIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.getArticles(categoryId, categoryName, typeIds, keyword, pageNum, pageSize);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public ResponseVo<CmsArticleVo> getArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    @GetMapping("/blogs/article/{id}")
    public ResponseVo<CmsArticleVo> getBlogArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public ResponseVo<List<CmsCategoryVo>> getCategories() {
        return articleService.getCategories();
    }

    @GetMapping("/blogs/categories")
    public ResponseVo<List<CmsCategoryVo>> getBlogCategories() {
        return articleService.getCategories();
    }

    /**
     * 根据分类ID获取类型列表（分类下的子类型）
     */
    @GetMapping("/types")
    public ResponseVo<List<CmsTypeVo>> getTypes(@RequestParam(required = false) Long categoryId) {
        return articleService.getTypes(categoryId);
    }

    @GetMapping("/blogs/types")
    public ResponseVo<List<CmsTypeVo>> getBlogTypes(@RequestParam(required = false) Long categoryId) {
        return articleService.getTypes(categoryId);
    }

    /**
     * 根据分类ID获取文章列表
     */
    @GetMapping("/category/{categoryId}/articles")
    public ResponseVo<List<CmsArticleVo>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.getArticlesByCategory(categoryId, pageNum, pageSize);
    }

    @GetMapping("/blogs/category/{categoryId}/articles")
    public ResponseVo<List<CmsArticleVo>> getBlogArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.getArticlesByCategory(categoryId, pageNum, pageSize);
    }

}
