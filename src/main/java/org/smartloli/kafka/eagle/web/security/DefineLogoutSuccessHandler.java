package org.smartloli.kafka.eagle.web.security;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 退出登陆成功处理器
 *
 * 作用：清理用户相关资源
 * @author zhiwei_yang
 * @time 2020-4-28-8:59
 */
@Component
@Slf4j
public class DefineLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if(null != authentication.getPrincipal()) {
            log.info("用户【{}】退出登陆成功", ((User) authentication.getPrincipal()).getUsername());
        }
        request.getSession().invalidate();
        response.sendRedirect(HttpConstants.LOGIN_URL);
    }
}
