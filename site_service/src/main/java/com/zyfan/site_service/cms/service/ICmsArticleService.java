package com.zyfan.site_service.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.site_service.cms.entity.CmsArticle;

import java.util.List;

public interface ICmsArticleService extends IService<CmsArticle> {

    IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo);

    List<CmsArticle> list(RequestVo<CmsArticle> requestVo);

    void insert(RequestVo<CmsArticle> requestVo);

    void update(RequestVo<CmsArticle> requestVo);

    CmsArticle info(Long id);

    void remove(Long id);

}
