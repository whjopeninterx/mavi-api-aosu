package com.openinterx.mavi.interceptor;

import com.openinterx.mavi.handler.UserSessionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final UserSessionHandler.User user = UserSessionHandler.getUser();
        user.setClientId("121321321312321");
        UserSessionHandler.setUser(user);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 一个请求结束后调用
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserSessionHandler.clear();
    }
}
