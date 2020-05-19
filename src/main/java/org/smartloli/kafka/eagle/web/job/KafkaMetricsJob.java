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
package org.smartloli.kafka.eagle.web.job;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.config.SingleClusterConfig;
import org.smartloli.kafka.eagle.web.constant.JmxConstants.BrokerServer;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.MBeanConstants;
import org.smartloli.kafka.eagle.web.protocol.*;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.smartloli.kafka.eagle.web.service.Mx4jService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.smartloli.kafka.eagle.web.util.ZKMetricsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kafka指标数据统计作业
 *
 * @author smartloli.
 * Created by Jul 19, 2017
 */
@Slf4j
@Component
public class KafkaMetricsJob {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Value("${" + KafkaConstants.KAFKA_EAGLE_METRICS_CHARTS + ":false}")
    private Boolean kafkaEagleMetricsCharts;

    private static final String zk_packets_received = "zk_packets_received";
    private static final String zk_packets_sent = "zk_packets_sent";
    private static final String zk_num_alive_connections = "zk_num_alive_connections";
    private static final String zk_outstanding_requests = "zk_outstanding_requests";
    private static final String[] zk_kpis = new String[]{zk_packets_received, zk_packets_sent, zk_num_alive_connections, zk_outstanding_requests};

    private static final String[] broker_kpis = new String[]{MBeanConstants.MESSAGEIN, MBeanConstants.BYTEIN, MBeanConstants.BYTEOUT, MBeanConstants.BYTESREJECTED, MBeanConstants.FAILEDFETCHREQUEST, MBeanConstants.FAILEDPRODUCEREQUEST, MBeanConstants.TOTALFETCHREQUESTSPERSEC, MBeanConstants.TOTALPRODUCEREQUESTSPERSEC, MBeanConstants.REPLICATIONBYTESINPERSEC, MBeanConstants.REPLICATIONBYTESOUTPERSEC, MBeanConstants.PRODUCEMESSAGECONVERSIONS,
            MBeanConstants.OSTOTALMEMORY, MBeanConstants.OSFREEMEMORY};
    private static final String[] BROKER_KPIS_OFFLINE = new String[]{MBeanConstants.MESSAGEIN, MBeanConstants.BYTEIN, MBeanConstants.BYTEOUT, MBeanConstants.BYTESREJECTED, MBeanConstants.FAILEDFETCHREQUEST, MBeanConstants.FAILEDPRODUCEREQUEST, MBeanConstants.TOTALFETCHREQUESTSPERSEC, MBeanConstants.TOTALPRODUCEREQUESTSPERSEC, MBeanConstants.REPLICATIONBYTESINPERSEC, MBeanConstants.REPLICATIONBYTESOUTPERSEC, MBeanConstants.PRODUCEMESSAGECONVERSIONS};

	/** Kafka service interface. */
	@Autowired
	private KafkaService kafkaService;

	/** Mx4j service interface. */
	@Autowired
	private Mx4jService mx4jService;

	@Scheduled(cron = "0 */10 * * * ?")
    protected void execute() {
        if (kafkaEagleMetricsCharts) {
            Map<String, List<KafkaBrokerInfo>> kafkaBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
            for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
                List<KafkaBrokerInfo> kafkaBrokerInfoList = kafkaBrokerInfoMap.get(singleClusterConfig.getAlias());
                kafkaCluster(singleClusterConfig.getAlias(), kafkaBrokerInfoList); //kafka集群数据采集
                zkCluster(singleClusterConfig.getAlias()); //zk集群数据采集
                brokerMbeanOffline(singleClusterConfig.getAlias(), kafkaBrokerInfoList);
            }
        }
    }

    private void brokerMbeanOffline(String clusterAlias, List<KafkaBrokerInfo> kafkaBrokerInfos) {
        List<MBeanOfflineInfo> list = new ArrayList<>();

        for (String kpi : BROKER_KPIS_OFFLINE) {
            MBeanOfflineInfo mbeanOffline = new MBeanOfflineInfo();
            mbeanOffline.setCluster(clusterAlias);
            mbeanOffline.setKey(kpi);
            for (KafkaBrokerInfo kafka : kafkaBrokerInfos) {
                kafkaMBeanOfflineAssembly(kpi, mbeanOffline, kafka);
            }
            list.add(mbeanOffline);
        }
        metricsService.mbeanOfflineInsert(list);
    }

    private void kafkaMBeanOfflineAssembly(String type, MBeanOfflineInfo mbeanOffline, KafkaBrokerInfo kafka) {
        String uri = kafka.getHost() + ":" + kafka.getJmxPort();
        switch (type) {
            case MBeanConstants.MESSAGEIN:
                MBeanInfo msg = mx4jService.messagesInPerSec(uri);
                if (msg != null) {
                    mbeanOffline.setOneMinute(StrUtils.assembly(msg.getOneMinute() == null ? "0.00" : msg.getOneMinute()));
                    mbeanOffline.setMeanRate(StrUtils.assembly(msg.getMeanRate() == null ? "0.00" : msg.getMeanRate()));
                    mbeanOffline.setFiveMinute(StrUtils.assembly(msg.getFiveMinute() == null ? "0.00" : msg.getFiveMinute()));
                    mbeanOffline.setFifteenMinute(StrUtils.assembly(msg.getFifteenMinute() == null ? "0.00" : msg.getFifteenMinute()));
                }
			break;
		case MBeanConstants.BYTEIN:
			MBeanInfo bin = mx4jService.bytesInPerSec(uri);
			if (bin != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bin.getOneMinute() == null ? "0.00" : bin.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bin.getMeanRate() == null ? "0.00" : bin.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bin.getFiveMinute() == null ? "0.00" : bin.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bin.getFifteenMinute() == null ? "0.00" : bin.getFifteenMinute()));
			}
			break;
		case MBeanConstants.BYTEOUT:
			MBeanInfo bout = mx4jService.bytesOutPerSec(uri);
			if (bout != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bout.getOneMinute() == null ? "0.00" : bout.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bout.getMeanRate() == null ? "0.00" : bout.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bout.getFiveMinute() == null ? "0.00" : bout.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bout.getFifteenMinute() == null ? "0.00" : bout.getFifteenMinute()));
			}
			break;
		case MBeanConstants.BYTESREJECTED:
			MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(uri);
			if (bytesRejectedPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(bytesRejectedPerSec.getOneMinute() == null ? "0.00" : bytesRejectedPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(bytesRejectedPerSec.getMeanRate() == null ? "0.00" : bytesRejectedPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(bytesRejectedPerSec.getFiveMinute() == null ? "0.00" : bytesRejectedPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(bytesRejectedPerSec.getFifteenMinute() == null ? "0.00" : bytesRejectedPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.FAILEDFETCHREQUEST:
			MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(uri);
			if (failedFetchRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(failedFetchRequestsPerSec.getOneMinute() == null ? "0.00" : failedFetchRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(failedFetchRequestsPerSec.getMeanRate() == null ? "0.00" : failedFetchRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFiveMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(failedFetchRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedFetchRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.FAILEDPRODUCEREQUEST:
			MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(uri);
			if (failedProduceRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(failedProduceRequestsPerSec.getOneMinute() == null ? "0.00" : failedProduceRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(failedProduceRequestsPerSec.getMeanRate() == null ? "0.00" : failedProduceRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(failedProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : failedProduceRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.TOTALFETCHREQUESTSPERSEC:
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri);
			if (totalFetchRequests != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(totalFetchRequests.getOneMinute() == null ? "0.00" : totalFetchRequests.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(totalFetchRequests.getMeanRate() == null ? "0.00" : totalFetchRequests.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(totalFetchRequests.getFiveMinute() == null ? "0.00" : totalFetchRequests.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(totalFetchRequests.getFifteenMinute() == null ? "0.00" : totalFetchRequests.getFifteenMinute()));
			}
			break;
		case MBeanConstants.TOTALPRODUCEREQUESTSPERSEC:
			MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(uri);
			if (totalProduceRequestsPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(totalProduceRequestsPerSec.getOneMinute() == null ? "0.00" : totalProduceRequestsPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(totalProduceRequestsPerSec.getMeanRate() == null ? "0.00" : totalProduceRequestsPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFiveMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(totalProduceRequestsPerSec.getFifteenMinute() == null ? "0.00" : totalProduceRequestsPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.REPLICATIONBYTESINPERSEC:
			MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(uri);
			if (replicationBytesInPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesInPerSec.getOneMinute() == null ? "0.00" : replicationBytesInPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesInPerSec.getMeanRate() == null ? "0.00" : replicationBytesInPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesInPerSec.getFiveMinute() == null ? "0.00" : replicationBytesInPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesInPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesInPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.REPLICATIONBYTESOUTPERSEC:
			MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(uri);
			if (replicationBytesOutPerSec != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(replicationBytesOutPerSec.getOneMinute() == null ? "0.00" : replicationBytesOutPerSec.getOneMinute()));
				mbeanOffline.setMeanRate(StrUtils.assembly(replicationBytesOutPerSec.getMeanRate() == null ? "0.00" : replicationBytesOutPerSec.getMeanRate()));
				mbeanOffline.setFiveMinute(StrUtils.assembly(replicationBytesOutPerSec.getFiveMinute() == null ? "0.00" : replicationBytesOutPerSec.getFiveMinute()));
				mbeanOffline.setFifteenMinute(StrUtils.assembly(replicationBytesOutPerSec.getFifteenMinute() == null ? "0.00" : replicationBytesOutPerSec.getFifteenMinute()));
			}
			break;
		case MBeanConstants.PRODUCEMESSAGECONVERSIONS:
			MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(uri);
			if (produceMessageConv != null) {
				mbeanOffline.setOneMinute(StrUtils.assembly(produceMessageConv.getOneMinute() == null ? "0.00" : produceMessageConv.getOneMinute()));
                mbeanOffline.setMeanRate(StrUtils.assembly(produceMessageConv.getMeanRate() == null ? "0.00" : produceMessageConv.getMeanRate()));
                mbeanOffline.setFiveMinute(StrUtils.assembly(produceMessageConv.getFiveMinute() == null ? "0.00" : produceMessageConv.getFiveMinute()));
                mbeanOffline.setFifteenMinute(StrUtils.assembly(produceMessageConv.getFifteenMinute() == null ? "0.00" : produceMessageConv.getFifteenMinute()));
            }
            break;
            default:
                break;
        }
    }

    /**
     * Kafka集群指标数据采集
     * @param clusterAlias kafka集群名称
     */
    private void kafkaCluster(String clusterAlias, List<KafkaBrokerInfo> brokers) {
        List<KpiInfo> list = new ArrayList<>();
        for (String kpi : broker_kpis) {
            KpiInfo kpiInfo = new KpiInfo();
            kpiInfo.setCluster(clusterAlias);
            kpiInfo.setTm(DateUtils.getCustomDate("yyyyMMdd"));
            kpiInfo.setTimespan(DateUtils.getTimeSpan());
            kpiInfo.setKey(kpi);
            for (KafkaBrokerInfo kafka : brokers) {
                kafkaAssembly(kpi, kpiInfo, kafka);
            }
            kpiInfo.setBroker(clusterAlias);
            kpiInfo.setType(KafkaConstants.KAFKA);
            list.add(kpiInfo);
        }
        metricsService.insert(list);
    }

    /**
     * kafka指标数据装配
     * @param type            指标数据类型
     * @param kpiInfo         指标数据
     * @param kafkaBrokerInfo kafka代理服务器信息
     */
    private void kafkaAssembly(String type, KpiInfo kpiInfo, KafkaBrokerInfo kafkaBrokerInfo) {
        String uri = kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getJmxPort();
        switch (type) {
            case MBeanConstants.MESSAGEIN:
                MBeanInfo msg = mx4jService.messagesInPerSec(uri);
                if (msg != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(msg.getOneMinute()) + "");
                }
                break;
            case MBeanConstants.BYTEIN:
                MBeanInfo bin = mx4jService.bytesInPerSec(uri);
                if (bin != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bin.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.BYTEOUT:
			MBeanInfo bout = mx4jService.bytesOutPerSec(uri);
			if (bout != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bout.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.BYTESREJECTED:
			MBeanInfo bytesRejectedPerSec = mx4jService.bytesRejectedPerSec(uri);
			if (bytesRejectedPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(bytesRejectedPerSec.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.FAILEDFETCHREQUEST:
			MBeanInfo failedFetchRequestsPerSec = mx4jService.failedFetchRequestsPerSec(uri);
			if (failedFetchRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedFetchRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.FAILEDPRODUCEREQUEST:
			MBeanInfo failedProduceRequestsPerSec = mx4jService.failedProduceRequestsPerSec(uri);
			if (failedProduceRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(failedProduceRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.TOTALFETCHREQUESTSPERSEC:
			MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(uri);
			if (totalFetchRequests != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalFetchRequests.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.TOTALPRODUCEREQUESTSPERSEC:
			MBeanInfo totalProduceRequestsPerSec = mx4jService.totalProduceRequestsPerSec(uri);
			if (totalProduceRequestsPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(totalProduceRequestsPerSec.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.REPLICATIONBYTESINPERSEC:
			MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(uri);
			if (replicationBytesInPerSec != null) {
				kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesInPerSec.getOneMinute()) + "");
			}
			break;
		case MBeanConstants.REPLICATIONBYTESOUTPERSEC:
			MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(uri);
			if (replicationBytesOutPerSec != null) {
                kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(replicationBytesOutPerSec.getOneMinute()) + "");
            }
            break;
            case MBeanConstants.PRODUCEMESSAGECONVERSIONS:
                MBeanInfo produceMessageConv = mx4jService.produceMessageConversionsPerSec(uri);
                if (produceMessageConv != null) {
                    kpiInfo.setValue(StrUtils.numberic(kpiInfo.getValue() == null ? "0.0" : kpiInfo.getValue()) + StrUtils.numberic(produceMessageConv.getOneMinute()) + "");
                }
                break;
            case MBeanConstants.OSTOTALMEMORY:
                long totalMemory = kafkaService.getOSMemory(kafkaBrokerInfo.getHost(), kafkaBrokerInfo.getJmxPort(), BrokerServer.TOTAL_PHYSICAL_MEMORY_SIZE.getValue());
                kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + totalMemory + "");
                break;
            case MBeanConstants.OSFREEMEMORY:
                long freeMemory = kafkaService.getOSMemory(kafkaBrokerInfo.getHost(), kafkaBrokerInfo.getJmxPort(), BrokerServer.FREE_PHYSICAL_MEMORY_SIZE.getValue());
                kpiInfo.setValue(Long.parseLong(kpiInfo.getValue() == null ? "0" : kpiInfo.getValue()) + freeMemory + "");
			break;
		default:
			break;
		}
	}

    /**
     * Kafka Zookeeper数据采集
     * @param clusterAlias kafka集群节点名称
     */
	private void zkCluster(String clusterAlias) {
        List<KpiInfo> list = new ArrayList<>();
        String zkList = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList();
        String[] zks = zkList.split(",");
        for (String kpi : zk_kpis) {
            KpiInfo kpiInfo = new KpiInfo();
            kpiInfo.setCluster(clusterAlias);
            kpiInfo.setTm(DateUtils.getCustomDate("yyyyMMdd"));
            kpiInfo.setTimespan(DateUtils.getTimeSpan());
            kpiInfo.setKey(kpi);
            StringBuilder broker = new StringBuilder();
            for (String zk : zks) {
                String ip = zk.split(":")[0];
                String port = zk.split(":")[1];
                if (port.contains("/")) {
                    port = port.split("/")[0];
                }
                broker.append(ip).append(",");
                try {
                    ZkClusterInfo zkInfo = ZKMetricsUtils.zkClusterMntrInfo(ip, Integer.parseInt(port));
                    zkAssembly(zkInfo, kpi, kpiInfo);
                } catch (Exception ex) {
                    log.error("Transcation string[" + port + "] to int has error", ex);
                }
            }
            kpiInfo.setBroker(broker.length() == 0 ? "unkowns" : broker.substring(0, broker.length() - 1));
            kpiInfo.setType(KafkaConstants.KafkaConstants);
            list.add(kpiInfo);
        }
		metricsService.insert(list);
	}

	private static void zkAssembly(ZkClusterInfo zkInfo, String type, KpiInfo kpiInfo) {
		switch (type) {
		case zk_packets_received:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkPacketsReceived()) == true ? "0" : zkInfo.getZkPacketsReceived()) + "");
			break;
		case zk_packets_sent:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkPacketsSent()) == true ? "0" : zkInfo.getZkPacketsSent()) + "");
			break;
		case zk_num_alive_connections:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkNumAliveConnections()) == true ? "0" : zkInfo.getZkNumAliveConnections()) + "");
			break;
		case zk_outstanding_requests:
			kpiInfo.setValue(Long.parseLong(StrUtils.isNull(kpiInfo.getValue()) == true ? "0" : kpiInfo.getValue()) + Long.parseLong(StrUtils.isNull(zkInfo.getZkOutstandingRequests()) == true ? "0" : zkInfo.getZkOutstandingRequests()) + "");
			break;
		default:
			break;
		}
	}
}
