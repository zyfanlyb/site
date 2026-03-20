package com.zyfan.site_cms.cms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.zyfan.site_cms.cms.entity.CmsCategory;
import com.zyfan.site_cms.cms.entity.CmsType;
import com.zyfan.site_cms.cms.mapper.CmsCategoryMapper;
import com.zyfan.site_cms.cms.mapper.CmsTypeMapper;
import com.zyfan.site_cms.cms.service.ICmsArticleService;
import com.zyfan.site_cms.elasticsearch.document.CmsArticleEsDocument;
import com.zyfan.site_cms.elasticsearch.service.CmsArticleEsService;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CmsArticleServiceImpl implements ICmsArticleService {

    /** 与全站分页习惯一致：未传 pageSize 时默认每页 10 条 */
    private static final long DEFAULT_PAGE_SIZE = 10L;

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

    @Autowired
    private CmsTypeMapper cmsTypeMapper;

    @Autowired
    private CmsArticleEsService cmsArticleEsService;

    /**
     * 文章分页列表整体逻辑：
     * <ul>
     *   <li><b>ES 主存储</b>：文章本体字段（标题/摘要/正文/状态/排序/时间/封面/typeIds/categoryId）均从 ES 读写。</li>
     *   <li><b>有关键词</b>：ES 做全文检索（title/summary/content），按 _score 降序，二级按 updateTime 降序；返回 score 供前端透传。</li>
     *   <li><b>无关键词</b>：ES match_all，按 sort 降序，二级按 createTime 降序。</li>
     *   <li><b>名称补全</b>：分类/类型名称仍从 MySQL 的 cms_category/cms_type 主表查询后填充到返回结构中。</li>
     * </ul>
     */
    @Override
    public IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo) {
        CmsArticle params = requestVo != null ? requestVo.getData() : null;
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : DEFAULT_PAGE_SIZE;
        String keyword = params != null ? params.getKeyword() : null;

        CmsArticleEsService.EsPageDocs es = cmsArticleEsService.pageQuery(
                keyword,
                params != null ? params.getCategoryId() : null,
                params != null ? params.getStatus() : null,
                params != null ? params.getTypeIds() : null,
                pageNum,
                pageSize,
                CmsArticleEsService.MAX_ES_PAGE_SIZE
        );

        Page<CmsArticle> page = new Page<>(pageNum, pageSize, es.total());
        if (es.hits().isEmpty()) {
            page.setRecords(List.of());
            return page;
        }

        List<CmsArticle> records = es.hits().stream()
                .map(hit -> {
                    CmsArticleEsDocument doc = hit.doc();
                    CmsArticle a = toCmsArticle(doc);
                    a.setSearchScore((keyword != null && !keyword.trim().isEmpty()) ? hit.score() : null);
                    return a;
                })
                .collect(Collectors.toList());

        fillCategoryAndTypeName(records);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<CmsArticle> list(RequestVo<CmsArticle> requestVo) {
        CmsArticle params = requestVo != null ? requestVo.getData() : null;
        String keyword = params != null ? params.getKeyword() : null;

        CmsArticleEsService.EsPageDocs es = cmsArticleEsService.pageQuery(
                keyword,
                params != null ? params.getCategoryId() : null,
                params != null ? params.getStatus() : null,
                params != null ? params.getTypeIds() : null,
                1L,
                CmsArticleEsService.MAX_ES_LIST_SIZE,
                CmsArticleEsService.MAX_ES_LIST_SIZE
        );
        if (es.hits().isEmpty()) {
            return List.of();
        }

        List<CmsArticle> records = es.hits().stream()
                .map(hit -> {
                    CmsArticle a = toCmsArticle(hit.doc());
                    a.setSearchScore((keyword != null && !keyword.trim().isEmpty()) ? hit.score() : null);
                    return a;
                })
                .collect(Collectors.toList());
        fillCategoryAndTypeName(records);
        return records;
    }

    /**
     * 填充分类名称和类型名称
     */
    private void fillCategoryAndTypeName(List<CmsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 获取所有分类ID
        List<Long> categoryIds = articles.stream()
                .map(CmsArticle::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Map.of() : cmsCategoryMapper.selectList(
                Wrappers.lambdaQuery(CmsCategory.class).in(CmsCategory::getId, categoryIds)
        ).stream().collect(Collectors.toMap(CmsCategory::getId, CmsCategory::getName, (v1, v2) -> v1));

        // typeIds 已由 ES 返回；这里只做名称补全
        Set<Long> allTypeIdSet = articles.stream()
                .flatMap(a -> (a.getTypeIds() == null ? List.<Long>of() : a.getTypeIds()).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> typeMap = allTypeIdSet.isEmpty() ? Map.of() : cmsTypeMapper.selectList(
                Wrappers.lambdaQuery(CmsType.class).in(CmsType::getId, allTypeIdSet)
        ).stream().collect(Collectors.toMap(CmsType::getId, CmsType::getName, (v1, v2) -> v1));

        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                article.setCategoryName(categoryMap.get(article.getCategoryId()));
            }

            List<Long> tids = article.getTypeIds();
            if (tids == null) tids = List.of();
            tids = tids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
            article.setTypeIds(tids);

            List<String> tnames = tids.stream()
                    .map(typeMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            article.setTypeNames(tnames);
            article.setTypeName(tnames.isEmpty() ? null : String.join("、", tnames));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        if (article == null) return;

        if (article.getId() == null) {
            // ES 主存储：不再依赖 MySQL 自增 id
            article.setId(com.baomidou.mybatisplus.core.toolkit.IdWorker.getId());
        }

        if (article.getStatus() == null) {
            article.setStatus(0); // 默认草稿
        }
        Date now = new Date();
        if (article.getCreateTime() == null) article.setCreateTime(now);
        article.setUpdateTime(now);
        cmsArticleEsService.saveOrUpdate(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RequestVo<CmsArticle> requestVo) {
        CmsArticle article = requestVo.getData();
        if (article == null || article.getId() == null) return;
        article.setUpdateTime(new Date());
        cmsArticleEsService.saveOrUpdate(article);
    }

    @Override
    public CmsArticle info(Long id) {
        if (id == null) return null;
        CmsArticleEsDocument doc = cmsArticleEsService.getById(id);
        if (doc == null) return null;
        CmsArticle article = toCmsArticle(doc);
        fillCategoryAndTypeName(List.of(article));
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (id == null) return;
        cmsArticleEsService.deleteByArticleId(id);
    }

    private static CmsArticle toCmsArticle(CmsArticleEsDocument doc) {
        CmsArticle a = new CmsArticle();
        if (doc == null) return a;
        a.setId(doc.getId());
        a.setTitle(doc.getTitle());
        a.setSummary(doc.getSummary());
        a.setContent(doc.getContent());
        a.setCategoryId(doc.getCategoryId());
        a.setStatus(doc.getStatus());
        a.setTypeIds(doc.getTypeIds() == null ? List.of() : doc.getTypeIds());
        a.setCoverImages(doc.getCoverImages() == null ? List.of() : doc.getCoverImages());
        a.setCreateTime(doc.getCreateTime());
        a.setUpdateTime(doc.getUpdateTime());
        return a;
    }
}
