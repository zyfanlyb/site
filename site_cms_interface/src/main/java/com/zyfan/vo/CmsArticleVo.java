package com.zyfan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 站点使用的文章 VO，同时也兼容 cms 返回的字段结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsArticleVo {

    /** 序列化为字符串，避免前端 JS 大整数精度丢失导致详情 id 对不上 ES */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private Long categoryId;

    /**
     * 类型ID列表（多选）
     */
    private List<Long> typeIds;

    private String summary;

    private String content;

    /**
     * 关键词：用于列表筛选（title/summary/content 模糊匹配）
     */
    private String keyword;

    /**
     * 多封面：CMS 存储/返回的封面 key 数组
     */
    private List<String> coverImages;

    /**
     * 状态(0:草稿,1:已发布)
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型名称列表
     */
    private List<String> typeNames;

    /**
     * Elasticsearch 检索得分（仅有关键词检索时由后端返回，前端可不展示）
     */
    private Double searchScore;
}

