package com.zyfan.system.controller;

import com.zyfan.anno.Permission;
import com.zyfan.system.entity.SysUserRole;
import com.zyfan.system.service.ISysUserRoleService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
* 用户角色关联表
*
*/
@RestController
@RequestMapping("/sysUserRole")
public class SysUserRoleController {

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Permission("system:user:bindRole")
    @PostMapping("/page")
    public ResponseVo<List<SysUserRole>> page(@RequestBody RequestVo<SysUserRole> requestVo) {
        return ResponseVo.success(sysUserRoleService.pageList(requestVo));
    }

    @Permission("system:user:bindRole")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<SysUserRole> requestVo) {
        sysUserRoleService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:user:bindRole")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<SysUserRole> requestVo) {
        sysUserRoleService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:user:bindRole")
    @PostMapping("/info/{id}")
    public ResponseVo<SysUserRole> info(@PathVariable("id") Long id) {
        return ResponseVo.success(sysUserRoleService.info(id));
    }

    @Permission("system:user:bindRole")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        sysUserRoleService.remove(id);
        return ResponseVo.success();
    }

}