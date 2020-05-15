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
/**
 * 
 */
package org.smartloli.kafka.eagle.factory;

import kafka.zk.KafkaZkClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.service.ZkService;
import org.smartloli.kafka.eagle.web.service.impl.KafkaServiceImpl;
import org.smartloli.kafka.eagle.web.service.impl.ZkServiceImpl;
import org.smartloli.kafka.eagle.web.util.KafkaResourcePoolUtils;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.*;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Mar 24, 2017
 */
public class TestKafkaServiceImpl {

	private final String BROKER_TOPICS_PATH = "/brokers/topics";

	private static final KafkaService kafkaService = new KafkaServiceImpl();

	private static final ZkService zkService = new ZkServiceImpl();

	public static void main(String[] args) {

		long logsize = kafkaService.getKafkaLogSize("cluster1", "kafka20191217", 0);
		System.out.println(logsize);

		String res = kafkaService.getKafkaOffset("cluster1");
		System.out.println(res);

		Set<Integer> partitionids = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			partitionids.add(i);
		}
		Map<Integer, Long> offsets = kafkaService.getKafkaOffset("cluster1", "kafka_app0", "test_16", partitionids);
		System.out.println("offsets: " + offsets);
	}

	public Map<TopicPartition, Long> getKafkaLogSize(String topic, Set<Integer> partitionIds) {
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConstants.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        Map<TopicPartition, Long> topicPartitionLongMap = new HashMap<>(partitionIds.size());
		for (int partitionId : partitionIds) {
			TopicPartition tp = new TopicPartition(topic, partitionId);
			long offset = consumer.position(tp);
            topicPartitionLongMap.put(tp, offset);
		}
        consumer.close();
        return topicPartitionLongMap;
	}

	public List<String> findTopicPartition(String clusterAlias, String topic) {
        KafkaZkClient zkc = KafkaResourcePoolUtils.getZookeeperClient(clusterAlias);
        Seq<String> brokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
        List<String> topicAndPartitions = JavaConversions.seqAsJavaList(brokerTopicsPaths);
        KafkaResourcePoolUtils.release(clusterAlias, zkc);
        return topicAndPartitions;
    }

}
