package com.zyfan.site_cms.elasticsearch.repository;

import com.zyfan.site_cms.elasticsearch.document.CmsArticleEsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsArticleEsRepository extends ElasticsearchRepository<CmsArticleEsDocument, Long> {
}
