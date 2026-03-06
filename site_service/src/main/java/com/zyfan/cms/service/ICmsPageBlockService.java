package com.zyfan.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.cms.entity.CmsPageBlock;

import java.util.List;

/**
 * CMS 页面模块块服务接口
 * 提供页面模块块的查询、保存和发布功能
 * 
 * @author system
 */
public interface ICmsPageBlockService extends IService<CmsPageBlock> {
    /**
     * 根据页面ID和状态查询模块块列表
     * 
     * @param pageId 页面ID
     * @param status 模块块状态，DRAFT（草稿）或 PUBLISHED（已发布）
     * @return 模块块列表，按 sort 字段升序排序
     */
    List<CmsPageBlock> listBlocks(Long pageId, String status);
    
    /**
     * 替换指定页面的所有草稿模块块
     * 先删除该页面所有状态为 DRAFT 的模块块，然后插入新的模块块列表
     * 
     * @param pageId 页面ID
     * @param blocks 新的模块块列表，所有模块块的状态会被设置为 DRAFT
     */
    void replaceDraftBlocks(Long pageId, List<CmsPageBlock> blocks);
    
    /**
     * 发布指定页面的所有草稿模块块
     * 将该页面所有状态为 DRAFT 的模块块复制为 PUBLISHED 状态
     * 如果已存在相同 ID 的 PUBLISHED 模块块，则更新；否则创建新记录
     * 
     * @param pageId 页面ID
     */
    void publishBlocks(Long pageId);
}

