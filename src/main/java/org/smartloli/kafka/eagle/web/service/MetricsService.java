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

import org.smartloli.kafka.eagle.web.protocol.KpiInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.consumer.ConsumerGroupsInfo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Define access to the kafka monitoring data interface via jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 17, 2017
 */
public interface MetricsService {

    /**
     * 获取Kafka集群所有代理节点监控数据
     * @param clusterAlias kafka集群名称
     * @return
     */
    String getAllBrokersMBean(String clusterAlias);

    /**
     * Collection statistics data from kafka jmx & insert into table.
     */
    int insert(List<KpiInfo> kpi);

    /**
     * Collection statistics data from kafka jmx & insert into table.
     */
    int mbeanOfflineInsert(List<MBeanOfflineInfo> kpis);

    /**
     * Query MBean data in different dimensions.
     */
    String query(Map<String, Object> params) throws ParseException;

    /** Crontab clean data. */
    void remove(int tm);

    /** Crontab clean topic logsize history data. */
    void cleanTopicLogSize(int tm);

    /** Crontab clean topic rank history data. */
    void cleanTopicRank(int tm);

    /** Crontab clean topic sql history data. */
    void cleanTopicSqlHistory(int tm);

    /** Crontab clean big screen topic history data. */
    void cleanBScreenConsumerTopic(int tm);

    /**
     * Write statistics big screen consumer topic.
     */
    int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers);

    /**
     * Read big screen topic lastest diffval data.
     */
    BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params);

    /**
     * Write consumer group topics.
     */
    int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups);

    /** Clean consumer group topics. */
    int cleanConsumerGroupTopic(Map<String, Object> params);

    /** Get all consumer groups. */
    List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params);
}
