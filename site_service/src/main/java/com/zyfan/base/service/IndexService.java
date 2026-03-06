package com.zyfan.base.service;

import com.zyfan.pojo.web.RequestVo;
import com.zyfan.vo.TokenVo;
import com.zyfan.vo.UserInfo;
import com.zyfan.vo.UserPasswordVo;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface IndexService {

    /**
     * 接收第三方平台的POST表单，将userToken和redirectUrl存入Redis并重定向
     */
    void page(String userToken, String redirectUrl, String originUrl, HttpServletResponse response) throws IOException;

    /**
     * 根据UUID获取缓存的userToken和redirectUrl
     */
    java.util.Map<String, String> getCachedTokenAndRedirectUrl(String uuid);

    /**
     * 获取当前登录用户信息
     */
    UserInfo userInfo(HttpServletRequest request);

    /**
     * 修改当前登录用户基本信息
     */
    Boolean updateUserInfo(UserInfo userInfo, HttpServletRequest request);

    /**
     * 修改当前登录用户密码
     */
    Boolean updateUserPassword(UserPasswordVo userPasswordVo, HttpServletRequest request);

    TokenVo login(RequestVo<String> requestVo, HttpServletRequest request);

    void logout(HttpServletRequest request);

    /**
     * 修改当前登录用户头像
     */
    Boolean updateAvatar(MultipartFile avatar, HttpServletRequest request);
}
