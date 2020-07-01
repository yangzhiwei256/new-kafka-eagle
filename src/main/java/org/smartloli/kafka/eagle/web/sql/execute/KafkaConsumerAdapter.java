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
package org.smartloli.kafka.eagle.web.sql.execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.web.sql.schema.TopicSchema;
import org.smartloli.kafka.eagle.web.support.KafkaConsumerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    private KafkaConsumerTemplate kafkaConsumerTemplate;
    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    /**
     * Executor ksql query topic data.
     */
    public List<JSONArray> executor(KafkaSqlInfo kafkaSql) {
        return kafkaConsumerTemplate.doExecute(kafkaSql.getClusterAlias(), kafkaConsumer -> {
            List<JSONArray> messages = new ArrayList<>();
            List<TopicPartition> topics = new ArrayList<>();
            for (Integer partition : kafkaSql.getPartition()) {
                TopicPartition tp = new TopicPartition(kafkaSql.getTableName(), partition);
                topics.add(tp);
            }
            kafkaConsumer.assign(topics);

            for (TopicPartition tp : topics) {
                Map<TopicPartition, Long> offsets = kafkaConsumer.endOffsets(Collections.singleton(tp));
                if (offsets.get(tp) > kafkaClustersConfig.getSqlTopicRecordsMax()) {
                    kafkaConsumer.seek(tp, offsets.get(tp) - kafkaClustersConfig.getSqlTopicRecordsMax());
                } else {
                    kafkaConsumer.seek(tp, 0);
                }
            }
            JSONArray datasets = new JSONArray();
            boolean flag = true;
            while (flag) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(KafkaConstants.TIME_OUT));
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
            return messages;
        });
    }
}
