package com.zyfan.site_cms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 应用管理表
 */
@Data
@TableName("sys_application")
public class SysApplication {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 应用名称
    */
    @TableField("app_name")
    private String appName;

    /**
    * 应用类型(false:内部应用 true:外部应用)
    */
    @TableField("app_type")
    private Boolean appType;

    /**
    * 客户端ID
    */
    @TableField("client_id")
    private String clientId;

    /**
    * 客户端密钥
    */
    @TableField("client_secret")
    private String clientSecret;

    /**
    * 密钥过期时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "secret_expire_time",updateStrategy = FieldStrategy.ALWAYS)
    private Date secretExpireTime;

    /**
    * 回调地址
    */
    @TableField("redirect_uri")
    private String redirectUri;

    /**
    * 状态(0:停用 1:启用)
    */
    @TableField("status")
    private Boolean status;

    /**
    * 过期状态(true:已过期 false:未过期)
    */
    @TableField(exist = false)
    private Boolean isExpired;

    /**
    * 描述
    */
    @TableField("description")
    private String description;

    /**
    * 图标
    */
    @TableField("icon")
    private String icon;

    /**
    * 图标背景颜色
    */
    @TableField("icon_bg_color")
    private String iconBgColor;

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
    * 删除标记(0:未删除 1:已删除)
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

    @TableField(exist = false)
    private List<String> uri;

}
