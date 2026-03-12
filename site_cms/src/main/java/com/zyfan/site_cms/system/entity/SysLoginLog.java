package com.zyfan.site_cms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 系统登录日志表
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 用户ID
    */
    @TableField("user_id")
    private Long userId;

    /**
    * 登录IP
    */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录地点
     */
    @TableField("login_location")
    private String loginLocation;

    /**
     * 经度
     */
    @TableField("longitude")
    private String longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private String latitude;

    /**
     * 客户端ID
     */
    @TableField("client_id")
    private String clientId;

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
     * 用户名（关联查询）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 客户端名称（关联查询）
     */
    @TableField(exist = false)
    private String clientName;

    /**
     * 开始时间（查询参数，非数据库字段）
     */
    @TableField(exist = false)
    private String startTime;

    /**
     * 结束时间（查询参数，非数据库字段）
     */
    @TableField(exist = false)
    private String endTime;

    /**
     * 用户ID集合（查询参数，非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> userIdList;

    /**
     * 客户端ID集合（查询参数，非数据库字段）
     */
    @TableField(exist = false)
    private List<String> clientIdList;

}
