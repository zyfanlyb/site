package com.zyfan.cms.service;

import com.zyfan.cms.entity.CmsCategory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

import java.util.List;

public interface ICmsCategoryService extends IService<CmsCategory> {

    IPage<CmsCategory> getCategoryTree(RequestVo<CmsCategory> requestVo);

    List<CmsCategory> getAllCategories();

    void insert(RequestVo<CmsCategory> requestVo);

    void update(RequestVo<CmsCategory> requestVo);

    CmsCategory info(Long id);

    void remove(Long id);

}
