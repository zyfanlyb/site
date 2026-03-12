package com.zyfan.site_cms.system.controller;

import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.interceptor.AuthInterceptor;
import com.zyfan.site_cms.system.entity.SysMenu;
import com.zyfan.site_cms.system.mapper.SysMenuMapper;
import com.zyfan.site_cms.system.service.ISysMenuService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* 菜单权限表
*
*/
@RestController
@RequestMapping("/sysMenu")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @GetMapping("/userMenus")
    public ResponseVo<List<SysMenu>> getUserMenus() {
        return ResponseVo.success(sysMenuService.getUserMenus(AuthInterceptor.getCurrentUserId()));
    }

    @Permission("system:menu:page")
    @PostMapping("/page")
    public ResponseVo<List<SysMenu>> page(@RequestBody RequestVo<SysMenu> requestVo) {
        return ResponseVo.success(sysMenuService.getMenuTree(requestVo));
    }

    @Permission("system:menu:add")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<SysMenu> requestVo) {
        sysMenuService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:menu:edit")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<SysMenu> requestVo) {
        sysMenuService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:menu:view")
    @PostMapping("/info/{id}")
    public ResponseVo<SysMenu> info(@PathVariable("id") Long id) {
        return ResponseVo.success(sysMenuService.info(id));
    }

    @Permission("system:menu:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        sysMenuService.remove(id);
        return ResponseVo.success();
    }

    @Permission("system:menu:page")
    @PostMapping("/listAll")
    public ResponseVo<List<SysMenu>> listAll() {
        return ResponseVo.success(sysMenuService.getAllMenus());
    }

}