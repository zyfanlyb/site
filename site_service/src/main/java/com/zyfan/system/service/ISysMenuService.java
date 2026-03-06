package com.zyfan.system.service;

import com.zyfan.system.entity.SysMenu;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.pojo.web.RequestVo;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    IPage<SysMenu> getMenuTree(RequestVo<SysMenu> requestVo);

    List<SysMenu> getAllMenus();

    List<SysMenu> getUserMenus(Long userId);

    void insert(RequestVo<SysMenu> requestVo);

    void update(RequestVo<SysMenu> requestVo);

    SysMenu info(Long id);

    void remove(Long id);

}