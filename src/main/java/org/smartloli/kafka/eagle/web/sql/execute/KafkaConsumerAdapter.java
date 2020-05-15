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
package org.smartloli.kafka.eagle.web.sql.execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.sql.schema.TopicSchema;
import org.smartloli.kafka.eagle.web.util.KafkaResourcePoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parse the sql statement, and execute the sql content, get the message record
 * of kafka in topic, and map to sql tree to query operation.
 *
 * @author smartloli.
 *
 *         Created by Jun 23, 2017
 */
@Component
public class KafkaConsumerAdapter {

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Autowired
    private KafkaService kafkaService;

    @Value("${" + KafkaConstants.KAFKA_EAGLE_SQL_FIX_ERROR + ":false}")
    private Boolean kafkaEagleSqlFixError;

    /**
     * Executor ksql query topic data.
     */
    public List<JSONArray> executor(KafkaSqlInfo kafkaSql) {
        List<JSONArray> messages = new ArrayList<>();
        KafkaConsumer<String, String> consumer = KafkaResourcePoolUtils.getKafkaConsumer(kafkaSql.getClusterAlias());
        try {
            List<TopicPartition> topics = new ArrayList<>();
            for (Integer partition : kafkaSql.getPartition()) {
                TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                topics.add(tp);
            }
            consumer.assign(topics);

            for (TopicPartition tp : topics) {
                Map<TopicPartition, Long> offsets = consumer.endOffsets(Collections.singleton(tp));
                if (offsets.get(tp).longValue() > KafkaConstants.POSITION) {
                    consumer.seek(tp, offsets.get(tp).longValue() - KafkaConstants.POSITION);
                } else {
                    consumer.seek(tp, 0);
                }
            }
            JSONArray datasets = new JSONArray();
            boolean flag = true;
            while (flag) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(KafkaConstants.TIME_OUT));
                for (ConsumerRecord<String, String> record : records) {
                    JSONObject object = new JSONObject();
                    object.put(TopicSchema.MSG, record.value());
                    object.put(TopicSchema.OFFSET, record.offset());
                    object.put(TopicSchema.PARTITION, record.partition());
                    datasets.add(object);
                }
                if (records.isEmpty()) {
                    flag = false;
                }
            }
            messages.add(datasets);
        } finally {
            KafkaResourcePoolUtils.release(kafkaSql.getClusterAlias(), consumer);
        }
		return messages;
	}

}
