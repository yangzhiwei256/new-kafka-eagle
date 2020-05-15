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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.protocol.ConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.web.protocol.OwnerInfo;
import org.smartloli.kafka.eagle.web.protocol.TopicConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicOffsetsInfo;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.util.KafkaResourcePoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

/**
 * Kafka consumer data interface, and set up the return data set.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

	@Autowired
	private MBeanDao mbeanDao;

    @Autowired
    private TopicDao topicDao;

    /**
     * Kafka service interface.
     */
    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Get active topic graph data from kafka cluster.
     */
    public String getActiveGraph(String clusterAlias) {
        JSONObject target = new JSONObject();
        target.put("active", getActiveGraphDatasets(clusterAlias));
        return target.toJSONString();
    }

    /**
     * Get active graph from zookeeper.
     */
    private String getActiveGraphDatasets(String clusterAlias) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Entry<String, List<String>> entry : activeTopics.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KafkaConstants.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", entry.getKey());
				for (String str : entry.getValue()) {
					JSONObject subInSubTarget = new JSONObject();
					if (subTargets.size() > KafkaConstants.CHILD_SIZE) {
						subInSubTarget.put("name", "...");
						subTargets.add(subInSubTarget);
						break;
					} else {
						subInSubTarget.put("name", str);
						subTargets.add(subInSubTarget);
					}
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get kafka active number & storage offset in zookeeper. */
	private int getActiveNumber(String clusterAlias, String group, List<String> topics) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		int sum = 0;
		for (String topic : topics) {
			if (activeTopics.containsKey(group + "_" + topic)) {
				sum++;
			}
		}
		return sum;
	}

	/** Storage offset in kafka or zookeeper. */
	public String getActiveTopic(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafkaActiveTopic(clusterAlias);
		} else {
			return getActiveGraph(clusterAlias);
		}
	}

	/** Get consumers from zookeeper. */
	private String getConsumer(String clusterAlias, DisplayInfo page) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias, page);
		List<ConsumerInfo> consumerPages = new ArrayList<ConsumerInfo>();
		int id = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			ConsumerInfo consumer = new ConsumerInfo();
			consumer.setGroup(entry.getKey());
			consumer.setNode("");
			consumer.setTopics(entry.getValue().size());
			consumer.setId(++id);
			consumer.setActiveThreads(getActiveNumber(clusterAlias, entry.getKey(), entry.getValue()));
			consumer.setActiveTopics(getActiveNumber(clusterAlias, entry.getKey(), entry.getValue()));
			consumerPages.add(consumer);
		}
		return consumerPages.toString();
	}

	/** Judge consumers storage offset in kafka or zookeeper. */
	public String getConsumer(String clusterAlias, String formatter, DisplayInfo page) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumer(page, clusterAlias);
		} else {
			return getConsumer(clusterAlias, page);
		}
	}

	/** Get consumer size from kafka topic. */
	public int getConsumerCount(String clusterAlias, String storeFormat) {
		if ("kafka".equals(storeFormat)) {
			return this.getKafkaConsumerGroups(clusterAlias);
		} else {
			return kafkaService.getConsumers(clusterAlias).size();
		}
	}

    /** Get kafka 0.10.x, 1.x, 2.x consumer groups. */
    @Override
    public int getKafkaConsumerGroups(String clusterAlias) {
        int counter = 0;
        AdminClient adminClient = KafkaResourcePoolUtils.getKafkaClient(clusterAlias);
        try {
            Collection<ConsumerGroupListing> consumerGroupListingList = adminClient.listConsumerGroups().all().get();
            for (ConsumerGroupListing consumerGroupListing : consumerGroupListingList) {
                String groupId = consumerGroupListing.groupId();
                if (!groupId.contains("kafka.eagle")) {
                    counter++;
                }
            }
        } catch (Exception e) {
            log.error("Get kafka cluster [{}] consumer group has error ", clusterAlias, e);
        } finally {
            KafkaResourcePoolUtils.release(clusterAlias, adminClient);
        }
        return counter;
    }

    /** Get kafka consumer & storage offset in kafka topic. */
    private String getKafkaConsumer(DisplayInfo page, String clusterAlias) {
        List<ConsumerInfo> kafkaConsumerPages = new ArrayList<ConsumerInfo>();
        JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias, page));
        int id = page.getDisplayStart();
        for (Object object : consumerGroups) {
            JSONObject consumerGroup = (JSONObject) object;
            String group = consumerGroup.getString("group");
            ConsumerInfo consumer = new ConsumerInfo();
            consumer.setGroup(group);
            consumer.setId(++id);
            consumer.setNode(consumerGroup.getString("node"));
            OwnerInfo ownerInfo = kafkaService.getKafkaActiverNotOwners(clusterAlias, group);
            consumer.setTopics(ownerInfo.getTopicSets().size());
            consumer.setActiveTopics(getKafkaActiveTopicNumbers(clusterAlias, group));
            consumer.setActiveThreads(ownerInfo.getActiveSize());
            kafkaConsumerPages.add(consumer);
        }
        return JSON.toJSONString(kafkaConsumerPages);

    }

	/** List the name of the topic in the consumer detail information. */
	private String getConsumerDetail(String clusterAlias, String group) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		Map<String, List<String>> actvTopics = kafkaService.getActiveTopic(clusterAlias);
		List<TopicConsumerInfo> kafkaConsumerDetails = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (actvTopics.containsKey(group + "_" + topic)) {
				consumerDetail.setConsumering(TopicConstants.RUNNING);
			} else {
				consumerDetail.setConsumering(TopicConstants.SHUTDOWN);
			}
			kafkaConsumerDetails.add(consumerDetail);
		}
		return kafkaConsumerDetails.toString();
	}

	/** Judge consumer storage offset in kafka or zookeeper. */
	public String getConsumerDetail(String clusterAlias, String formatter, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumerDetail(clusterAlias, group);
		} else {
			return getConsumerDetail(clusterAlias, group);
		}
	}

	/** Get active grahp data & storage offset in kafka topic. */
	private Object getKafkaActive(String clusterAlias) {
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KafkaConstants.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", consumerGroup.getString("group"));
				for (String str : getKafkaTopicSets(clusterAlias, consumerGroup.getString("group"))) {
					JSONObject subInSubTarget = new JSONObject();
					if (subTargets.size() > KafkaConstants.CHILD_SIZE) {
						subInSubTarget.put("name", "...");
						subTargets.add(subInSubTarget);
						break;
					} else {
						subInSubTarget.put("name", str);
						subTargets.add(subInSubTarget);
					}
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get active topic from kafka cluster & storage offset in kafka topic. */
	private String getKafkaActiveTopic(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("active", getKafkaActive(clusterAlias));
		return target.toJSONString();
	}

	/** Get kafka active topic by active graph. */
	private Set<String> getKafkaTopicSets(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == TopicConstants.RUNNING) {
				activerTopics.add(topic);
			}
		}
		Set<String> activeTopicSets = new HashSet<>();
		for (String topic : consumerTopics) {
			if (activerTopics.contains(topic)) {
				activeTopicSets.add(topic);
			} else {
				if (isConsumering(clusterAlias, group, topic) == TopicConstants.RUNNING) {
					activeTopicSets.add(topic);
				}
			}
		}
		return activeTopicSets;
	}

	/** Get kafka active topic total. */
	private int getKafkaActiveTopicNumbers(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == TopicConstants.RUNNING) {
				activerTopics.add(topic);
			}
		}
		int active = 0;
		for (String topic : consumerTopics) {
			if (activerTopics.contains(topic)) {
				active++;
			} else {
				if (isConsumering(clusterAlias, group, topic) == TopicConstants.RUNNING) {
					active++;
				}
			}
		}
		return active;
	}

	/** Get consumer detail from kafka topic. */
	private String getKafkaConsumerDetail(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		for (String topic : consumerTopics) {
			if (isConsumering(clusterAlias, group, topic) == TopicConstants.RUNNING) {
				activerTopics.add(topic);
			}
		}
		List<TopicConsumerInfo> kafkaConsumerPages = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumerTopics) {
			TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (activerTopics.contains(topic)) {
				consumerDetail.setConsumering(TopicConstants.RUNNING);
			} else {
				consumerDetail.setConsumering(isConsumering(clusterAlias, group, topic));
			}
			kafkaConsumerPages.add(consumerDetail);
		}
		return kafkaConsumerPages.toString();
	}

	/** Check if the application is consuming. */
	public int isConsumering(String clusterAlias, String group, String topic) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cluster", clusterAlias);
		params.put("group", group);
		params.put("topic", topic);
		List<TopicOffsetsInfo> topicOffsets = mbeanDao.getConsumerRateTopic(params);
		if (topicOffsets.size() == 2) {
			try {
				long resultOffsets = Math.abs(Long.parseLong(topicOffsets.get(0).getOffsets()) - Long.parseLong(topicOffsets.get(1).getOffsets()));
				long resultLogSize = Math.abs(Long.parseLong(topicOffsets.get(0).getLogsize()) - Long.parseLong(topicOffsets.get(0).getOffsets()));

				/**
				 * offset equal offset,maybe producer rate equal consumer rate.
				 */
				if (resultOffsets == 0) {
					/**
					 * logsize equal offsets,follow two states.<br>
					 * 1. maybe application shutdown.<br>
					 * 2. maybe application run, but producer rate equal
					 * consumer rate.<br>
					 */
					if (resultLogSize == 0) {
						return TopicConstants.PENDING;
					} else {
						return TopicConstants.SHUTDOWN;
					}
				} else {
					return TopicConstants.RUNNING;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (topicOffsets.size() == 1) {
			long resultLogSize = Math.abs(Long.parseLong(topicOffsets.get(0).getLogsize()) - Long.parseLong(topicOffsets.get(0).getOffsets()));
			if (resultLogSize == 0) {
				return TopicConstants.PENDING;
			} else {
				return TopicConstants.SHUTDOWN;
			}
		}
		return TopicConstants.SHUTDOWN;
	}

	@Override
	public long getConsumerCountByDB(String clusterAlias) {
		Map<String, Object> params = new HashMap<>();
		params.put("cluster", clusterAlias);
		return topicDao.countConsumerGroupPages(params);
	}

	@Override
	public String getConsumerByDB(String clusterAlias, DisplayInfo page) {
		// Map<String, Object>
		return null;
	}

}
