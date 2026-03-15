package com.zyfan.site_cms.cms.controller;

import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.cms.entity.CmsType;
import com.zyfan.site_cms.cms.service.ICmsTypeService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CMS类型管理（分类下的子类型）
 */
@RestController
@RequestMapping("/cms/type")
public class CmsTypeController {

    @Autowired
    private ICmsTypeService cmsTypeService;

    /**
     * 根据分类ID获取启用的类型列表（不分页，文章表单下拉用）
     */
    @Permission("cms:article:page")
    @PostMapping("/list")
    public ResponseVo<List<CmsType>> list(@RequestBody(required = false) RequestVo<CmsType> requestVo) {
        Long categoryId = requestVo != null && requestVo.getData() != null ? requestVo.getData().getCategoryId() : null;
        return ResponseVo.success(cmsTypeService.listByCategoryId(categoryId));
    }

    /**
     * 分页查询类型（类型配置弹窗用）
     */
    @Permission("cms:type:page")
    @PostMapping("/page")
    public ResponseVo page(@RequestBody RequestVo<CmsType> requestVo) {
        return ResponseVo.success(cmsTypeService.pageList(requestVo));
    }

    @Permission("cms:type:add")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<CmsType> requestVo) {
        cmsTypeService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:type:edit")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<CmsType> requestVo) {
        cmsTypeService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("cms:type:view")
    @PostMapping("/info/{id}")
    public ResponseVo<CmsType> info(@PathVariable("id") Long id) {
        return ResponseVo.success(cmsTypeService.info(id));
    }

    @Permission("cms:type:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        cmsTypeService.remove(id);
        return ResponseVo.success();
    }

}
