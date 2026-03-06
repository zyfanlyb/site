package com.zyfan.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.cms.entity.CmsPost;
import com.zyfan.pojo.web.RequestVo;

/**
 * CMS 内容服务接口（文章/项目共用）
 * 提供内容的查询、保存和发布功能
 * 
 * @author system
 */
public interface ICmsPostService extends IService<CmsPost> {
    /**
     * 分页查询内容列表
     * 支持按类型、状态、标题等条件筛选
     * 
     * @param requestVo 请求对象，包含分页参数和查询条件
     * @return 分页结果，包含内容列表和分页信息
     */
    IPage<CmsPost> pageList(RequestVo<CmsPost> requestVo);
    
    /**
     * 根据 slug 获取内容详情
     * 
     * @param slug URL 友好标识符，例如 "my-first-article"
     * @param publishedOnly 是否只查询已发布的内容，true 表示只返回 PUBLISHED 状态的内容
     * @return 内容对象，如果不存在或 publishedOnly=true 但内容未发布则返回 null
     */
    CmsPost getBySlug(String slug, boolean publishedOnly);
    
    /**
     * 保存内容为草稿
     * 如果内容ID为空，则创建新记录；否则更新现有记录
     * 如果 slug 已存在且不是当前记录，会自动生成新的 slug（添加数字后缀）
     * 内容状态会被设置为 DRAFT，published_at 保持为空
     * 
     * @param post 内容对象
     * @return 保存后的内容对象（包含自动生成的ID和slug）
     */
    CmsPost saveDraft(CmsPost post);
    
    /**
     * 发布内容
     * 将内容状态从 DRAFT 改为 PUBLISHED，并设置 published_at 为当前时间
     * 使内容在前台可见
     * 
     * @param id 内容ID
     * @return 发布后的内容对象
     */
    CmsPost publish(Long id);
}

