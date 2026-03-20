package com.zyfan.site_cms.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.zyfan.site_cms.elasticsearch.document.CmsArticleEsDocument;
import com.zyfan.site_cms.elasticsearch.repository.CmsArticleEsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章 ES 主存储：保存/删除/按条件分页查询。
 */
@Service
public class CmsArticleEsService {

    public static final String INDEX = "cms_article";

    /**
     * 关键词分页单页上限（业务默认每页 10 条；此处防止异常大 pageSize 压垮 ES / 回表）。
     */
    public static final int MAX_ES_PAGE_SIZE = 50;
    /** 关键词 list 接口：限制一次拉取条数 */
    public static final int MAX_ES_LIST_SIZE = 1000;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private CmsArticleEsRepository cmsArticleEsRepository;

    public void saveOrUpdate(CmsArticle article) {
        if (article == null || article.getId() == null) {
            return;
        }
        CmsArticleEsDocument doc = new CmsArticleEsDocument();
        doc.setId(article.getId());
        doc.setTitle(article.getTitle());
        doc.setSummary(article.getSummary());
        doc.setContent(article.getContent());
        doc.setCategoryId(article.getCategoryId());
        doc.setStatus(article.getStatus());
        List<String> covers = article.getCoverImages();
        doc.setCoverImages(covers == null ? List.of() : covers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        List<Long> tids = article.getTypeIds();
        doc.setTypeIds(tids == null ? List.of() : tids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        Date ct = article.getCreateTime();
        Date ut = article.getUpdateTime() != null ? article.getUpdateTime() : ct;
        Date now = new Date();
        doc.setCreateTime(ct != null ? ct : now);
        doc.setUpdateTime(ut != null ? ut : now);
        cmsArticleEsRepository.save(doc);
    }

    public void deleteByArticleId(Long articleId) {
        if (articleId == null) {
            return;
        }
        cmsArticleEsRepository.deleteById(articleId);
    }

    /**
     * 按主键取文档。
     * <p>优先用 ES {@code GET /{index}/_doc/{id}}（与写入时 _id 一致）；</p>
     * <p>若历史数据 _id 与业务 id 不一致，再兜底用 {@code term} 查 {@code id} 字段。</p>
     * <p>Spring Data {@code findById} 在部分集群/版本组合下会取不到文档，故统一走 Java API。</p>
     */
    public CmsArticleEsDocument getById(Long id) {
        if (id == null) {
            return null;
        }
        String idStr = String.valueOf(id);
        return fetchByIdsQuery(idStr, id);
    }

    private CmsArticleEsDocument fetchByIdsQuery(String idStr, Long fallbackId) {
        try {
            SearchResponse<CmsArticleEsDocument> sr = elasticsearchClient.search(s -> s
                            .index(INDEX)
                            .query(q -> q.ids(i -> i.values(idStr)))
                            .size(1),
                    CmsArticleEsDocument.class
            );
            if (sr.hits().hits().isEmpty()) {
                return null;
            }
            Hit<CmsArticleEsDocument> hit = sr.hits().hits().get(0);
            CmsArticleEsDocument src = hit.source();
            if (src == null) {
                return null;
            }
            ensureDocumentId(src, hit.id(), fallbackId);
            return src;
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch ids 查询失败: " + e.getMessage(), e);
        }
    }

    private static void ensureDocumentId(CmsArticleEsDocument doc, String esDocId, Long fallbackId) {
        if (doc.getId() != null) {
            return;
        }
        if (esDocId != null && !esDocId.isEmpty()) {
            try {
                doc.setId(Long.parseLong(esDocId));
                return;
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        if (fallbackId != null) {
            doc.setId(fallbackId);
        }
    }

    /**
     * 按条件分页查询（ES 主存储）：
     * <ul>
     *   <li>有 keyword：multi_match(title/summary/content)，按 _score desc、updateTime desc</li>
     *   <li>无 keyword：match_all，按 sort desc、createTime desc</li>
     *   <li>过滤：categoryId/status/typeIds（每个 tid 一个 term filter，满足“所选类型 ⊆ 文章类型”）</li>
     * </ul>
     */
    public EsPageDocs pageQuery(String keyword,
                                Long categoryId,
                                Integer status,
                                List<Long> typeIds,
                                long pageNum,
                                long pageSize,
                                int maxSizeCap) {
        String kw = keyword == null ? "" : keyword.trim();
        int size = (int) Math.max(1, Math.min(pageSize, maxSizeCap));
        int from = (int) Math.max(0, (pageNum - 1) * (long) size);

        List<Long> filterTypeIds = typeIds == null ? List.of() : typeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Query query = buildBoolQuery(kw, categoryId, status, filterTypeIds);

        try {
            SearchResponse<CmsArticleEsDocument> response = elasticsearchClient.search(s -> s
                            .index(INDEX)
                            .query(query)
                            .from(from)
                            .size(size)
                            .sort(so -> {
                                // 有 keyword：只按 _score 排序
                                if (!kw.isEmpty()) {
                                    return so.score(sc -> sc.order(SortOrder.Desc));
                                }
                                // 无 keyword：只按创建时间排序
                                // 如果历史文档缺少 createTime，为了保证稳定性，显式放到最后
                                return so.field(f -> f.field("createTime").order(SortOrder.Desc).missing("_last"));
                            })
                            // 总命中数以 ES 默认策略为准
                            .trackTotalHits(t -> t.enabled(true)),
                    CmsArticleEsDocument.class
            );

            long total = response.hits().total() != null ? response.hits().total().value() : 0L;
            List<DocScore> hits = new ArrayList<>();
            for (Hit<CmsArticleEsDocument> hit : response.hits().hits()) {
                CmsArticleEsDocument doc = hit.source();
                if (doc == null || doc.getId() == null) {
                    // 部分场景 hit.id() 可能存在，但 source 为空；此处以 ES 主存储为准
                    continue;
                }
                double score = hit.score() != null ? hit.score() : 0.0;
                hits.add(new DocScore(doc, score));
            }
            return new EsPageDocs(total, hits);
        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 检索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 用户输入里「空格分隔的词」个数 ≥ 2 时，对 multi_match 设置 minimum_should_match，
     * 要求命中一定比例的词，减少「只沾边一个词」的弱相关文档（单字/单短语保持原召回）。
     */
    private static final String MULTI_WORD_MINIMUM_SHOULD_MATCH = "70%";

    private static Query buildBoolQuery(String keyword, Long categoryId, Integer status, List<Long> filterTypeIds) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            String msm = resolveMinimumShouldMatch(k);
            bool.must(m -> m.multiMatch(mm -> {
                mm.query(k)
                        .fields("title^3", "summary", "content")
                        .type(TextQueryType.BestFields);
                if (msm != null) {
                    mm.minimumShouldMatch(msm);
                }
                return mm;
            }));
        }
        if (categoryId != null) {
            bool.filter(f -> f.term(t -> t.field("categoryId").value(FieldValue.of(categoryId))));
        }
        if (status != null) {
            bool.filter(f -> f.term(t -> t.field("status").value(FieldValue.of(status))));
        }
        for (Long tid : filterTypeIds) {
            bool.filter(f -> f.term(t -> t.field("typeIds").value(FieldValue.of(tid))));
        }
        return Query.of(q -> q.bool(bool.build()));
    }

    /**
     * 仅对「显式多词」输入收紧召回；中文连续短语不分词时无法在此拆分，仍走默认 multi_match。
     */
    private static String resolveMinimumShouldMatch(String keywordTrimmed) {
        if (keywordTrimmed == null || keywordTrimmed.isEmpty()) {
            return null;
        }
        String[] parts = keywordTrimmed.split("\\s+");
        int n = 0;
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                n++;
            }
        }
        return n >= 2 ? MULTI_WORD_MINIMUM_SHOULD_MATCH : null;
    }

    public record DocScore(CmsArticleEsDocument doc, double score) {
    }

    public record EsPageDocs(long total, List<DocScore> hits) {
        static EsPageDocs empty() {
            return new EsPageDocs(0L, List.of());
        }
    }
}
