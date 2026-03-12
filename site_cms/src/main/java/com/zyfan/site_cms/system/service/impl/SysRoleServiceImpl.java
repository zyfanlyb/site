package com.zyfan.site_cms.system.service.impl;

import com.zyfan.site_cms.system.entity.SysRole;
import com.zyfan.site_cms.system.mapper.SysRoleMapper;
import com.zyfan.site_cms.system.service.ISysRoleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper,SysRole> implements ISysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public IPage<SysRole> pageList(RequestVo<SysRole> requestVo) {
        SysRole queryParams = requestVo.getData();
        return sysRoleMapper.selectPage(new Page<>(requestVo.getPageNum(), requestVo.getPageSize()),
            Wrappers.lambdaQuery(SysRole.class)
                    .like(queryParams.getRoleName() != null, SysRole::getRoleName, queryParams.getRoleName())
                    .eq(queryParams.getStatus() != null, SysRole::getStatus, queryParams.getStatus())
        );
    }

    @Override
    public void insert(RequestVo<SysRole> requestVo) {
        sysRoleMapper.insertOrUpdate(requestVo.getData());
    }

    @Override
    public void update(RequestVo<SysRole> requestVo) {
        sysRoleMapper.insertOrUpdate(requestVo.getData());
    }

    @Override
    public SysRole info(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        sysRoleMapper.deleteById(id);
    }
}