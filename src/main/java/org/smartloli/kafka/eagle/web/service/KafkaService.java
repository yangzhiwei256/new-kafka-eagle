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

import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.protocol.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kafka group,topic and partition interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017
 * 
 *         Update by hexiang 20170216
 */
public interface KafkaService {

	/** Find topic and group exist in zookeeper. */
	boolean findTopicAndGroupExist(String clusterAlias, String topic, String group);

    /**
     * Obtaining metadata in zookeeper by topic.
     */
    List<String> findTopicPartition(String clusterAlias, String topic);

    /**
     * Get kafka active consumer topic.
     */
    Map<String, List<String>> getActiveTopic(String clusterAlias);

    /**
     * Get kafka active consumer topic.
     */
    Set<String> getActiveTopic(String clusterAlias, String group);

    /**
     * 通过别名获取Kafka 代理节点信息
     *
     * @param clusterName kafka集群列表
     * @return
     */
    List<KafkaBrokerInfo> getBrokerInfos(String clusterName);

    /**
     * 通过别名获取Kafka 代理节点信息
     *
     * @param clusterNames kafka集群列表
     * @return
     */
    Map<String, List<KafkaBrokerInfo>> getAllBrokerInfos(List<String> clusterNames);

    /**
     * Get broker host info from ids.
     */
    String getBrokerJMXFromIds(String clusterAlias, int ids);

    /**
     * Obtaining kafka consumer information from zookeeper.
     */
    Map<String, List<String>> getConsumers(String clusterAlias);

    /**
     * Obtaining kafka consumer page information from zookeeper.
     */
    Map<String, List<String>> getConsumers(String clusterAlias, DisplayInfo page);

	/** According to group, topic and partition to get offset from zookeeper. */
	OffsetZkInfo getOffset(String clusterAlias, String topic, String group, int partition);

	/** Get kafka 0.10.x offset from topic. */
	String getKafkaOffset(String clusterAlias);
	
	/** Get the data for the topic partition in the specified consumer group */
	Map<Integer, Long> getKafkaOffset(String clusterAlias, String group, String topic, Set<Integer> partitionids);

	/** Use kafka console comand to create topic. */
	Map<String, Object> create(String clusterAlias, String topicName, String partitions, String replic);

	/** Use kafka console command to delete topic. */
    Map<String, Object> delete(String clusterAlias, String topicName);

    /**
     * 解析集群代理服务器信息
     * @param clusterAlias kafka集群名称
     * @return
     */
    String parseBrokerServer(String clusterAlias);

	/** Convert query kafka to topic in the sql message for standard sql. */
	KafkaSqlInfo parseSql(String clusterAlias, String sql);

	/** Get kafka 0.10.x active consumer group & topics. */
	Set<String> getKafkaActiverTopics(String clusterAlias, String group);

	/** Get kafka 0.10.x consumer topic, maybe consumer topic owner is null. */
	Set<String> getKafkaConsumerTopics(String clusterAlias, String group);

	/** Get kafka 0.10.x consumer group & topic information. */
	String getKafkaConsumer(String clusterAlias);
	
	/** Get kafka 0.10.x consumer group & topic information used for page. */
	String getKafkaConsumer(String clusterAlias, DisplayInfo displayInfo);

	@Deprecated
	/** Get kafka consumer information pages. */ String getKafkaActiverSize(String clusterAlias, String group);

	/** Get kafka consumer information pages not owners. */
	OwnerInfo getKafkaActiverNotOwners(String clusterAlias, String group);

	/** Get kafka broker bootstrap server. */
	String getKafkaBrokerServer(String clusterAlias);

	/** Get kafka consumer topics. */
	Set<String> getKafkaConsumerTopic(String clusterAlias, String group);

	/** Get kafka consumer group & topic. */
	String getKafkaConsumerGroupTopic(String clusterAlias, String group);

	/** Get kafka topic history logsize . */
	long getKafkaLogSize(String clusterAlias, String topic, int partitionid);

	/** Get kafka topic history batch logsize. */
	Map<TopicPartition, Long> getKafkaLogSize(String clusterAlias, String topic, Set<Integer> partitionids);

	/** Get kafka topic real logsize by partitionid. */
	long getKafkaRealLogSize(String clusterAlias, String topic, int partitionid);

	/** Get kafka topic real logsize by partitionid set. */
	long getKafkaRealLogSize(String clusterAlias, String topic, Set<Integer> partitionids);
	
	/** Get topic producer send logsize records. */
	long getKafkaProducerLogSize(String clusterAlias, String topic, Set<Integer> partitionids);

	/** Get kafka sasl topic metadate. */
	List<MetadataInfo> findKafkaLeader(String clusterAlias, String topic);

	/** Send mock message to kafka. */
	boolean mockMessage(String clusterAlias, String topic, String message);

	/** Get kafka consumer group all topics lag. */
	long getKafkaLag(String clusterAlias, String group, String topic);

	/** Get consumer group all topics lag. */
	long getLag(String clusterAlias, String group, String topic);

	/** Get kafka history logsize by old version. */
	long getLogSize(String clusterAlias, String topic, int partitionid);

	/** Get kafka history logsize by old version. */
	long getLogSize(String clusterAlias, String topic, Set<Integer> partitionids);

	/** Get kafka real logsize by old version partition set. */
	long getRealLogSize(String clusterAlias, String topic, int partitionid);

    /**
     * Get topic metadata.
     */
    String getReplicasIsr(String clusterAlias, String topic, int partitionid);

    /**
     * Get kafka version.
     */
    String getKafkaVersion(String host, int port, String ids, String clusterAlias);

    /**
     * Get kafka os memory.
     */
    long getOSMemory(String host, int port, String property);
}
