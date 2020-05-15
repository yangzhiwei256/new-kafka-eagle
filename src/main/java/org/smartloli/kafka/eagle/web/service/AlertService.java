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

import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicLogSize;

import java.util.List;
import java.util.Map;

/**
 * Alarm service interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface AlertService {

    /**
     * Add alerter interface.
     */
    int insertAlarmConsumer(AlarmConsumerInfo alarmConsumer);

    /**
     * Get consumer group alarm.
     */
    String getAlarmConsumerGroup(String clusterAlias, String formatter, String search);

    /**
     * Get consumer topic alarm.
     */
    String getAlarmConsumerTopic(String clusterAlias, String formatter, String group, String search);

    /**
     * List alarmer information.
     */
    List<AlarmConsumerInfo> getAlarmConsumerAppList(Map<String, Object> params);

    /**
     * Count alarm consumer application size.
     */
    int alertConsumerAppCount(Map<String, Object> params);

    /**
     * Modify consumer alarm switch.
     */
    int modifyConsumerAlertSwitchById(AlarmConsumerInfo alarmConsumer);

    /** Find alarm consumer info by id. */
    AlarmConsumerInfo findAlarmConsumerAlertById(int id);

    /** Get all alarm consumer tasks. */
    List<AlarmConsumerInfo> getAllAlarmConsumerTasks();

    /** Delete alarm consumer by id. */
    int deleteAlarmConsumerById(int id);

    /** Modify alarm consumer info by id. */
    int modifyAlarmConsumerById(AlarmConsumerInfo alarmConsumer);

    /**
     * Modify alert consumer(alarmtimes,isnormal) info by id.
     */
    int modifyConsumerStatusAlertById(AlarmConsumerInfo alarmConsumer);

    /**
     * Storage or update alarm cluster,such as kafka or zookeeper.
     */
    int create(AlarmClusterInfo clusterInfo);

    /**
     * List cluster information from alert.
     */
    List<AlarmClusterInfo> getAlarmClusterList(Map<String, Object> params);

    /**
     * Get all alarm cluster tasks.
     */
    List<AlarmClusterInfo> getAllAlarmClusterTasks();

    int getAlarmClusterCount(Map<String, Object> params);

    /**
     * Delete alert by id.
     */
    int deleteAlarmClusterAlertById(int id);

    /**
     * Find alert cluster info by id.
     */
    AlarmClusterInfo findAlarmClusterAlertById(int id);

    /**
     * Modify alarm cluster switch by id.
     */
    int modifyClusterAlertSwitchById(AlarmClusterInfo clusterInfo);

    /**
     * Modify alert cluster(server,alarm group,alarm level) info by id.
     */
    int modifyClusterAlertById(AlarmClusterInfo cluster);

    /** Modify alert cluster(alarmtimes,isnormal) info by id. */
    int modifyClusterStatusAlertById(AlarmClusterInfo cluster);

    /** Get alert type list. */
    String getAlertTypeList();

    /**
     * Get alert cluster type list.
     */
    String getAlertClusterTypeList(String type, Map<String, Object> params);

    /** Storage or update alarm config info. */
    int insertOrUpdateAlarmConfig(AlarmConfigInfo alarmConfig);

    /** Find alarm config by group name. */
    boolean findAlarmConfigByGroupName(Map<String, Object> params);

    /** Get alarm config list. */
    List<AlarmConfigInfo> getAlarmConfigList(Map<String, Object> params);

    /** Get alarm config count. */
    int alarmConfigCount(Map<String, Object> params);

    /** Delete alarm config by group name. */
    int deleteAlertByGroupName(Map<String, Object> params);

    /** Get alarm config by group name. */
    AlarmConfigInfo getAlarmConfigByGroupName(Map<String, Object> params);

    /** Get lastest lag used to alarm consumer. */
    long queryLastestLag(Map<String, Object> params);

	/** Get topic producer logsize by alarm. */
    List<TopicLogSize> queryTopicProducerByAlarm(Map<String, Object> params);
}
