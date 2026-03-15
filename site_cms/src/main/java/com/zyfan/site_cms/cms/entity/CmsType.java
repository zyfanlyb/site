package com.zyfan.site_cms.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * CMS类型表（分类下的子类型）
 */
@Data
@TableName("cms_type")
public class CmsType {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 类型名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态(0:禁用,1:启用)
     */
    @TableField("status")
    private Boolean status;

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

}
