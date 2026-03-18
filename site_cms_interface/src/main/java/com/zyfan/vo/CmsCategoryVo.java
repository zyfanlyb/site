package com.zyfan.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * CMS 分类简化 VO，仅用于站点服务消费；通过 ignoreUnknown=true 兼容后端多余字段
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsCategoryVo {

    private Long id;

    private String name;
}

