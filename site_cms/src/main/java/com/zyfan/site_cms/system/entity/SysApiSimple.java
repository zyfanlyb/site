package com.zyfan.site_cms.system.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 接口简化信息（仅包含id、名称和状态）
 */
@Data
public class SysApiSimple implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 接口标题
     */
    private String title;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Boolean status;

}
