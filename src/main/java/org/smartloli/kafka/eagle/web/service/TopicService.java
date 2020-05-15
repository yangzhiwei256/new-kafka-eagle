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
package org.smartloli.kafka.eagle.web.service;

import org.smartloli.kafka.eagle.web.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.web.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicConfig;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicSqlHistory;

import java.util.List;
import java.util.Map;

/**
 * kafka主题服务接口
 * @author smartloli.
 * Created by Jan 17, 2017.
 * Update by hexiang 20170216
 */
public interface TopicService {

    /**
     * Find topic name in all topics.
     */
    boolean hasTopic(String clusterAlias, String topicName);

    /**
     * Get metadata in topic.
     */
    List<MetadataInfo> metadata(String clusterAlias, String topicName, Map<String, Object> params);

    /**
     * Get topic numbers.
     */
    long getTopicNumbers(String clusterAlias);

    /**
     * Get topic numbers with match name.
     */
    long getTopicNumbers(String clusterAlias, String topic);

    /**
     * Get topic partition numbers.
     */
    long getPartitionNumbers(String clusterAlias, String topic);

    /**
     * Get topic list.
     */
    List<PartitionsInfo> list(String clusterAlias, Map<String, Object> params);

    /** Execute kafka query sql. */
    String execute(String clusterAlias, String sql);

    /** Get mock topics. */
    String mockTopics(String clusterAlias, String name);

    /** Get manager topic property keys. */
    String getTopicProperties(String clusterAlias, String name);

    /**
     * Alter topic config.
     */
    String changeTopicConfig(String clusterAlias, TopicConfig topicConfig);

    /** Send mock message to topic. */
    boolean mockSendMsg(String clusterAlias, String topic, String message);

    /** Get topic metrics from brokers. */
    String getTopicMBean(String clusterAlias, String topic);

    /** Get topic logsize, topicsize. */
    String getTopicSizeAndCapacity(String clusterAlias, String topic);

    /** Get topic producer logsize chart datasets. */
    String queryTopicProducerChart(Map<String, Object> params);

    /** Get select topic list. */
    String getSelectTopics(String clusterAlias, String prefixTopic);

    /**
     * Get select filter topic logsize.
     */
    String getSelectTopicsLogSize(String clusterAlias, Map<String, Object> params);

    /** Write topic sql history data into table. */
    int writeTopicSqlHistory(List<TopicSqlHistory> topicSqlHistorys);

    /** Read topic sql history data. */
    List<TopicSqlHistory> readTopicSqlHistory(Map<String, Object> params);

    /** Read topic sql history data by admin. */
    List<TopicSqlHistory> readTopicSqlHistoryByAdmin(Map<String, Object> params);

    /**
     * Count topic sql history.
     */
    long countTopicSqlHistory(Map<String, Object> params);

    /** Count topic sql history by admin. */
    long countTopicSqlHistoryByAdmin(Map<String, Object> params);

    /**
     * Find topic sql history by id.
     */
    TopicSqlHistory findTopicSqlByID(Map<String, Object> params);

    /**
     * Add clean topic logsize data.
     */
    int addCleanTopicData(List<TopicRank> topicRanks);

    /**
     * Get clean topic state.
     */
    List<TopicRank> getCleanTopicState(Map<String, Object> params);

    /**
     * 获取主题容量
     * @param params
     * @return
     */
    Long getTopicCapacity(Map<String, Object> params);
}
