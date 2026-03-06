package com.zyfan.base.service.impl;

import com.alibaba.fastjson2.JSON;
import com.zyfan.client.AuthClient;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.system.entity.SysMenu;
import com.zyfan.system.service.ISysMenuService;
import com.zyfan.vo.TokenVo;
import com.zyfan.vo.UserInfo;
import com.zyfan.vo.UserPasswordVo;
import com.zyfan.exception.ZException;
import com.zyfan.base.service.IndexService;
import com.zyfan.pojo.web.CodeStatusEnum;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.task.FlushAccessTokenTask;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AuthClient authClient;

    @Autowired
    private FlushAccessTokenTask flushAccessTokenTask;

    @Autowired
    private ISysMenuService sysMenuService;

    @Value("${userToken.expiration}")
    private long userTokenExpiration;

    @Value("${client_id}")
    private String clientId;
    @Value("${client_secret}")
    private String clientSecret;

    @Override
    public void page(String userToken, String redirectUrl, String originUrl, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(userToken)) {
            throw new ZException(CodeStatusEnum.FAILED, "userToken不能为空");
        }
        if (StringUtils.isBlank(redirectUrl)) {
            throw new ZException(CodeStatusEnum.FAILED, "redirectUrl不能为空");
        }
        if (StringUtils.isBlank(originUrl)) {
            throw new ZException(CodeStatusEnum.FAILED, "originUrl不能为空");
        }

        // 生成UUID作为key
        String uuid = UUID.randomUUID().toString();

        // 将userToken和redirectUrl存入Redis，缓存1分钟
        Map<String, String> cacheData = new HashMap<>();
        cacheData.put("userToken", userToken);
        cacheData.put("redirectUrl", originUrl);
        String cacheKey = "thirdPartyToken:" + uuid;
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(cacheData), 1, TimeUnit.MINUTES);

        // 重定向到前端index页面，携带UUID参数
        String frontendUrl = redirectUrl.replaceFirst("(https?://[^:/]+(?::\\d+)?).*", "$1")+"/site/?uuid=" + URLEncoder.encode(uuid, "UTF-8");
        response.sendRedirect(frontendUrl);
    }

    @Override
    public Map<String, String> getCachedTokenAndRedirectUrl(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new ZException(CodeStatusEnum.FAILED, "uuid不能为空");
        }

        String cacheKey = "thirdPartyToken:" + uuid;
        String cacheData = redisTemplate.opsForValue().get(cacheKey);

        if (StringUtils.isBlank(cacheData)) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED, "缓存已过期或不存在");
        }
        // 删除已使用的缓存（一次性使用）
        redisTemplate.delete(cacheKey);

        // 解析JSON数据
        Map<String, String> result = JSON.parseObject(cacheData, Map.class);

        //缓存用户按钮权限
        String userToken = result.get("userToken");
        cacheUserInfo(userToken);
        return result;
    }

    @Override
    public UserInfo userInfo(HttpServletRequest request) {
        ResponseVo<UserInfo> responseVo = null;
        String token = request.getHeader("Authorization");
        try {
            responseVo = authClient.loginUserInfo(token,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.loginUserInfo(token,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户信息失败");
        }
        return responseVo.getData();
    }

    @Override
    public Boolean updateUserInfo(UserInfo userInfo, HttpServletRequest request) {
        if (userInfo == null || userInfo.getUserId() == null) {
            throw new ZException(CodeStatusEnum.FAILED, "用户信息不能为空");
        }
        ResponseVo<Boolean> responseVo = null;
        String token = request.getHeader("Authorization");
        try {
            responseVo = authClient.loginUserUpdate(userInfo, token, flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.loginUserUpdate(userInfo, token, flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()|| !responseVo.getData()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"更新用户信息失败");
        }
        return responseVo.getData();
    }

    @Override
    public Boolean updateUserPassword(UserPasswordVo userPasswordVo, HttpServletRequest request) {
        if (userPasswordVo == null
                || StringUtils.isBlank(userPasswordVo.getOldPassword())
                || StringUtils.isBlank(userPasswordVo.getNewPassword())) {
            throw new ZException(CodeStatusEnum.FAILED, "密码信息不能为空");
        }
        ResponseVo<Boolean> responseVo = null;
        String token = request.getHeader("Authorization");
        try {
            responseVo = authClient.loginUserUpdatePassword(userPasswordVo, token, flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.loginUserUpdatePassword(userPasswordVo, token, flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode() || !Boolean.TRUE.equals(responseVo.getData())) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED, responseVo.getMessage());
        }
        return responseVo.getData();
    }

    @Override
    public Boolean updateAvatar(MultipartFile avatar, HttpServletRequest request) {
        if (avatar == null || avatar.isEmpty()) {
            throw new ZException(CodeStatusEnum.FAILED, "头像文件不能为空");
        }
        ResponseVo<Boolean> responseVo = null;
        String token = request.getHeader("Authorization");
        try {
            responseVo = authClient.updateAvatar(avatar, token, flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.updateAvatar(avatar, token, flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED, "更新头像失败");
        }
        return responseVo.getData();
    }

    private void cacheUserInfo(String token) {
        ResponseVo<UserInfo> responseVo = null;
        try {
            responseVo = authClient.loginUserInfo(token,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.loginUserInfo(token,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户信息失败");
        }
        UserInfo userInfo = responseVo.getData();
        // 获取用户的按钮权限（type=2 的菜单，提取 perms 字段）
        List<SysMenu> userMenus = sysMenuService.getUserMenus(userInfo.getUserId());
        List<String> buttons = userMenus.stream()
                .filter(menu -> "1".equals(menu.getType())||"2".equals(menu.getType()) && StringUtils.isNotBlank(menu.getPerms()))
                .map(SysMenu::getPerms)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        userInfo.setButtons(buttons);
        redisTemplate.opsForValue().set("userToken:"+token, JSON.toJSONString(userInfo),userTokenExpiration+1000*60*60*2, TimeUnit.MILLISECONDS);
    }


    @Override
    public TokenVo login(RequestVo<String> requestVo, HttpServletRequest request) {
        if (StringUtils.isBlank(requestVo.getData())) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"授权码错误");
        }
        ResponseVo<TokenVo> responseVo = null;
        String realIp = request.getHeader("x-real-ip");
        try {
            responseVo = authClient.userToken(requestVo.getData(),clientId,clientSecret, realIp,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.userToken(requestVo.getData(),clientId,clientSecret, realIp,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"登录失败");
        }
        cacheUserInfo(responseVo.getData().getUserToken());
        return responseVo.getData();
    }

    @Override
    public void logout(HttpServletRequest request) {
        ResponseVo<UserInfo> responseVo = null;
        try {
            responseVo = authClient.userLogout(request.getHeader("Authorization"),flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.userLogout(request.getHeader("Authorization"),flushAccessTokenTask.getAccessToken());
        }
    }
}
