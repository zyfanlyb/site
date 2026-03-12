package com.zyfan.site_cms.system.controller;

import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.system.entity.SysRole;
import com.zyfan.site_cms.system.service.ISysRoleService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
* 系统角色表
*
*/
@RestController
@RequestMapping("/sysRole")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Permission("system:role:page")
    @PostMapping("/page")
    public ResponseVo<List<SysRole>> page(@RequestBody RequestVo<SysRole> requestVo) {
        return ResponseVo.success(sysRoleService.pageList(requestVo));
    }

    @Permission("system:role:add")
    @PostMapping("/insert")
    public ResponseVo insert(@RequestBody RequestVo<SysRole> requestVo) {
        sysRoleService.insert(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:role:edit")
    @PostMapping("/update")
    public ResponseVo update(@RequestBody RequestVo<SysRole> requestVo) {
        sysRoleService.update(requestVo);
        return ResponseVo.success();
    }

    @Permission("system:role:view")
    @PostMapping("/info/{id}")
    public ResponseVo<SysRole> info(@PathVariable("id") Long id) {
        return ResponseVo.success(sysRoleService.info(id));
    }

    @Permission("system:role:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        sysRoleService.remove(id);
        return ResponseVo.success();
    }

}