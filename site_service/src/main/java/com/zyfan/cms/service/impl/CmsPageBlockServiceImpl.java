package com.zyfan.cms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.cms.entity.CmsPageBlock;
import com.zyfan.cms.enums.CmsStatus;
import com.zyfan.cms.mapper.CmsPageBlockMapper;
import com.zyfan.cms.service.ICmsPageBlockService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * CMS 页面模块块服务实现类
 * 
 * @author system
 */
@Service
public class CmsPageBlockServiceImpl extends ServiceImpl<CmsPageBlockMapper, CmsPageBlock> implements ICmsPageBlockService {

    /**
     * {@inheritDoc}
     * 实现逻辑：根据页面ID和状态查询模块块，按 sort 字段升序排序，如果 sort 相同则按 id 排序
     */
    @Override
    public List<CmsPageBlock> listBlocks(Long pageId, String status) {
        List<CmsPageBlock> blocks = this.list(Wrappers.lambdaQuery(CmsPageBlock.class)
                .eq(CmsPageBlock::getPageId, pageId)
                .eq(CmsPageBlock::getStatus, status));
        if (blocks != null) {
            blocks.sort(Comparator.comparing(CmsPageBlock::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Comparator.comparing(CmsPageBlock::getId, Comparator.nullsLast(Comparator.naturalOrder()))));
        }
        return blocks;
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：
     * 1. 删除该页面所有状态为 DRAFT 的模块块
     * 2. 将新模块块列表的 id 置空、pageId 设置为当前页面、status 设置为 DRAFT
     * 3. 如果 sort 或 propsJson 为空，设置默认值
     * 4. 批量保存新模块块
     */
    @Override
    public void replaceDraftBlocks(Long pageId, List<CmsPageBlock> blocks) {
        // clear existing draft
        this.remove(Wrappers.lambdaQuery(CmsPageBlock.class)
                .eq(CmsPageBlock::getPageId, pageId)
                .eq(CmsPageBlock::getStatus, CmsStatus.DRAFT.name()));
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        for (CmsPageBlock block : blocks) {
            block.setId(null);
            block.setPageId(pageId);
            block.setStatus(CmsStatus.DRAFT.name());
            if (block.getSort() == null) {
                block.setSort(0);
            }
            if (block.getPropsJson() == null) {
                block.setPropsJson("{}");
            }
        }
        this.saveBatch(blocks);
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：
     * 1. 删除该页面所有状态为 PUBLISHED 的模块块
     * 2. 查询该页面所有状态为 DRAFT 的模块块
     * 3. 将草稿模块块的 id 置空、status 设置为 PUBLISHED
     * 4. 批量保存为已发布模块块
     */
    @Override
    public void publishBlocks(Long pageId) {
        // remove existing published and copy from draft
        this.remove(Wrappers.lambdaQuery(CmsPageBlock.class)
                .eq(CmsPageBlock::getPageId, pageId)
                .eq(CmsPageBlock::getStatus, CmsStatus.PUBLISHED.name()));
        List<CmsPageBlock> draft = listBlocks(pageId, CmsStatus.DRAFT.name());
        if (draft == null || draft.isEmpty()) {
            return;
        }
        for (CmsPageBlock block : draft) {
            block.setId(null);
            block.setStatus(CmsStatus.PUBLISHED.name());
        }
        this.saveBatch(draft);
    }
}

