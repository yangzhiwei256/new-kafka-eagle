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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.OffsetInfo;
import org.smartloli.kafka.eagle.web.protocol.offsets.TopicOffsetInfo;
import org.smartloli.kafka.eagle.web.service.OffsetService;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka offset controller to viewer data.
 * 
 * @author smartloli.
 * Created by Sep 6, 2016.
 * Update by hexiang 20170216
 */
@Controller
@Api("Kafka偏移控制器")
public class OffsetController {

    @Autowired
    private OffsetService offsetService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Consumer viewer.
     */
    @GetMapping("/consumers/offset")
    public String consumersActiveView(HttpServletRequest request, HttpSession httpSession) {
        String clusterAlias = httpSession.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        String group = StrUtils.convertNull(request.getParameter("group"));
        String topic = StrUtils.convertNull(request.getParameter("topic"));

        try {
            group = URLDecoder.decode(group, "UTF-8");
            topic = URLDecoder.decode(topic, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
		return offsetService.hasGroupTopic(clusterAlias, formatter, group, topic) ? "/consumers/offset_consumers" : "/error/404";
	}

	/** Get real-time offset data from Kafka by ajax. */
	@GetMapping("/consumers/offset/realtime")
	public String offsetRealtimeView(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        String group = StrUtils.convertNull(request.getParameter("group"));
        String topic = StrUtils.convertNull(request.getParameter("topic"));

        try {
            group = URLDecoder.decode(group, "UTF-8");
            topic = URLDecoder.decode(topic, "UTF-8");
        } catch (Exception e) {
			e.printStackTrace();
		}
		return offsetService.hasGroupTopic(clusterAlias, formatter, group, topic) ? "/consumers/offset_realtime" : "/error/404";
	}

	/** Get detail offset from Kafka by ajax. */
	@GetMapping("/consumer/offset/group/topic")
    @ResponseBody
    @ApiOperation("kafka偏移详情")
	public String offsetDetailAjax(HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		String group = StrUtils.convertNull(request.getParameter("group"));
		String topic = StrUtils.convertNull(request.getParameter("topic"));
		try {
			group = URLDecoder.decode(group, "UTF-8");
			topic = URLDecoder.decode(topic, "UTF-8");
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
        TopicOffsetInfo topicOffset = new TopicOffsetInfo();
        topicOffset.setCluster(clusterAlias);
        topicOffset.setFormatter(formatter);
        topicOffset.setGroup(group);
        topicOffset.setPageSize(iDisplayLength);
        topicOffset.setStartPage(iDisplayStart);
        topicOffset.setTopic(topic);

        List<OffsetInfo> logSizes = offsetService.getConsumerOffsets(topicOffset);
        long count = topicService.getPartitionNumbers(clusterAlias, topic);
		JSONArray aaDatas = new JSONArray();
		for (OffsetInfo offsetInfo : logSizes) {
			JSONObject object = new JSONObject();
			object.put("partition", offsetInfo.getPartition());
			if (offsetInfo.getLogSize() == 0) {
				object.put("logsize", "<a class='btn btn-warning btn-xs'>0</a>");
			} else {
				object.put("logsize", offsetInfo.getLogSize());
			}
			if (offsetInfo.getOffset() == -1) {
				object.put("offset", "<a class='btn btn-warning btn-xs'>0</a>");
			} else {
				object.put("offset", "<a class='btn btn-success btn-xs'>" + offsetInfo.getOffset() + "</a>");
			}
			object.put("lag", "<a class='btn btn-danger btn-xs'>" + offsetInfo.getLag() + "</a>");
			object.put("owner", offsetInfo.getOwner());
			object.put("created", offsetInfo.getCreate());
			object.put("modify", offsetInfo.getModify());
			aaDatas.add(object);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get real-time offset graph data from Kafka by ajax. */
	@GetMapping("/consumer/offset/group/topic/realtime")
    @ResponseBody
    @ApiOperation("kafka偏移图")
	public String offsetGraphAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

        String group = StrUtils.convertNull(request.getParameter("group"));
        String topic = StrUtils.convertNull(request.getParameter("topic"));
        try {
            group = URLDecoder.decode(group, "UTF-8");
            topic = URLDecoder.decode(topic, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("cluster", clusterAlias);
        param.put("group", group);
        param.put("topic", topic);
        param.put("stime", request.getParameter("stime"));
        param.put("etime", request.getParameter("etime"));
        return offsetService.getOffsetsGraph(param);
	}

	/** Get real-time offset graph data from Kafka by ajax. */
	@GetMapping("/consumer/offset/rate/group/topic/realtime")
    @ResponseBody
    @ApiOperation("kafka偏移比例")
	public String offsetRateGraphAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

        String group = StrUtils.convertNull(request.getParameter("group"));
        String topic = StrUtils.convertNull(request.getParameter("topic"));
        try {
            group = URLDecoder.decode(group, "UTF-8");
            topic = URLDecoder.decode(topic, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("group", group);
        params.put("topic", topic);
        return offsetService.getOffsetRate(params);
	}

}
