package com.zyfan.cms.enums;

/**
 * CMS 内容状态枚举
 * 用于标识页面、模块块、文章/项目的发布状态
 * 
 * @author system
 */
public enum CmsStatus {
    /**
     * 草稿状态
     * 内容已保存但未发布，前台不可见，仅后台可见
     */
    DRAFT,
    
    /**
     * 已发布状态
     * 内容已发布，前台可见
     */
    PUBLISHED
}

