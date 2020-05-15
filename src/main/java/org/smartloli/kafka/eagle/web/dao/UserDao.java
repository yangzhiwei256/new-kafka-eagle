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
package org.smartloli.kafka.eagle.web.dao;

import org.apache.ibatis.annotations.Param;
import org.smartloli.kafka.eagle.web.pojo.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * User interface definition
 * 
 * @author smartloli.
 *
 *         Created by May 16, 2017
 */
public interface UserDao {
	
	int resetPassword(UserInfo signin);

	List<UserInfo> findUserBySearch(Map<String, Object> params);

	int userCounts();

    /**
     * 新增用户
     * @param userInfo 用户信息
     * @return
     */
	int insertUser(UserInfo userInfo);

    /**
     * 更新用户信息
     * @param userInfo
     * @return
     */
	int update(UserInfo userInfo);

    /**
     * 删除用户信息
     * @param userInfo
     * @return
     */
	int delete(UserInfo userInfo);

    /**
     * 通过用户ID查询用户信息
     * @param userId
     * @return
     */
	UserInfo findUserById(int userId);
	
	UserInfo findUserLimitOne();

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    UserInfo findUserByName(@Param("username") String username);
}
