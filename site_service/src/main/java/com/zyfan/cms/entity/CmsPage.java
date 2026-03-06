package com.zyfan.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * CMS 页面实体类
 * 用于管理网站各个页面的基本信息，如首页、关于页、项目页等
 * 每个页面由多个模块块（CmsPageBlock）组成，通过 page_id 关联
 * 
 * @author system
 */
@Data
@TableName("cms_page")
public class CmsPage {
    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 页面路径，唯一标识
     * 例如："/" 表示首页，"/about" 表示关于页，"/projects" 表示项目页
     * 用于前端路由匹配和内容查询
     */
    @TableField("path")
    private String path;

    /**
     * 页面标题
     * 用于页面显示和后台管理
     */
    @TableField("title")
    private String title;

    /**
     * SEO标题
     * 用于搜索引擎优化，显示在浏览器标签页和搜索结果中
     * 如果为空，则使用 title 字段
     */
    @TableField("seo_title")
    private String seoTitle;

    /**
     * SEO描述
     * 用于搜索引擎优化，显示在搜索结果摘要中
     */
    @TableField("seo_description")
    private String seoDescription;

    /**
     * 页面状态
     * 取值：DRAFT（草稿）、PUBLISHED（已发布）
     * 只有 PUBLISHED 状态的页面才会在前台显示
     */
    @TableField("status")
    private String status;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;
}

