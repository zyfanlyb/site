package com.zyfan.site_service.article.service;

import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;
import com.zyfan.vo.CmsTypeVo;

import java.util.List;

public interface ArticleService {

    ResponseVo<List<CmsArticleVo>> getArticles(Long categoryId,
                                                    String categoryName,
                                                    List<Long> typeIds,
                                                    String keyword,
                                                    Integer pageNum,
                                                    Integer pageSize);

    ResponseVo<CmsArticleVo> getArticle(Long id);

    ResponseVo<List<CmsCategoryVo>> getCategories();

    ResponseVo<List<CmsTypeVo>> getTypes(Long categoryId);

    ResponseVo<List<CmsArticleVo>> getArticlesByCategory(Long categoryId, Integer pageNum, Integer pageSize);
}

