package com.zyfan.system.service.impl;

import com.zyfan.system.entity.SysRoleMenu;
import com.zyfan.system.entity.SysUserRole;
import com.zyfan.system.mapper.SysUserRoleMapper;
import com.zyfan.system.service.ISysUserRoleService;
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
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public IPage<SysUserRole> pageList(RequestVo<SysUserRole> requestVo) {
        return sysUserRoleMapper.selectPage(new Page<>(requestVo.getPageNum(), requestVo.getPageSize()),
                Wrappers.lambdaQuery(SysUserRole.class)
                        .eq(Objects.nonNull(requestVo.getData().getUserId()), SysUserRole::getUserId, requestVo.getData().getUserId())

        );
    }

    @Transactional
    @Override
    public void insert(RequestVo<SysUserRole> requestVo) {
        if (Objects.isNull(requestVo.getData().getUserId())) {
            return;
        }
        List<SysUserRole> userRoles = Optional.ofNullable(requestVo.getData().getRoleIds()).orElse(new ArrayList<>())
                .stream().map(roleId -> {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(requestVo.getData().getUserId());
                    sysUserRole.setRoleId(roleId);
                    return sysUserRole;
                }).toList();
        if (userRoles!=null && userRoles.size()>0) {
            this.saveBatch(userRoles);
        }
    }

    @Transactional
    @Override
    public void update(RequestVo<SysUserRole> requestVo) {
        if (Objects.isNull(requestVo.getData().getUserId())) {
            return;
        }
        // 先删除原有关联
        sysUserRoleMapper.delete(Wrappers.lambdaQuery(SysUserRole.class)
                .eq(SysUserRole::getUserId, requestVo.getData().getUserId()));
        List<SysUserRole> userRoles = Optional.ofNullable(requestVo.getData().getRoleIds()).orElse(new ArrayList<>())
                .stream().map(roleId -> {
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setUserId(requestVo.getData().getUserId());
                    sysUserRole.setRoleId(roleId);
                    return sysUserRole;
                }).toList();
        if (userRoles!=null && userRoles.size()>0) {
            this.saveBatch(userRoles);
        }
    }

    @Override
    public SysUserRole info(Long id) {
        return sysUserRoleMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        sysUserRoleMapper.deleteById(id);
    }
}