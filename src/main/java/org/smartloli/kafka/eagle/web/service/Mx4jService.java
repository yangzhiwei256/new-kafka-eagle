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
package org.smartloli.kafka.eagle.web.service;

import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanInfo;

import java.util.Map;

/**
 * Mx4jService operate command and get metadata from kafka jmx interface.
 *
 * @author smartloli.
 * Created by Jul 14, 2017
 */
public interface Mx4jService {

    /**
     * Get brokers all topics bytes in per sec.
     */
    MBeanInfo bytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /**
     * Get brokers bytes in per sec by topic.
     */
    MBeanInfo bytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /**
     * Get brokers all topics bytes out per sec.
     */
    MBeanInfo bytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /**
     * Get brokers bytes out per sec by topic.
     */
    MBeanInfo bytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /**
     * Get brokers all topics byte rejected per sec.
     */
    MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /**
     * Get brokers byte rejected per sec by topic.
     */
    MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /**
     * Get brokers all topic failed fetch request per sec.
     */
    MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /**
     * Get brokers failed fetch request per sec by topic.
     */
    MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /** Get brokers all topics failed fetch produce request per sec. */
    MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /** Get brokers failed fetch produce request per sec by topic. */
    MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /**
     * Get brokers topic all partitions log end offset.
     */
    Map<Integer, Long> logEndOffset(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    /**
     * Get brokers all topics message in per sec.
     */
    MBeanInfo messagesInPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    /**
     * Get brokers message in per sec by topic.
     */
    MBeanInfo messagesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo);

    MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);

    MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo);

	MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic);
}
