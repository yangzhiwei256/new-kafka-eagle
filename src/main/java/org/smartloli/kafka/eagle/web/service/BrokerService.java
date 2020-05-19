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

import java.util.List;
import java.util.Map;

/**
 * Consumer group, topic and topic page or partition interface.Kafka api 2.x
 * version.
 * 
 * @author smartloli.
 *
 *         Created by Jun 13, 2019
 */
public interface BrokerService {

	/** Check topic from zookeeper metadata. */
    boolean findKafkaTopic(String clusterAlias, String topic);

	/** Get topic list. */
    List<String> topicList(String clusterAlias);

	/** Get select topic list. */
    String topicListParams(String clusterAlias, String search);

	/** Get kafka broker numbers. */
    long brokerNumbers(String clusterAlias);

	/** Get topic number from zookeeper. */
    long topicNumbers(String clusterAlias);

	/** Get topic number with match name from zookeeper. */
    long topicNumbers(String clusterAlias, String topic);

	/** Get partition number from zookeeper. */
    long partitionNumbers(String clusterAlias, String topic);

	/** Scan topic page display. */
    List<PartitionsInfo> topicRecords(String clusterAlias, Map<String, Object> params);

	/** Scan topic meta page display. */
    List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params);

	/** Get topic producer logsize total. */
    long getTopicLogSizeTotal(String clusterAlias, String topic);

    /**
     * 获取真实Kafka主题日志数据量
     * @param clusterAlias kafka集群名称
     * @param topic 主题名称
     * @return
     */
    long getTopicRealLogSize(String clusterAlias, String topic);

    /**
     * 获取kafka集群生产者发送主题日志数量
     * @param clusterAlias kafka集群名称
     * @param topic 主题名称
     * @return
     */
    long getTopicProducerLogSize(String clusterAlias, String topic);

	/** Add topic partitions. */
    Map<String, Object> createTopicPartitions(String clusterAlias, String topic, int totalCount);

	/** Get broker spread by topic. */
    int getBrokerSpreadByTopic(String clusterAlias, String topic);

	/** Get broker skewed by topic. */
    int getBrokerSkewedByTopic(String clusterAlias, String topic);

	/** Get broker leader skewed by topic. */
    int getBrokerLeaderSkewedByTopic(String clusterAlias, String topic);

}
