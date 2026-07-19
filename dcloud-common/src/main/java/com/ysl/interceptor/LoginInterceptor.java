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

/**
 * 登录拦截器。
 * <p>
 * 这个拦截器的职责是：
 * 1. 在请求进入 Controller 之前校验客户端传入的 token；
 * 2. 如果 token 合法，则从 JWT 中解析出当前登录用户信息；
 * 3. 将当前请求对应的用户信息放入 ThreadLocal，方便后续业务代码直接获取；
 * 4. 在请求处理完成后清理 ThreadLocal，避免线程复用导致用户信息串用。
 * <p>
 * 注意：ThreadLocal 只是“当前请求处理期间”的临时上下文，并不是用户登录态的永久存储位置。
 * 用户是否处于登录状态，核心还是取决于客户端携带的 token 是否合法、是否过期。
 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 保存当前请求对应的登录用户信息。
     * <p>
     * 这里虽然是一个 static 变量，但真正保存的数据是“按线程隔离”的：
     * 不同线程调用 threadLocal.set(...) 时，各自保存的是自己线程里的值，互不影响。
     * 因此它适合在一次请求链路内，给 Controller / Service / Manager 传递当前登录用户。
     */
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(
            @SuppressWarnings("null") HttpServletRequest request,
            HttpServletResponse response,
            Object handler)
            throws Exception {

        // 浏览器跨域时，可能会先发送 OPTIONS 预检请求。
        // 这类请求只用于探测服务端是否允许跨域，不代表真实的业务访问，因此直接放行。
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }

        // 优先从请求头中获取 token，这是前后端分离场景中最常见的传递方式。
        String accessToken = request.getHeader("token");

        // 如果请求头没有，再尝试从请求参数中获取，兼容少量通过 URL/Form 传 token 的调用方式。
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }

        if (StringUtils.isNotBlank(accessToken)) {
            // 校验 JWT 的合法性。
            // checkJWT 内部通常会完成签名校验、有效期校验、格式校验等工作。
            Claims claims = JWTUtil.checkJWT(accessToken);
            if (claims == null) {
                // token 无效或已过期，直接返回“未登录”结果，请求不再继续进入后续业务逻辑。
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            // token 校验通过后，从 JWT 中提取当前用户的关键信息。
            // 这些字段在登录签发 token 时就已经写入 claims 中了。
            Long accountNo = Long.parseLong(claims.get("account_no").toString());
            String headImg = (String) claims.get("head_img");
            String username = (String) claims.get("username");
            String mail = (String) claims.get("mail");
            String phone = (String) claims.get("phone");
            String auth = (String) claims.get("auth");

            // 将 JWT 中的用户信息组装成项目内部统一使用的登录用户对象。
            // 后续业务层如果需要当前登录用户，可以直接从 ThreadLocal 中获取，而不必重复解析 token。
            LoginUser loginUser = LoginUser.builder()
                    .accountNo(accountNo)
                    .auth(auth)
                    .phone(phone)
                    .headImg(headImg)
                    .mail(mail)
                    .username(username)
                    .build();

            // 将当前请求对应的登录用户保存到当前线程的上下文中。
            // 这样在同一条请求链路里的其他位置就能通过 threadLocal.get() 拿到当前用户。
            threadLocal.set(loginUser);
            return true;
        }

        // 没有携带 token 时，返回与 token 无效一致的标准未登录响应。
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        // 请求处理完成后必须清理 ThreadLocal。
        // Web 容器中的工作线程通常来自线程池，会被后续请求复用。
        // 如果不 remove，下一次复用这个线程的请求就有可能读到上一个用户的数据，造成严重的串号问题。
        threadLocal.remove();
    }

}
