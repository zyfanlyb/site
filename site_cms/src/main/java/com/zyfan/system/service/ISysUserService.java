package com.zyfan.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ISysUserService {

    IPage<UserVo> pageList(RequestVo<UserVo> requestVo, HttpServletRequest request);

    UserVo info(Long id, HttpServletRequest request);

    /**
     * 通过模糊用户名查询用户ID集合
     *
     * @param username 用户名（支持模糊查询）
     * @param request
     * @return 用户ID集合
     */
    List<Long> queryIdsByUsername(String username, HttpServletRequest request);

}