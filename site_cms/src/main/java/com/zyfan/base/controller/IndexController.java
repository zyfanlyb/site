package com.zyfan.base.controller;

import com.zyfan.pojo.web.RequestVo;
import com.zyfan.vo.TokenVo;
import com.zyfan.vo.UserInfo;
import com.zyfan.vo.UserPasswordVo;
import com.zyfan.base.service.IndexService;
import com.zyfan.pojo.web.ResponseVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequestMapping
@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 接收第三方平台的POST表单，将userToken和redirectUrl存入Redis并重定向
     */
    @PostMapping("/noAuth/page")
    public void page(
            @RequestParam("userToken") String userToken,
            @RequestParam("redirectUrl") String redirectUrl,
            @RequestParam("originUrl") String originUrl,
            HttpServletResponse response) throws IOException {
        indexService.page(userToken, redirectUrl, originUrl, response);
    }

    /**
     * 根据UUID获取缓存的userToken和redirectUrl
     */
    @PostMapping("/noAuth/getCachedTokenAndRedirectUrl")
    public ResponseVo<Map<String, String>> getCachedTokenAndRedirectUrl(@RequestParam String uuid) {
        return ResponseVo.success(indexService.getCachedTokenAndRedirectUrl(uuid));
    }

    /**
     * 获取用户信息
     */
    @PostMapping("/user/info")
    public ResponseVo<UserInfo> userInfo(HttpServletRequest request) {
        return ResponseVo.success(indexService.userInfo(request));
    }

    /**
     * 修改当前登录用户基本信息
     */
    @PostMapping("/user/updateInfo")
    public ResponseVo<Boolean> updateUser(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        return ResponseVo.success(indexService.updateUserInfo(userInfo, request));
    }

    /**
     * 修改当前登录用户密码
     */
    @PostMapping("/user/updatePassword")
    public ResponseVo<Boolean> updateUserPassword(@RequestBody UserPasswordVo userPasswordVo, HttpServletRequest request) {
        return ResponseVo.success(indexService.updateUserPassword(userPasswordVo, request));
    }

    /**
     * 登入
     * @param requestVo
     * @param request
     * @return
     */
    @PostMapping("/noAuth/login")
    public ResponseVo<TokenVo> login(@RequestBody RequestVo<String> requestVo, HttpServletRequest request) {
        return ResponseVo.success(indexService.login(requestVo,request));
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseVo logout(HttpServletRequest request) {
        indexService.logout(request);
        return ResponseVo.success();
    }

    /**
     * 修改当前登录用户头像
     */
    @PostMapping("/user/updateAvatar")
    public ResponseVo<Boolean> updateAvatar(@RequestParam("avatar") MultipartFile avatar, HttpServletRequest request) {
        return ResponseVo.success(indexService.updateAvatar(avatar, request));
    }
}
