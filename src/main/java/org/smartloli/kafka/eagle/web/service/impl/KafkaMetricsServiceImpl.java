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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.ConfigResource.Type;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.apache.zookeeper.data.Stat;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.JmxConstants.KafkaLog;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.web.service.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.util.JMXFactoryUtils;
import org.smartloli.kafka.eagle.web.util.KafkaResourcePoolUtils;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.Tuple2;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Implements KafkaMetricsService all methods.
 *
 * @author smartloli.
 *
 *         Created by Oct 26, 2018
 */
@Service
@Slf4j
public class KafkaMetricsServiceImpl implements KafkaMetricsService {

    private final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Kafka service interface.
     */
    @Autowired
    private KafkaService kafkaService;

    /**
     * Get topic config path in zookeeper.
     */
    private final String CONFIG_TOPIC_PATH = "/config/topics/";

    public JSONObject topicKafkaCapacity(String clusterAlias, String topic) {
        if (KafkaConstants.CONSUMER_OFFSET_TOPIC.equals(topic)) {
            return new JSONObject();
        }
        long sum = 0L;
        AdminClient adminClient = KafkaResourcePoolUtils.getKafkaClient(clusterAlias);
        try {
            List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
            Set<Integer> ids = new HashSet<>();
            for (MetadataInfo metadata : leaders) {
                ids.add(metadata.getLeader());
			}
			DescribeLogDirsResult logSizeBytes = adminClient.describeLogDirs(ids);
			Map<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> tmp = logSizeBytes.all().get();
			if (tmp == null) {
				return new JSONObject();
			}

			for (Map.Entry<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> entry : tmp.entrySet()) {
				Map<String, DescribeLogDirsResponse.LogDirInfo> logDirInfos = entry.getValue();
				for (Map.Entry<String, DescribeLogDirsResponse.LogDirInfo> logDirInfo : logDirInfos.entrySet()) {
					DescribeLogDirsResponse.LogDirInfo info = logDirInfo.getValue();
					Map<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicaInfoMap = info.replicaInfos;
					for (Map.Entry<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicas : replicaInfoMap.entrySet()) {
						if (topic.equals(replicas.getKey().topic())) {
							sum += replicas.getValue().size;
						}
					}
				}
			}

        } catch (Exception e) {
            log.error("Get topic capacity has error, msg is " + e.getCause().getMessage());
            e.printStackTrace();
		} finally {
            KafkaResourcePoolUtils.release(clusterAlias, adminClient);
        }
		return StrUtils.stringifyByObject(sum);
	}

	/** Get topic size from kafka jmx. */
	public JSONObject topicSize(String clusterAlias, String topic) {
		String jmx = "";
		JMXConnector connector = null;
		List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
		long tpSize = 0L;
		for (MetadataInfo leader : leaders) {
			String jni = kafkaService.getBrokerJMXFromIds(clusterAlias, leader.getLeader());
			jmx = String.format(JMX, jni);
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(jmx);
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
				String objectName = String.format(KafkaLog.SIZE.getValue(), topic, leader.getPartitionId());
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.VALUE.getValue());
				tpSize += Long.parseLong(size.toString());
            } catch (Exception ex) {
                log.error("Get topic size from jmx has error, msg is " + ex.getMessage());
                ex.printStackTrace();
			} finally {
				if (connector != null) {
					try {
						connector.close();
                    } catch (IOException e) {
                        log.error("Close jmx connector has error, msg is " + e.getMessage());
					}
				}
			}
		}

		return StrUtils.stringifyByObject(tpSize);
	}

	/** Alter topic config. */
	public String changeTopicConfig(String clusterAlias, String topic, String type, ConfigEntry configEntry) {
        JSONObject object = new JSONObject();
        AdminClient adminClient = KafkaResourcePoolUtils.getKafkaClient(clusterAlias);
        try {
            switch (type) {
                case TopicConstants.ADD:
                    object.put("type", type);
                    object.put("value", addTopicConfig(clusterAlias, adminClient, topic, configEntry));
                    break;
                case TopicConstants.DELETE:
                    object.put("type", type);
                    object.put("value", deleteTopicConfig(clusterAlias, adminClient, topic, configEntry));
                    break;
                case TopicConstants.DESCRIBE:
                    object.put("type", type);
                    object.put("value", describeTopicConfig(clusterAlias, topic));
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            log.error("Type[" + type + "] topic config has error", e);
        } finally {
            KafkaResourcePoolUtils.release(clusterAlias, adminClient);
        }
		return object.toJSONString();
	}

	private String addTopicConfig(String clusterAlias, AdminClient adminClient, String topic, ConfigEntry configEntry) {
		try {
			String describeTopicConfigs = describeTopicConfig(clusterAlias, topic);
			JSONObject object = JSON.parseObject(describeTopicConfigs).getJSONObject("config");
			object.remove(configEntry.name());
			List<ConfigEntry> configEntrys = new ArrayList<>();
			for (String key : TopicConstants.getTopicConfigKeys()) {
				if (object.containsKey(key)) {
					configEntrys.add(new ConfigEntry(key, object.getString(key)));
				}
			}
			configEntrys.add(configEntry);
			Map<ConfigResource, Config> configs = new HashMap<>();
			ConfigResource configRes = new ConfigResource(Type.TOPIC, topic);
			Config config = new Config(configEntrys);
			configs.put(configRes, config);
			AlterConfigsResult alterConfig = adminClient.alterConfigs(configs);
			alterConfig.all().get();
			return TopicConstants.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Add topic[" + topic + "] config has error, msg is " + e.getMessage());
            return e.getMessage();
		}
	}

	private String deleteTopicConfig(String clusterAlias, AdminClient adminClient, String topic, ConfigEntry configEntry) {
		try {
			String describeTopicConfigs = describeTopicConfig(clusterAlias, topic);
			JSONObject object = JSON.parseObject(describeTopicConfigs).getJSONObject("config");
			object.remove(configEntry.name());
			List<ConfigEntry> configEntrys = new ArrayList<>();
			for (String key : TopicConstants.getTopicConfigKeys()) {
				if (object.containsKey(key)) {
					configEntrys.add(new ConfigEntry(key, object.getString(key)));
				}
			}
			Map<ConfigResource, Config> configs = new HashMap<>();
			ConfigResource configRes = new ConfigResource(Type.TOPIC, topic);
			Config config = new Config(configEntrys);
			configs.put(configRes, config);
			adminClient.alterConfigs(configs);
			return TopicConstants.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Delete topic[" + topic + "] config has error, msg is " + e.getMessage());
            return e.getMessage();
		}
	}

	private String describeTopicConfig(String clusterAlias, String topic) {
		String target = "";
		try {
            KafkaZkClient kafkaZkCli = KafkaResourcePoolUtils.getZookeeperClient(clusterAlias);
            if (kafkaZkCli.pathExists(CONFIG_TOPIC_PATH + topic)) {
                Tuple2<Option<byte[]>, Stat> tuple = kafkaZkCli.getDataAndStat(CONFIG_TOPIC_PATH + topic);
                target = new String(tuple._1.get());
            }
            if (kafkaZkCli != null) {
                KafkaResourcePoolUtils.release(clusterAlias, kafkaZkCli);
                kafkaZkCli = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Describe topic[" + topic + "] config has error, msg is " + e.getMessage());
		}
		return target;
	}

	private String parseBrokerServer(String clusterAlias) {
        String brokerServer = "";
        List<KafkaBrokerInfo> brokers = kafkaService.getBrokerInfos(clusterAlias);
        for (KafkaBrokerInfo broker : brokers) {
            brokerServer += broker.getHost() + ":" + broker.getPort() + ",";
        }
        if ("".equals(brokerServer)) {
            return "";
        }
        return brokerServer.substring(0, brokerServer.length() - 1);
    }

	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}

	/** Get kafka topic capacity size . */
	public long topicCapacity(String clusterAlias, String topic) {
		String jmx = "";
		JMXConnector connector = null;
		List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
		long tpSize = 0L;
		for (MetadataInfo leader : leaders) {
			String jni = kafkaService.getBrokerJMXFromIds(clusterAlias, leader.getLeader());
			jmx = String.format(JMX, jni);
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(jmx);
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
				String objectName = String.format(KafkaLog.SIZE.getValue(), topic, leader.getPartitionId());
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.VALUE.getValue());
				tpSize += Long.parseLong(size.toString());
            } catch (Exception ex) {
                log.error("Get topic size from jmx has error, msg is " + ex.getMessage());
                ex.printStackTrace();
			} finally {
				if (connector != null) {
					try {
						connector.close();
                    } catch (IOException e) {
                        log.error("Close jmx connector has error, msg is " + e.getMessage());
					}
				}
			}
		}

		return tpSize;
	}
}
