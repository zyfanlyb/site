package com.zyfan.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 应用接口关联表
 */
@Data
@TableName("sys_application_api")
public class SysApplicationApi {

    /**
    * 主键ID
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(exist = false)
    private List<Long> apiIds;

    /**
    * 应用ID
    */
    @TableField("application_id")
    private Long applicationId;

    /**
    * 接口ID
    */
    @TableField("api_id")
    private Long apiId;

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
