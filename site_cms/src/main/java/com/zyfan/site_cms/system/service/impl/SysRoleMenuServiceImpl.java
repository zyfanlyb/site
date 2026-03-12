package com.zyfan.site_cms.system.service.impl;

import com.zyfan.site_cms.system.entity.SysRoleMenu;
import com.zyfan.site_cms.system.mapper.SysRoleMenuMapper;
import com.zyfan.site_cms.system.service.ISysRoleMenuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper,SysRoleMenu> implements ISysRoleMenuService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public IPage<SysRoleMenu> pageList(RequestVo<SysRoleMenu> requestVo) {
        return sysRoleMenuMapper.selectPage(new Page<>(requestVo.getPageNum(), requestVo.getPageSize()),
            Wrappers.lambdaQuery(SysRoleMenu.class)
                    .eq(Objects.nonNull(requestVo.getData().getRoleId()), SysRoleMenu::getRoleId, requestVo.getData().getRoleId())
        );
    }

    @Transactional
    @Override
    public void insert(RequestVo<SysRoleMenu> requestVo) {
        if (Objects.isNull(requestVo.getData().getRoleId())) {
            return;
        }
        List<SysRoleMenu> roleMenus = Optional.ofNullable(requestVo.getData().getMenuIds()).orElse(new ArrayList<>())
                .stream().map(menuId -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(requestVo.getData().getRoleId());
                    sysRoleMenu.setMenuId(menuId);
                    return sysRoleMenu;
                }).toList();
        if (roleMenus!=null && roleMenus.size()>0) {
            this.saveBatch(roleMenus);
        }
    }

    @Transactional
    @Override
    public void update(RequestVo<SysRoleMenu> requestVo) {
        if (Objects.isNull(requestVo.getData().getRoleId())) {
            return;
        }
        // 先删除原有关联
        sysRoleMenuMapper.delete(Wrappers.lambdaQuery(SysRoleMenu.class)
                .eq(SysRoleMenu::getRoleId, requestVo.getData().getRoleId()));
        List<SysRoleMenu> roleMenus = Optional.ofNullable(requestVo.getData().getMenuIds()).orElse(new ArrayList<>())
                .stream().map(menuId -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(requestVo.getData().getRoleId());
                    sysRoleMenu.setMenuId(menuId);
                    return sysRoleMenu;
                }).toList();
        if (roleMenus!=null && roleMenus.size()>0) {
            this.saveBatch(roleMenus);
        }
    }

    @Override
    public SysRoleMenu info(Long id) {
        return sysRoleMenuMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        sysRoleMenuMapper.deleteById(id);
    }
}