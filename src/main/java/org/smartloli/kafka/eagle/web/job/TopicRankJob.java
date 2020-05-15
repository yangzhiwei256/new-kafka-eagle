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
package org.smartloli.kafka.eagle.web.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.smartloli.kafka.eagle.web.service.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Collector topic logsize, capacity etc.
 *
 * @author smartloli.
 * Created by Jul 27, 2019
 */
@Slf4j
@Component
public class TopicRankJob {

    /** Kafka service interface. */
    @Autowired
    private KafkaMetricsService kafkaMetricsService;

    /**
     * Broker service interface.
     */
    @Autowired
    private BrokerService brokerService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    //    @Scheduled(cron = "0 0/5 * * * ?")
    protected void execute() {
        topicLogSizeStats();
        topicCapacityStats();
        topicProducerLogSizeStats();
        for (String bType : TopicConstants.BROKER_PERFORMANCE_LIST) {
            brokerPerformanceByTopicStats(bType);
        }
        topicCleanTask();
    }

    private void topicCleanTask() {
        for (String clusterAlias : kafkaClustersConfig.getClusterAllAlias()) {
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            List<TopicRank> allCleanTopics = dashboardService.getCleanTopicList(params);
            if (allCleanTopics != null) {
                for (TopicRank tr : allCleanTopics) {
                    long logSize = brokerService.getTopicRealLogSize(clusterAlias, tr.getTopic());
                    if (logSize > 0) {
                        String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), TopicConstants.ADD, new ConfigEntry(TopicConstants.CLEANUP_POLICY_KEY, TopicConstants.CLEANUP_POLICY_VALUE));
                        String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), TopicConstants.ADD, new ConfigEntry(TopicConstants.RETENTION_MS_KEY, TopicConstants.RETENTION_MS_VALUE));
                        log.info("Add [" + TopicConstants.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                        log.info("Add [" + TopicConstants.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                    } else {
                        // delete znode
                        String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), TopicConstants.DELETE, new ConfigEntry(TopicConstants.CLEANUP_POLICY_KEY, ""));
                        String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), TopicConstants.DELETE, new ConfigEntry(TopicConstants.RETENTION_MS_KEY, ""));
                        log.info("Delete [" + TopicConstants.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
                        log.info("Delete [" + TopicConstants.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
                        // update db state
                        tr.setTvalue(1);
                        dashboardService.writeTopicRank(Collections.singletonList(tr));
                    }
                }
			}
		}
	}

	private void brokerPerformanceByTopicStats(String bType) {
        List<TopicRank> topicRanks = new ArrayList<>();
        for (String clusterAlias : kafkaClustersConfig.getClusterAllAlias()) {
            List<String> topics = brokerService.topicList(clusterAlias);
            for (String topic : topics) {
                int tValue = 0;
                if (bType.equals(TopicConstants.BROKER_SPREAD)) {
                    tValue = brokerService.getBrokerSpreadByTopic(clusterAlias, topic);
                } else if (bType.equals(TopicConstants.BROKER_SKEWED)) {
                    tValue = brokerService.getBrokerSkewedByTopic(clusterAlias, topic);
                } else if (bType.equals(TopicConstants.BROKER_LEADER_SKEWED)) {
                    tValue = brokerService.getBrokerLeaderSkewedByTopic(clusterAlias, topic);
                }
				TopicRank topicRank = new TopicRank();
				topicRank.setCluster(clusterAlias);
				topicRank.setTopic(topic);
				topicRank.setTkey(bType);
				topicRank.setTvalue(tValue);
				topicRanks.add(topicRank);
				if (topicRanks.size() > TopicConstants.BATCH_SIZE) {
					try {
                        dashboardService.writeTopicRank(topicRanks);
                        topicRanks.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Write topic [" + bType + "] has error, msg is " + e.getMessage());
                    }
                }
			}
		}
		try {
			if (topicRanks.size() > 0) {
                dashboardService.writeTopicRank(topicRanks);
                topicRanks.clear();
            }
		} catch (Exception e) {
            log.error("Write topic [" + bType + "] end data has error,msg is " + e.getMessage());
            e.printStackTrace();
        }
    }

	private void topicCapacityStats() {
        List<TopicRank> topicRanks = new ArrayList<>();
        for (String clusterAlias : kafkaClustersConfig.getClusterAllAlias()) {
            List<String> topics = brokerService.topicList(clusterAlias);
            for (String topic : topics) {
                long capacity = kafkaMetricsService.topicCapacity(clusterAlias, topic);
                TopicRank topicRank = new TopicRank();
                topicRank.setCluster(clusterAlias);
                topicRank.setTopic(topic);
                topicRank.setTkey(TopicConstants.CAPACITY);
                topicRank.setTvalue(capacity);
                topicRanks.add(topicRank);
                if (topicRanks.size() > TopicConstants.BATCH_SIZE) {
					try {
                        dashboardService.writeTopicRank(topicRanks);
                        topicRanks.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Write topic rank capacity has error, msg is " + e.getMessage());
                    }
                }
			}
		}
		try {
			if (topicRanks.size() > 0) {
                dashboardService.writeTopicRank(topicRanks);
                topicRanks.clear();
            }
		} catch (Exception e) {
            log.error("Write topic rank capacity end data has error,msg is " + e.getMessage());
            e.printStackTrace();
        }

    }

	private void topicLogSizeStats() {
        List<TopicRank> topicRanks = new ArrayList<>();
        for (String clusterAlias : kafkaClustersConfig.getClusterAllAlias()) {
            List<String> topics = brokerService.topicList(clusterAlias);
            for (String topic : topics) {
                long logsize = brokerService.getTopicRealLogSize(clusterAlias, topic);
                TopicRank topicRank = new TopicRank();
                topicRank.setCluster(clusterAlias);
                topicRank.setTopic(topic);
                topicRank.setTkey(TopicConstants.LOGSIZE);
                topicRank.setTvalue(logsize);
                topicRanks.add(topicRank);
                if (topicRanks.size() > TopicConstants.BATCH_SIZE) {
					try {
						dashboardService.writeTopicRank(topicRanks);
						topicRanks.clear();
					} catch (Exception e) {
                        log.error("Write topic rank logsize has error", e);
                    }
                }
			}
		}
		try {
			if (topicRanks.size() > 0) {
				dashboardService.writeTopicRank(topicRanks);
				topicRanks.clear();
			}
		} catch (Exception e) {
            log.error("Write topic rank logsize end data has error", e);
        }
    }

	private void topicProducerLogSizeStats() {
        List<TopicLogSize> topicLogSizes = new ArrayList<>();
        for (String clusterAlias : kafkaClustersConfig.getClusterAllAlias()) {
            List<String> topics = brokerService.topicList(clusterAlias);
            for (String topic : topics) {
                long logsize = brokerService.getTopicProducerLogSize(clusterAlias, topic);
                Map<String, Object> params = new HashMap<>();
                params.put("cluster", clusterAlias);
                params.put("topic", topic);
                TopicLogSize lastTopicLogSize = dashboardService.readLastTopicLogSize(params);
                TopicLogSize topicLogSize = new TopicLogSize();
                if (lastTopicLogSize == null || lastTopicLogSize.getLogsize() == 0) {
                    topicLogSize.setDiffval(0);
                } else {
                    topicLogSize.setDiffval(logsize - lastTopicLogSize.getLogsize());
                }
                topicLogSize.setCluster(clusterAlias);
                topicLogSize.setTopic(topic);
                topicLogSize.setLogsize(logsize);
                topicLogSize.setTimespan(DateUtils.getTimeSpan());
                topicLogSize.setTm(DateUtils.getCustomDate("yyyyMMdd"));
                topicLogSizes.add(topicLogSize);
                if (topicLogSizes.size() > TopicConstants.BATCH_SIZE) {
                    try {
                        dashboardService.writeTopicLogSize(topicLogSizes);
                        topicLogSizes.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Write topic producer logsize has error, msg is " + e.getCause().getMessage());
                    }
                }
			}
		}
		try {
			if (topicLogSizes.size() > 0) {
				dashboardService.writeTopicLogSize(topicLogSizes);
				topicLogSizes.clear();
			}
		} catch (Exception e) {
            log.error("Write topic producer logsize end data has error", e);
        }
    }
}
