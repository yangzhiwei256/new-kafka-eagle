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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.entity.KafkaMessage;
import org.smartloli.kafka.eagle.web.protocol.KafkaSqlInfo;
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
     * 执行KSQL
     */
    public List<KafkaMessage> executor(KafkaSqlInfo kafkaSql) {
        return kafkaConsumerTemplate.doExecute(kafkaSql.getClusterAlias(), kafkaConsumer -> {
            List<KafkaMessage> messages = new ArrayList<>();
            List<TopicPartition> topics = new ArrayList<>();
            for (Integer partition : kafkaSql.getPartition()) {
                TopicPartition topicPartition = new TopicPartition(kafkaSql.getTopic(), partition);
                topics.add(topicPartition);
            }
            kafkaConsumer.assign(topics);
            for (TopicPartition topicPartition : topics) {
                Map<TopicPartition, Long> offsets = kafkaConsumer.endOffsets(Collections.singleton(topicPartition));
                if (offsets.get(topicPartition) > kafkaClustersConfig.getSqlTopicRecordsMax()) {
                    kafkaConsumer.seek(topicPartition, offsets.get(topicPartition) - kafkaClustersConfig.getSqlTopicRecordsMax());
                } else {
                    kafkaConsumer.seek(topicPartition, 0);
                }
            }
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(KafkaConstants.POLL_MESSAGE_TIME_OUT_MS));
                for (ConsumerRecord<String, String> record : records) {
                    KafkaMessage kafkaMessage = new KafkaMessage();
                    kafkaMessage.setMsg(record.value());
                    kafkaMessage.setOffset(record.offset());
                    kafkaMessage.setPartition(record.partition());
                    kafkaMessage.setMessageId(record.key());
                    // limit限制校验
                    if (null != kafkaSql.getLimit() && messages.size() >= kafkaSql.getLimit()) {
                        break;
                    }
                    messages.add(kafkaMessage);
                }
                if (records.isEmpty() || messages.size() >= kafkaClustersConfig.getSqlTopicRecordsMax()) {
                    break;
                }
            }
            return messages;
        });
    }
}
