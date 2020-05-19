package org.smartloli.kafka.eagle.web.security;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 认证失败处理器
 * @author zhiwei_yang
 * @time 2020-4-28-8:48
 */
@Component
@Slf4j
public class DefineAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("用户登陆认证失败", exception);
        HttpSession httpSession = request.getSession(false);
        httpSession.setAttribute(KafkaConstants.ERROR_DISPLAY, true);
        httpSession.setAttribute(KafkaConstants.ERROR_LOGIN, "Account or password is error .");
        response.sendRedirect(HttpConstants.LOGIN_URL);
    }
}
