package com.zyfan.site_cms.system.service;

import com.zyfan.site_cms.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

public interface ISysUserRoleService extends IService<SysUserRole> {

    IPage<SysUserRole> pageList(RequestVo<SysUserRole> requestVo);

    void insert(RequestVo<SysUserRole> requestVo);

    void update(RequestVo<SysUserRole> requestVo);

    SysUserRole info(Long id);

    void remove(Long id);

}