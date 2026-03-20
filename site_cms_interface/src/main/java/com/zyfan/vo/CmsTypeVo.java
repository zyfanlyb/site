package com.zyfan.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 站点端使用的类型 VO（承接 CMS 返回的 CmsType）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsTypeVo {

    private Long id;

    private Long categoryId;

    private String name;
}

