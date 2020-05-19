/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.config.SingleClusterConfig;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

/**
 * Collector kafka consumer topic lag trend, metrics broker topic.
 * @author smartloli.
 * Created by Jan 15, 2019
 */
@Slf4j
@Component
public class KafkaTopicMetricsJob {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Scheduled(cron = "0 */10 * * * ?")
    public void execute() {
        List<BScreenConsumerInfo> bscreenConsumers = new ArrayList<>();
        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            if ("kafka".equals(singleClusterConfig.getOffsetStorage())) {
                JSONArray consumerGroups = JSON.parseArray(consumerService.getKafkaConsumer(singleClusterConfig.getAlias()));
                for (Object object : consumerGroups) {
                    JSONObject consumerGroup = (JSONObject) object;
                    String group = consumerGroup.getString("group");
                    for (String topic : kafkaService.getKafkaConsumerTopics(singleClusterConfig.getAlias(), group)) {
                        BScreenConsumerInfo bscreenConsumer = new BScreenConsumerInfo();
                        bscreenConsumer.setCluster(singleClusterConfig.getAlias());
                        bscreenConsumer.setGroup(group);
                        bscreenConsumer.setTopic(topic);

                        List<String> partitions = kafkaService.findTopicPartition(singleClusterConfig.getAlias(), topic);
                        Set<Integer> partitionsInts = new HashSet<>();
                        for (String partition : partitions) {
                            try {
                                partitionsInts.add(Integer.parseInt(partition));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Map<Integer, Long> partitionOffset = kafkaService.getKafkaOffset(bscreenConsumer.getCluster(), bscreenConsumer.getGroup(), bscreenConsumer.getTopic(), partitionsInts);
                        Map<TopicPartition, Long> tps = kafkaService.getKafkaLogSize(bscreenConsumer.getCluster(), bscreenConsumer.getTopic(), partitionsInts);
                        long logsize = 0L;
                        long offsets = 0L;
                        if (tps != null && partitionOffset != null) {
                            for (Entry<TopicPartition, Long> entrySet : tps.entrySet()) {
                                try {
                                    logsize += entrySet.getValue();
                                    offsets += partitionOffset.get(entrySet.getKey().partition());
                                } catch (Exception e) {
                                    log.error("Get logsize and offsets has error, msg is " + e.getCause().getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("cluster", singleClusterConfig.getAlias());
                        params.put("group", group);
                        params.put("topic", topic);
                        BScreenConsumerInfo lastBScreenConsumerTopic = metricsService.readBScreenLastTopic(params);
                        if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getLogsize() == 0) {
                            bscreenConsumer.setDifflogsize(0);
                        } else {
                            bscreenConsumer.setDifflogsize(logsize - lastBScreenConsumerTopic.getLogsize());
                        }
                        if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getOffsets() == 0) {
                            bscreenConsumer.setDiffoffsets(0);
                        } else {
                            bscreenConsumer.setDiffoffsets(offsets - lastBScreenConsumerTopic.getOffsets());
                        }
                        bscreenConsumer.setLogsize(logsize);
                        bscreenConsumer.setOffsets(offsets);
                        bscreenConsumer.setLag(logsize - offsets);
                        bscreenConsumer.setTimespan(DateUtils.getTimeSpan());
                        bscreenConsumer.setTm(DateUtils.getCustomDate("yyyyMMdd"));
                        bscreenConsumers.add(bscreenConsumer);
                        if (bscreenConsumers.size() > TopicConstants.BATCH_SIZE) {
                            try {
                                metricsService.writeBSreenConsumerTopic(bscreenConsumers);
                                bscreenConsumers.clear();
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Write bsreen kafka topic consumer has error, msg is " + e.getCause().getMessage());
                            }
                        }
                    }
                }
            } else {
                Map<String, List<String>> consumerGroups = consumerService.getConsumers(singleClusterConfig.getAlias());
                for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
                    String group = entry.getKey();
                    for (String topic : kafkaService.findActiveTopics(singleClusterConfig.getAlias(), group)) {
                        BScreenConsumerInfo bscreenConsumer = new BScreenConsumerInfo();
                        bscreenConsumer.setCluster(singleClusterConfig.getAlias());
                        bscreenConsumer.setGroup(group);
                        bscreenConsumer.setTopic(topic);
                        long logsize = brokerService.getTopicLogSizeTotal(singleClusterConfig.getAlias(), topic);
                        bscreenConsumer.setLogsize(logsize);
                        long lag = kafkaService.getLag(singleClusterConfig.getAlias(), group, topic);

                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("cluster", singleClusterConfig.getAlias());
                        params.put("group", group);
                        params.put("topic", topic);
                        BScreenConsumerInfo lastBScreenConsumerTopic = metricsService.readBScreenLastTopic(params);
                        if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getLogsize() == 0) {
                            bscreenConsumer.setDifflogsize(0);
                        } else {
                            bscreenConsumer.setDifflogsize(logsize - lastBScreenConsumerTopic.getLogsize());
                        }
                        if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getOffsets() == 0) {
                            bscreenConsumer.setDiffoffsets(0);
                        } else {
                            bscreenConsumer.setDiffoffsets((logsize - lag) - lastBScreenConsumerTopic.getOffsets());
                        }
                        bscreenConsumer.setLag(lag);
                        bscreenConsumer.setOffsets(logsize - lag);
                        bscreenConsumer.setTimespan(DateUtils.getTimeSpan());
                        bscreenConsumer.setTm(DateUtils.getCustomDate("yyyyMMdd"));
                        bscreenConsumers.add(bscreenConsumer);
                        if (bscreenConsumers.size() > TopicConstants.BATCH_SIZE) {
                            try {
                                metricsService.writeBSreenConsumerTopic(bscreenConsumers);
                                bscreenConsumers.clear();
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Write bsreen topic consumer has error, msg is " + e.getCause().getMessage());
                            }
                        }
                    }
                }
            }
        }
        try {
            if (bscreenConsumers.size() > 0) {
                try {
                    metricsService.writeBSreenConsumerTopic(bscreenConsumers);
                    bscreenConsumers.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Write bsreen final topic consumer has error, msg is " + e.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Collector consumer lag data has error,msg is " + e.getCause().getMessage());
            e.printStackTrace();
        }
    }
}
