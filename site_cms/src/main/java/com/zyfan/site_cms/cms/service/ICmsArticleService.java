package com.zyfan.site_cms.cms.service;

import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zyfan.pojo.web.RequestVo;

import java.util.List;

/**
 * 文章服务（ES 主存储版本）。
 *
 * <p>注意：不再继承 MyBatis-Plus 的 IService，避免引入与“废弃文章表”相冲突的数据库通用能力。</p>
 */
public interface ICmsArticleService {

    IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo);

    List<CmsArticle> list(RequestVo<CmsArticle> requestVo);

    void insert(RequestVo<CmsArticle> requestVo);

    void update(RequestVo<CmsArticle> requestVo);

    CmsArticle info(Long id);

    void remove(Long id);

}
