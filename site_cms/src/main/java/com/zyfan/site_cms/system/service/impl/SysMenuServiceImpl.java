package com.zyfan.site_cms.system.service.impl;

import com.zyfan.site_cms.system.entity.SysMenu;
import com.zyfan.site_cms.system.entity.SysRoleMenu;
import com.zyfan.site_cms.system.entity.SysUserRole;
import com.zyfan.site_cms.system.mapper.SysMenuMapper;
import com.zyfan.site_cms.system.service.ISysMenuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.site_cms.system.service.ISysRoleMenuService;
import com.zyfan.site_cms.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> getAllMenus() {
        List<SysMenu> menus = sysMenuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class).ne(SysMenu::getType, "2"));
        // 按 sort 字段排序
        if (menus != null && !menus.isEmpty()) {
            menus.sort(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Comparator.comparing(SysMenu::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))));
        }
        return menus;
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        // 1. 获取用户角色ID列表
        List<Long> roleIds = Optional.ofNullable(
                        sysUserRoleService.list(Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getUserId, userId))
                ).orElseGet(ArrayList::new)
                .stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 用户没有任何角色时，直接返回空菜单（不要抛错）
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取角色关联的菜单ID列表
        List<Long> menuIds = Optional.ofNullable(
                        sysRoleMenuService.list(Wrappers.lambdaQuery(SysRoleMenu.class).in(SysRoleMenu::getRoleId, roleIds))
                ).orElseGet(ArrayList::new)
                .stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 角色没有关联任何菜单时，直接返回空菜单（不要抛错）
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询这些菜单
        List<SysMenu> menus = Optional.ofNullable(sysMenuMapper.selectList(
                        Wrappers.lambdaQuery(SysMenu.class)
                                .eq(SysMenu::getStatus, true)
                                .in(SysMenu::getId, menuIds)
                )).orElseGet(ArrayList::new)
                .stream()
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Comparator.comparing(SysMenu::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))))
                .toList();
        return menus;
    }

    @Override
    public IPage<SysMenu> getMenuTree(RequestVo<SysMenu> requestVo) {
        SysMenu params = requestVo.getData();
        // 1. 查询所有菜单
        List<SysMenu> allMenus = sysMenuMapper.selectList(Wrappers.lambdaQuery(SysMenu.class));

        // 2. 如果有搜索条件，先过滤出匹配的节点
        List<SysMenu> matchedMenus = allMenus;
        if (params != null && params.getName() != null) {
            matchedMenus = allMenus.stream()
                    .filter(menu -> menu.getName().contains(params.getName()))
                    .collect(Collectors.toList());
        }
        if (params != null && params.getStatus() != null) {
            matchedMenus = matchedMenus.stream()
                    .filter(menu -> menu.getStatus().equals(params.getStatus()))
                    .collect(Collectors.toList());
        }

        // 3. 获取匹配节点及其所有上级节点
        Set<SysMenu> resultMenus = new LinkedHashSet<>();
        for (SysMenu menu : matchedMenus) {
            resultMenus.add(menu);
            findAllParentNodes(menu, allMenus, resultMenus);
        }

        // 4. 构建树形结构（buildTree内部已递归排序所有节点）
        List<SysMenu> treeList = Optional.ofNullable(buildTree(new ArrayList<>(resultMenus))).orElse(new ArrayList<>());

        // 5. 创建分页对象
        long total = treeList.size();
        long pageNum = requestVo.getPageNum() != null ? requestVo.getPageNum() : 1L;
        long pageSize = requestVo.getPageSize() != null ? requestVo.getPageSize() : 10L;

        // 6. 对树形列表进行分页（只对顶级节点分页）
        int start = (int) ((pageNum - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, total);
        List<SysMenu> pagedList = start < total ? treeList.subList(start, end) : new ArrayList<>();

        // 7. 创建并返回分页对象
        Page<SysMenu> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pagedList);
        return page;
    }

    /**
     * 递归查找某个节点的所有上级节点
     */
    private void findAllParentNodes(SysMenu menu, List<SysMenu> allMenus, Set<SysMenu> result) {
        if (menu.getParentId() == null || menu.getParentId() == 0) {
            return;
        }

        allMenus.stream()
                .filter(m -> m.getId().equals(menu.getParentId()))
                .findFirst()
                .ifPresent(parent -> {
                    result.add(parent);
                    findAllParentNodes(parent, allMenus, result);
                });
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, Function.identity()));

        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                tree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        // 递归排序所有节点的子节点
        sortTree(tree);
        return tree;
    }

    /**
     * 递归排序树形结构的所有节点的子节点
     */
    private void sortTree(List<SysMenu> tree) {
        if (tree == null || tree.isEmpty()) {
            return;
        }
        // 对当前层级的节点按sort排序
        tree.sort(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparing(SysMenu::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))));
        // 递归排序每个节点的子节点
        for (SysMenu menu : tree) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortTree(menu.getChildren());
            }
        }
    }

    @Override
    public void insert(RequestVo<SysMenu> requestVo) {
        sysMenuMapper.insertOrUpdate(requestVo.getData());
    }

    @Override
    public void update(RequestVo<SysMenu> requestVo) {
        sysMenuMapper.insertOrUpdate(requestVo.getData());
    }

    @Override
    public SysMenu info(Long id) {
        return sysMenuMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        // 使用Set记录已访问的菜单ID，防止循环引用导致的死循环
        Set<Long> visitedIds = new HashSet<>();
        // 设置最大递归深度，防止异常数据导致无限递归
        int maxDepth = 10;
        removeRecursive(id, visitedIds, maxDepth);
    }

    /**
     * 递归删除菜单及其子菜单
     * @param id 菜单ID
     * @param visitedIds 已访问的菜单ID集合，用于防止循环引用
     * @param maxDepth 最大递归深度
     */
    private void removeRecursive(Long id, Set<Long> visitedIds, int maxDepth) {
        // 防止死循环：如果已经访问过该ID，直接返回
        if (visitedIds.contains(id)) {
            return;
        }
        
        // 防止无限递归：如果达到最大深度，停止递归
        if (maxDepth <= 0) {
            return;
        }
        
        // 标记当前ID为已访问
        visitedIds.add(id);
        
        // 查找所有子菜单
        List<SysMenu> children = sysMenuMapper.selectList(
            Wrappers.lambdaQuery(SysMenu.class)
                .eq(SysMenu::getParentId, id)
        );
        
        // 递归删除所有子菜单
        if (children != null && !children.isEmpty()) {
            for (SysMenu child : children) {
                removeRecursive(child.getId(), visitedIds, maxDepth - 1);
            }
        }
        
        // 删除当前菜单
        sysMenuMapper.deleteById(id);
    }
}