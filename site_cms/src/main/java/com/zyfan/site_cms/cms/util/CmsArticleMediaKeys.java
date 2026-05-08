package com.zyfan.site_cms.cms.util;

import com.zyfan.site_cms.elasticsearch.document.CmsArticleEsDocument;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从文章 ES 文档中收集 MinIO 对象路径（与 CMS 上传命名一致：yyyy/MM/dd/uuid.ext）。
 */
public final class CmsArticleMediaKeys {

    /** Markdown / HTML 正文中可能出现的对象路径 */
    private static final Pattern IN_CONTENT =
            Pattern.compile("(\\d{4}/\\d{2}/\\d{2}/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(\\.[A-Za-z0-9]{1,24})?)");

    private CmsArticleMediaKeys() {
    }

    public static Set<String> collectFromDocument(CmsArticleEsDocument doc) {
        Set<String> keys = new LinkedHashSet<>();
        if (doc == null) {
            return keys;
        }
        addCoverKeys(keys, doc.getCoverImages());
        addFromText(keys, doc.getContent());
        addFromText(keys, doc.getSummary());
        return keys;
    }

    private static void addCoverKeys(Set<String> keys, List<String> coverImages) {
        if (coverImages == null) {
            return;
        }
        for (String raw : coverImages) {
            String t = StringUtils.trimToNull(raw);
            if (t == null || t.startsWith("http://") || t.startsWith("https://")) {
                continue;
            }
            if (looksLikeStorageObjectKey(t)) {
                keys.add(t);
            }
        }
    }

    private static void addFromText(Set<String> keys, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        Matcher m = IN_CONTENT.matcher(text);
        while (m.find()) {
            keys.add(m.group(1));
        }
    }

    private static boolean looksLikeStorageObjectKey(String t) {
        return IN_CONTENT.matcher(t).matches();
    }
}
