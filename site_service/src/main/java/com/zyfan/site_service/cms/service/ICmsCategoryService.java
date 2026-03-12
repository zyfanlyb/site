package com.zyfan.site_service.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.site_service.cms.entity.CmsCategory;

import java.util.List;

public interface ICmsCategoryService extends IService<CmsCategory> {

    IPage<CmsCategory> pageList(RequestVo<CmsCategory> requestVo);

    List<CmsCategory> getAllCategories();

    void insert(RequestVo<CmsCategory> requestVo);

    void update(RequestVo<CmsCategory> requestVo);

    CmsCategory info(Long id);

    void remove(Long id);

}
