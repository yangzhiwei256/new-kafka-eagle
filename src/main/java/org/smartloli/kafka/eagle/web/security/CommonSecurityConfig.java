package org.smartloli.kafka.eagle.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

/**
 * @author ZHIWEI.YANG
 * @time 2019/12/2 - 23:05
 * @decription
 */
@Configuration
public class CommonSecurityConfig {

    /**
     * 密码加密/解密策略
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 持久化token: 保存到内存或数据库
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    /**
     * 角色层次
     * @return
     */
//    @Bean
//    public RoleHierarchy roleHierarchy(){
//        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//        roleHierarchy.setHierarchy("ROLE_SUPER > ROLE_ADMIN \n" +
//                                   "ROLE_ADMIN > ROLE_USER");
//        return roleHierarchy;
//    }

    /**
     * 权限继承投票器
     * @param roleHierarchy
     * @return
     */
//    @Bean
//    public RoleHierarchyVoter roleHierarchyVoter(RoleHierarchy roleHierarchy){
//        return new RoleHierarchyVoter(roleHierarchy);
//    }
}
