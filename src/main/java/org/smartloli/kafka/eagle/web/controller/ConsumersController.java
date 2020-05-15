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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Kafka 消费者控制器
 * @author smartloli.
 * Created by Sep 6, 2016.
 * Update by hexiang 20170216
 */
@Controller
public class ConsumersController {

    /**
     * Kafka consumer service interface.
     */
    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Consumer viewer.
     */
    @RequestMapping(value = "/consumers", method = RequestMethod.GET)
    public ModelAndView consumersView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/consumers/consumers");
        return mav;
    }

    /**
     * Get consumer data by ajax.
     */
	@GetMapping("/consumers/info/ajax")
    @ResponseBody
	public String consumersGraphAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        return consumerService.getActiveTopic(clusterAlias, formatter);

	}

	/** Get consumer datasets by ajax. */
	@RequestMapping(value = "/consumer/list/table/ajax", method = RequestMethod.GET)
    @ResponseBody
	public String consumerTableAjax(HttpServletResponse response, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
                iDisplayStart = param.getIntValue("value");
            } else if ("iDisplayLength".equals(param.getString("name"))) {
                iDisplayLength = param.getIntValue("value");
            } else if ("sSearch".equals(param.getString("name"))) {
                search = param.getString("value");
            }
        }

        DisplayInfo displayInfo = new DisplayInfo();
        displayInfo.setSearch(search);
        displayInfo.setDisplayLength(iDisplayLength);
        displayInfo.setDisplayStart(iDisplayStart);

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        long count = consumerService.getConsumerCount(clusterAlias, formatter);
		JSONArray consumers = JSON.parseArray(consumerService.getConsumer(clusterAlias, formatter, displayInfo));
		JSONArray aaDatas = new JSONArray();
		for (Object object : consumers) {
			JSONObject consumer = (JSONObject) object;
			JSONObject obj = new JSONObject();
			obj.put("id", consumer.getInteger("id"));
			String group = "";
			try {
				group = URLEncoder.encode(consumer.getString("group"), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			obj.put("group", "<a class='link' group='" + group + "' href='#'>" + consumer.getString("group") + "</a>");
			obj.put("topics", consumer.getInteger("topics"));
			obj.put("node", consumer.getString("node"));
			int activeTopics = consumer.getInteger("activeTopics");
			int activeThreads = consumer.getInteger("activeThreads");
			if (activeTopics > 0) {
				obj.put("activeTopics", "<a class='btn btn-success btn-xs'>" + consumer.getInteger("activeTopics") + "</a>");
			} else {
				obj.put("activeTopics", "<a class='btn btn-danger btn-xs'>" + consumer.getInteger("activeTopics") + "</a>");
			}
			if (activeThreads > 0) {
				obj.put("activeThreads", "<a class='btn btn-success btn-xs'>" + consumer.getInteger("activeThreads") + "</a>");
			} else {
				obj.put("activeThreads", "<a class='btn btn-danger btn-xs'>" + consumer.getInteger("activeThreads") + "</a>");
			}

			aaDatas.add(obj);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get consumer data through group by ajax. */
	@RequestMapping(value = "/consumer/group/table/ajax", method = RequestMethod.GET)
    @ResponseBody
	public String consumerTableListAjax(HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		String group = "";
		try {
			group = URLDecoder.decode(request.getParameter("group"), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
                sEcho = param.getIntValue("value");
            } else if ("iDisplayStart".equals(param.getString("name"))) {
                iDisplayStart = param.getIntValue("value");
            } else if ("iDisplayLength".equals(param.getString("name"))) {
                iDisplayLength = param.getIntValue("value");
            }
        }

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        JSONArray consumerDetails = JSON.parseArray(consumerService.getConsumerDetail(clusterAlias, formatter, group));
        int offset = 0;
        JSONArray aaDatas = new JSONArray();
        for (Object object : consumerDetails) {
            JSONObject consumerDetail = (JSONObject) object;
            if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                JSONObject obj = new JSONObject();
                String topic = consumerDetail.getString("topic");
                obj.put("id", consumerDetail.getInteger("id"));
				obj.put("topic", topic);

				try {
					group = URLEncoder.encode(group, "UTF-8");
					topic = URLEncoder.encode(topic, "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (consumerDetail.getInteger("isConsumering") == TopicConstants.RUNNING) {
					obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='btn btn-success btn-xs'>Running</a>");
				} else if (consumerDetail.getInteger("isConsumering") == TopicConstants.SHUTDOWN) {
					obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='btn btn-danger btn-xs'>Shutdown</a>");
				} else {
					obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='btn btn-warning btn-xs'>Pending</a>");
				}
				aaDatas.add(obj);
			}
			offset++;
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", consumerDetails.size());
		target.put("iTotalDisplayRecords", consumerDetails.size());
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

}
