package com.zyfan.site_service.blogs.service;

import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;

import java.util.List;

public interface ArticleService {

    ResponseVo<List<CmsArticleVo>> getArticles(Long categoryId,
                                                    String categoryName,
                                                    Long typeId,
                                                    String keyword,
                                                    Integer pageNum,
                                                    Integer pageSize);

    ResponseVo<CmsArticleVo> getArticle(Long id);

    ResponseVo<List<CmsCategoryVo>> getCategories();

    ResponseVo<List<Object>> getTypes(Long categoryId);

    ResponseVo<List<CmsArticleVo>> getArticlesByCategory(Long categoryId, Integer pageNum, Integer pageSize);
}

