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
package org.smartloli.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.constant.JmxConstants.BrokerServer;
import org.smartloli.kafka.eagle.web.constant.MBeanConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.web.service.Mx4jService;
import org.smartloli.kafka.eagle.web.util.JMXFactoryUtils;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implements Mx4jService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jul 14, 2017
 */
@Slf4j
@Service
public class Mx4jServiceImpl implements Mx4jService {

	private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
	private static final String TOPIC_CONCAT_CHARACTER = ",topic=";

    /**
     * Get brokers all topics bytes in per sec.
     */
    @Override
    public MBeanInfo bytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.BYTES_IN_PER_SEC.getValue());
    }

    /**
     * Get brokers bytes in per sec by topic.
     */
    @Override
    public MBeanInfo bytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        String mbean = BrokerServer.BYTES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
        return common(kafkaBrokerInfo, mbean);
    }

    /**
     * Get brokers all topics bytes out per sec.
     */
    @Override
    public MBeanInfo bytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.BYTES_OUT_PER_SEC.getValue());
    }

    /**
     * Get brokers bytes out per sec by topic.
     */
    @Override
    public MBeanInfo bytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        String mbean = BrokerServer.BYTES_OUT_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
        return common(kafkaBrokerInfo, mbean);
    }

    /**
     * Get brokers all topics byte rejected per sec.
     */
    @Override
    public MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.BYTES_REJECTED_PER_SEC.getValue());
    }

    /**
     * Get brokers byte rejected per sec by topic.
     */
    @Override
    public MBeanInfo bytesRejectedPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        String mbean = BrokerServer.BYTES_REJECTED_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
        return common(kafkaBrokerInfo, mbean);
    }

    /**
     * Get brokers all topic failed fetch request per sec.
     */
    @Override
    public MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.FAILED_FETCH_REQUESTS_PER_SEC.getValue());
    }

    /**
     * Get brokers failed fetch request per sec by topic.
     */
    @Override
    public MBeanInfo failedFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.FAILED_FETCH_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    /**
     * Get brokers all topics failed fetch produce request per sec.
     */
    @Override
    public MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.FAILED_PRODUCE_REQUESTS_PER_SEC.getValue());
    }

    /**
     * Get brokers failed fetch produce request per sec by topic.
     */
    @Override
    public MBeanInfo failedProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.FAILED_PRODUCE_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    /**
     * Get brokers topic all partitions log end offset.
     */
    @Override
    public Map<Integer, Long> logEndOffset(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        String mbean = "kafka.log:type=Log,name=LogEndOffset,topic=" + topic + ",partition=*";
        JMXConnector connector = null;
        Map<Integer, Long> endOffsets = new HashMap<>();
        try {
            connector = JMXFactoryUtils.connectWithTimeout(kafkaBrokerInfo);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            Set<ObjectName> objectNames = mbeanConnection.queryNames(new ObjectName(mbean), null);
            for (ObjectName objectName : objectNames) {
                int partition = Integer.parseInt(objectName.getKeyProperty("partition"));
                Object value = mbeanConnection.getAttribute(new ObjectName(mbean), MBeanConstants.VALUE);
				if (value != null) {
                    endOffsets.put(partition, Long.valueOf(value.toString()));
                }
            }
        } catch (Exception e) {
            log.error("JMX service url[" + kafkaBrokerInfo.getHost()+":"+ kafkaBrokerInfo.getJmxPort() + "] create has error,msg is " + e.getMessage());
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (Exception e) {
                    log.error("Close JMXConnector[" + kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getJmxPort() + "] has error,msg is " + e.getMessage());
                }
            }
        }
        return endOffsets;
    }

    /**
     * Get brokers all topics message in per sec.
     */
    @Override
    public MBeanInfo messagesInPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.MESSAGES_IN_PER_SEC.getValue());
    }

    /**
     * Get brokers message in per sec by topic.
     */
    @Override
    public MBeanInfo messagesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.MESSAGES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    @Override
    public MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC.getValue());
    }

    @Override
    public MBeanInfo produceMessageConversionsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    @Override
    public MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.TOTAL_FETCH_REQUESTS_PER_SEC.getValue());
    }

    @Override
    public MBeanInfo totalFetchRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.TOTAL_FETCH_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    @Override
    public MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.TOTAL_PRODUCE_REQUESTS_PER_SEC.getValue());
    }

    @Override
    public MBeanInfo totalProduceRequestsPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.TOTAL_PRODUCE_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    @Override
    public MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.REPLICATION_BYTES_IN_PER_SEC.getValue());
    }

    @Override
    public MBeanInfo replicationBytesInPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        String mbean = BrokerServer.REPLICATION_BYTES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
        return common(kafkaBrokerInfo, mbean);
    }

    @Override
    public MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo) {
        return common(kafkaBrokerInfo, BrokerServer.REPLICATION_BYTES_OUT_PER_SEC.getValue());
    }

    @Override
    public MBeanInfo replicationBytesOutPerSec(KafkaBrokerInfo kafkaBrokerInfo, String topic) {
        return common(kafkaBrokerInfo, BrokerServer.REPLICATION_BYTES_OUT_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic);
    }

    /**
     * Mx4j通用服务
     *
     * @param kafkaBrokerInfo kafka节点信息
     * @param mbeanPath       kafka jmx路径
     * @return
     */
    private MBeanInfo common(KafkaBrokerInfo kafkaBrokerInfo, String mbeanPath) {
        MBeanInfo mbeanInfo = new MBeanInfo();
        if (!kafkaBrokerInfo.getJmxEnabled()) {
            return mbeanInfo;
        }

        String uri = kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getJmxPort();
        JMXConnector connector = null;
        try {
            connector = JMXFactoryUtils.connectWithTimeout(kafkaBrokerInfo);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            if (mbeanConnection.isRegistered(new ObjectName(mbeanPath))) {
                Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbeanPath), MBeanConstants.FIFTEEN_MINUTE_RATE);
                Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbeanPath), MBeanConstants.FIVE_MINUTE_RATE);
                Object meanRate = mbeanConnection.getAttribute(new ObjectName(mbeanPath), MBeanConstants.MEAN_RATE);
                Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbeanPath), MBeanConstants.ONE_MINUTE_RATE);
                mbeanInfo.setFifteenMinute(fifteenMinuteRate.toString());
                mbeanInfo.setFiveMinute(fiveMinuteRate.toString());
                mbeanInfo.setMeanRate(meanRate.toString());
                mbeanInfo.setOneMinute(oneMinuteRate.toString());
            } else {
                mbeanInfo.setFifteenMinute("0.0");
                mbeanInfo.setFiveMinute("0.0");
                mbeanInfo.setMeanRate("0.0");
                mbeanInfo.setOneMinute("0.0");
            }
        } catch (Exception e) {
            log.error("JMX service url[" + uri + "] create has error", e);
            mbeanInfo.setFifteenMinute("0.0");
            mbeanInfo.setFiveMinute("0.0");
            mbeanInfo.setMeanRate("0.0");
            mbeanInfo.setOneMinute("0.0");
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (Exception e) {
                    log.error("Close JMXConnector[" + uri + "] has error", e);
                }
            }
        }
        return mbeanInfo;
    }

}
