package com.zyfan.cms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.cms.entity.CmsPost;
import com.zyfan.cms.enums.CmsStatus;
import com.zyfan.cms.mapper.CmsPostMapper;
import com.zyfan.cms.service.ICmsPostService;
import com.zyfan.pojo.web.RequestVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * CMS 内容服务实现类（文章/项目共用）
 * 
 * @author system
 */
@Service
public class CmsPostServiceImpl extends ServiceImpl<CmsPostMapper, CmsPost> implements ICmsPostService {

    /**
     * {@inheritDoc}
     * 实现逻辑：
     * 1. 支持按标题模糊查询（like）
     * 2. 支持按类型精确查询（eq）
     * 3. 支持按状态精确查询（eq）
     * 4. 按发布时间降序，如果发布时间相同则按更新时间降序
     */
    @Override
    public IPage<CmsPost> pageList(RequestVo<CmsPost> requestVo) {
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;
        CmsPost params = requestVo.getData();

        return this.page(new Page<>(pageNum, pageSize),
                Wrappers.lambdaQuery(CmsPost.class)
                        .like(params != null && StringUtils.isNotBlank(params.getTitle()), CmsPost::getTitle, params.getTitle())
                        .eq(params != null && StringUtils.isNotBlank(params.getType()), CmsPost::getType, params.getType())
                        .eq(params != null && StringUtils.isNotBlank(params.getStatus()), CmsPost::getStatus, params.getStatus())
                        .orderByDesc(CmsPost::getPublishedAt)
                        .orderByDesc(CmsPost::getUpdateTime));
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：根据 slug 查询，如果 publishedOnly 为 true 则只查询 PUBLISHED 状态的内容
     */
    @Override
    public CmsPost getBySlug(String slug, boolean publishedOnly) {
        return this.getOne(Wrappers.lambdaQuery(CmsPost.class)
                .eq(CmsPost::getSlug, slug)
                .eq(publishedOnly, CmsPost::getStatus, CmsStatus.PUBLISHED.name())
                .last("limit 1"));
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：
     * 1. 如果状态为空，设置为 DRAFT
     * 2. 如果 id 为空，执行新增；否则执行更新
     * 3. 返回保存后的完整对象
     * 注意：此方法不会自动处理 slug 冲突，需要在调用前确保 slug 唯一
     */
    @Override
    public CmsPost saveDraft(CmsPost post) {
        if (post.getStatus() == null) {
            post.setStatus(CmsStatus.DRAFT.name());
        }
        if (post.getId() == null) {
            this.save(post);
        } else {
            this.updateById(post);
        }
        return this.getById(post.getId());
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：
     * 1. 根据 id 查询内容
     * 2. 将状态更新为 PUBLISHED
     * 3. 如果 publishedAt 为空，设置为当前时间
     * 4. 返回发布后的完整对象
     */
    @Override
    public CmsPost publish(Long id) {
        CmsPost post = this.getById(id);
        if (post == null) {
            return null;
        }
        post.setStatus(CmsStatus.PUBLISHED.name());
        if (post.getPublishedAt() == null) {
            post.setPublishedAt(new Date());
        }
        this.updateById(post);
        return this.getById(id);
    }
}

