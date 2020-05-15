package org.smartloli.kafka.eagle.web.security;

import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * 开启spring security安全认证
 */
@EnableWebSecurity
public class KafkaEagleSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DefineAuthenticationSuccessHandler defineAuthenticationSuccessHandler;
    @Autowired
    private KafkaEagleAccessDeniedHandler kafkaEagleAccessDeniedHandler;
    @Autowired
    private DefineAuthenticationFailureHandler defineAuthenticationFailureHandler;
    @Autowired
    private DefineLogoutSuccessHandler defineLogoutSuccessHandler;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    /**
     * 表单登录认证
     * @param httpSecurity
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        //认证配置
        httpSecurity.authorizeRequests()
                .antMatchers(HttpConstants.LOGIN_URL, HttpConstants.LOGIN_ACTION_URL, HttpConstants.LOGOUT_URL).permitAll()
                .antMatchers(HttpConstants.STATIC_RESOURCE_PATTERN_ARRAY).permitAll()
                .anyRequest().authenticated();

        //表单配置
        httpSecurity.formLogin()
                .usernameParameter(HttpConstants.LOGIN_USERNAME)
                .passwordParameter(HttpConstants.LOGIN_PASSWORD)
                .loginPage(HttpConstants.LOGIN_URL)
                .loginProcessingUrl(HttpConstants.LOGIN_ACTION_URL)
                .successHandler(defineAuthenticationSuccessHandler)
                .failureHandler(defineAuthenticationFailureHandler);

        // 退出登陆
        httpSecurity.logout()
                .logoutUrl(HttpConstants.LOGOUT_URL)
                .logoutSuccessHandler(defineLogoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true);

        httpSecurity.csrf().disable();
        httpSecurity.exceptionHandling().accessDeniedHandler(kafkaEagleAccessDeniedHandler);
        httpSecurity.rememberMe()
                .tokenRepository(persistentTokenRepository)
                .tokenValiditySeconds(600);
    }
}