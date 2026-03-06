package com.zyfan.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.cms.entity.CmsPage;

/**
 * CMS 页面服务接口
 * 提供页面的获取、创建和发布功能
 * 
 * @author system
 */
public interface ICmsPageService extends IService<CmsPage> {
    /**
     * 根据路径获取页面，如果不存在则自动创建并返回默认值
     * 
     * @param path 页面路径，例如 "/" 表示首页，"/about" 表示关于页
     * @return 页面对象，如果不存在则创建新记录并返回
     */
    CmsPage getOrInitByPath(String path);
    
    /**
     * 发布指定路径的页面
     * 将页面状态从 DRAFT 改为 PUBLISHED，使页面在前台可见
     * 
     * @param path 页面路径
     * @return 发布后的页面对象
     */
    CmsPage publishByPath(String path);
}

