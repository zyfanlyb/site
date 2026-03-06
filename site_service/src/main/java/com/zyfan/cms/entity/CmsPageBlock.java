package com.zyfan.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * CMS 页面模块块实体类
 * 用于存储页面的各个功能模块，如 Hero 横幅、文章列表、项目卡片、Markdown 内容等
 * 每个页面由多个模块块按 sort 顺序排列组成，支持草稿和发布双版本
 * 
 * @author system
 */
@Data
@TableName("cms_page_block")
public class CmsPageBlock {
    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属页面ID
     * 关联 cms_page 表的 id 字段
     */
    @TableField("page_id")
    private Long pageId;

    /**
     * 模块类型
     * 取值示例：
     * - "hero": Hero 横幅模块
     * - "markdown": Markdown 内容模块
     * - "post_list": 文章列表模块
     * - "project_list": 项目列表模块
     * - "timeline": 时间线模块
     * 前端根据此类型渲染对应的组件
     */
    @TableField("type")
    private String type;

    /**
     * 排序序号
     * 数值越小越靠前，用于控制模块在页面中的显示顺序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 模块状态
     * 取值：DRAFT（草稿）、PUBLISHED（已发布）
     * 只有 PUBLISHED 状态的模块才会在前台显示
     */
    @TableField("status")
    private String status;

    /**
     * 模块配置属性JSON字符串
     * 根据不同的 type，存储不同的配置信息
     * 
     * 示例（hero 类型）：
     * {
     *   "title": "欢迎来到我的网站",
     *   "subtitle": "这是我的个人主页",
     *   "backgroundImage": "/images/hero-bg.jpg",
     *   "buttonText": "了解更多",
     *   "buttonLink": "/about"
     * }
     * 
     * 示例（markdown 类型）：
     * {
     *   "content": "# 标题\n\n这是内容..."
     * }
     * 
     * 示例（post_list 类型）：
     * {
     *   "limit": 10,
     *   "showCover": true,
     *   "category": "tech"
     * }
     */
    @TableField("props_json")
    private String propsJson;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;
}

