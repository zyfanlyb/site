package com.zyfan.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 系统角色表
 */
@Data
@TableName("sys_role")
public class SysRole {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 角色名称
    */
    @TableField("role_name")
    private String roleName;

    /**
    * 角色描述
    */
    @TableField("description")
    private String description;

    /**
    * 状态(0-禁用 1-正常)
    */
    @TableField("status")
    private Boolean status;

    /**
    * 删除标识(0-未删除 1-已删除)
    */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

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
