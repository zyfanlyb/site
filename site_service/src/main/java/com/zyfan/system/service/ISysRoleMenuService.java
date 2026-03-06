package com.zyfan.system.service;

import com.zyfan.system.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

public interface ISysRoleMenuService extends IService<SysRoleMenu> {

    IPage<SysRoleMenu> pageList(RequestVo<SysRoleMenu> requestVo);

    void insert(RequestVo<SysRoleMenu> requestVo);

    void update(RequestVo<SysRoleMenu> requestVo);

    SysRoleMenu info(Long id);

    void remove(Long id);

}