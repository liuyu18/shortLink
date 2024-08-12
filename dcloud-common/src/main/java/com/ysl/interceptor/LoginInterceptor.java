package com.ysl.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang3.StringUtils;

import com.ysl.enums.BizCodeEnum;
import com.ysl.model.LoginUser;
import com.ysl.util.CommonUtil;
import com.ysl.util.JWTUtil;
import com.ysl.util.JsonData;

import io.jsonwebtoken.Claims;

public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(
            @SuppressWarnings("null") HttpServletRequest request,
            HttpServletResponse response,
            Object handler)
            throws Exception {

        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }

        String accessToken = request.getHeader("token");
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }

        if (StringUtils.isNotBlank(accessToken)) {
            Claims claims = JWTUtil.checkJWT(accessToken);
            if (claims == null) {
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }
            Long accountNo = Long.parseLong(claims.get("account_no").toString());
            String headImg = (String) claims.get("head_img");
            String username = (String) claims.get("username");
            String mail = (String) claims.get("mail");
            String phone = (String) claims.get("phone");
            String auth = (String) claims.get("auth");

            LoginUser loginUser = LoginUser.builder()
                    .accountNo(accountNo)
                    .auth(auth)
                    .phone(phone)
                    .headImg(headImg)
                    .mail(mail)
                    .username(username)
                    .build();
            threadLocal.set(loginUser);
            return true;
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        threadLocal.remove();
    }

}
