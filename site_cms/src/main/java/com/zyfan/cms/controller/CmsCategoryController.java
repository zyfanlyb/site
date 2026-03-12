package com.zyfan.cms.controller;

import com.zyfan.anno.Permission;
import com.zyfan.cms.entity.CmsCategory;
import com.zyfan.cms.service.ICmsCategoryService;
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

    @Permission("cms:category:page")
    @PostMapping("/page")
    public ResponseVo<List<CmsCategory>> page(@RequestBody RequestVo<CmsCategory> requestVo) {
        return ResponseVo.success(cmsCategoryService.getCategoryTree(requestVo));
    }

    @Permission("cms:category:list")
    @PostMapping("/list")
    public ResponseVo<List<CmsCategory>> list() {
        return ResponseVo.success(cmsCategoryService.getAllCategories());
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
