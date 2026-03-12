package com.zyfan.site_cms.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * CMS文章表
 */
@Data
@TableName("cms_article")
public class CmsArticle {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    @TableField("title")
    private String title;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 文章摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 文章内容
     */
    @TableField("content")
    private String content;

    /**
     * 封面图URL
     */
    @TableField("cover")
    private String cover;

    /**
     * 状态(0:草稿,1:已发布)
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;

    /**
     * 删除标记(0:未删除,1:已删除)
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 分类名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String categoryName;

}
