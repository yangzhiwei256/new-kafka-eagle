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
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.service.BScreenService;
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
 * 大屏视图接口
 * @author smartloli.
 * Created by Aug 28, 2019.
 */
@Controller
@Api("大屏接口")
public class BigScreenController {

	@Autowired
	private BScreenService bscreen;

	@GetMapping("/bs")
    @ApiOperation("跳转大屏首页")
	public String indexView() {
        return "/bscreen/bscreen";
	}

	@GetMapping("/bs/brokers/ins/outs/realrate")
    @ResponseBody
    @ApiOperation("获取实时消费数据")
	public String getProducerAndConsumerRealRateAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return bscreen.getProducerAndConsumerRate(clusterAlias);
	}

	/** Get producer and consumer real rate data by ajax. */
	@GetMapping(value = "/bs/topic/total/logsize")
    @ResponseBody
    @ApiOperation("获取消费/生产者实时消费数据")
	public String getTopicTotalLogSizeAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return bscreen.getTopicTotalLogSize(clusterAlias);
	}

	@GetMapping("/bs/{type}/history")
    @ResponseBody
    @ApiOperation("获取生产/消费历史")
	public String getProducerOrConsumerHistoryAjax(@PathVariable("type") String type, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return bscreen.getProducerOrConsumerHistory(clusterAlias, type);
	}

	@GetMapping("/bs/{dtype}/day")
    @ResponseBody
    @ApiOperation("获取当天消费/生产数据")
	public String getTodayOrHistoryConsumerProducerAjax(@PathVariable("dtype") String dtype, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return bscreen.getTodayOrHistoryConsumerProducer(clusterAlias, dtype);
	}

	@GetMapping("/bs/topic/total/capacity")
    @ResponseBody
    @ApiOperation("获取当天Kafak容量")
	public String getTopicTotalCapacityAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("tkey", TopicConstants.CAPACITY);
        return bscreen.getTopicCapacity(params);
	}

}
