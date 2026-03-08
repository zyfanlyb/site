package com.zyfan.system.controller;

import com.zyfan.anno.Permission;
import com.zyfan.system.entity.SysRoleMenu;
import com.zyfan.system.service.ISysRoleMenuService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
* 角色菜单关联表
*
*/
@RestController
@RequestMapping("/sysRoleMenu")
public class SysRoleMenuController {

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @Permission("system:role:bindMenu")
    @PostMapping("/page")
    public ResponseVo<List<SysRoleMenu>> page(@RequestBody RequestVo<SysRoleMenu> requestVo) {
        return ResponseVo.success(sysRoleMenuService.pageList(requestVo));
    }

    @Permission("system:role:bindMenu")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<SysRoleMenu> requestVo) {
        sysRoleMenuService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:role:bindMenu")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<SysRoleMenu> requestVo) {
        sysRoleMenuService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:role:bindMenu")
    @PostMapping("/info/{id}")
    public ResponseVo<SysRoleMenu> info(@PathVariable("id") Long id) {
        return ResponseVo.success(sysRoleMenuService.info(id));
    }

    @Permission("system:role:bindMenu")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        sysRoleMenuService.remove(id);
        return ResponseVo.success();
    }

}