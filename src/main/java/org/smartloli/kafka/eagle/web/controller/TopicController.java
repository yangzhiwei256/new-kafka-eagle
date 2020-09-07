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
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.smartloli.kafka.eagle.web.config.ResponseMsg;
import org.smartloli.kafka.eagle.web.constant.CommonConstants;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.controller.vo.QueryMsgResultVo;
import org.smartloli.kafka.eagle.web.entity.QueryKafkaMessage;
import org.smartloli.kafka.eagle.web.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.web.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicConfig;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicMessage;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicSqlHistory;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Kafka 主题控制器
 *
 * @author smartloli.
 * Created by Sep 6, 2016.
 * Update by hexiang 20170216
 */
@Controller
@Slf4j
@Api("Kafka主题控制器")
public class TopicController {

    @Value("${" + KafkaConstants.KAFKA_EAGLE_TOPIC_TOKEN + ": }")
    private String kafkaEagleTopicToken;

    /** Kafka topic service interface. */
    @Autowired
    private TopicService topicService;

    /** Kafka service interface. */
    @Autowired
    private KafkaService kafkaService;

    /** BrokerService interface. */
	@Autowired
	private BrokerService brokerService;

	/** Topic create viewer. */
	@GetMapping("/topic/create")
    @ApiOperation("跳转创建主题页面")
	public String topicCreateView() {
        return "/topic/create";
	}

	/** Topic message viewer. */
	@GetMapping("/topic/message")
    @ApiOperation("跳转主题消息页面")
	public String topicMessageView() {
        return "/topic/msg";
	}

	/** Topic message manager. */
	@GetMapping("/topic/manager")
    @ApiOperation("跳转主题管理页面")
	public String topicManagerView() {
        return "/topic/manager";
	}

	/** Topic mock viewer. */
	@GetMapping("/topic/mock")
    @ApiOperation("跳转主题模拟页面")
	public String topicMockView() {
		return "/topic/mock";
	}

	/** Topic list viewer. */
	@GetMapping("/topic/list")
    @ApiOperation("跳转主题列表")
	public String topicListView() {
		return "/topic/list";
	}

	/**
     * Topic metadata viewer.
     */
    @GetMapping("/topic/meta/page/{tname}")
    @ApiOperation("跳转主题元数据页面")
    public String topicMetaView(@PathVariable("tname") String tname, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        if (topicService.hasTopic(clusterAlias, tname)) {
            return "/topic/topic_meta";
        } else {
            return "/error/404";
        }
    }

	/** Create topic success viewer. */
	@RequestMapping("/topic/create/success")
    @ApiOperation("跳转主题创建成功页面")
	public String successView() {
		return "/topic/add_success";
	}

	/** Create topic failed viewer. */
	@GetMapping("/topic/create/failed")
    @ApiOperation("跳转设备失败页面")
	public String failedView() {
        return "/topic/add_failed";
	}

	/** Get topic metadata by ajax. */
	@GetMapping("/topic/meta/{tname}")
    @ResponseBody
    @ApiOperation("获取主题元数据")
	public String topicMetaAjax(@PathVariable("tname") String tname, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
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

		Map<String, Object> map = new HashMap<>();
		map.put("start", iDisplayStart);
		map.put("length", iDisplayLength);
		long count = topicService.getPartitionNumbers(clusterAlias, tname);
		List<MetadataInfo> metadatas = topicService.metadata(clusterAlias, tname, map);
		JSONArray aaDatas = new JSONArray();
		for (MetadataInfo metadata : metadatas) {
			JSONObject object = new JSONObject();
			object.put("topic", tname);
			object.put("partition", metadata.getPartitionId());
			object.put("logsize", metadata.getLogSize());
			object.put("leader", metadata.getLeader());
			object.put("replicas", metadata.getReplicas());
			object.put("isr", metadata.getIsr());
			if (metadata.isPreferredLeader()) {
				object.put("preferred_leader", "<a class='btn btn-success btn-xs'>true</a>");
			} else {
				object.put("preferred_leader", "<a class='btn btn-danger btn-xs'>false</a>");
			}
			if (metadata.isUnderReplicated()) {
				object.put("under_replicated", "<a class='btn btn-danger btn-xs'>true</a>");
			} else {
				object.put("under_replicated", "<a class='btn btn-success btn-xs'>false</a>");
			}
			aaDatas.add(object);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get cluster data by ajax. */
	@GetMapping("/topic/meta/mbean/{tname}")
    @ResponseBody
    @ApiOperation("获取集群数据")
	public String topicMetaMetricsAjax(@PathVariable("tname") String tname, HttpSession session) {
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        return topicService.getTopicMBean(clusterAlias, tname);
	}

	/** Get cluster data by ajax. */
	@GetMapping("/topic/meta/jmx/{tname}")
    @ResponseBody
    @ApiOperation("获取集群数据")
	public String topicMsgByJmxAjax(@PathVariable("tname") String tname, HttpSession session) {
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        return topicService.getTopicSizeAndCapacity(clusterAlias, tname);
	}

	/** Get topic datasets by ajax. */
	@GetMapping("/topic/mock/list")
    @ResponseBody
    @ApiOperation("获取模拟主题数据")
	public String topicMockAjax(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();
        object.put("items", JSON.parseArray(topicService.mockTopics(clusterAlias, name)));
        return object.toJSONString();
	}

	/** Get topic datasets by ajax. */
	@GetMapping("/topic/manager/keys")
    @ResponseBody
    @ApiOperation("获取主题数据")
	public String getTopicProperties(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();
        object.put("items", JSON.parseArray(topicService.getTopicProperties(clusterAlias, name)));
        return object.toJSONString();
	}

	/** Get topic datasets by ajax. */
	@GetMapping("/topic/manager/{type}")
    @ResponseBody
    @ApiOperation("修改主题配置")
	public String alterTopicConfigAjax(@PathVariable("type") String type, HttpServletRequest request) {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
			String topic = request.getParameter("topic");
			TopicConfig topicConfig = new TopicConfig();
			topicConfig.setName(topic);
			topicConfig.setType(type.toUpperCase());
			if (TopicConstants.ADD.equals(topicConfig.getType())) {
				String key = request.getParameter("key");
				String value = request.getParameter("value");
				ConfigEntry configEntry = new ConfigEntry(key, value);
				topicConfig.setConfigEntry(configEntry);
			} else if (TopicConstants.DELETE.equals(topicConfig.getType())) {
				String key = request.getParameter("key");
				ConfigEntry configEntry = new ConfigEntry(key, "");
				topicConfig.setConfigEntry(configEntry);
			}
			JSONObject object = new JSONObject();
			object.put("result", topicService.changeTopicConfig(clusterAlias, topicConfig));
			return object.toJSONString();
	}

	/** Send mock data to topic. */
	@PostMapping("/topic/send/message")
    @ResponseBody
    @ApiOperation("发送主题消息")
	public String topicMockSend(@RequestBody TopicMessage topicMessage, HttpSession httpSession) {
        String clusterAlias = httpSession.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        JSONObject object = new JSONObject();
        object.put("status", topicService.mockSendMsg(clusterAlias, topicMessage.getTopic(), topicMessage.getMessage()));
        return object.toJSONString();
	}

	/** Get topic datasets by ajax. */
	@GetMapping("/topic/list/table")
    @ResponseBody
    @ApiOperation("主题列表")
	public String topicListAjax(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
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

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

		Map<String, Object> map = new HashMap<>();
		map.put("search", search);
		map.put("start", iDisplayStart);
		map.put("length", iDisplayLength);
		long count = 0L;
		if (search != null && !"".equals(search)) {
			count = topicService.getTopicNumbers(clusterAlias, search);
		} else {
			count = topicService.getTopicNumbers(clusterAlias);
		}
		List<PartitionsInfo> topics = topicService.list(clusterAlias, map);
		JSONArray aaDatas = new JSONArray();
		for (PartitionsInfo partition : topics) {
            JSONObject object = new JSONObject();
            object.put("id", partition.getId());
            object.put("topic", "<a href='/topic/meta/page/" + partition.getTopic() + "' target='_blank'>" + partition.getTopic() + "</a>");
            object.put("partitions", partition.getPartitionNumbers());
            try {
                long brokerSpread = partition.getBrokersSpread();
                if (brokerSpread < TopicConstants.TOPIC_BROKER_SPREAD_ERROR) {
                    object.put("brokerSpread", "<a class='btn btn-danger btn-xs'>" + brokerSpread + "%</a>");
                } else if (brokerSpread >= TopicConstants.TOPIC_BROKER_SPREAD_ERROR && brokerSpread < TopicConstants.TOPIC_BROKER_SPREAD_NORMAL) {
                    object.put("brokerSpread", "<a class='btn btn-warning btn-xs'>" + brokerSpread + "%</a>");
                } else if (brokerSpread >= TopicConstants.TOPIC_BROKER_SPREAD_NORMAL) {
                    object.put("brokerSpread", "<a class='btn btn-success btn-xs'>" + brokerSpread + "%</a>");
                } else {
					object.put("brokerSpread", "<a class='btn btn-primary btn-xs'>" + brokerSpread + "%</a>");
				}

				long brokerSkewed = partition.getBrokersSkewed();
				if (brokerSkewed >= TopicConstants.TOPIC_BROKER_SKEW_ERROR) {
					object.put("brokerSkewed", "<a class='btn btn-danger btn-xs'>" + brokerSkewed + "%</a>");
				} else if (brokerSkewed > TopicConstants.TOPIC_BROKER_SKEW_NORMAL && brokerSkewed < TopicConstants.TOPIC_BROKER_SKEW_ERROR) {
					object.put("brokerSkewed", "<a class='btn btn-warning btn-xs'>" + brokerSkewed + "%</a>");
				} else if (brokerSkewed <= TopicConstants.TOPIC_BROKER_SKEW_NORMAL) {
					object.put("brokerSkewed", "<a class='btn btn-success btn-xs'>" + brokerSkewed + "%</a>");
				} else {
					object.put("brokerSkewed", "<a class='btn btn-primary btn-xs'>" + brokerSkewed + "%</a>");
				}

				long brokerLeaderSkewed = partition.getBrokersLeaderSkewed();
				if (brokerLeaderSkewed >= TopicConstants.TOPIC_BROKER_LEADER_SKEW_ERROR) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-danger btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else if (brokerSkewed > TopicConstants.TOPIC_BROKER_LEADER_SKEW_NORMAL && brokerLeaderSkewed < TopicConstants.TOPIC_BROKER_LEADER_SKEW_ERROR) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-warning btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else if (brokerSkewed <= TopicConstants.TOPIC_BROKER_LEADER_SKEW_NORMAL) {
					object.put("brokerLeaderSkewed", "<a class='btn btn-success btn-xs'>" + brokerLeaderSkewed + "%</a>");
				} else {
					object.put("brokerLeaderSkewed", "<a class='btn btn-primary btn-xs'>" + brokerLeaderSkewed + "%</a>");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			object.put("created", partition.getCreated());
			object.put("modify", partition.getModify());
			Map<String, Object> topicStateParams = new HashMap<>();
			topicStateParams.put("cluster", clusterAlias);
			topicStateParams.put("topic", partition.getTopic());
			topicStateParams.put("tkey", TopicConstants.TRUNCATE);
			List<TopicRank> topicStates = topicService.getCleanTopicState(topicStateParams);
			if (topicStates != null && topicStates.size() > 0) {
				if (topicStates.get(0).getTvalue() == 0) {
					if (KafkaConstants.ADMIN.equals(userDetails.getUsername())) {
						object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
								+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name=''><i class='fa fa-fw fa-trash-o'></i>Truncating</a></li></ul></div>");
					} else {
						object.put("operate", "");
					}
				} else {
					if (KafkaConstants.ADMIN.equals(userDetails.getUsername())) {
						object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
								+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name='topic_clean'><i class='fa fa-fw fa-trash-o'></i>Truncated</a></li></ul></div>");
					} else {
						object.put("operate", "");
					}
				}
			} else {
				if (KafkaConstants.ADMIN.equals(userDetails.getUsername())) {
					object.put("operate", "<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a name='topic_modify' href='#" + partition.getTopic()
							+ "'><i class='fa fa-fw fa-edit'></i>Alter</a></li><li><a href='#" + partition.getTopic() + "' name='topic_remove'><i class='fa fa-fw fa-minus-circle'></i>Drop</a></li><li><a href='#" + partition.getTopic() + "' name='topic_clean'><i class='fa fa-fw fa-trash-o'></i>Truncate</a></li></ul></div>");
				} else {
					object.put("operate", "");
				}
			}
			aaDatas.add(object);
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get select topic datasets by ajax. */
	@PostMapping("/topic/list/select")
    @ResponseBody
    @ApiOperation("选择列表主题")
	public String topicSelectListAjax(HttpServletRequest request) {
        String topic = request.getParameter("topic");
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        return topicService.getSelectTopics(clusterAlias, topic);
	}

	/** Get select filter topic datasets by ajax. */
	@GetMapping("/topic/list/filter/select")
    @ResponseBody
    @ApiOperation("过滤主题数据集")
	public String topicSelectFilterListAjax(HttpServletRequest request) {
			String topics = request.getParameter("topics");
			String stime = request.getParameter("stime");
			String etime = request.getParameter("etime");
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			params.put("stime", stime);
			params.put("etime", etime);
			if (!Strings.isNullOrEmpty(topics)) {
				String[] topicStrs = topics.split(",");
				Set<String> topicSets = new HashSet<>();
				for (String topic : topicStrs) {
					topicSets.add(topic);
				}
				params.put("topics", topicSets);
			}
			return topicService.getSelectTopicsLogSize(clusterAlias, params);
	}

	/** Clean topic data by ajax. */
	@GetMapping("/topic/clean/data/{topic}/")
    @ApiOperation("清理主题数据")
	public String cleanTopicDataAjax(@PathVariable("topic") String topic, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

			TopicRank tr = new TopicRank();
			tr.setCluster(clusterAlias);
			tr.setTopic(topic);
			tr.setTkey(TopicConstants.TRUNCATE);
			tr.setTvalue(0);
			if (topicService.addCleanTopicData(Collections.singletonList(tr)) > 0) {
				return "redirect:/topic/list";
			} else {
                return "redirect:/500";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "redirect:/500";
		}
	}

	/** Create topic form. */
	@PostMapping("/topic/create/form")
    @ApiOperation("跳转创建主题页面")
	public String topicAddForm(HttpSession session, HttpServletRequest request) {
		String ke_topic_name = request.getParameter("ke_topic_name");
		String ke_topic_partition = request.getParameter("ke_topic_partition");
		String ke_topic_repli = request.getParameter("ke_topic_repli");
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		Map<String, Object> respons = topicService.createTopic(clusterAlias, ke_topic_name, ke_topic_partition, ke_topic_repli);
		if ("success".equals(respons.get("status"))) {
			session.removeAttribute("Submit_Status");
			session.setAttribute("Submit_Status", respons.get("info"));
			return "redirect:/topic/create/success";
		} else {
			session.removeAttribute("Submit_Status");
			session.setAttribute("Submit_Status", respons.get("info"));
			return  "redirect:/topic/create/failed";
		}
	}

	/** Delete topic. */
	@GetMapping("/topic/{topicName}/{token}/delete")
    @ApiOperation("删除主题")
	public String topicDelete(@PathVariable("topicName") String topicName, @PathVariable("token") String token, HttpSession session) {
        if (kafkaEagleTopicToken.equals(token) && !KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topicName)) {
            String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
            Map<String, String> response = topicService.deleteTopic(clusterAlias, topicName);
            if ("success".equals(response.get("status"))) {
                return "redirect:/topic/list";
            } else {
                return "redirect:/500";
            }
        } else {
            return "redirect:/403";
        }
	}

	/** Modify topic partitions. */
	@GetMapping("/topic/{topicName}/{partitions}/modify")
    @ApiOperation("修改主题分区")
	public String topicModifyPartitions(@PathVariable("topicName") String topicName, @PathVariable("partitions") int token, HttpSession session) {
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		Map<String, Object> respons = brokerService.createTopicPartitions(clusterAlias, topicName, token);
		if ("success".equals(respons.get("status"))) {
			return "redirect:/topic/list";
		} else {
			return "redirect:/500";
		}
	}

    @GetMapping("/topic/logical/commit/")
    @ResponseBody
    @ApiOperation("执行KSQL")
    public ResponseMsg topicSqlLogicalAjax(@RequestParam String sql, HttpSession session,
                                           HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        QueryMsgResultVo queryMsgResultVo = new QueryMsgResultVo();
        TopicSqlHistory topicSqlHistory = new TopicSqlHistory();
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        try {
            QueryKafkaMessage queryKafkaMessage = topicService.execute(clusterAlias, sql);
            queryMsgResultVo.setQueryKafkaMessage(queryKafkaMessage);
            topicSqlHistory.setCluster(clusterAlias);
            topicSqlHistory.setCreated(DateUtils.getCurrentDate());
            topicSqlHistory.setHost(request.getRemoteHost());
            topicSqlHistory.setKsql(sql);

            if (queryKafkaMessage.isError()) {
                topicSqlHistory.setStatus(CommonConstants.FAILURE_MSG);
                topicSqlHistory.setSpendTime(0);
            } else {
                topicSqlHistory.setStatus(CommonConstants.SUCCESS_MSG);
                topicSqlHistory.setSpendTime(queryKafkaMessage.getSpent());
            }
            topicSqlHistory.setTm(DateUtils.getCustomDate("yyyyMMdd"));
            topicSqlHistory.setUsername(userDetails.getUsername());
            topicService.writeTopicSqlHistory(Collections.singletonList(topicSqlHistory));
            queryMsgResultVo.setTopicSqlHistory(topicSqlHistory);
        } catch (Exception e) {
            log.error("KSQL执行报错:[{}]", sql, e);
            return ResponseMsg.buildFailureResponse(e.getMessage());
        }
        return ResponseMsg.buildSuccessResponse(queryMsgResultVo);
	}

	/**
     * Get topic sql history.
     */
    @GetMapping("/topic/sql/history")
    @ResponseBody
    @ApiOperation("kafka sql查询历史")
    public String topicSqlHistoryAjax(HttpServletRequest request, HttpSession session, @AuthenticationPrincipal UserDetails userDetails) {
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

		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		Map<String, Object> map = new HashMap<>();
		map.put("cluster", clusterAlias);
		map.put("search", search);
		map.put("username", userDetails.getUsername());
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);
		long count = 0L;
		List<TopicSqlHistory> topicSqls = null;
		if (userDetails.getUsername().equals(KafkaConstants.ADMIN)) {
			topicSqls = topicService.readTopicSqlHistoryByAdmin(map);
			count = topicService.countTopicSqlHistoryByAdmin(map);
		} else {
			topicSqls = topicService.readTopicSqlHistory(map);
			count = topicService.countTopicSqlHistory(map);
		}

		JSONArray aaDatas = new JSONArray();
		if (topicSqls != null) {
			for (TopicSqlHistory topicSql : topicSqls) {
                JSONObject obj = new JSONObject();
                int id = topicSql.getId();
                String host = topicSql.getHost();
                String ksql = topicSql.getKsql();
                obj.put("id", id);
                obj.put("username", topicSql.getUsername());
                obj.put("host", "<a href='#" + id + "/host' name='ke_sql_query_detail'>" + (host.length() > 20 ? host.substring(0, 20) + "..." : host) + "</a>");
                obj.put("ksql", "<a href='#" + id + "/ksql' name='ke_sql_query_detail'>" + (ksql.length() > 60 ? ksql.substring(0, 60) + "..." : ksql) + "</a>");
                if (topicSql.getStatus().equalsIgnoreCase(CommonConstants.SUCCESS_MSG)) {
                    obj.put("status", "<a class='btn btn-success btn-xs'>" + topicSql.getStatus() + "</a>");
                } else {
                    obj.put("status", "<a class='btn btn-danger btn-xs'>" + topicSql.getStatus() + "</a>");
                }
                obj.put("spendTime", topicSql.getSpendTime() / 1000.0 + "s");
                obj.put("created", topicSql.getCreated());
                aaDatas.add(obj);
			}
		}
		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get ksql host or sql detail. */
	@GetMapping("/topic/ksql/detail/{type}/{id}")
    @ResponseBody
    @ApiOperation("获取KafkaSql详情")
	public String getKSqlDetailByIdAjax(@PathVariable("id") int id, @PathVariable("type") String type) {
        JSONObject object = new JSONObject();
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        if ("host".equals(type)) {
            object.put("result", topicService.findTopicSqlByID(params).getHost());
        } else if ("ksql".equals(type)) {
            object.put("result", topicService.findTopicSqlByID(params).getKsql());
        }
        return object.toJSONString();
	}

	/** Get producer chart data by ajax. */
	@GetMapping("/topic/producer/chart")
    @ResponseBody
    @ApiOperation("生产主题数据")
	public String topicProducerChartAjax(HttpServletRequest request, HttpSession session) {
        String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
        Map<String, Object> param = new HashMap<>();
        param.put("cluster", clusterAlias);
        param.put("stime", request.getParameter("stime"));
        param.put("etime", request.getParameter("etime"));
        param.put("topic", request.getParameter("topic"));
        String target = topicService.queryTopicProducerChart(param);
        if (StringUtils.isEmpty(target)) {
            target = "";
        }
        return target;
	}

}
