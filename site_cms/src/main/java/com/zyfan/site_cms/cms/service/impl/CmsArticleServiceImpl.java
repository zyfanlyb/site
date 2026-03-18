package com.zyfan.site_cms.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.zyfan.site_cms.cms.entity.CmsArticleCover;
import com.zyfan.site_cms.cms.entity.CmsCategory;
import com.zyfan.site_cms.cms.entity.CmsType;
import com.zyfan.site_cms.cms.mapper.CmsArticleMapper;
import com.zyfan.site_cms.cms.mapper.CmsArticleCoverMapper;
import com.zyfan.site_cms.cms.mapper.CmsCategoryMapper;
import com.zyfan.site_cms.cms.mapper.CmsTypeMapper;
import com.zyfan.site_cms.cms.service.ICmsArticleService;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CmsArticleServiceImpl extends ServiceImpl<CmsArticleMapper, CmsArticle> implements ICmsArticleService {

    @Autowired
    private CmsArticleMapper cmsArticleMapper;

    @Autowired
    private CmsArticleCoverMapper cmsArticleCoverMapper;

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

    @Autowired
    private CmsTypeMapper cmsTypeMapper;

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
            if (params.getTypeId() != null) {
                wrapper.eq(CmsArticle::getTypeId, params.getTypeId());
            }
            if (params.getStatus() != null) {
                wrapper.eq(CmsArticle::getStatus, params.getStatus());
            }
        }

        wrapper.orderByDesc(CmsArticle::getSort)
                .orderByDesc(CmsArticle::getCreateTime);

        IPage<CmsArticle> result = cmsArticleMapper.selectPage(page, wrapper);

        // 填充分类名称和类型名称
        fillCategoryAndTypeName(result.getRecords());
        // 填充封面 coverImages（从 cms_article_cover 一对多查询）
        fillCoverImages(result.getRecords());

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
            if (params.getTypeId() != null) {
                wrapper.eq(CmsArticle::getTypeId, params.getTypeId());
            }
            if (params.getStatus() != null) {
                wrapper.eq(CmsArticle::getStatus, params.getStatus());
            }
        }

        wrapper.orderByDesc(CmsArticle::getSort)
                .orderByDesc(CmsArticle::getCreateTime);

        List<CmsArticle> result = cmsArticleMapper.selectList(wrapper);

        // 填充分类名称和类型名称
        fillCategoryAndTypeName(result);
        // 填充封面 coverImages（从 cms_article_cover 一对多查询）
        fillCoverImages(result);

        return result;
    }

    private void fillCoverImages(List<CmsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        List<Long> articleIds = articles.stream()
                .map(CmsArticle::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return;
        }

        List<CmsArticleCover> coverRecords = cmsArticleCoverMapper.selectList(
                Wrappers.lambdaQuery(CmsArticleCover.class)
                        .in(CmsArticleCover::getArticleId, articleIds)
                        .orderByDesc(CmsArticleCover::getSort)
                        .orderByDesc(CmsArticleCover::getCreateTime)
        );

        Map<Long, List<String>> coverImagesByArticleId = new HashMap<>();
        for (CmsArticleCover record : coverRecords) {
            if (record == null || record.getArticleId() == null) continue;
            coverImagesByArticleId
                    .computeIfAbsent(record.getArticleId(), k -> new java.util.ArrayList<>())
                    .add(record.getObjectName());
        }

        for (CmsArticle article : articles) {
            if (article == null || article.getId() == null) continue;
            article.setCoverImages(coverImagesByArticleId.getOrDefault(article.getId(), List.of()));
        }
    }

    private void replaceCovers(Long articleId, List<String> coverImages) {
        if (articleId == null) return;

        // 先删后插（简单可靠）
        cmsArticleCoverMapper.delete(
                Wrappers.lambdaQuery(CmsArticleCover.class).eq(CmsArticleCover::getArticleId, articleId)
        );

        if (coverImages == null || coverImages.isEmpty()) {
            return;
        }

        int sort = 0;
        for (String objectName : coverImages) {
            if (objectName == null) continue;
            String trimmed = objectName.trim();
            if (trimmed.isEmpty()) continue;

            CmsArticleCover record = new CmsArticleCover();
            record.setArticleId(articleId);
            record.setObjectName(trimmed);
            record.setSort(sort++);
            record.setCreateTime(new Date());
            cmsArticleCoverMapper.insert(record);
        }
    }

    /**
     * 填充分类名称和类型名称
     */
    private void fillCategoryAndTypeName(List<CmsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 获取所有分类ID和类型ID
        List<Long> categoryIds = articles.stream()
                .map(CmsArticle::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<Long> typeIds = articles.stream()
                .map(CmsArticle::getTypeId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Map.of() : cmsCategoryMapper.selectList(
                Wrappers.lambdaQuery(CmsCategory.class).in(CmsCategory::getId, categoryIds)
        ).stream().collect(Collectors.toMap(CmsCategory::getId, CmsCategory::getName, (v1, v2) -> v1));

        Map<Long, String> typeMap = typeIds.isEmpty() ? Map.of() : cmsTypeMapper.selectList(
                Wrappers.lambdaQuery(CmsType.class).in(CmsType::getId, typeIds)
        ).stream().collect(Collectors.toMap(CmsType::getId, CmsType::getName, (v1, v2) -> v1));

        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                article.setCategoryName(categoryMap.get(article.getCategoryId()));
            }
            if (article.getTypeId() != null) {
                article.setTypeName(typeMap.get(article.getTypeId()));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        if (article.getStatus() == null) {
            article.setStatus(0); // 默认草稿
        }
        cmsArticleMapper.insert(article);
        replaceCovers(article.getId(), article.getCoverImages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        cmsArticleMapper.updateById(article);
        replaceCovers(article.getId(), article.getCoverImages());
    }

    @Override
    public CmsArticle info(Long id) {
        CmsArticle article = cmsArticleMapper.selectById(id);
        if (article != null) {
            fillCoverImages(List.of(article));
            if (article.getCategoryId() != null) {
                CmsCategory category = cmsCategoryMapper.selectById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
            if (article.getTypeId() != null) {
                CmsType type = cmsTypeMapper.selectById(article.getTypeId());
                if (type != null) {
                    article.setTypeName(type.getName());
                }
            }
        }
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        cmsArticleCoverMapper.delete(
                Wrappers.lambdaQuery(CmsArticleCover.class).eq(CmsArticleCover::getArticleId, id)
        );
        cmsArticleMapper.deleteById(id);
    }
}
