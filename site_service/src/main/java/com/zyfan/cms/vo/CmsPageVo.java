package com.zyfan.cms.vo;

import com.zyfan.cms.entity.CmsPage;
import com.zyfan.cms.entity.CmsPageBlock;
import lombok.Data;

import java.util.List;

/**
 * CMS 页面视图对象
 * 用于封装页面信息和其关联的模块块列表，便于前后端数据传输
 * 
 * @author system
 */
@Data
public class CmsPageVo {
    /**
     * 页面基本信息
     * 包含页面路径、标题、SEO信息、状态等
     */
    private CmsPage page;
    
    /**
     * 页面模块块列表
     * 按 sort 字段排序，用于前端按顺序渲染各个模块
     */
    private List<CmsPageBlock> blocks;
}

