package org.smartloli.kafka.eagle.web.security;

import org.smartloli.kafka.eagle.web.pojo.AuthorityInfo;
import org.smartloli.kafka.eagle.web.pojo.UserInfo;
import org.smartloli.kafka.eagle.web.service.AuthorityService;
import org.smartloli.kafka.eagle.web.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证服务
 */
@Component
public class KafkaEagleUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 获取用户名绑定用户信息
     * @param username 用户名
     * @return 用户认证信息
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //查询用户信息
        User.UserBuilder userBuilder = User.builder().username(username);
        UserInfo userInfo = userInfoService.queryUserByName(username);
        if(null == userInfo){
            userBuilder.password(null);
            userBuilder.authorities(Collections.emptyList());
            return userBuilder.build();
        }

        //设置密码
        userBuilder.password(userInfo.getPassword());

        //查询授权信息
        List<AuthorityInfo> authorityInfoList = authorityService.findAuthorityInfoByUserId(userInfo.getId());
        if(CollectionUtils.isEmpty(authorityInfoList)){
            return userBuilder.build();
        }

        List<String> roleNameList = authorityInfoList.stream().map(AuthorityInfo::getName).collect(Collectors.toList());
        List<GrantedAuthority> grantedAuthorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils.collectionToDelimitedString(roleNameList,","));
        userBuilder.authorities(grantedAuthorityList);
        return userBuilder.build();
    }
}