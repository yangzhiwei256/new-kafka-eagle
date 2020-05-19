package org.smartloli.kafka.eagle.web.security;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录成功处理器(类似拦截器)
 *
 * 作用：初始化默认Kafka集群节点
 */
@Component
@Slf4j
public class DefineAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${kafka.eagle.version:}")
    private String version;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        User user = (User) authentication.getPrincipal();
        log.info("用户【{}】认证成功", user.getUsername());
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(KafkaConstants.LOGIN_USER_NAME, user.getUsername());
        httpSession.setAttribute(KafkaConstants.SYSTEM_VERSION, version);

        Collection<GrantedAuthority> grantedAuthorityList = (Collection<GrantedAuthority>) authentication.getAuthorities();
        List<String> grantedAuthorities = grantedAuthorityList.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        //设置系统管理员标识
        if(grantedAuthorities.contains(KafkaConstants.SYSTEM)){
            httpSession.setAttribute(KafkaConstants.IF_SYSTEM_ADMIN, KafkaConstants.ADMINISTRATOR);
        }

        Object object = httpSession.getAttribute(KafkaConstants.CLUSTER_ALIAS);
        if (object == null) {
            List<String> clusterAliasList = kafkaClustersConfig.getClusterAllAlias();
            String defaultClusterAlias = clusterAliasList.get(0);
            httpSession.setAttribute(KafkaConstants.CLUSTER_ALIAS, defaultClusterAlias);
            clusterAliasList.remove(defaultClusterAlias);
            httpSession.setAttribute(KafkaConstants.CLUSTER_ALIAS_LIST, clusterAliasList);
        }
        response.sendRedirect(HttpConstants.INDEX_URL);
    }
}