package com.zyfan.site_cms.cms.service;

import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

import java.util.List;

public interface ICmsArticleService extends IService<CmsArticle> {

    IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo);

    List<CmsArticle> list(RequestVo<CmsArticle> requestVo);

    void insert(RequestVo<CmsArticle> requestVo);

    void update(RequestVo<CmsArticle> requestVo);

    CmsArticle info(Long id);

    void remove(Long id);

}
