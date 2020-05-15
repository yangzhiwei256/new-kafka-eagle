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

import org.smartloli.kafka.eagle.web.pojo.AuthorityInfo;

import java.util.List;
import java.util.Map;

/**
 * 资源授权服务
 * @author smartloli.
 * Created by May 18, 2017
 */
public interface AuthorityService {
	List<Integer> findRoleIdByUserId(int userId);

    /**
     * 角色ID查询资源ID
     * @param roleIdList 角色列表
     * @return
     */
    List<AuthorityInfo> findAuthorityByRoleIds(List<Integer> roleIdList);

    /**
     * 查询用户资源
     * @param userId 用户ID
     * @return
     */
    List<AuthorityInfo> findAuthorityInfoByUserId(int userId);

	String getResourcesTree();

	int insertResource(Map<String, Object> params);

	List<AuthorityInfo> getResourceParent();

	List<AuthorityInfo> findResourceByParentId(int parentId);

	int deleteParentOrChildByResId(Map<String, Object> params);
}
