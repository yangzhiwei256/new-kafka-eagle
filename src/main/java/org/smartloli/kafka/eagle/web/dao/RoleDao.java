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

import org.smartloli.kafka.eagle.web.pojo.RoleAuthorityInfo;
import org.smartloli.kafka.eagle.web.pojo.RoleInfo;
import org.smartloli.kafka.eagle.web.pojo.UserRoleInfo;

import java.util.List;

/**
 * KafkaConstants interface definition
 * 
 * @author smartloli.
 *
 *         Created by May 24, 2017
 */
public interface RoleDao {

	public List<RoleInfo> getRoles();

	public List<RoleAuthorityInfo> findRoleResourcesByRoleId(int roleId);

	public int insertRoleResource(RoleAuthorityInfo roleAuthorityInfo);

	public int deleteRoleResource(RoleAuthorityInfo roleAuthorityInfo);

	public List<UserRoleInfo> findRoleByUserId(int userId);

	public int insertUserRole(UserRoleInfo userRoleInfo);

	public int deleteUserRole(UserRoleInfo userRoleInfo);

}
