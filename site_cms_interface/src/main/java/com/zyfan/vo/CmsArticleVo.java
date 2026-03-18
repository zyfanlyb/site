package com.zyfan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 站点使用的文章 VO，同时也兼容 cms 返回的字段结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsArticleVo {

    private Long id;

    private String title;

    private Long categoryId;

    private Long typeId;

    private String summary;

    private String content;

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
}

