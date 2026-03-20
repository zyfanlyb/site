package com.zyfan.site_service.article.service.impl;

import com.zyfan.client.CmsClient;
import com.zyfan.exception.ZException;
import com.zyfan.pojo.web.CodeStatusEnum;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.site_service.article.service.ArticleService;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;
import com.zyfan.vo.CmsTypeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    private CmsClient cmsClient;

    private void fillCoverImages(CmsArticleVo articleVo) {
        if (articleVo == null) return;
        if (articleVo.getCoverImages() == null || articleVo.getCoverImages().isEmpty()) return;
        articleVo.setCoverImages(articleVo.getCoverImages().stream()
                .map(x -> endpoint + "/" + bucketName + "/" + x)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseVo<List<CmsArticleVo>> getArticles(Long categoryId,
                                                           String categoryName,
                                                           List<Long> typeIds,
                                                           String keyword,
                                                           Integer pageNum,
                                                           Integer pageSize) {
        RequestVo<CmsArticleVo> requestVo = new RequestVo<>();
        requestVo.setPageNum(pageNum == null ? 1L : pageNum.longValue());
        requestVo.setPageSize(pageSize == null ? 10L : pageSize.longValue());

        CmsArticleVo params = new CmsArticleVo();
        params.setStatus(1); // 只查询已发布的
        if (keyword != null && !keyword.isBlank()) {
            params.setKeyword(keyword.trim());
        }

        // 优先按 ID，其次按名称映射
        if (categoryId != null) {
            params.setCategoryId(categoryId);
        } else if (categoryName != null && !categoryName.isBlank()) {
            ResponseVo<List<CmsCategoryVo>> resp = cmsClient.listCategories();
            if (resp != null
                    && resp.getCode() == CodeStatusEnum.SUCCESS.getCode()
                    && resp.getData() != null) {
                CmsCategoryVo match = resp.getData().stream()
                        .filter(c -> categoryName.equals(c.getName()))
                        .findFirst()
                        .orElse(null);
                if (match != null) {
                    params.setCategoryId(match.getId());
                }
            }
        }

        if (typeIds != null && !typeIds.isEmpty()) {
            List<Long> distinct = typeIds.stream()
                    .filter(x -> x != null && x > 0)
                    .distinct()
                    .collect(Collectors.toList());
            params.setTypeIds(distinct.isEmpty() ? null : distinct);
        }

        requestVo.setData(params);

        ResponseVo<List<CmsArticleVo>> resp = cmsClient.pageArticles(requestVo);
        if (resp == null || resp.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException("获取文章列表失败");
        }
        Optional.ofNullable(resp.getData()).orElseGet(ArrayList::new)
                .forEach(this::fillCoverImages);
        return resp;
    }

    @Override
    public ResponseVo<CmsArticleVo> getArticle(Long id) {
        ResponseVo<CmsArticleVo> resp = cmsClient.getArticleInfo(id);
        if (resp == null || resp.getCode() != CodeStatusEnum.SUCCESS.getCode() || resp.getData() == null) {
            throw new ZException("文章不存在或未发布");
        }
        fillCoverImages(resp.getData());
        return resp;
    }

    @Override
    public ResponseVo<List<CmsCategoryVo>> getCategories() {
        return cmsClient.listCategories();
    }

    @Override
    public ResponseVo<List<CmsTypeVo>> getTypes(Long categoryId) {
        RequestVo<CmsTypeVo> requestVo = new RequestVo<>();
        CmsTypeVo params = new CmsTypeVo();
        params.setCategoryId(categoryId);
        requestVo.setData(params);
        ResponseVo<List<CmsTypeVo>> resp = cmsClient.listTypes(requestVo);
        if (resp == null || resp.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException("获取类型列表失败");
        }
        return resp;
    }

    @Override
    public ResponseVo<List<CmsArticleVo>> getArticlesByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        RequestVo<CmsArticleVo> requestVo = new RequestVo<>();
        requestVo.setPageNum(pageNum == null ? 1L : pageNum.longValue());
        requestVo.setPageSize(pageSize == null ? 10L : pageSize.longValue());

        CmsArticleVo params = new CmsArticleVo();
        params.setStatus(1); // 只查询已发布的
        params.setCategoryId(categoryId);
        requestVo.setData(params);

        ResponseVo<List<CmsArticleVo>> resp = cmsClient.pageArticles(requestVo);
        if (resp == null || resp.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException("获取文章列表失败");
        }
        Optional.ofNullable(resp.getData()).orElseGet(ArrayList::new)
                .forEach(this::fillCoverImages);
        return resp;
    }
}

