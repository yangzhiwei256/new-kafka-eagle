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

import org.smartloli.kafka.eagle.web.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.web.protocol.TopicConsumerInfo;

import java.util.List;
import java.util.Map;

/**
 * Kafka consumer data interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface ConsumerService {

    /**
     * 获取消费者信息
     * @param clusterAlias kafka集群名称
     */
    Map<String, List<String>> getConsumers(String clusterAlias);

    /** Get kafka 0.10.x consumer group & topic information. */
    String getKafkaConsumer(String clusterAlias);

    /**
     * Get active topic graph data interface.
     */
    String getActiveGraph(String clusterAlias);

    /**
     * Storage offset in kafka or zookeeper interface.
     */
    String getActiveTopic(String clusterAlias, String formatter);

    /**
     * Judge consumer detail information storage offset in kafka or zookeeper
     * interface.
     */
    List<TopicConsumerInfo> getConsumerDetail(String clusterAlias, String formatter, String group);

    /**
     * Judge consumers storage offset in kafka or zookeeper interface.
     */
    String getConsumer(String clusterAlias, String formatter, DisplayInfo page);

    /**
     * Judge consumers storage offset in kafka or zookeeper interface by db.
     */
    String getConsumerByDB(String clusterAlias, DisplayInfo page);

    /**
     * Get consumer size from kafka topic interface.
     * @param clusterAlias kafka集群名称
     * @param storeFormat kafka元数据存储位置：kafka/zk
     */
    int getConsumerCount(String clusterAlias, String storeFormat);

    /** Get kafka consumer groups. */
    int getKafkaConsumerGroups(String clusterAlias);

    /**
     * Check if the application is consuming.
     */
    int isConsumering(String clusterAlias, String group, String topic);

    /** Get consumer group count by db. */
    long getConsumerCountByDB(String clusterAlias);

}
