package com.zyfan.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * CMS 内容实体类（文章/项目共用）
 * 用于管理博客文章和项目作品，通过 type 字段区分类型
 * 支持草稿和发布双版本，只有已发布的内容才会在前台显示
 * 
 * @author system
 */
@Data
@TableName("cms_post")
public class CmsPost {
    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 内容类型
     * 取值：POST（博客文章）、PROJECT（项目作品）
     * 用于区分不同类型的内容，前端可根据此字段渲染不同的展示样式
     */
    @TableField("type")
    private String type;

    /**
     * URL友好标识符（slug）
     * 用于生成文章/项目的访问路径，例如：/post/my-first-article
     * 必须唯一，建议使用英文、数字、连字符组成
     */
    @TableField("slug")
    private String slug;

    /**
     * 标题
     * 文章或项目的名称
     */
    @TableField("title")
    private String title;

    /**
     * 摘要/简介
     * 用于列表页展示和SEO描述，建议控制在150字以内
     */
    @TableField("summary")
    private String summary;

    /**
     * 封面图片URL
     * 用于列表页和详情页的展示
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * Markdown 格式的内容
     * 存储文章或项目的正文内容，使用 Markdown 语法
     * 前端需要使用 Markdown 渲染器（如 marked、markdown-it）转换为 HTML
     */
    @TableField("content_md")
    private String contentMd;

    /**
     * 内容状态
     * 取值：DRAFT（草稿）、PUBLISHED（已发布）
     * 只有 PUBLISHED 状态的内容才会在前台显示
     */
    @TableField("status")
    private String status;

    /**
     * 发布时间
     * 当内容从草稿状态发布时，自动设置为当前时间
     * 用于列表排序和前台展示
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("published_at")
    private Date publishedAt;

    /**
     * 更新时间
     * 每次保存（包括草稿和发布）时自动更新
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;
}

