package com.zyfan.site_cms.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 文章索引（ES 作为文章主存储）：
 * <ul>
 *   <li>文章本体字段：标题/摘要/正文/状态/排序/创建时间/更新时间</li>
 *   <li>关联维度：categoryId、typeIds、coverImages（分类/类型主表仍在 MySQL，用于名称展示）</li>
 * </ul>
 */
@Data
@Document(indexName = "cms_article")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsArticleEsDocument {

    @Id
    private Long id;

    // 标题短文本：写入用更细粒度（max_word）以提高召回，查询用 smart 减少噪音
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Integer)
    private Integer status;

    /** 文章封面对象 key 列表（MinIO objectName） */
    @Field(type = FieldType.Keyword)
    private List<String> coverImages;

    /** 类型 ID 列表，用于「所选类型 ⊆ 文章类型」过滤 */
    @Field(type = FieldType.Long)
    private List<Long> typeIds;

    /** 创建时间（无关键词列表默认按 createTime 降序） */
    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 与 DB 同步，用于得分相同时的二级排序（减少分页顺序抖动）
     */
    @Field(type = FieldType.Date)
    private Date updateTime;
}
