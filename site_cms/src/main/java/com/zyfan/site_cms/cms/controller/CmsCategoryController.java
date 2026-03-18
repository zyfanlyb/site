package com.zyfan.site_cms.cms.controller;

import com.zyfan.site_cms.anno.NoAuth;
import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.cms.entity.CmsCategory;
import com.zyfan.site_cms.cms.service.ICmsCategoryService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CMS分类管理
 */
@RestController
@RequestMapping("/cms/category")
public class CmsCategoryController {

    @Autowired
    private ICmsCategoryService cmsCategoryService;

    /**
     * 启用分类列表（不分页，文章表单下拉用）
     * 使用 cms:article:page，避免新增文章时因无分类权限导致 403
     */
    @NoAuth
    @Permission("cms:article:page")
    @PostMapping("/list")
    public ResponseVo<List<CmsCategory>> list() {
        return ResponseVo.success(cmsCategoryService.getAllCategories());
    }

    /**
     * 分页查询分类（分类配置弹窗用，支持按名称筛选）
     */
    @Permission("cms:category:page")
    @PostMapping("/page")
    public ResponseVo<List<CmsCategory>> page(@RequestBody RequestVo<CmsCategory> requestVo) {
        return ResponseVo.success(cmsCategoryService.pageList(requestVo));
    }

    @Permission("cms:category:add")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<CmsCategory> requestVo) {
        cmsCategoryService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:category:edit")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<CmsCategory> requestVo) {
        cmsCategoryService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:category:view")
    @PostMapping("/info/{id}")
    public ResponseVo<CmsCategory> info(@PathVariable("id") Long id) {
        return ResponseVo.success(cmsCategoryService.info(id));
    }

    @Permission("cms:category:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        cmsCategoryService.remove(id);
        return ResponseVo.success();
    }

}
