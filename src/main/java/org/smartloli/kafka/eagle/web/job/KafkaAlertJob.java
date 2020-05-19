/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.alarm.AlarmMessageInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.web.service.AlertService;
import org.smartloli.kafka.eagle.web.service.IMService;
import org.smartloli.kafka.eagle.web.service.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.impl.IMServiceImpl;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.smartloli.kafka.eagle.web.util.NetUtils;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka事件通知服务
 *
 * @author smartloli.
 * Created by Oct 28, 2018
 */
@Slf4j
@Component
public class KafkaAlertJob {

    @Autowired
    private AlertService alertService;

    /** Kafka topic config service interface. */
    @Autowired
    private KafkaMetricsService kafkaMetricsService;

    @Autowired
    private IMService imService;

    @Scheduled(cron = "0 */10 * * * ?")
    protected void execute() {
        consumer();
        cluster();
    }

    /**
     * 消费者服务
     */
    public void consumer() {
        try {
            List<AlarmConsumerInfo> alarmConsumers = alertService.getAllAlarmConsumerTasks();
            for (AlarmConsumerInfo alarmConsumer : alarmConsumers) {
                if (KafkaConstants.DISABLE.equals(alarmConsumer.getIsEnable())) {
                    break;
                }

                Map<String, Object> map = new HashMap<>();
                map.put("cluster", alarmConsumer.getCluster());
                map.put("alarmGroup", alarmConsumer.getAlarmGroup());
                AlarmConfigInfo alarmConfig = alertService.getAlarmConfigByGroupName(map);

                Map<String, Object> params = new HashMap<>();
                params.put("cluster", alarmConsumer.getCluster());
                params.put("tday", DateUtils.getCustomDate("yyyyMMdd"));
                params.put("group", alarmConsumer.getGroup());
                params.put("topic", alarmConsumer.getTopic());
                // real consumer lag
                long lag = alertService.queryLastestLag(params);
                if (lag > alarmConsumer.getLag() && (alarmConsumer.getAlarmTimes() < alarmConsumer.getAlarmMaxTimes() || alarmConsumer.getAlarmMaxTimes() == -1)) {
                    // alarm consumer
                    alarmConsumer.setAlarmTimes(alarmConsumer.getAlarmTimes() + 1);
                    alarmConsumer.setIsNormal("N");
                    alertService.modifyConsumerStatusAlertById(alarmConsumer);
                    sendAlarmConsumerError(alarmConfig, alarmConsumer, lag);
                } else if (lag <= alarmConsumer.getLag()) {
                    if (alarmConsumer.getIsNormal().equals("N")) {
                        alarmConsumer.setIsNormal("Y");
                        // clear error alarm and reset
                        alarmConsumer.setAlarmTimes(0);
                        // notify the cancel of the alarm
                        alertService.modifyConsumerStatusAlertById(alarmConsumer);
                        sendAlarmConsumerNormal(alarmConfig, alarmConsumer, lag);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Alarm consumer lag has error", ex);
        }
    }

    private void sendAlarmConsumerError(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag) {
        if (alarmConfing.getAlarmType().equals(KafkaConstants.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("Kafka Eagle Alarm Consumer Notice");
            alarmMsg.setAlarmContent("lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            object.put("msg", alarmMsg.toMail());
            imService.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Consumer Notice</font>** \n\n");
            alarmMsg.setAlarmContent("<font color=\"#FF0000\">lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            imService.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.KafkaConstants)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("`Kafka Eagle Alarm Consumer Notice`\n");
            alarmMsg.setAlarmContent("<font color=\"warning\">lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        }
    }

    private void sendAlarmConsumerNormal(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag) {
        if (alarmConfing.getAlarmType().equals(KafkaConstants.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("Kafka Eagle Alarm Consumer Cancel");
            alarmMsg.setAlarmContent("lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("NORMAL");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            object.put("msg", alarmMsg.toMail());
            imService.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Consumer Cancel</font>** \n\n");
            alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            imService.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.KafkaConstants)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(alarmConsumer.getId());
            alarmMsg.setTitle("`Kafka Eagle Alarm Consumer Cancel`\n");
            alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
            alarmMsg.setAlarmProject("Consumer");
            alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
            alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        }
    }

    public void cluster() {
        for (AlarmClusterInfo cluster : alertService.getAllAlarmClusterTasks()) {
            if (KafkaConstants.DISABLE.equals(cluster.getIsEnable())) {
                break;
            }
            String alarmGroup = cluster.getAlarmGroup();
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", cluster.getCluster());
            params.put("alarmGroup", alarmGroup);
            AlarmConfigInfo alarmConfig = alertService.getAlarmConfigByGroupName(params);
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject topicAlarmJson = JSON.parseObject(cluster.getServer());
                String topic = topicAlarmJson.getString("topic");
                long alarmCapacity = topicAlarmJson.getLong("capacity");
                long realCapacity = kafkaMetricsService.topicCapacity(cluster.getCluster(), topic);
                JSONObject alarmTopicMsg = new JSONObject();
                alarmTopicMsg.put("topic", topic);
                alarmTopicMsg.put("alarmCapacity", alarmCapacity);
                alarmTopicMsg.put("realCapacity", realCapacity);
                if (realCapacity > alarmCapacity && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
                    cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
                    cluster.setIsNormal("N");
                    alertService.modifyClusterStatusAlertById(cluster);
                    try {
                        sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                    } catch (Exception e) {
                        log.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
                    }
                } else if (realCapacity < alarmCapacity) {
                    if (cluster.getIsNormal().equals("N")) {
                        cluster.setIsNormal("Y");
                        // clear error alarm and reset
                        cluster.setAlarmTimes(0);
                        // notify the cancel of the alarm
                        alertService.modifyClusterStatusAlertById(cluster);
                        try {
                            sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                        } catch (Exception e) {
                            log.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
                        }
                    }
                }
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject producerAlarmJson = JSON.parseObject(cluster.getServer());
                String topic = producerAlarmJson.getString("topic");
                String[] speeds = producerAlarmJson.getString("speed").split(",");
                long startSpeed = 0L;
                long endSpeed = 0L;
                if (speeds.length == 2) {
                    startSpeed = Long.parseLong(speeds[0]);
                    endSpeed = Long.parseLong(speeds[1]);
                }
                Map<String, Object> producerSpeedParams = new HashMap<>();
                producerSpeedParams.put("cluster", cluster.getCluster());
                producerSpeedParams.put("topic", topic);
                producerSpeedParams.put("stime", DateUtils.getCustomDate("yyyyMMdd"));
                List<TopicLogSize> topicLogSizes = alertService.queryTopicProducerByAlarm(producerSpeedParams);
                long realSpeed = 0;
                if (topicLogSizes != null && topicLogSizes.size() > 0) {
                    realSpeed = topicLogSizes.get(0).getDiffval();
                }

                JSONObject alarmTopicMsg = new JSONObject();
                alarmTopicMsg.put("topic", topic);
                alarmTopicMsg.put("alarmSpeeds", startSpeed + "," + endSpeed);
                alarmTopicMsg.put("realSpeeds", realSpeed);
                if ((realSpeed < startSpeed || realSpeed > endSpeed) && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
                    cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
                    cluster.setIsNormal("N");
                    alertService.modifyClusterStatusAlertById(cluster);
                    try {
                        sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                    } catch (Exception e) {
                        log.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
                    }
                } else if (realSpeed >= startSpeed && realSpeed <= endSpeed) {
                    if (cluster.getIsNormal().equals("N")) {
                        cluster.setIsNormal("Y");
                        // clear error alarm and reset
                        cluster.setAlarmTimes(0);
                        // notify the cancel of the alarm
                        alertService.modifyClusterStatusAlertById(cluster);
                        try {
                            sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                        } catch (Exception e) {
                            log.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
                        }
                    }
                }
            } else {
                String[] servers = cluster.getServer().split(",");
                List<String> errorServers = new ArrayList<String>();
                List<String> normalServers = new ArrayList<String>();
                for (String server : servers) {
                    String host = server.split(":")[0];
                    int port = 0;
                    try {
                        port = Integer.parseInt(server.split(":")[1]);
                        boolean status = NetUtils.telnet(host, port);
                        if (!status) {
                            errorServers.add(server);
                        } else {
                            normalServers.add(server);
                        }
                    } catch (Exception e) {
                        log.error("Alarm cluster has error, msg is " + e.getCause().getMessage());
                        e.printStackTrace();
                    }
                }
                if (errorServers.size() > 0 && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
                    cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
                    cluster.setIsNormal("N");
                    alertService.modifyClusterStatusAlertById(cluster);
                    try {
                        sendAlarmClusterError(alarmConfig, cluster, errorServers.toString());
                    } catch (Exception e) {
                        log.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
                    }
                } else if (errorServers.size() == 0) {
                    if (cluster.getIsNormal().equals("N")) {
                        cluster.setIsNormal("Y");
                        // clear error alarm and reset
                        cluster.setAlarmTimes(0);
                        // notify the cancel of the alarm
                        alertService.modifyClusterStatusAlertById(cluster);
                        try {
                            sendAlarmClusterNormal(alarmConfig, cluster, normalServers.toString());
                        } catch (Exception e) {
                            log.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
                        }
                    }
                }
            }
        }
    }

    private void sendAlarmClusterError(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
        if (alarmConfing.getAlarmType().equals(KafkaConstants.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("Kafka Eagle Alarm Cluster Notice");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            object.put("msg", alarmMsg.toMail());
            imService.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Cluster Notice</font>** \n\n");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"#FF0000\">topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"#FF0000\">producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"#FF0000\">node.shutdown [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            imService.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.KafkaConstants)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("`Kafka Eagle Alarm Cluster Notice`\n");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"warning\">topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"warning\">producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"warning\">node.shutdown [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        }
    }

    private void sendAlarmClusterNormal(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
        if (alarmConfing.getAlarmType().equals(KafkaConstants.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("Kafka Eagle Alarm Cluster Cancel");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.alive [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("NORMAL");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            object.put("msg", alarmMsg.toMail());
            imService.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Cluster Cancel</font>** \n\n");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"#008000\">topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"#008000\">producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            imService.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KafkaConstants.KafkaConstants)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setTitle("`Kafka Eagle Alarm Cluster Cancel`\n");
            if (KafkaConstants.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"#008000\">topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KafkaConstants.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"#008000\">producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(DateUtils.getCurrentDateStr());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        }
    }
}
