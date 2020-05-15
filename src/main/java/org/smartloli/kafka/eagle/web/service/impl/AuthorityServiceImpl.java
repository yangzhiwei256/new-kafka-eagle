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
import org.smartloli.kafka.eagle.web.pojo.AuthorityInfo;
import org.smartloli.kafka.eagle.web.service.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Handle, add, delete, modify, and other operations.
 * 
 * @author smartloli.
 *
 *         Created by May 18, 2017
 */
@Service
public class AuthorityServiceImpl implements AuthorityService {

	@Autowired
	private AuthorityDao resourcesDao;

	@Override
	public List<Integer> findRoleIdByUserId(int userId) {
		return resourcesDao.findRoleIdByUserId(userId);
	}

	@Override
	public List<AuthorityInfo> findAuthorityByRoleIds(List<Integer> roleIdList) {
		return resourcesDao.findAuthorityByRoleIds(roleIdList);
	}

	@Override
	public List<AuthorityInfo> findAuthorityInfoByUserId(int userId) {
		List<Integer> roleIds = resourcesDao.findRoleIdByUserId(userId);
		if(CollectionUtils.isEmpty(roleIds)){
		    return Collections.emptyList();
        }
		List<AuthorityInfo> authorityInfos = resourcesDao.findAuthorityByRoleIds(roleIds);
		if(CollectionUtils.isEmpty(authorityInfos)){
		    return Collections.emptyList();
        }
		return resourcesDao.findAuthorityInfoByIds(authorityInfos.stream().map(AuthorityInfo::getResourceId).collect(Collectors.toList()));
	}

	@Override
	public String getResourcesTree() {
		List<AuthorityInfo> authorityInfos = resourcesDao.getResourcesTree();
		Map<String, List<String>> map = new HashMap<>();
		Map<Integer, String> parent = new HashMap<>();
		for (AuthorityInfo authorityInfo : authorityInfos) {
			if (authorityInfo.getParentId() == -1) {
				parent.put(authorityInfo.getResourceId(), authorityInfo.getName());
			}
		}
		for (AuthorityInfo authorityInfo : authorityInfos) {
			if (authorityInfo.getParentId() != -1) {
				if (map.containsKey(parent.get(authorityInfo.getParentId()))) {
					map.get(parent.get(authorityInfo.getParentId())).add(authorityInfo.getName());
				} else {
					List<String> subName = new ArrayList<>();
					subName.add(authorityInfo.getName());
					map.put(parent.get(authorityInfo.getParentId()), subName);
				}
			}
		}

		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Home");
		for (Entry<String, List<String>> entry : map.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			subTarget.put("name", entry.getKey());
			for (String str : entry.getValue()) {
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", str);
				subTargets.add(subInSubTarget);
			}
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	@Override
	public int insertResource(Map<String, Object> params) {
		return resourcesDao.insertResource(params);
	}

	@Override
	public List<AuthorityInfo> getResourceParent() {
		return resourcesDao.getResourceParent();
	}

	@Override
	public List<AuthorityInfo> findResourceByParentId(int parentId) {
		return resourcesDao.findResourceByParentId(parentId);
	}

	@Override
	public int deleteParentOrChildByResId(Map<String, Object> params) {
		return resourcesDao.deleteParentOrChildByResId(params);
	}

}
