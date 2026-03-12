package com.zyfan.site_cms.interceptor;

import com.alibaba.fastjson2.JSON;
import com.zyfan.site_cms.anno.Permission;
import com.zyfan.client.AuthClient;
import com.zyfan.exception.ZException;
import com.zyfan.pojo.web.CodeStatusEnum;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.task.FlushAccessTokenTask;
import com.zyfan.vo.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    @Lazy
    private AuthClient authClient;

    @Autowired
    @Lazy
    private FlushAccessTokenTask flushAccessTokenTask;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission permission = handlerMethod.getMethodAnnotation(Permission.class);

        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未授权的访问");
            return false;
        }
        ResponseVo<Boolean> responseVo = null;
        try {
            responseVo = authClient.userVerify(token,flushAccessTokenTask.getAccessToken());
            if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseVo = authClient.userVerify(token,flushAccessTokenTask.getAccessToken());
        }
        if (responseVo.getCode() != CodeStatusEnum.SUCCESS.getCode()) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED,"获取用户信息失败");
        }
        if (!responseVo.getData()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "登录已失效");
            return false;
        }
        String userInfoStr = redisTemplate.opsForValue().get("userToken:" + token);
        if (StringUtils.isBlank(userInfoStr)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "获取用户信息失败");
            return false;
        }
        UserInfo userInfo = JSON.parseObject(userInfoStr, UserInfo.class);
        if (!userInfo.getAllowExternalUser()&&(userInfo.getUserType() || userInfo.getAppType())) {
            throw new ZException(CodeStatusEnum.UNAUTHORIZED, "无访问权限");
        }
        // 校验接口权限
        if (permission != null && StringUtils.isNotBlank(permission.value())) {
            String requiredPerm = permission.value();
            // 从UserInfo中获取按钮权限列表
            List<String> buttons = userInfo.getButtons();
            if (buttons == null || !buttons.contains(requiredPerm)) {
                throw new ZException(CodeStatusEnum.UNAUTHORIZED, "无访问权限");
            }
        }

        userThreadLocal.set(userInfo.getUserId());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        userThreadLocal.remove();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    public static Long getCurrentUserId() {
        return userThreadLocal.get();
    }
}
