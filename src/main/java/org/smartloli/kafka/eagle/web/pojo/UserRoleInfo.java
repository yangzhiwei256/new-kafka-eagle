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
package org.smartloli.kafka.eagle.web.pojo;


import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * 用户角色匹配信息
 * @author smartloli.
 * Created by May 25, 2017
 */
@Data
public class UserRoleInfo {

    /**
     * 用户ID
     */
	private int userId;

    /**
     * 角色ID
     */
	private int roleId;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

}
