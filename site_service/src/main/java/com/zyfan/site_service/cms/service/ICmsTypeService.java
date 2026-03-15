package com.zyfan.site_service.cms.service;

import com.zyfan.site_service.cms.entity.CmsType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICmsTypeService extends IService<CmsType> {

    /**
     * 根据分类ID获取启用的类型列表（不分页）
     */
    List<CmsType> listByCategoryId(Long categoryId);

}
