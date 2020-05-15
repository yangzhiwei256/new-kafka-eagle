/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.service;

import org.smartloli.kafka.eagle.web.pojo.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 * @author smartloli.
 * Created by May 16, 2017
 */
public interface UserInfoService {

	int resetPassword(UserInfo signin);

	List<UserInfo> findUserBySearch(Map<String, Object> params);

    /**
     * 统计用户数
     * @return
     */
	int userCounts();

    /**
     * 新增用户
     * @param userInfo
     * @return
     */
	int insertUser(UserInfo userInfo);

    /**
     * 修改用户信息
     * @param signin
     * @return
     */
	int modify(UserInfo signin);

    /**
     * 删除用户
     * @param signin
     * @return
     */
	int delete(UserInfo signin);

    /**
     * 通过用户ID查找用户信息
     * @param userId
     * @return
     */
	String findUserById(int userId);

    /**
     * 获取自动生成的用户账号
     * @return
     */
	String getAutoUserRtxNo();

    /**
     * 通过用户名查找用户信息
     * @param username
     * @return
     */
	UserInfo queryUserByName(String username);
}
