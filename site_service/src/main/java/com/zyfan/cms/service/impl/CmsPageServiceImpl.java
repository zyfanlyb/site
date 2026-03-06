package com.zyfan.cms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.cms.entity.CmsPage;
import com.zyfan.cms.enums.CmsStatus;
import com.zyfan.cms.mapper.CmsPageMapper;
import com.zyfan.cms.service.ICmsPageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * CMS 页面服务实现类
 * 
 * @author system
 */
@Service
public class CmsPageServiceImpl extends ServiceImpl<CmsPageMapper, CmsPage> implements ICmsPageService {

    /**
     * {@inheritDoc}
     * 实现逻辑：根据路径查询页面，如果不存在则创建新记录（状态为 DRAFT）
     */
    @Override
    public CmsPage getOrInitByPath(String path) {
        if (StringUtils.isBlank(path)) {
            path = "/";
        }
        CmsPage page = this.getOne(Wrappers.lambdaQuery(CmsPage.class).eq(CmsPage::getPath, path).last("limit 1"));
        if (page != null) {
            return page;
        }
        CmsPage init = new CmsPage();
        init.setPath(path);
        init.setStatus(CmsStatus.DRAFT.name());
        this.save(init);
        return init;
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：先获取或创建页面，然后将状态更新为 PUBLISHED
     */
    @Override
    public CmsPage publishByPath(String path) {
        CmsPage page = getOrInitByPath(path);
        page.setStatus(CmsStatus.PUBLISHED.name());
        this.updateById(page);
        return page;
    }
}

