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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.MBeanConstants;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.protocol.KpiInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.web.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.smartloli.kafka.eagle.web.service.Mx4jService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Achieve access to the kafka monitoring data interface through jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 17, 2017 Update by No 3, 2018 by cocodroid
 */
@Service
@Slf4j
public class MetricsServiceImpl implements MetricsService {

	@Autowired
	private MBeanDao mbeanDao;

	@Autowired
    private TopicDao topicDao;

    /**
     * Kafka service interface.
     */
    @Autowired
    private KafkaService kafkaService;

    /**
     * Mx4j service interface.
     */
    @Autowired
    private Mx4jService mx4jService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Override
    public String getAllBrokersMBean(String clusterAlias) {
        List<KafkaBrokerInfo> brokers = kafkaService.getBrokerInfos(clusterAlias);
        int brokerSize = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getBrokerSize();
        String result = null;
        if (brokers.size() <= brokerSize) {
            result = getOnlineAllBrokersMBean(brokers);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            result = getOfflineAllBrokersMBean(params);
        }
        log.info("查询Kafka集群[{}]代理节点监控信息 ==> {}", clusterAlias, result);
        return result;
    }

	/** Gets summary offline monitoring data for all broker. */
	private String getOfflineAllBrokersMBean(Map<String, Object> params) {
		Map<String, MBeanInfo> mbeans = new HashMap<>();
		List<MBeanOfflineInfo> mbeanOfflines = mbeanDao.getMBeanOffline(params);
		for (MBeanOfflineInfo mbeanOffline : mbeanOfflines) {
			MBeanInfo mbeanInfo = new MBeanInfo();
			mbeanInfo.setOneMinute(mbeanOffline.getOneMinute());
			mbeanInfo.setMeanRate(mbeanOffline.getMeanRate());
			mbeanInfo.setFiveMinute(mbeanOffline.getFiveMinute());
			mbeanInfo.setFifteenMinute(mbeanOffline.getFifteenMinute());
			switch (mbeanOffline.getKey()) {
			case MBeanConstants.MESSAGEIN:
				mbeans.put(MBeanConstants.MESSAGES_IN, mbeanInfo);
				break;
			case MBeanConstants.BYTEIN:
				mbeans.put(MBeanConstants.BYTES_IN, mbeanInfo);
				break;
			case MBeanConstants.BYTEOUT:
				mbeans.put(MBeanConstants.BYTES_OUT, mbeanInfo);
				break;
			case MBeanConstants.BYTESREJECTED:
				mbeans.put(MBeanConstants.BYTES_REJECTED, mbeanInfo);
				break;
			case MBeanConstants.FAILEDFETCHREQUEST:
				mbeans.put(MBeanConstants.FAILED_FETCH_REQUEST, mbeanInfo);
				break;
			case MBeanConstants.FAILEDPRODUCEREQUEST:
				mbeans.put(MBeanConstants.FAILED_PRODUCE_REQUEST, mbeanInfo);
				break;
			case MBeanConstants.TOTALFETCHREQUESTSPERSEC:
				mbeans.put(MBeanConstants.TOTALFETCHREQUESTSPERSEC, mbeanInfo);
				break;
			case MBeanConstants.TOTALPRODUCEREQUESTSPERSEC:
				mbeans.put(MBeanConstants.TOTALPRODUCEREQUESTSPERSEC, mbeanInfo);
				break;
			case MBeanConstants.REPLICATIONBYTESINPERSEC:
				mbeans.put(MBeanConstants.REPLICATIONBYTESINPERSEC, mbeanInfo);
				break;
			case MBeanConstants.REPLICATIONBYTESOUTPERSEC:
				mbeans.put(MBeanConstants.REPLICATIONBYTESOUTPERSEC, mbeanInfo);
				break;
			case MBeanConstants.PRODUCEMESSAGECONVERSIONS:
				mbeans.put(MBeanConstants.PRODUCEMESSAGECONVERSIONS, mbeanInfo);
				break;
			default:
				break;
			}
		}

		return JSON.toJSONString(mbeans);
	}

    /**
     * 获取在线节点监控信息
     * @param brokers
     * @return
     */
	private String getOnlineAllBrokersMBean(List<KafkaBrokerInfo> brokers) {
        Map<String, MBeanInfo> mbeanInfoMap = new HashMap<>();
        for (KafkaBrokerInfo kafkaBrokerInfo : brokers) {
            MBeanInfo bytesIn = mx4jService.bytesInPerSec(kafkaBrokerInfo);
            MBeanInfo bytesOut = mx4jService.bytesOutPerSec(kafkaBrokerInfo);
            MBeanInfo bytesRejected = mx4jService.bytesRejectedPerSec(kafkaBrokerInfo);
            MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(kafkaBrokerInfo);
            MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(kafkaBrokerInfo);
            MBeanInfo messageIn = mx4jService.messagesInPerSec(kafkaBrokerInfo);
            MBeanInfo produceMessageConversions = mx4jService.produceMessageConversionsPerSec(kafkaBrokerInfo);
            MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(kafkaBrokerInfo);
            MBeanInfo totalProduceRequests = mx4jService.totalProduceRequestsPerSec(kafkaBrokerInfo);
            MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(kafkaBrokerInfo);
            MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(kafkaBrokerInfo);

            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.MESSAGES_IN, messageIn);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.BYTES_IN, bytesIn);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.BYTES_OUT, bytesOut);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.BYTES_REJECTED, bytesRejected);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.FAILED_FETCH_REQUEST, failedFetchRequest);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.FAILED_PRODUCE_REQUEST, failedProduceRequest);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.PRODUCEMESSAGECONVERSIONS, produceMessageConversions);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.TOTALFETCHREQUESTSPERSEC, totalFetchRequests);
            assembleMBeanInfo(mbeanInfoMap, MBeanConstants.TOTALPRODUCEREQUESTSPERSEC, totalProduceRequests);
			assembleMBeanInfo(mbeanInfoMap, MBeanConstants.REPLICATIONBYTESINPERSEC, replicationBytesInPerSec);
			assembleMBeanInfo(mbeanInfoMap, MBeanConstants.REPLICATIONBYTESOUTPERSEC, replicationBytesOutPerSec);
		}
		for (Entry<String, MBeanInfo> entry : mbeanInfoMap.entrySet()) {
			if (entry == null || entry.getValue() == null) {
				continue;
			}
			entry.getValue().setFifteenMinute(StrUtils.assembly(entry.getValue().getFifteenMinute()));
			entry.getValue().setFiveMinute(StrUtils.assembly(entry.getValue().getFiveMinute()));
			entry.getValue().setMeanRate(StrUtils.assembly(entry.getValue().getMeanRate()));
			entry.getValue().setOneMinute(StrUtils.assembly(entry.getValue().getOneMinute()));
		}
		return JSON.toJSONString(mbeanInfoMap);
	}

	private void assembleMBeanInfo(Map<String, MBeanInfo> mbeans, String mBeanInfoKey, MBeanInfo mBeanInfo) {
		if (mbeans.containsKey(mBeanInfoKey) && mBeanInfo != null) {
			MBeanInfo mbeanInfo = mbeans.get(mBeanInfoKey);
			String fifteenMinuteOld = mbeanInfo.getFifteenMinute() == null ? "0.0" : mbeanInfo.getFifteenMinute();
			String fifteenMinuteLastest = mBeanInfo.getFifteenMinute() == null ? "0.0" : mBeanInfo.getFifteenMinute();
			String fiveMinuteOld = mbeanInfo.getFiveMinute() == null ? "0.0" : mbeanInfo.getFiveMinute();
			String fiveMinuteLastest = mBeanInfo.getFiveMinute() == null ? "0.0" : mBeanInfo.getFiveMinute();
			String meanRateOld = mbeanInfo.getMeanRate() == null ? "0.0" : mbeanInfo.getMeanRate();
			String meanRateLastest = mBeanInfo.getMeanRate() == null ? "0.0" : mBeanInfo.getMeanRate();
			String oneMinuteOld = mbeanInfo.getOneMinute() == null ? "0.0" : mbeanInfo.getOneMinute();
			String oneMinuteLastest = mBeanInfo.getOneMinute() == null ? "0.0" : mBeanInfo.getOneMinute();
			long fifteenMinute = Math.round(StrUtils.numberic(fifteenMinuteOld)) + Math.round(StrUtils.numberic(fifteenMinuteLastest));
			long fiveMinute = Math.round(StrUtils.numberic(fiveMinuteOld)) + Math.round(StrUtils.numberic(fiveMinuteLastest));
			long meanRate = Math.round(StrUtils.numberic(meanRateOld)) + Math.round(StrUtils.numberic(meanRateLastest));
			long oneMinute = Math.round(StrUtils.numberic(oneMinuteOld)) + Math.round(StrUtils.numberic(oneMinuteLastest));
			mbeanInfo.setFifteenMinute(String.valueOf(fifteenMinute));
			mbeanInfo.setFiveMinute(String.valueOf(fiveMinute));
			mbeanInfo.setMeanRate(String.valueOf(meanRate));
			mbeanInfo.setOneMinute(String.valueOf(oneMinute));
		} else {
			mbeans.put(mBeanInfoKey, mBeanInfo);
		}
	}

	/** Collection statistics data from kafka jmx & insert into table. */
	public int insert(List<KpiInfo> kpi) {
		return mbeanDao.insert(kpi);
	}

	/** Query MBean data in different dimensions. */
	public String query(Map<String, Object> params) throws ParseException {

		List<KpiInfo> kpis = mbeanDao.query(params);

		JSONArray messageIns = new JSONArray();
		JSONArray byteIns = new JSONArray();
		JSONArray byteOuts = new JSONArray();
		JSONArray byteRejected = new JSONArray();
		JSONArray failedFetchRequest = new JSONArray();
		JSONArray failedProduceRequest = new JSONArray();
		JSONArray produceMessageConversions = new JSONArray();
		JSONArray totalFetchRequests = new JSONArray();
		JSONArray totalProduceRequests = new JSONArray();
		JSONArray replicationBytesOuts = new JSONArray();
		JSONArray replicationBytesIns = new JSONArray();

		JSONArray osFreeMems = new JSONArray();

		JSONArray zkSendPackets = new JSONArray();
		JSONArray zkReceivedPackets = new JSONArray();
		JSONArray zkNumAliveConnections = new JSONArray();
		JSONArray zkOutstandingRequests = new JSONArray();
		for (KpiInfo kpi : kpis) {
			switch (kpi.getKey()) {
			case KafkaConstants.ZK_SEND_PACKETS:
				assembly(zkSendPackets, kpi);
				break;
			case KafkaConstants.ZK_RECEIVEDPACKETS:
				assembly(zkReceivedPackets, kpi);
				break;
			case KafkaConstants.ZK_OUTSTANDING_REQUESTS:
				assembly(zkOutstandingRequests, kpi);
				break;
			case KafkaConstants.ZK_NUM_ALIVECONNRCTIONS:
				assembly(zkNumAliveConnections, kpi);
				break;
			case MBeanConstants.MESSAGEIN:
				assembly(messageIns, kpi);
				break;
			case MBeanConstants.BYTEIN:
				assembly(byteIns, kpi);
				break;
			case MBeanConstants.BYTEOUT:
				assembly(byteOuts, kpi);
				break;
			case MBeanConstants.BYTESREJECTED:
				assembly(byteRejected, kpi);
				break;
			case MBeanConstants.FAILEDFETCHREQUEST:
				assembly(failedFetchRequest, kpi);
				break;
			case MBeanConstants.FAILEDPRODUCEREQUEST:
				assembly(failedProduceRequest, kpi);
				break;
			case MBeanConstants.PRODUCEMESSAGECONVERSIONS:
				assembly(produceMessageConversions, kpi);
				break;
			case MBeanConstants.TOTALFETCHREQUESTSPERSEC:
				assembly(totalFetchRequests, kpi);
				break;
			case MBeanConstants.TOTALPRODUCEREQUESTSPERSEC:
				assembly(totalProduceRequests, kpi);
				break;
			case MBeanConstants.REPLICATIONBYTESINPERSEC:
				assembly(replicationBytesOuts, kpi);
				break;
			case MBeanConstants.REPLICATIONBYTESOUTPERSEC:
				assembly(replicationBytesIns, kpi);
				break;
			case MBeanConstants.OSFREEMEMORY:
				assembly(osFreeMems, kpi);
				break;
			default:
				break;
			}
		}
		JSONObject target = new JSONObject();
		target.put("send", zkSendPackets);
		target.put("received", zkReceivedPackets);
		target.put("queue", zkOutstandingRequests);
		target.put("alive", zkNumAliveConnections);
		target.put("messageIns", messageIns);
		target.put("byteIns", byteIns);
		target.put("byteOuts", byteOuts);
		target.put("byteRejected", byteRejected);
		target.put("failedFetchRequest", failedFetchRequest);
		target.put("failedProduceRequest", failedProduceRequest);
		target.put("produceMessageConversions", produceMessageConversions);
		target.put("totalFetchRequests", totalFetchRequests);
		target.put("totalProduceRequests", totalProduceRequests);
		target.put("replicationBytesIns", replicationBytesIns);
		target.put("replicationBytesOuts", replicationBytesOuts);
		target.put("osFreeMems", osFreeMems);

		return target.toJSONString();
	}

	private void assembly(JSONArray assemblys, KpiInfo kpi) throws ParseException {
        JSONObject object = new JSONObject();
        object.put("x", DateUtils.convertUnixTime(kpi.getTimespan(), "yyyy-MM-dd HH:mm"));
        object.put("y", kpi.getValue());
        assemblys.add(object);
    }

	/** Crontab clean data. */
	public void remove(int tm) {
		mbeanDao.remove(tm);
	}

	@Override
	public void cleanTopicLogSize(int tm) {
		topicDao.cleanTopicLogSize(tm);
	}

	@Override
	public void cleanTopicRank(int tm) {
		topicDao.cleanTopicRank(tm);
	}

	@Override
	public void cleanBScreenConsumerTopic(int tm) {
		topicDao.cleanBScreenConsumerTopic(tm);
	}

	@Override
	public int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers) {
		return topicDao.writeBSreenConsumerTopic(bscreenConsumers);
	}

	@Override
	public BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params) {
		return topicDao.readBScreenLastTopic(params);
	}

	@Override
	public void cleanTopicSqlHistory(int tm) {
		topicDao.cleanTopicSqlHistory(tm);
	}

	@Override
	public int mbeanOfflineInsert(List<MBeanOfflineInfo> kpis) {
		return mbeanDao.mbeanOfflineInsert(kpis);
	}

	@Override
	public int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups) {
		return topicDao.writeConsumerGroupTopics(consumerGroups);
	}

	@Override
	public List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params) {
		return topicDao.getAllConsumerGroups(params);
	}

	@Override
	public int cleanConsumerGroupTopic(Map<String, Object> params) {
		return topicDao.cleanConsumerGroupTopic(params);
	}

}
