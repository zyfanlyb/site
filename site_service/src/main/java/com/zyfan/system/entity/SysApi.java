package com.zyfan.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 接口开放管理表
 */
@Data
@TableName("sys_api")
public class SysApi {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 接口标题
    */
    @TableField("title")
    private String title;

    /**
    * 接口描述
    */
    @TableField("description")
    private String description;

    /**
    * 接口路径
    */
    @TableField("path")
    private String path;

    /**
    * 请求类型(GET/POST/PUT/DELETE)
    */
    @TableField("method")
    private String method;

    /**
    * 请求参数(JSON格式)
    */
    @TableField("params")
    private String params;

    /**
    * 响应示例(JSON格式)
    */
    @TableField("response_example")
    private String responseExample;

    /**
    * 状态(0-禁用 1-启用)
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
    * 删除标记(0-未删除 1-已删除)
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
