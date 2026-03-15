package com.zyfan.site_service.cms.service.impl;

import com.zyfan.site_service.cms.entity.CmsType;
import com.zyfan.site_service.cms.mapper.CmsTypeMapper;
import com.zyfan.site_service.cms.service.ICmsTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CmsTypeServiceImpl extends ServiceImpl<CmsTypeMapper, CmsType> implements ICmsTypeService {

    @Autowired
    private CmsTypeMapper cmsTypeMapper;

    @Override
    public List<CmsType> listByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return cmsTypeMapper.selectList(
                Wrappers.lambdaQuery(CmsType.class)
                        .eq(CmsType::getCategoryId, categoryId)
                        .eq(CmsType::getStatus, true)
                        .orderByAsc(CmsType::getSort)
                        .orderByDesc(CmsType::getCreateTime)
        );
    }

}
