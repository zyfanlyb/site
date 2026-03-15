package com.zyfan.site_service.cms.entity;

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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("category_id")
    private Long categoryId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Boolean status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

}
