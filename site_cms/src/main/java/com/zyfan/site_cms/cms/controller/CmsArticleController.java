package com.zyfan.site_cms.cms.controller;

import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.zyfan.site_cms.cms.service.ICmsArticleService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CMS文章管理
 */
@RestController
@RequestMapping("/cms/article")
public class CmsArticleController {

    @Autowired
    private ICmsArticleService cmsArticleService;

    @Permission("cms:article:page")
    @PostMapping("/page")
    public ResponseVo<List<CmsArticle>> page(@RequestBody RequestVo<CmsArticle> requestVo) {
        return ResponseVo.success(cmsArticleService.pageList(requestVo));
    }

    @PostMapping("/list")
    public ResponseVo<List<CmsArticle>> list(@RequestBody RequestVo<CmsArticle> requestVo) {
        return ResponseVo.success(cmsArticleService.list(requestVo));
    }

    @Permission("cms:article:add")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<CmsArticle> requestVo) {
        cmsArticleService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:article:edit")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<CmsArticle> requestVo) {
        cmsArticleService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:article:view")
    @PostMapping("/info/{id}")
    public ResponseVo<CmsArticle> info(@PathVariable("id") Long id) {
        return ResponseVo.success(cmsArticleService.info(id));
    }

    @Permission("cms:article:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        cmsArticleService.remove(id);
        return ResponseVo.success();
    }

}
