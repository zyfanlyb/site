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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Autowired
    private CmsClient cmsClient;

    /** Markdown 图片：<code>![](path)</code>，path 为 CMS 写入的 objectName */
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    /** 正文中可能包含的 HTML 插图（与 front_system md-editor 导出一致） */
    private static final Pattern HTML_IMG_SRC = Pattern.compile(
            "<img\\b[^>]*?\\bsrc\\s*=\\s*(['\"])([^'\"]+)\\1",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private void fillCoverImages(CmsArticleVo articleVo) {
        if (articleVo == null) return;
        if (articleVo.getCoverImages() == null || articleVo.getCoverImages().isEmpty()) return;
        articleVo.setCoverImages(articleVo.getCoverImages().stream()
                .map(x -> endpoint + "/" + bucketName + "/" + x)
                .collect(Collectors.toList()));
    }

    /**
     * 将正文中的 MinIO objectName（如 {@code yyyy/MM/dd/uuid.png}）补成与 {@link #fillCoverImages} 相同的浏览器可访问 URL，
     * 便于 VitePress {@code BlogPreview} / {@code BlogList} 直接展示，无需登录 CMS。
     */
    private void fillContentAssetUrls(CmsArticleVo articleVo) {
        if (articleVo == null || StringUtils.isBlank(articleVo.getContent())) {
            return;
        }
        String content = articleVo.getContent();

        Matcher md = MD_IMAGE.matcher(content);
        StringBuffer mdOut = new StringBuffer();
        while (md.find()) {
            String alt = md.group(1);
            String rawUrl = md.group(2).trim();
            String resolved = toMinioBrowserUrl(rawUrl);
            if (rawUrl.equals(resolved)) {
                md.appendReplacement(mdOut, Matcher.quoteReplacement(md.group(0)));
            } else {
                md.appendReplacement(mdOut, Matcher.quoteReplacement("![" + alt + "](" + resolved + ")"));
            }
        }
        md.appendTail(mdOut);
        content = mdOut.toString();

        Matcher hi = HTML_IMG_SRC.matcher(content);
        StringBuffer hiOut = new StringBuffer();
        while (hi.find()) {
            String full = hi.group(0);
            String quote = hi.group(1);
            String rawSrc = hi.group(2);
            String resolved = toMinioBrowserUrl(rawSrc.trim());
            if (rawSrc.trim().equals(resolved)) {
                hi.appendReplacement(hiOut, Matcher.quoteReplacement(full));
            } else {
                String repl = full.replace(quote + rawSrc + quote, quote + resolved + quote);
                hi.appendReplacement(hiOut, Matcher.quoteReplacement(repl));
            }
        }
        hi.appendTail(hiOut);
        articleVo.setContent(hiOut.toString());
    }

    /**
     * CMS 存库的相对 objectKey → {@code endpoint/bucket/key}；已是 http(s) 或与站点逻辑一致的完整地址则不改。
     */
    private String toMinioBrowserUrl(String raw) {
        if (StringUtils.isBlank(raw)) {
            return raw;
        }
        String u = raw.trim();
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return u;
        }
        String key = StringUtils.removeStart(u, "/");
        key = StringUtils.removeStart(key, "upload/");
        if (StringUtils.isBlank(key)) {
            return raw;
        }
        String base = endpoint.replaceAll("/+$", "") + "/" + bucketName + "/";
        return base + key;
    }

    private void fillArticleMediaForSite(CmsArticleVo articleVo) {
        fillCoverImages(articleVo);
        fillContentAssetUrls(articleVo);
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
                .forEach(this::fillArticleMediaForSite);
        return resp;
    }

    @Override
    public ResponseVo<CmsArticleVo> getArticle(Long id) {
        ResponseVo<CmsArticleVo> resp = cmsClient.getArticleInfo(id);
        if (resp == null || resp.getCode() != CodeStatusEnum.SUCCESS.getCode() || resp.getData() == null) {
            throw new ZException("文章不存在或未发布");
        }
        fillArticleMediaForSite(resp.getData());
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
                .forEach(this::fillArticleMediaForSite);
        return resp;
    }
}

