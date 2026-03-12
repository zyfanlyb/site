package com.zyfan.site_cms.system.service;

import com.zyfan.site_cms.system.entity.SysRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

public interface ISysRoleService extends IService<SysRole> {

    IPage<SysRole> pageList(RequestVo<SysRole> requestVo);

    void insert(RequestVo<SysRole> requestVo);

    void update(RequestVo<SysRole> requestVo);

    SysRole info(Long id);

    void remove(Long id);

}