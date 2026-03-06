package com.zyfan.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户角色关联表
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(exist = false)
    private List<Long> roleIds;

    /**
    * 用户ID
    */
    @TableField("user_id")
    private Long userId;

    /**
    * 角色ID
    */
    @TableField("role_id")
    private Long roleId;

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
