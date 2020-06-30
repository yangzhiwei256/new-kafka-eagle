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
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.common.Node;
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
import org.smartloli.kafka.eagle.web.support.KafkaAdminClientTemplate;
import org.smartloli.kafka.eagle.web.support.KafkaZkClientTemplate;
import org.smartloli.kafka.eagle.web.support.OperationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import scala.collection.JavaConversions;
import scala.collection.Seq;

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
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private KafkaZkClientTemplate kafkaZkClientTemplate;
    @Autowired
    private KafkaAdminClientTemplate kafkaAdminClientTemplate;

    @Override
    public Map<String, List<String>> getConsumers(String clusterAlias) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> execute(KafkaZkClient kafkaZkClient) {
                Map<String, List<String>> consumers = new HashMap<>();
                Seq<String> subConsumerPaths = kafkaZkClient.getChildren(KafkaConstants.CONSUMERS_PATH);
                List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
                for (String group : groups) {
                    String path = KafkaConstants.CONSUMERS_PATH + "/" + group + "/owners";
                    if (kafkaZkClient.pathExists(path)) {
                        Seq<String> owners = kafkaZkClient.getChildren(path);
                        List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
                        consumers.put(group, ownersSerialize);
                    } else {
                        log.error("Consumer Path[" + path + "] is not exist.");
                    }
                }
                log.info("查询kafka集群[{}]消费者信息 ==> {}", clusterAlias, consumers);
                return consumers;
            }
        });
    }

    /** Get kafka 0.10.x, 1.x, 2.x consumer metadata. */
    @Override
    public String getKafkaConsumer(String clusterAlias) {
        return kafkaAdminClientTemplate.doExecute(clusterAlias, adminClient -> {
            JSONArray consumerGroups = new JSONArray();
            try {
                ListConsumerGroupsResult cgrs = adminClient.listConsumerGroups();
                Collection<ConsumerGroupListing> consumerGroupListings = cgrs.all().get();
                if (!CollectionUtils.isEmpty(consumerGroupListings)) {
                    for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
                        JSONObject consumerGroup = new JSONObject();
                        String groupId = consumerGroupListing.groupId();
                        DescribeConsumerGroupsResult descConsumerGroup = adminClient.describeConsumerGroups(Arrays.asList(groupId));
                        if (!groupId.contains("kafka.eagle")) {
                            consumerGroup.put("group", groupId);
                            try {
                                Node node = descConsumerGroup.all().get().get(groupId).coordinator();
                                consumerGroup.put("node", node.host() + ":" + node.port());
                            } catch (Exception e) {
                                log.error("Get coordinator node has error, msg is " + e.getMessage());
                                e.printStackTrace();
                            }
                            consumerGroup.put("meta", kafkaService.getKafkaMetadata(clusterAlias, groupId));
                            consumerGroups.add(consumerGroup);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Get kafka consumer has error", e);
            }
            return consumerGroups.toJSONString();
        });
    }

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
		Map<String, List<String>> activeTopics = kafkaService.findActiveTopics(clusterAlias);
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
		Map<String, List<String>> activeTopics = kafkaService.findActiveTopics(clusterAlias);
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
			return this.getConsumers(clusterAlias).size();
		}
	}

    /** Get kafka 0.10.x, 1.x, 2.x consumer groups. */
    @Override
    public int getKafkaConsumerGroups(String clusterAlias) {
        return kafkaAdminClientTemplate.doExecute(clusterAlias, new OperationCallback<AdminClient, Integer>() {
            @Override
            public Integer execute(AdminClient adminClient) {
                int counter = 0;
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
                }
                return counter;
            }
        });

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
            OwnerInfo ownerInfo = kafkaService.getKafkaActiveNotOwners(clusterAlias, group);
            consumer.setTopics(ownerInfo.getTopicSets().size());
            consumer.setActiveTopics(getKafkaActiveTopicNumbers(clusterAlias, group));
            consumer.setActiveThreads(ownerInfo.getActiveSize());
            kafkaConsumerPages.add(consumer);
        }
        return JSON.toJSONString(kafkaConsumerPages);

    }

	/** List the name of the topic in the consumer detail information. */
	private List<TopicConsumerInfo> getConsumerDetail(String clusterAlias, String group) {
		Map<String, List<String>> consumers = this.getConsumers(clusterAlias);
		Map<String, List<String>> activeTopics = kafkaService.findActiveTopics(clusterAlias);
		List<TopicConsumerInfo> kafkaConsumerDetails = new ArrayList<>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (activeTopics.containsKey(group + "_" + topic)) {
				consumerDetail.setConsuming(TopicConstants.RUNNING);
			} else {
				consumerDetail.setConsuming(TopicConstants.SHUTDOWN);
			}
			kafkaConsumerDetails.add(consumerDetail);
		}
		return kafkaConsumerDetails;
	}

	@Override
	public List<TopicConsumerInfo> getConsumerDetail(String clusterAlias, String formatter, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumerDetail(clusterAlias, group);
		} else {
			return getConsumerDetail(clusterAlias, group);
		}
	}

	/** Get active grahp data & storage offset in kafka topic. */
	private Object getKafkaActive(String clusterAlias) {
		JSONArray consumerGroups = JSON.parseArray(this.getKafkaConsumer(clusterAlias));
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
	private List<TopicConsumerInfo> getKafkaConsumerDetail(String clusterAlias, String group) {
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
				consumerDetail.setConsuming(TopicConstants.RUNNING);
			} else {
				consumerDetail.setConsuming(isConsumering(clusterAlias, group, topic));
			}
			kafkaConsumerPages.add(consumerDetail);
		}
		return kafkaConsumerPages;
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
