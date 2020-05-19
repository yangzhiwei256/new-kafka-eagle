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

import org.apache.commons.lang3.StringUtils;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Metrics mbean controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016.
 */
@Controller
public class MetricsController {

	@Autowired
	private MetricsService metricsService;

	/** Brokers viewer. */
	@GetMapping("/metrics/brokers")
	public String clusterView() {
        return "/metrics/brokers";
	}

	/** Trend viewer. */
	@GetMapping("/metrics/kafka")
	public String trendView() {
        return "/metrics/kafka";
	}

	/** Trend viewer. */
	@GetMapping("/metrics/zk")
	public String zkView() {
		return "/metrics/zk";
	}

	/** Get cluster data by ajax. */
	@GetMapping("/metrics/brokers/mbean")
    @ResponseBody
	public String clusterAjax(HttpSession session) {
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        return metricsService.getAllBrokersMBean(clusterAlias);
	}

	/** Get trend data by ajax. */
	@GetMapping("/metrics/trend/mbean")
    @ResponseBody
	public String trendAjax(HttpServletRequest request, HttpSession session) throws ParseException {
			String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

			Map<String, Object> param = new HashMap<>();
			param.put("cluster", clusterAlias);
			param.put("stime", request.getParameter("stime"));
			param.put("etime", request.getParameter("etime"));
			param.put("type", request.getParameter("type"));
			String modules = request.getParameter("modules");
			if (StringUtils.isNotBlank(modules)) {
				param.put("modules", Arrays.asList(modules.split(",")));
			}
			String target = metricsService.query(param);
			if (StringUtils.isEmpty(target)) {
				target = "";
			}
			return target;
	}
}
