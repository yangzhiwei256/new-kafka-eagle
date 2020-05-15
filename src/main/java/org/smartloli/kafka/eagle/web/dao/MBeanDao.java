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

import org.smartloli.kafka.eagle.web.protocol.KpiInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicOffsetsInfo;

import java.util.List;
import java.util.Map;

/**
 * MBeanDao interface definition
 * 
 * @author smartloli.
 *
 *         Created by Jul 19, 2017
 */
public interface MBeanDao {

    /**
     * Collection statistics data from kafka jmx & insert into table.
     */
    int insert(List<KpiInfo> kpi);

    /**
     * Collection statistics offline data from kafka jmx & insert into table.
     */
    int mbeanOfflineInsert(List<MBeanOfflineInfo> kpi);

    /**
     * Query collector data.
     */
    List<KpiInfo> query(Map<String, Object> params);

    /**
     * Query collector data.
     */
    List<MBeanOfflineInfo> getMBeanOffline(Map<String, Object> params);

    /**
     * Get broker kpi, such bytein or byteout etc.
     */
    KpiInfo getBrokersKpi(Map<String, Object> params);

    /**
     * Crontab clean data.
     */
    void remove(int tm);

    /**
     * Get consumer offset topic metrics.
     */
    List<BScreenConsumerInfo> getConsumerOffsetsTopic(Map<String, Object> params);

    /** Get consumer rate topic metrics. */
    List<TopicOffsetsInfo> getConsumerRateTopic(Map<String, Object> params);

    /** Query os memory data. */
    List<KpiInfo> getOsMem(Map<String, Object> params);

}
