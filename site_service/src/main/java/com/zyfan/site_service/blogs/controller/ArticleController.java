package com.zyfan.site_service.blogs.controller;

import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;
import com.zyfan.site_service.blogs.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
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
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return articleService.getArticles(categoryId, categoryName, typeId, pageNum, pageSize);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public ResponseVo<CmsArticleVo> getArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public ResponseVo<List<CmsCategoryVo>> getCategories() {
        return articleService.getCategories();
    }

    /**
     * 根据分类ID获取类型列表（分类下的子类型）
     */
    @GetMapping("/types")
    public ResponseVo<List<Object>> getTypes(@RequestParam(required = false) Long categoryId) {
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

}
