package com.zyfan.site_cms.cms.service.impl;

import com.zyfan.site_cms.cms.entity.CmsType;
import com.zyfan.site_cms.cms.mapper.CmsTypeMapper;
import com.zyfan.site_cms.cms.service.ICmsTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CmsTypeServiceImpl extends ServiceImpl<CmsTypeMapper, CmsType> implements ICmsTypeService {

    @Autowired
    private CmsTypeMapper cmsTypeMapper;

    @Override
    public IPage<CmsType> pageList(RequestVo<CmsType> requestVo) {
        CmsType params = requestVo.getData();
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;

        Page<CmsType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CmsType> wrapper = Wrappers.lambdaQuery(CmsType.class);

        if (params != null) {
            if (params.getName() != null && !params.getName().isEmpty()) {
                wrapper.like(CmsType::getName, params.getName());
            }
            if (params.getCategoryId() != null) {
                wrapper.eq(CmsType::getCategoryId, params.getCategoryId());
            }
        }

        wrapper.orderByAsc(CmsType::getSort)
                .orderByDesc(CmsType::getCreateTime);

        return cmsTypeMapper.selectPage(page, wrapper);
    }

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

    @Override
    public void insert(RequestVo<CmsType> requestVo) {
        CmsType type = requestVo.getData();
        if (type.getCreateTime() == null) {
            type.setCreateTime(new Date());
        }
        if (type.getUpdateTime() == null) {
            type.setUpdateTime(new Date());
        }
        cmsTypeMapper.insert(type);
    }

    @Override
    public void update(RequestVo<CmsType> requestVo) {
        CmsType type = requestVo.getData();
        type.setUpdateTime(new Date());
        cmsTypeMapper.updateById(type);
    }

    @Override
    public CmsType info(Long id) {
        return cmsTypeMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        cmsTypeMapper.deleteById(id);
    }

}
