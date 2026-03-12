package com.zyfan.site_cms.system.service.impl;

import com.zyfan.client.AuthClient;
import com.zyfan.exception.ZException;
import com.zyfan.pojo.web.CodeStatusEnum;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.task.FlushAccessTokenTask;
import com.zyfan.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import com.zyfan.site_cms.system.service.ISysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private AuthClient authClient;
    @Autowired
    private FlushAccessTokenTask flushAccessTokenTask;

    @Override
    public IPage<UserVo> pageList(RequestVo<UserVo> requestVo, HttpServletRequest request) {
        ResponseVo<List<UserVo>> responseVo = null;
        try {
            responseVo = authClient.userPage(requestVo,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.userPage(requestVo,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户详情失败");
        }
        IPage<UserVo> page = new Page<>();
        page.setCurrent(responseVo.getPage().getPageNum());
        page.setSize(responseVo.getPage().getPageSize());
        page.setTotal(responseVo.getPage().getTotal());
        page.setRecords(responseVo.getData());
        return page;
    }

    @Override
    public UserVo info(Long id, HttpServletRequest request) {
        ResponseVo<UserVo> responseVo = null;
        try {
            responseVo = authClient.userInfo(id,request.getHeader("Authorization"),flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.userInfo(id,request.getHeader("userToken"),flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户详情失败");
        }
        return responseVo.getData();
    }

    @Override
    public List<Long> queryIdsByUsername(String username, HttpServletRequest request) {
        ResponseVo<List<Long>> responseVo = null;
        RequestVo<String> requestVo = new RequestVo<>();
        requestVo.setData(username);
        try {
            responseVo = authClient.queryUserIdsByUsername(requestVo,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.queryUserIdsByUsername(requestVo,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户详情失败");
        }
        return responseVo.getData();
    }
}