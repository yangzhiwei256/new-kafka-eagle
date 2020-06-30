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

import com.alibaba.fastjson.JSONArray;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.protocol.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kafka group,topic and partition interface.
 * @author smartloli.
 * Created by Jan 18, 2017
 * Update by hexiang 20170216
 */
public interface KafkaService {

    /**
     * 查询消费组是否消费指定主题
     * @param clusterAlias kafka集群名称
     * @param topic 主题名称
     * @param consumerGroup 消费组名称
     * @return
     */
	boolean findTopicExistInGroup(String clusterAlias, String topic, String consumerGroup);

    /**
     * 获取主题分区信息
     * @param clusterAlias 集群名
     * @param topic 主题名称
     * @return List.
     */
    List<String> findTopicPartition(String clusterAlias, String topic);

    /**
     * 获取活跃kafka消费者主题信息
     * @param clusterName kafka系群名
     */
    Map<String, List<String>> findActiveTopics(String clusterName);

    /**
     * 获取kafka集群消费组订阅的活跃主题信息
     * @param clusterAlias kafka集群名称
     * @param consumerGroup 消费组
     * @return
     */
    Set<String> findActiveTopics(String clusterAlias, String consumerGroup);

    /**
     * 通过别名获取Kafka 代理节点信息
     *
     * @param clusterName kafka集群名称
     * @return
     */
    List<KafkaBrokerInfo> getBrokerInfos(String clusterName);

    /**
     * 通过别名获取Kafka 代理节点信息
     * @param clusterNames kafka集群列表
     * @return
     */
    Map<String, List<KafkaBrokerInfo>> getBrokerInfos(List<String> clusterNames);

    /**
     * Get broker host info from ids.
     */
    String getBrokerJMXFromIds(String clusterAlias, int ids);

    /**
     * 从Zookeeper获取分页消费者信息
     *
     * @param clusterAlias kafka集群名称
     * @param displayInfo 分页请求
     */
    Map<String, List<String>> getConsumers(String clusterAlias, DisplayInfo displayInfo);

    /**
     * 获取消费组监听主题的偏移量
     * @param clusterAlias kafka集群名称
     * @param topic 主题名称
     * @param group 消费组名
     * @param partition 分区号
     * @return
     */
	OffsetZkInfo getGroupTopicPartitionOffset(String clusterAlias, String topic, String group, int partition);

	/** Get kafka 0.10.x offset from topic. */
	String getKafkaOffset(String clusterAlias);
	
	/** Get the data for the topic partition in the specified consumer group */
	Map<Integer, Long> getKafkaOffset(String clusterAlias, String group, String topic, Set<Integer> partitionids);

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
	
	/** Get kafka 0.10.x consumer group & topic information used for page. */
	String getKafkaConsumer(String clusterAlias, DisplayInfo displayInfo);

    /**
     * 获取Kafka元数据信息
     * @param clusterAlias kafka集群名称
     * @param group 消费组名称
     * @return
     */
    JSONArray getKafkaMetadata(String clusterAlias, String group);

	@Deprecated
	/** Get kafka consumer information pages. */ String getKafkaActiverSize(String clusterAlias, String group);

	/** Get kafka consumer information pages not owners. */
	OwnerInfo getKafkaActiveNotOwners(String clusterAlias, String group);

	/** Get kafka broker bootstrap server. */
	String getKafkaBrokerServer(String clusterAlias);

	/** Get kafka consumer topics. */
	Set<String> getKafkaConsumerTopic(String clusterAlias, String group);

	/** Get kafka consumer group & topic. */
	String getKafkaConsumerGroupTopic(String clusterAlias, String group);

	/** Get kafka topic history logsize . */
	long getKafkaLogSize(String clusterAlias, String topic, int partitionid);

	/** Get kafka topic history batch logsize. */
	Map<TopicPartition, Long> getKafkaLogSize(String clusterAlias, String topic, Set<Integer> partitionIds);

	/** Get kafka topic real logsize by partitionid. */
	long getKafkaRealLogSize(String clusterAlias, String topic, int partitionId);

    /**
     * Get kafka topic real logsize by partitionid set.
     */
    long getKafkaRealLogSize(String clusterAlias, String topic, Set<Integer> partitionIds);

    /**
     * Get topic producer send logsize records.
     */
    long getKafkaProducerLogSize(String clusterAlias, String topic, Set<Integer> partitionIds);

    /**
     * 获取Kafka主题Leader 远点元数据
     */
    List<MetadataInfo> findKafkaLeader(String clusterAlias, String topic);

    /**
     * 发送kafka模拟消息
     */
    boolean mockMessage(String clusterAlias, String topic, String message);

    /**
     * Get kafka consumer group all topics lag.
     */
    long getKafkaLag(String clusterAlias, String group, String topic);

    /**
     * Get consumer group all topics lag.
     */
    long getLag(String clusterAlias, String group, String topic);

    /**
     * Get kafka history logsize by old version.
     */
    long getLogSize(String clusterAlias, String topic, int partitionid);

	/** Get kafka history logsize by old version. */
	long getLogSize(String clusterAlias, String topic, Set<Integer> partitionids);

	/** Get kafka real logsize by old version partition set. */
	long getRealLogSize(String clusterAlias, String topic, int partitionid);

    /**
     * 获取主题分区的复制分区编号信息
     * @param clusterAlias kafka集群信息
     * @param topic 主题名称
     * @param partitionId 分区id
     * @return
     */
    String getTopicPartitionReplicas(String clusterAlias, String topic, int partitionId);

    /**
     * 获取KAFKA版本
     */
    String getKafkaVersion(String clusterAlias, KafkaBrokerInfo kafkaBrokerInfo, String ids);

    /**
     * Get kafka os memory.
     */
    long getOSMemory(KafkaBrokerInfo kafkaBrokerInfo, String property);
}
