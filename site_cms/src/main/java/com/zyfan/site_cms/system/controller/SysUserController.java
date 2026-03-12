package com.zyfan.site_cms.system.controller;

import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.system.service.ISysUserService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
* 系统用户表
*
*/
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Permission("system:user:page")
    @PostMapping("/page")
    public ResponseVo<List<UserVo>> page(@RequestBody RequestVo<UserVo> requestVo, HttpServletRequest request) {
        return ResponseVo.success(sysUserService.pageList(requestVo,request));
    }

    @Permission("system:user:view")
    @PostMapping("/info/{id}")
    public ResponseVo<UserVo> info(@PathVariable("id") Long id, HttpServletRequest request) {
        return ResponseVo.success(sysUserService.info(id,request));
    }

    /**
     * 通过模糊用户名查询用户ID集合（供筛选使用）
     */
    @PostMapping("/queryIdsByUsername")
    public ResponseVo<List<Long>> queryIdsByUsername(@RequestBody RequestVo<String> requestVo,HttpServletRequest request) {
        String username = requestVo.getData();
        return ResponseVo.success(sysUserService.queryIdsByUsername(username,request));
    }

}