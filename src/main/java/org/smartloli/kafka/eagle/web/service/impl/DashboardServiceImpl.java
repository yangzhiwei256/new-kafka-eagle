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
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.protocol.DashboardInfo;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.protocol.KpiInfo;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Kafka Eagle dashboard data generator.
 * 
 * @author smartloli.
 *
 *         Created by Aug 12, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class DashboardServiceImpl implements DashboardService {

	/** Kafka service interface. */
	@Autowired
	private KafkaService kafkaService;

	/** Broker service interface. */
	@Autowired
	private BrokerService brokerService;

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private MBeanDao mbeanDao;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Get consumer number from zookeeper.
     */
    private int getConsumerNumbers(String clusterAlias) {
        Map<String, List<String>> consumers = consumerService.getConsumers(clusterAlias);
        int count = 0;
        for (Entry<String, List<String>> entry : consumers.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

	/** Get kafka & dashboard dataset. */
	public String getDashboard(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("kafka", kafkaBrokersGraph(clusterAlias));
		target.put("dashboard", panel(clusterAlias));
		return target.toJSONString();
	}

	/** Get kafka data. */
	private String kafkaBrokersGraph(String clusterAlias) {
        List<KafkaBrokerInfo> brokers = kafkaService.getBrokerInfos(clusterAlias);
        JSONObject target = new JSONObject();
        target.put("name", "Kafka Brokers");
        JSONArray targets = new JSONArray();
        int count = 0;
        for (KafkaBrokerInfo broker : brokers) {
            if (count > KafkaConstants.SIZE) {
                JSONObject subTarget = new JSONObject();
                subTarget.put("name", "...");
                targets.add(subTarget);
                break;
			} else {
				JSONObject subTarget = new JSONObject();
				subTarget.put("name", broker.getHost() + ":" + broker.getPort());
				targets.add(subTarget);
			}
			count++;
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get dashboard data. */
	private String panel(String clusterAlias) {
        int zks = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getZkList().split(",").length;
        DashboardInfo dashboard = new DashboardInfo();
        dashboard.setBrokers(brokerService.brokerNumbers(clusterAlias));
        dashboard.setTopics(brokerService.topicNumbers(clusterAlias));
        dashboard.setZks(zks);
        String formatter = kafkaClustersConfig.getClusterConfigByName(clusterAlias).getOffsetStorage();
        if ("kafka".equals(formatter)) {
            dashboard.setConsumers(consumerService.getKafkaConsumerGroups(clusterAlias));
        } else {
            dashboard.setConsumers(getConsumerNumbers(clusterAlias));
        }
        return JSON.toJSONString(dashboard);
	}

	/** Get topic rank data,such as logsize and topic capacity. */
	@Override
	public JSONArray getTopicRank(Map<String, Object> params) {
		List<TopicRank> topicRank = topicDao.readTopicRank(params);
		JSONArray array = new JSONArray();
		if (TopicConstants.LOGSIZE.equals(params.get("tkey"))) {
			int index = 1;
			for (int i = 0; i < 10; i++) {
				JSONObject object = new JSONObject();
				if (i < topicRank.size()) {
                    object.put("id", index);
                    object.put("topic", "<a href='/topic/meta/page/" + topicRank.get(i).getTopic() + "'>" + topicRank.get(i).getTopic() + "</a>");
                    object.put("logsize", topicRank.get(i).getTvalue());
                } else {
					object.put("id", index);
					object.put("topic", "");
					object.put("logsize", "");
				}
				index++;
				array.add(object);
			}
		} else if (TopicConstants.CAPACITY.equals(params.get("tkey"))) {
			int index = 1;
			for (int i = 0; i < 10; i++) {
				JSONObject object = new JSONObject();
				if (i < topicRank.size()) {
                    object.put("id", index);
                    object.put("topic", "<a href='/topic/meta/page/" + topicRank.get(i).getTopic() + "/'>" + topicRank.get(i).getTopic() + "</a>");
                    object.put("capacity", StrUtils.stringify(topicRank.get(i).getTvalue()));
                } else {
					object.put("id", index);
					object.put("topic", "");
					object.put("capacity", "");
				}
				index++;
				array.add(object);
			}
		}
		return array;
	}

	/** Write statistics topic rank data from kafka jmx & insert into table. */
	public int writeTopicRank(List<TopicRank> topicRanks) {
        return topicDao.writeTopicRank(topicRanks);
    }

    /**
     * Write statistics topic logsize data from kafka jmx & insert into table.
     */
    public int writeTopicLogSize(List<TopicLogSize> topicLogSize) {
        return topicDao.writeTopicLogSize(topicLogSize);
    }

    @Override
    public JSONObject getOSMem(Map<String, Object> params) {
        List<KpiInfo> kpis = mbeanDao.getOsMem(params);
        JSONObject object = new JSONObject();
        object.put("mem", 0.0);
        if (kpis.size() == 2) {
            long valueFirst = Long.parseLong(kpis.get(0).getValue());
            long valueSecond = Long.parseLong(kpis.get(1).getValue());
            if (valueFirst >= valueSecond && valueFirst != 0L) {
                object.put("mem", 100 * StrUtils.numberic((valueFirst - valueSecond) * 1.0 / valueFirst, "###.###"));
            } else if (valueSecond != 0L) {
                object.put("mem", 100 * StrUtils.numberic((valueSecond - valueFirst) * 1.0 / valueSecond, "###.###"));
            }
        }
        return object;
    }

	/** Read topic lastest logsize diffval data. */
	public TopicLogSize readLastTopicLogSize(Map<String, Object> params) {
		return topicDao.readLastTopicLogSize(params);
	}

	/** Get all clean topic list. */
	public List<TopicRank> getCleanTopicList(Map<String, Object> params) {
		return topicDao.getCleanTopicList(params);
	}

}