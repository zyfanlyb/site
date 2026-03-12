package com.zyfan.site_service.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.site_service.cms.entity.CmsArticle;
import com.zyfan.site_service.cms.entity.CmsCategory;
import com.zyfan.site_service.cms.mapper.CmsArticleMapper;
import com.zyfan.site_service.cms.mapper.CmsCategoryMapper;
import com.zyfan.site_service.cms.service.ICmsArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CmsArticleServiceImpl extends ServiceImpl<CmsArticleMapper, CmsArticle> implements ICmsArticleService {

    @Autowired
    private CmsArticleMapper cmsArticleMapper;

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

    @Override
    public IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo) {
        CmsArticle params = requestVo.getData();
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;

        Page<CmsArticle> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CmsArticle> wrapper = Wrappers.lambdaQuery(CmsArticle.class);

        if (params != null) {
            if (params.getTitle() != null && !params.getTitle().isEmpty()) {
                wrapper.like(CmsArticle::getTitle, params.getTitle());
            }
            if (params.getCategoryId() != null) {
                wrapper.eq(CmsArticle::getCategoryId, params.getCategoryId());
            }
            if (params.getStatus() != null) {
                wrapper.eq(CmsArticle::getStatus, params.getStatus());
            }
        }

        wrapper.orderByDesc(CmsArticle::getSort)
                .orderByDesc(CmsArticle::getCreateTime);

        IPage<CmsArticle> result = cmsArticleMapper.selectPage(page, wrapper);

        // 填充分类名称
        fillCategoryName(result.getRecords());

        return result;
    }

    @Override
    public List<CmsArticle> list(RequestVo<CmsArticle> requestVo) {
        CmsArticle params = requestVo.getData();

        LambdaQueryWrapper<CmsArticle> wrapper = Wrappers.lambdaQuery(CmsArticle.class);

        if (params != null) {
            if (params.getTitle() != null && !params.getTitle().isEmpty()) {
                wrapper.like(CmsArticle::getTitle, params.getTitle());
            }
            if (params.getCategoryId() != null) {
                wrapper.eq(CmsArticle::getCategoryId, params.getCategoryId());
            }
            if (params.getStatus() != null) {
                wrapper.eq(CmsArticle::getStatus, params.getStatus());
            }
        }

        wrapper.orderByDesc(CmsArticle::getSort)
                .orderByDesc(CmsArticle::getCreateTime);

        List<CmsArticle> result = cmsArticleMapper.selectList(wrapper);

        // 填充分类名称
        fillCategoryName(result);

        return result;
    }

    /**
     * 填充分类名称
     */
    private void fillCategoryName(List<CmsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 获取所有分类ID
        List<Long> categoryIds = articles.stream()
                .map(CmsArticle::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return;
        }

        // 批量查询分类
        List<CmsCategory> categories = cmsCategoryMapper.selectList(
                Wrappers.lambdaQuery(CmsCategory.class)
                        .in(CmsCategory::getId, categoryIds)
        );

        // 构建分类ID到名称的映射
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CmsCategory::getId, CmsCategory::getName, (v1, v2) -> v1));

        // 填充分类名称
        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                article.setCategoryName(categoryMap.get(article.getCategoryId()));
            }
        });
    }

    @Override
    public void insert(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        if (article.getCreateTime() == null) {
            article.setCreateTime(new Date());
        }
        if (article.getUpdateTime() == null) {
            article.setUpdateTime(new Date());
        }
        if (article.getStatus() == null) {
            article.setStatus(0); // 默认草稿
        }
        cmsArticleMapper.insert(article);
    }

    @Override
    public void update(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        article.setUpdateTime(new Date());
        cmsArticleMapper.updateById(article);
    }

    @Override
    public CmsArticle info(Long id) {
        CmsArticle article = cmsArticleMapper.selectById(id);
        if (article != null && article.getCategoryId() != null) {
            CmsCategory category = cmsCategoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                article.setCategoryName(category.getName());
            }
        }
        return article;
    }

    @Override
    public void remove(Long id) {
        cmsArticleMapper.deleteById(id);
    }
}
