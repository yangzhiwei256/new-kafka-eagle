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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.constant.MBeanConstants;
import org.smartloli.kafka.eagle.web.constant.TopicConstants;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.protocol.KpiInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenBarInfo;
import org.smartloli.kafka.eagle.web.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.web.service.BScreenService;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Big screen get data.
 * 
 * @author smartloli.
 *
 *         Created by Aug 29, 2019
 */
@Service
public class BScreenServiceImpl implements BScreenService {

	@Autowired
	private TopicDao topicDao;

	@Autowired
	private TopicService topicService;

	@Autowired
	private MBeanDao mbeanDao;

	/** Broker service interface. */
	@Autowired
	private BrokerService brokerService;

	/** Get producer and consumer real rate data . */
	public String getProducerAndConsumerRate(String clusterAlias) {
		KpiInfo byteIn = getBrokersKpi(clusterAlias, MBeanConstants.BYTEIN);
		KpiInfo byteOut = getBrokersKpi(clusterAlias, MBeanConstants.BYTEOUT);

		JSONObject object = new JSONObject();
		object.put("ins", byteIn.getValue());
		object.put("outs", byteOut.getValue());
		return object.toJSONString();
	}

	private KpiInfo getBrokersKpi(String clusterAlias, String key) {
        Map<String, Object> param = new HashMap<>();
        param.put("cluster", clusterAlias);
        param.put("tday", DateUtils.getCustomDate("yyyyMMdd"));
        param.put("type", KafkaConstants.KAFKA);
        param.put("key", key);
        return mbeanDao.getBrokersKpi(param);
    }

	/** Get topic total logsize data . */
	public String getTopicTotalLogSize(String clusterAlias) {
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("topics", brokerService.topicList(clusterAlias));
        params.put("size", brokerService.topicList(clusterAlias).size());
        params.put("tday", DateUtils.getCustomDate("yyyyMMdd"));
        long totalRecords = topicDao.getBScreenTotalRecords(params);
        JSONObject object = new JSONObject();
        object.put("total", totalRecords);
        return object.toJSONString();
    }

	/** Get producer and consumer history data. */
	public String getProducerOrConsumerHistory(String clusterAlias, String type) {
        JSONArray array = new JSONArray();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cluster", clusterAlias);
        params.put("stime", DateUtils.getCustomLastDay(7));
        params.put("etime", DateUtils.getCustomDate("yyyyMMdd"));
        if ("producer".equals(type)) {
            List<BScreenBarInfo> bsProducers = topicDao.queryProducerHistoryBar(params);
            Map<String, Object> bsMaps = new HashMap<>();
            for (BScreenBarInfo bsProducer : bsProducers) {
                if (bsProducer != null) {
                    bsMaps.put(bsProducer.getTm(), bsProducer.getValue());
                }
            }
            for (int i = 6; i >= 0; i--) {
                String tm = DateUtils.getCustomLastDay(i);
                if (bsMaps.containsKey(tm)) {
                    JSONObject object = new JSONObject();
                    object.put("x", DateUtils.getCustomLastDay("MM-dd", i));
                    object.put("y", bsMaps.get(tm).toString());
                    array.add(object);
                } else {
                    JSONObject object = new JSONObject();
                    object.put("x", DateUtils.getCustomLastDay("MM-dd", i));
                    object.put("y", 0);
                    array.add(object);
                }
			}
		} else {
			List<BScreenBarInfo> bsConsumers = topicDao.queryConsumerHistoryBar(params);
			Map<String, Object> bsMaps = new HashMap<>();
			for (BScreenBarInfo bsConsumer : bsConsumers) {
				if (bsConsumer != null) {
					bsMaps.put(bsConsumer.getTm(), bsConsumer.getValue());
				}
			}
			for (int i = 6; i >= 0; i--) {
                String tm = DateUtils.getCustomLastDay(i);
                if (bsMaps.containsKey(tm)) {
                    JSONObject object = new JSONObject();
                    object.put("x", DateUtils.getCustomLastDay("MM-dd", i));
                    object.put("y", bsMaps.get(tm).toString());
                    array.add(object);
                } else {
                    JSONObject object = new JSONObject();
                    object.put("x", DateUtils.getCustomLastDay("MM-dd", i));
                    object.put("y", 0);
                    array.add(object);
                }
			}
		}

		return array.toJSONString();
	}

	@Override
	public String getTodayOrHistoryConsumerProducer(String clusterAlias, String type) {
        JSONArray targets = new JSONArray();
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", clusterAlias);
        params.put("tday", DateUtils.getCustomDate("yyyyMMdd"));
        List<BScreenConsumerInfo> bscreenConsumers = topicDao.queryTodayBScreenConsumer(params);
        Map<Integer, Long> map = new HashMap<>();
        if (bscreenConsumers != null) {
            if (TopicConstants.PRODUCERS.equals(type)) {
                for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
                    int hour = 0;
                    try {
                        hour = Integer.parseInt(DateUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH"));
                    } catch (Exception e) {
                        e.printStackTrace();
					}
					if (map.containsKey(hour)) {
						map.put(hour, map.get(hour) + bscreenConsumer.getDifflogsize());
					} else {
						map.put(hour, bscreenConsumer.getDifflogsize());
					}
				}
			} else if (TopicConstants.CONSUMERS.equals(type)) {
				for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
					int hour = 0;
					try {
                        hour = Integer.parseInt(DateUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH"));
                    } catch (Exception e) {
						e.printStackTrace();
					}
					if (map.containsKey(hour)) {
						map.put(hour, map.get(hour) + bscreenConsumer.getDiffoffsets());
					} else {
						map.put(hour, bscreenConsumer.getDiffoffsets());
					}
				}
			} else if (TopicConstants.LAG.equals(type)) {
				for (BScreenConsumerInfo bscreenConsumer : bscreenConsumers) {
					int hour = 0;
					try {
                        hour = Integer.parseInt(DateUtils.convertUnixTime(bscreenConsumer.getTimespan(), "HH"));
                    } catch (Exception e) {
						e.printStackTrace();
					}
					if (map.containsKey(hour)) {
						map.put(hour, map.get(hour) + bscreenConsumer.getLag());
					} else {
						map.put(hour, bscreenConsumer.getLag());
					}
				}
			}
		}
		for (Entry<Integer, Long> entry : map.entrySet()) {
			JSONObject object = new JSONObject();
			object.put("x", entry.getKey());
			object.put("y", entry.getValue());
			targets.add(object);
		}
		return targets.toJSONString();
	}

	@Override
	public String getTopicCapacity(Map<String, Object> params) {
		return StrUtils.stringifyByObject(topicService.getTopicCapacity(params)).toJSONString();
	}

}
