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
package org.smartloli.kafka.eagle.web.dao;

import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenBarInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicSqlHistory;

import java.util.List;
import java.util.Map;

/**
 * TopicDao interface definition
 * 
 * @author smartloli.
 *
 *         Created by Jul 27, 2019
 */
public interface TopicDao {

    /**
     * Write statistics topic rank data from kafka jmx & insert into table.
     */
    int writeTopicRank(List<TopicRank> topicRanks);

    /**
     * Crontab clean topic rank history data.
     */
    void cleanTopicRank(int tm);

    /**
     * Read topic rank data.
     */
    List<TopicRank> readTopicRank(Map<String, Object> params);

    /**
     * Get clean topic state.
     */
    List<TopicRank> getCleanTopicState(Map<String, Object> params);

    /**
     * Get all clean topic list.
     */
    List<TopicRank> getCleanTopicList(Map<String, Object> params);

    /**
     * Read topic spread, skewed, leader skewed data.
     */
    TopicRank readBrokerPerformance(Map<String, Object> params);

    /** Get topic total capacity. */
    long getTopicCapacity(Map<String, Object> params);

    /**
     * Write statistics topic logsize data from kafka jmx & insert into table.
     */
    int writeTopicLogSize(List<TopicLogSize> topicLogSize);

    /** Crontab clean topic logsize history data. */
    void cleanTopicLogSize(int tm);

    /** Read topic lastest logsize diffval data. */
    TopicLogSize readLastTopicLogSize(Map<String, Object> params);

    /**
     * Get topic producer logsize chart datasets.
     */
    List<TopicLogSize> queryTopicProducerChart(Map<String, Object> params);

    /** Get topic producer logsize by alarm. */
    List<TopicLogSize> queryTopicProducerByAlarm(Map<String, Object> params);

    /** Get producer history bar data. */
    List<BScreenBarInfo> queryProducerHistoryBar(Map<String, Object> params);

    /** Write topic sql history data into table. */
    int writeTopicSqlHistory(List<TopicSqlHistory> topicSqlHistorys);

    /** Read topic sql history data. */
    List<TopicSqlHistory> readTopicSqlHistory(Map<String, Object> params);

    /** Read topic sql history data by admin. */
    List<TopicSqlHistory> readTopicSqlHistoryByAdmin(Map<String, Object> params);

    /** Count topic sql history. */
    long countTopicSqlHistory(Map<String, Object> params);

    /** Count topic sql history by admin. */
    long countTopicSqlHistoryByAdmin(Map<String, Object> params);

    /** Find topic sql history by id. */
    TopicSqlHistory findTopicSqlByID(Map<String, Object> params);

    /** Crontab clean topic sql history data. */
    void cleanTopicSqlHistory(int tm);

    /** Get bscreen topic total records. */
    long getBScreenTotalRecords(Map<String, Object> params);

    /**
     * Write statistics big screen consumer topic.
     */
    int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers);

    /** Crontab clean big screen topic history data. */
    void cleanBScreenConsumerTopic(int tm);

    /** Read big screen topic lastest diffval data. */
    BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params);

    /** Get consumer history bar data. */
    List<BScreenBarInfo> queryConsumerHistoryBar(Map<String, Object> params);

    /**
     * Get bscreen consumer by today, such logsize offset and lag diff.
     */
    List<BScreenConsumerInfo> queryTodayBScreenConsumer(Map<String, Object> params);

    /** Get lastest lag used to alarm consumer. */
    long queryLastestLag(Map<String, Object> params);

    /** Write consumer group topic. */
    int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups);

    /** Clean consumer group topics. */
    int cleanConsumerGroupTopic(Map<String, Object> params);

    /** Get all consumer groups. */
    List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params);

    /** Get consumer group pages. */
    List<ConsumerGroupsInfo> getConsumerGroupPages(Map<String, Object> params);

    /** Count consumer group pages. */
    long countConsumerGroupPages(Map<String, Object> params);

}
