package com.zyfan.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 菜单权限表
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 菜单名称
    */
    @TableField("name")
    private String name;

    /**
    * 菜单路径
    */
    @TableField("path")
    private String path;

    /**
    * 组件路径
    */
    @TableField("component")
    private String component;

    /**
    * 权限标识
    */
    @TableField("perms")
    private String perms;

    /**
    * 菜单类型(0:目录 1:菜单 2:按钮)
    */
    @TableField("type")
    private String type;

    /**
    * 排序
    */
    @TableField("sort")
    private Integer sort;

    /**
    * 父菜单ID
    */
    @TableField("parent_id")
    private Long parentId;

    /**
    * 菜单图标
    */
    @TableField("icon")
    private String icon;

    /**
    * 状态(0:禁用,1:启用)
    */
    @TableField("status")
    private Boolean status;

    /**
    * 路由参数
    */
    @TableField("query")
    private String query;

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
    * 子节点
    */
    @TableField(exist = false)
    private List<SysMenu> children;

}
