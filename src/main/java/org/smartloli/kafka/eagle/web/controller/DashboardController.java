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
package org.smartloli.kafka.eagle.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Dashboard控制器
 * @author smartloli.
 * Created by Sep 6, 2016.
 * Update by hexiang 20170216
 */
@Controller
@Api("视图面板")
public class DashboardController {

	@Autowired
	private DashboardService dashboradService;

	@GetMapping(value = "/dash/kafka/ajax")
    @ResponseBody
    @ApiOperation("获取面板数据")
	public String dashboardAjax(HttpSession httpSession) {
		String clusterAlias = httpSession.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return dashboradService.getDashboard(clusterAlias);
	}

    /**
     * 获取主题数据量和容量
     * @param tkey 数据维度：logsize/capacity
     * @param request 请求体
     */
	@GetMapping(value = "/dash/{tkey}/table/ajax")
    @ResponseBody
    @ApiOperation("获取主题数据量和容量")
	public String dashTopicRankAjax(@PathVariable("tkey") String tkey, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("tkey", tkey);
        return dashboradService.getTopicRank(params).toJSONString();
	}

    /**
     * 获取系统内存数据
     * @param request
     */
	@GetMapping(value = "/dash/os/mem/ajax")
    @ResponseBody
    @ApiOperation("获取系统内存数据")
	public String dashOSMemAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("key", "os%");
        return dashboradService.getOSMem(params);
	}

}
