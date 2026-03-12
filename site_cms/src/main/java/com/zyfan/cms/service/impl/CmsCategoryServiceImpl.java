package com.zyfan.cms.service.impl;

import com.zyfan.cms.entity.CmsCategory;
import com.zyfan.cms.mapper.CmsCategoryMapper;
import com.zyfan.cms.service.ICmsCategoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CmsCategoryServiceImpl extends ServiceImpl<CmsCategoryMapper, CmsCategory> implements ICmsCategoryService {

    @Autowired
    private CmsCategoryMapper cmsCategoryMapper;

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
    public IPage<CmsCategory> getCategoryTree(RequestVo<CmsCategory> requestVo) {
        CmsCategory params = requestVo.getData();
        // 1. 查询所有分类
        List<CmsCategory> allCategories = cmsCategoryMapper.selectList(Wrappers.lambdaQuery(CmsCategory.class));

        // 2. 如果有搜索条件，先过滤出匹配的节点
        List<CmsCategory> matchedCategories = allCategories;
        if (params != null && params.getName() != null) {
            matchedCategories = allCategories.stream()
                    .filter(category -> category.getName().contains(params.getName()))
                    .collect(Collectors.toList());
        }
        if (params != null && params.getStatus() != null) {
            matchedCategories = matchedCategories.stream()
                    .filter(category -> category.getStatus().equals(params.getStatus()))
                    .collect(Collectors.toList());
        }

        // 3. 获取匹配节点及其所有上级节点
        Set<CmsCategory> resultCategories = new LinkedHashSet<>();
        for (CmsCategory category : matchedCategories) {
            resultCategories.add(category);
            findAllParentNodes(category, allCategories, resultCategories);
        }

        // 4. 构建树形结构
        List<CmsCategory> treeList = Optional.ofNullable(buildTree(new ArrayList<>(resultCategories))).orElse(new ArrayList<>());

        // 5. 创建分页对象
        long total = treeList.size();
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;

        // 6. 对树形列表进行分页（只对顶级节点分页）
        int start = (int) ((pageNum - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, total);
        List<CmsCategory> pagedList = start < total ? treeList.subList(start, end) : new ArrayList<>();

        // 7. 创建并返回分页对象
        Page<CmsCategory> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pagedList);
        return page;
    }

    /**
     * 递归查找某个节点的所有上级节点
     */
    private void findAllParentNodes(CmsCategory category, List<CmsCategory> allCategories, Set<CmsCategory> result) {
        if (category.getParentId() == null || category.getParentId() == 0) {
            return;
        }

        allCategories.stream()
                .filter(c -> c.getId().equals(category.getParentId()))
                .findFirst()
                .ifPresent(parent -> {
                    result.add(parent);
                    findAllParentNodes(parent, allCategories, result);
                });
    }

    private List<CmsCategory> buildTree(List<CmsCategory> categories) {
        Map<Long, CmsCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(CmsCategory::getId, Function.identity()));

        List<CmsCategory> tree = new ArrayList<>();
        for (CmsCategory category : categories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                tree.add(category);
            } else {
                CmsCategory parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    // 这里需要添加children字段，但CmsCategory实体中没有，需要添加
                    // 暂时先不处理children
                    tree.add(category);
                }
            }
        }
        // 排序
        tree.sort(Comparator.comparing(CmsCategory::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparing(CmsCategory::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))));
        return tree;
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
