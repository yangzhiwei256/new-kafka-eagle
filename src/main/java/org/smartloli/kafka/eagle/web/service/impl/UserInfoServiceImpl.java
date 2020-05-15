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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.web.dao.UserDao;
import org.smartloli.kafka.eagle.web.pojo.UserInfo;
import org.smartloli.kafka.eagle.web.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Handling requests for login, reset password, logout, etc.
 * 
 * @author smartloli.
 *
 *         Created by May 17, 2017
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

	@Autowired
	private UserDao userDao;

	@Override
	public List<UserInfo> findUserBySearch(Map<String, Object> params) {
		return userDao.findUserBySearch(params);
	}

	@Override
	public int userCounts() {
		return userDao.userCounts();
	}

	@Override
	public int insertUser(UserInfo userInfo) {
		return userDao.insertUser(userInfo);
	}

	@Override
	public int resetPassword(UserInfo signin) {
		return userDao.resetPassword(signin);
	}

	@Override
	public int modify(UserInfo userInfo) {
		return userDao.update(userInfo);
	}

	@Override
	public int delete(UserInfo signin) {
		return userDao.delete(signin);
	}

	@Override
	public String findUserById(int userId) {
		UserInfo signer = userDao.findUserById(userId);
		JSONObject object = new JSONObject();
		object.put("rtxno", signer.getRtxno());
		object.put("username", signer.getUsername());
		object.put("realname", signer.getRealname());
		object.put("email", signer.getEmail());
		return object.toJSONString();
	}

	@Override
	public String getAutoUserRtxNo() {
		UserInfo signer = userDao.findUserLimitOne();
		JSONObject object = new JSONObject();
		object.put("rtxno", signer.getRtxno()+1);
		return object.toJSONString();
	}

    @Override
    public UserInfo queryUserByName(String username) {
        return userDao.findUserByName(username);
    }

}
