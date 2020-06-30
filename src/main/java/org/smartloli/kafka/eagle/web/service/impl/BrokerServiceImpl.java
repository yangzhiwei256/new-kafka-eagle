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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.zookeeper.data.Stat;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.web.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.support.KafkaAdminClientTemplate;
import org.smartloli.kafka.eagle.web.support.KafkaZkClientTemplate;
import org.smartloli.kafka.eagle.web.support.OperationCallback;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.smartloli.kafka.eagle.web.util.MathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.*;
import java.util.Map.Entry;

/**
 * Implements {@link BrokerService} all method.
 *
 * @author smartloli.
 *
 *         Created by Jun 13, 2019
 */
@Service
@Slf4j
public class BrokerServiceImpl implements BrokerService {

    private final String BROKER_IDS_PATH = "/brokers/ids";
    private final String BROKER_TOPICS_PATH = "/brokers/topics";
    private final String TOPIC_ISR = "/brokers/topics/%s/partitions/%s/state";

    @Autowired
    private KafkaZkClientTemplate kafkaZkClientTemplate;
    @Autowired
    private KafkaAdminClientTemplate kafkaAdminClientTemplate;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Kafka service interface.
     */
    @Autowired
    private KafkaService kafkaService;

    /**
     * Statistics topic total used as page.
     */
    public Integer topicNumbers(String clusterAlias) {
        return topicList(clusterAlias).size();
    }

    /**
     * Exclude kafka topic(__consumer_offsets).
     */
    private void excludeTopic(List<String> topics) {
        topics.remove(KafkaConstants.CONSUMER_OFFSET_TOPIC);
    }

    /**
     * Get search topic list numbers.
     */
    public long topicNumbers(String clusterAlias, String topic) {
        long count = 0L;
        List<String> topics = topicList(clusterAlias);
        for (String name : topics) {
            if (topic != null && name.contains(topic)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Statistics topic partitions total used as page.
     */
    @Override
    public long partitionNumbers(String clusterAlias, String topic) {

        if (KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topic)) {
            return 0L;
        }
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Long>() {
            @Override
            public Long execute(KafkaZkClient kafkaZkClient) {
                long count = 0L;
                if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic + "/partitions")) {
                    Seq<String> subBrokerTopicsPaths = kafkaZkClient.getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
                    count = JavaConversions.seqAsJavaList(subBrokerTopicsPaths).size();
                }
                return count;
            }
        });
    }

    /**
     * Get the number of page records for topic.
     */
    @Override
    public List<PartitionsInfo> topicRecords(String clusterAlias, Map<String, Object> params) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, List<PartitionsInfo>>() {
            @Override
            public List<PartitionsInfo> execute(KafkaZkClient kafkaZkClient) {
                List<PartitionsInfo> targets = new ArrayList<PartitionsInfo>();
                List<String> topics = topicList(clusterAlias);
                int start = Integer.parseInt(params.get("start").toString());
                int length = Integer.parseInt(params.get("length").toString());
                if (params.containsKey("search") && params.get("search").toString().length() > 0) {
                    String search = params.get("search").toString();
                    int offset = 0;
                    int id = start + 1;
                    for (String topic : topics) {
                        if (search != null && topic.contains(search)) {
                            if (offset < (start + length) && offset >= start) {
                                try {
                                    if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
                                        Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                                        PartitionsInfo partition = new PartitionsInfo();
                                        partition.setId(id++);
                                        partition.setCreated(DateUtils.convertUnixTime2Date(tuple._2.getCtime()));
                                        partition.setModify(DateUtils.convertUnixTime2Date(tuple._2.getMtime()));
                                        partition.setTopic(topic);
                                        String tupleString = new String(tuple._1.get());
                                        JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                                        partition.setPartitionNumbers(partitionObject.size());
                                        partition.setPartitions(partitionObject.keySet());
                                        targets.add(partition);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    log.error("Scan topic search from zookeeper has error, msg is " + ex.getMessage());
                                }
                            }
                            offset++;
                        }
                    }
                } else {
                    int offset = 0;
                    int id = start + 1;
                    for (String topic : topics) {
                        if (offset < (start + length) && offset >= start) {
                            try {
                                if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
                                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                                    PartitionsInfo partition = new PartitionsInfo();
                                    partition.setId(id++);
                                    partition.setCreated(DateUtils.convertUnixTime2Date(tuple._2.getCtime()));
                                    partition.setModify(DateUtils.convertUnixTime2Date(tuple._2.getMtime()));
                                    partition.setTopic(topic);
                                    String tupleString = new String(tuple._1.get());
                                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                                    partition.setPartitionNumbers(partitionObject.size());
                                    partition.setPartitions(partitionObject.keySet());
                                    targets.add(partition);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                log.error("Scan topic page from zookeeper has error, msg is " + ex.getMessage());
                            }
                        }
                        offset++;
                    }
                }
                return targets;
            }
        });
    }

    /**
     * Get broker spread by topic.
     */
    public int getBrokerSpreadByTopic(String clusterAlias, String topic) {
        int spread = 0;
        try {
            List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
            Set<Integer> brokerSizes = new HashSet<>();
            for (MetadataInfo meta : topicMetas) {
                List<Integer> replicasIntegers = new ArrayList<>();
                try {
                    replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
                }
                brokerSizes.addAll(replicasIntegers);
            }
            int brokerSize = kafkaService.getBrokerInfos(clusterAlias).size();
            spread = brokerSizes.size() * 100 / brokerSize;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Get topic skewed info has error, msg is ", e);
        }
        return spread;
    }

    /**
     * Get broker skewed by topic.
     */
    public int getBrokerSkewedByTopic(String clusterAlias, String topic) {
        int skewed = 0;
        try {
            List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
            int partitionAndReplicaTopics = 0;
            Set<Integer> brokerSizes = new HashSet<>();
            Map<Integer, Integer> brokers = new HashMap<>();
            for (MetadataInfo meta : topicMetas) {
                List<Integer> replicasIntegers = new ArrayList<>();
                try {
                    replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
                }
                brokerSizes.addAll(replicasIntegers);
                partitionAndReplicaTopics += replicasIntegers.size();
                for (Integer brokerId : replicasIntegers) {
                    if (brokers.containsKey(brokerId)) {
                        int value = brokers.get(brokerId);
                        brokers.put(brokerId, value + 1);
                    } else {
                        brokers.put(brokerId, 1);
                    }
                }
            }
            int brokerSize = brokerSizes.size();
            int normalSkewedValue = MathUtils.ceil(brokerSize, partitionAndReplicaTopics);
            int brokerSkewSize = 0;
            for (Entry<Integer, Integer> entry : brokers.entrySet()) {
                if (entry.getValue() > normalSkewedValue) {
                    brokerSkewSize++;
                }
            }
            skewed = brokerSkewSize * 100 / brokerSize;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Get topic skewed info has error, msg is ", e);
        }
        return skewed;
    }

    /**
     * Get broker skewed leader by topic.
     */
    public int getBrokerLeaderSkewedByTopic(String clusterAlias, String topic) {
        int leaderSkewed = 0;
        try {
            List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
            Map<Integer, Integer> brokerLeaders = new HashMap<>();
            Set<Integer> brokerSizes = new HashSet<>();
            for (MetadataInfo meta : topicMetas) {
                List<Integer> replicasIntegers = new ArrayList<>();
                try {
                    replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
                }
                brokerSizes.addAll(replicasIntegers);
                if (brokerLeaders.containsKey(meta.getLeader())) {
                    int value = brokerLeaders.get(meta.getLeader());
                    brokerLeaders.put(meta.getLeader(), value + 1);
                } else {
                    brokerLeaders.put(meta.getLeader(), 1);
                }
            }
            int brokerSize = brokerSizes.size();
            int brokerSkewLeaderNormal = MathUtils.ceil(brokerSize, topicMetas.size());
            int brokerSkewLeaderSize = 0;
            for (Entry<Integer, Integer> entry : brokerLeaders.entrySet()) {
                if (entry.getValue() > brokerSkewLeaderNormal) {
                    brokerSkewLeaderSize++;
                }
            }
            leaderSkewed = brokerSkewLeaderSize * 100 / brokerSize;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Get topic skewed info has error, msg is ", e);
        }
        return leaderSkewed;
    }

    /**
     * Check topic from zookeeper metadata.
     */
    public boolean findKafkaTopic(String clusterAlias, String topic) {
        return topicList(clusterAlias).contains(topic);
    }

    /**
     * 从Zookeeper获取Kafka集群代理节点数量
     */
    @Override
    public Integer brokerNumbers(String clusterAlias) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Integer>() {
            @Override
            public Integer execute(KafkaZkClient kafkaZkClient) {
                Integer count = 0;
                if (kafkaZkClient.pathExists(BROKER_IDS_PATH)) {
                    Seq<String> subBrokerIdsPaths = kafkaZkClient.getChildren(BROKER_IDS_PATH);
                    subBrokerIdsPaths.length();
                    count = JavaConversions.seqAsJavaList(subBrokerIdsPaths).size();
                }
                return count;
            }
        });
    }

    /**
     * Get topic list from zookeeper.
     */
    @Override
    public List<String> topicList(String clusterAlias) {
        if (kafkaClustersConfig.getClusterConfigByName(clusterAlias).getSasl().getCgroupEnable()) {
            return Arrays.asList(kafkaClustersConfig.getClusterConfigByName(clusterAlias).getSasl().getCgroupTopics().split(","));
        }
        return kafkaZkClientTemplate.doExecute(clusterAlias, kafkaZkClient -> {
            List<String> topicList = new ArrayList<>();
            if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH)) {
                Seq<String> subBrokerTopicsPaths = kafkaZkClient.getChildren(BROKER_TOPICS_PATH);
                topicList = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
                topicList.remove(KafkaConstants.CONSUMER_OFFSET_TOPIC);
            }
            return topicList;
        });
    }

    /**
     * Get select topic list from zookeeper.
     */
    public String topicListParams(String clusterAlias, String search) {
        JSONArray targets = new JSONArray();
        int limit = 15;

        List<String> topics = topicList(clusterAlias);
        try {
            if (Strings.isNullOrEmpty(search)) {
                int id = 1;
                for (String topic : topics) {
                    if (id <= limit) {
                        JSONObject object = new JSONObject();
                        object.put("id", id);
                        object.put("name", topic);
                        targets.add(object);
                        id++;
                    }
                }
            } else {
                int id = 1;
                for (String topic : topics) {
                    if (topic.contains(search)) {
                        if (id <= limit) {
                            JSONObject object = new JSONObject();
                            object.put("id", id);
                            object.put("name", topic);
                            targets.add(object);
                            id++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Get topic list has error, msg is " + e.getCause().getMessage());
            e.printStackTrace();
        }
        return targets.toJSONString();
    }

    /**
     * Scan topic meta page display from zookeeper and kafka.
     */
    @Override
    public List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, kafkaZkClient -> {
            List<MetadataInfo> targets = new ArrayList<>();
            if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH)) {
                List<String> topics = topicList(clusterAlias);
                if (topics.contains(topic)) {
                    int start = Integer.parseInt(params.get("start").toString());
                    int length = Integer.parseInt(params.get("length").toString());
                    int offset = 0;
                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                    String tupleString = new String(tuple._1.get());
                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                    Set<Integer> partitionSet = new TreeSet<>();
                    for (String partitionId : partitionObject.keySet()) {
                        partitionSet.add(Integer.valueOf(partitionId));
                    }
                    Set<Integer> partitionSortSet = new TreeSet<>(new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            int diff = o1 - o2;// asc
                            if (diff > 0) {
                                return 1;
                            } else if (diff < 0) {
                                return -1;
                            }
                            return 0;
                        }
                    });
                    partitionSortSet.addAll(partitionSet);
                    for (int partition : partitionSortSet) {
                        if (offset < (start + length) && offset >= start) {
                            String path = String.format(TOPIC_ISR, topic, partition);
                            Tuple2<Option<byte[]>, Stat> tuple2 = kafkaZkClient.getDataAndStat(path);
                            String tupleString2 = new String(tuple2._1.get());
                            JSONObject topicMetadata = JSON.parseObject(tupleString2);
                            MetadataInfo metadate = new MetadataInfo();
                            metadate.setIsr(topicMetadata.getString("isr"));
                            metadate.setLeader(topicMetadata.getInteger("leader"));
                            metadate.setPartitionId(partition);
                            metadate.setReplicas(kafkaService.getTopicPartitionReplicas(clusterAlias, topic, partition));
                            long logSize = 0L;
                            if ("kafka".equals(kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage())) {
                                logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partition);
                            } else {
                                logSize = kafkaService.getRealLogSize(clusterAlias, topic, partition);
                            }
                            List<Integer> isrIntegers = new ArrayList<>();
                            List<Integer> replicasIntegers = new ArrayList<>();
                            try {
                                isrIntegers = JSON.parseObject(metadate.getIsr(), new TypeReference<ArrayList<Integer>>() {
                                });
                                replicasIntegers = JSON.parseObject(metadate.getReplicas(), new TypeReference<ArrayList<Integer>>() {
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("Parse string to int list has error, msg is ", e);
                            }
                            // replicas lost
                            // replicas normal
                            metadate.setUnderReplicated(isrIntegers.size() != replicasIntegers.size());
                            // partition preferred leader
                            // partition occurs preferred leader exception
                            metadate.setPreferredLeader(replicasIntegers != null && replicasIntegers.size() > 0 && replicasIntegers.get(0) == metadate.getLeader());
                            metadate.setLogSize(logSize);
                            targets.add(metadate);
                        }
                        offset++;
                    }
                }
            }
            return targets;
        });
    }

    /**
     * 获取主题元数据
     *
     * @param clusterAlias 集群名称
     * @param topic        主题名称
     * @return
     */
    private List<MetadataInfo> topicMetadata(String clusterAlias, String topic) {
        return kafkaZkClientTemplate.doExecute(clusterAlias, kafkaZkClient -> {
            List<MetadataInfo> targets = new ArrayList<>();
            if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH)) {
                List<String> topics = topicList(clusterAlias);
                if (topics.contains(topic)) {
                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                    String tupleString = new String(tuple._1.get());
                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                    for (String partition : partitionObject.keySet()) {
                        String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
                        Tuple2<Option<byte[]>, Stat> tuple2 = kafkaZkClient.getDataAndStat(path);
                        String tupleString2 = new String(tuple2._1.get());
                        JSONObject topicMetadata = JSON.parseObject(tupleString2);
                        MetadataInfo metadate = new MetadataInfo();
                        metadate.setIsr(topicMetadata.getString("isr"));
                        metadate.setLeader(topicMetadata.getInteger("leader"));
                        metadate.setPartitionId(Integer.parseInt(partition));
                        metadate.setReplicas(kafkaService.getTopicPartitionReplicas(clusterAlias, topic, Integer.parseInt(partition)));
                        targets.add(metadate);
                    }
                }
            }
            return targets;
        });
    }

    /**
     * Get topic producer logsize total.
     */
    @Override
    public long getTopicLogSizeTotal(String clusterAlias, String topic) {

        if (KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topic)) {
            return 0L;
        }
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Long>() {
            @Override
            public Long execute(KafkaZkClient kafkaZkClient) {
                long logSize = 0L;
                if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                    String tupleString = new String(tuple._1.get());
                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                    Set<Integer> partitions = new HashSet<>();
                    for (String partition : partitionObject.keySet()) {
                        try {
                            partitions.add(Integer.valueOf(partition));
                        } catch (Exception e) {
                            log.error("Convert partition string to integer has error, msg is " + e.getCause().getMessage());
                            e.printStackTrace();
                        }
                    }
                    if ("kafka".equals(kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage())) {
                        logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partitions);
                    } else {
                        logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
                    }
                }
                return logSize;
            }
        });
    }

    @Override
    public long getTopicRealLogSize(String clusterAlias, String topic) {

        if (KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topic)) {
            return 0L;
        }
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Long>() {
            @Override
            public Long execute(KafkaZkClient kafkaZkClient) {
                long logSize = 0L;
                if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                    String tupleString = new String(tuple._1.get());
                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                    Set<Integer> partitions = new HashSet<>();
                    for (String partition : partitionObject.keySet()) {
                        try {
                            partitions.add(Integer.valueOf(partition));
                        } catch (Exception e) {
                            log.error("Convert partition string to integer has error", e);
                        }
                    }
                    if ("kafka".equals(kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage())) {
                        logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partitions);
                    } else {
                        logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
                    }
                }
                return logSize;
            }
        });
    }

    /**
     * Get topic producer send logsize records.
     */
    @Override
    public long getTopicProducerLogSize(String clusterAlias, String topic) {
        if (KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topic)) {
            return 0L;
        }
        return kafkaZkClientTemplate.doExecute(clusterAlias, new OperationCallback<KafkaZkClient, Long>() {
            @Override
            public Long execute(KafkaZkClient kafkaZkClient) {
                long logSize = 0L;
                if (kafkaZkClient.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
                    Tuple2<Option<byte[]>, Stat> tuple = kafkaZkClient.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
                    String tupleString = new String(tuple._1.get());
                    JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
                    Set<Integer> partitions = new HashSet<>();
                    for (String partition : partitionObject.keySet()) {
                        try {
                            partitions.add(Integer.valueOf(partition));
                        } catch (Exception e) {
                            log.error("Convert partition string to integer has error, msg is " + e.getCause().getMessage());
                            e.printStackTrace();
                        }
                    }
                    if ("kafka".equals(kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage())) {
                        logSize = kafkaService.getKafkaProducerLogSize(clusterAlias, topic, partitions);
                    } else {
                        logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
                    }
                }
                return logSize;
            }
        });
    }

    /**
     * Add topic partitions.
     */
    @Override
    public Map<String, Object> createTopicPartitions(String clusterAlias, String topic, int totalCount) {
        return kafkaAdminClientTemplate.doExecute(clusterAlias, new OperationCallback<AdminClient, Map<String, Object>>() {
            @Override
            public Map<String, Object> execute(AdminClient adminClient) {
                Map<String, Object> targets = new HashMap<>();
                int existPartitions = (int) partitionNumbers(clusterAlias, topic);
                Map<String, NewPartitions> newPartitions = new HashMap<>();
                newPartitions.put(topic, NewPartitions.increaseTo(existPartitions + totalCount));
                adminClient.createPartitions(newPartitions);
                targets.put("status", "success");
                targets.put("info", "Add topic[" + topic + "], before partition[" + existPartitions + "], after partition[" + (existPartitions + totalCount) + "] has successed.");
                return targets;
            }
        });
    }

}
