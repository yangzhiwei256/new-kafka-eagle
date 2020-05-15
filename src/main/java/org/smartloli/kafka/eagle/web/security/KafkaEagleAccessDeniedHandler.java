package org.smartloli.kafka.eagle.web.security;

import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ZHIWEI.YANG
 * @createtime 2019/1/9 - 10:32
 * @decription 自定义未授权页面
 **/
@Component
public class KafkaEagleAccessDeniedHandler implements AccessDeniedHandler {

    // 重定向策略
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        redirectStrategy.sendRedirect(request, response, HttpConstants.ERROR_403);
    }
}
