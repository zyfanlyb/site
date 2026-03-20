package com.zyfan.site_cms.cms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * CMS文章 DTO（ES 主存储版本）
 */
@Data
public class CmsArticle {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 类型ID列表（非数据库字段）：文章可关联多个类型
     */
    private List<Long> typeIds;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 关键词（非数据库字段）：用于列表筛选（title/summary/content 模糊匹配）
     */
    private String keyword;

    /**
     * 多封面（一对多：cms_article_cover），非数据库字段
     */
    private List<String> coverImages;

    /**
     * 状态(0:草稿,1:已发布)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 删除标记(0:未删除,1:已删除)
     */
    private Boolean deleted;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 分类名称（非数据库字段，用于前端显示）
     */
    private String categoryName;

    /**
     * 类型名称（非数据库字段，用于前端显示）
     */
    private String typeName;

    /**
     * 类型名称列表（非数据库字段，用于前端显示）
     */
    private List<String> typeNames;

    /**
     * Elasticsearch 检索得分（仅有关键词检索时返回，非数据库字段）
     */
    private Double searchScore;

}
