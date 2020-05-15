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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.web.dao.AuthorityDao;
import org.smartloli.kafka.eagle.web.dao.RoleDao;
import org.smartloli.kafka.eagle.web.pojo.AuthorityInfo;
import org.smartloli.kafka.eagle.web.pojo.RoleAuthorityInfo;
import org.smartloli.kafka.eagle.web.pojo.RoleInfo;
import org.smartloli.kafka.eagle.web.pojo.UserRoleInfo;
import org.smartloli.kafka.eagle.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handle actions such as adding, deleting, modifying, etc.
 * 
 * @author smartloli.
 *
 *         Created by May 24, 2017
 */
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleDao roleDao;
	@Autowired
	private AuthorityDao resDao;

	@Override
	public List<RoleInfo> getRoles() {
		return roleDao.getRoles();
	}

	@Override
	public String getRoleTree(int roleId) {
		List<RoleAuthorityInfo> roleAuthorityInfos = roleDao.findRoleResourcesByRoleId(roleId);
		List<AuthorityInfo> authorityInfos = resDao.getResourcesTree();
		Map<String, List<AuthorityInfo>> map = new HashMap<>();
		Map<Integer, String> parent = new HashMap<>();
		for (AuthorityInfo authorityInfo : authorityInfos) {
			if (authorityInfo.getParentId() == -1) {
				parent.put(authorityInfo.getResourceId(), authorityInfo.getName());
			}
		}
		for (AuthorityInfo authorityInfo : authorityInfos) {
			if (authorityInfo.getParentId() != -1) {
				if (map.containsKey(parent.get(authorityInfo.getParentId()))) {
					map.get(parent.get(authorityInfo.getParentId())).add(authorityInfo);
				} else {
					List<AuthorityInfo> subName = new ArrayList<>();
					subName.add(authorityInfo);
					map.put(parent.get(authorityInfo.getParentId()), subName);
				}
			}
		}
		JSONArray targets = new JSONArray();
		for (Entry<String, List<AuthorityInfo>> entry : map.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			subTarget.put("text", entry.getKey());
			for (AuthorityInfo authorityInfo : entry.getValue()) {
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("text", authorityInfo.getName());
				subInSubTarget.put("href", authorityInfo.getResourceId());
				if (roleAuthorityInfos != null && roleAuthorityInfos.size() > 0) {
					for (RoleAuthorityInfo roleAuthorityInfo : roleAuthorityInfos) {
						if (roleAuthorityInfo.getResourceId() == authorityInfo.getResourceId()) {
							JSONObject state = new JSONObject();
							state.put("checked", true);
							subInSubTarget.put("state", state);
							break;
						}
					}
				}
				subTargets.add(subInSubTarget);
			}
			subTarget.put("nodes", subTargets);
			targets.add(subTarget);
		}
		return targets.toJSONString();
	}

	@Override
	public int insertRoleResource(RoleAuthorityInfo roleAuthorityInfo) {
		return roleDao.insertRoleResource(roleAuthorityInfo);
	}

	@Override
	public int deleteRoleResource(RoleAuthorityInfo roleAuthorityInfo) {
		return roleDao.deleteRoleResource(roleAuthorityInfo);
	}

	@Override
	public List<UserRoleInfo> findRoleByUserId(int userId) {
		return roleDao.findRoleByUserId(userId);
	}

	@Override
	public int insertUserRole(UserRoleInfo userRoleInfo) {
		return roleDao.insertUserRole(userRoleInfo);
	}

	@Override
	public int deleteUserRole(UserRoleInfo userRoleInfo) {
		return roleDao.deleteUserRole(userRoleInfo);
	}

}
