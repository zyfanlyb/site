package com.zyfan.site_cms.cms.service;

import com.zyfan.site_cms.cms.entity.CmsType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

import java.util.List;

public interface ICmsTypeService extends IService<CmsType> {

    IPage<CmsType> pageList(RequestVo<CmsType> requestVo);

    /**
     * 根据分类ID获取启用的类型列表（不分页，文章表单下拉用）
     */
    List<CmsType> listByCategoryId(Long categoryId);

    void insert(RequestVo<CmsType> requestVo);

    void update(RequestVo<CmsType> requestVo);

    CmsType info(Long id);

    void remove(Long id);

}
