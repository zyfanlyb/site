package com.zyfan.site_service.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.site_service.cms.entity.CmsCategory;
import com.zyfan.site_service.cms.mapper.CmsCategoryMapper;
import com.zyfan.site_service.cms.service.ICmsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CmsCategoryServiceImpl extends ServiceImpl<CmsCategoryMapper, CmsCategory> implements ICmsCategoryService {

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

    @Override
    public IPage<CmsCategory> pageList(RequestVo<CmsCategory> requestVo) {
        CmsCategory params = requestVo.getData();
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;

        Page<CmsCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CmsCategory> wrapper = Wrappers.lambdaQuery(CmsCategory.class);

        if (params != null && params.getName() != null && !params.getName().isEmpty()) {
            wrapper.like(CmsCategory::getName, params.getName());
        }

        wrapper.orderByAsc(CmsCategory::getSort)
                .orderByDesc(CmsCategory::getCreateTime);

        return cmsCategoryMapper.selectPage(page, wrapper);
    }

    @Override
    public List<CmsCategory> getAllCategories() {
        List<CmsCategory> categories = cmsCategoryMapper.selectList(
                Wrappers.lambdaQuery(CmsCategory.class)
                        .eq(CmsCategory::getStatus, true)
                        .orderByAsc(CmsCategory::getSort)
                        .orderByDesc(CmsCategory::getCreateTime)
        );
        return categories;
    }

    @Override
    public void insert(RequestVo<CmsCategory> requestVo) {
        CmsCategory category = requestVo.getData();
        if (category.getCreateTime() == null) {
            category.setCreateTime(new Date());
        }
        if (category.getUpdateTime() == null) {
            category.setUpdateTime(new Date());
        }
        cmsCategoryMapper.insert(category);
    }

    @Override
    public void update(RequestVo<CmsCategory> requestVo) {
        CmsCategory category = requestVo.getData();
        category.setUpdateTime(new Date());
        cmsCategoryMapper.updateById(category);
    }

    @Override
    public CmsCategory info(Long id) {
        return cmsCategoryMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        // 使用Set记录已访问的分类ID，防止循环引用导致的死循环
        Set<Long> visitedIds = new HashSet<>();
        // 设置最大递归深度，防止异常数据导致无限递归
        int maxDepth = 10;
        removeRecursive(id, visitedIds, maxDepth);
    }

    /**
     * 递归删除分类及其子分类
     */
    private void removeRecursive(Long id, Set<Long> visitedIds, int maxDepth) {
        if (visitedIds.contains(id) || maxDepth <= 0) {
            return;
        }

        visitedIds.add(id);

        // 查找所有子分类
        List<CmsCategory> children = cmsCategoryMapper.selectList(
                Wrappers.lambdaQuery(CmsCategory.class)
                        .eq(CmsCategory::getParentId, id)
        );

        // 递归删除所有子分类
        if (children != null && !children.isEmpty()) {
            for (CmsCategory child : children) {
                removeRecursive(child.getId(), visitedIds, maxDepth - 1);
            }
        }

        // 删除当前分类
        cmsCategoryMapper.deleteById(id);
    }
}
