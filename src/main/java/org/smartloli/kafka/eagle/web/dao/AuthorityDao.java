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
import org.smartloli.kafka.eagle.web.pojo.AuthorityInfo;

import java.util.List;
import java.util.Map;

/**
 * Resource interface definition
 * 
 * @author smartloli.
 *
 *         Created by May 18, 2017
 */
public interface AuthorityDao {

	List<Integer> findRoleIdByUserId(int userId);

    /**
     * 查询资源ID
     * @param roleIdList
     * @return
     */
    List<AuthorityInfo> findAuthorityByRoleIds(@Param("roleIdList") List<Integer> roleIdList);

    /**
     * 查询资源信息
     * @param resourceIdList 资源ID列表
     * @return
     */
    List<AuthorityInfo> findAuthorityInfoByIds(@Param("resourceIdList") List<Integer> resourceIdList);

	List<AuthorityInfo> getResourcesTree();

	int insertResource(Map<String, Object> params);
	
	List<AuthorityInfo> getResourceParent();
	
	List<AuthorityInfo> findResourceByParentId(int parentId);
	
	int deleteParentOrChildByResId(Map<String, Object> params);
	
}
