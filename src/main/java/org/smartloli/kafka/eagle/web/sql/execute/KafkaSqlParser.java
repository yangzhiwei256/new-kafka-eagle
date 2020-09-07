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

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.entity.KafkaMessage;
import org.smartloli.kafka.eagle.web.entity.QueryKafkaMessage;
import org.smartloli.kafka.eagle.web.protocol.KafkaSqlInfo;
import org.smartloli.kafka.eagle.web.service.BrokerService;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pre processing the SQL submitted by the client.
 * 
 * @author smartloli.
 *
 *         Created by Feb 28, 2017
 */
@Component
@Slf4j
public class KafkaSqlParser {

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaConsumerAdapter kafkaConsumerAdapter;

    @Autowired
    private BrokerService brokerService;

    /**
     * 执行KSQL
     *
     * @param clusterAlias
     * @param sql
     * @return
     */
    public QueryKafkaMessage execute(String clusterAlias, String sql) {
        QueryKafkaMessage queryKafkaMessage = new QueryKafkaMessage();
        try {
            KafkaSqlInfo kafkaSql = kafkaService.parseSql(clusterAlias, sql);
            log.info("KafkaSqlParser - SQL[" + kafkaSql.getSql() + "]");
            if (!kafkaSql.isValid()) {
                queryKafkaMessage.setError(true);
                queryKafkaMessage.setStatus("ERROR - SQL[" + kafkaSql.getSql() + "] has error,please start with select.");
                return queryKafkaMessage;
            }

            if (!hasTopic(clusterAlias, kafkaSql)) {
                queryKafkaMessage.setError(true);
                queryKafkaMessage.setStatus("ERROR - Topic[" + kafkaSql.getTopic() + "] not exist.");
                return queryKafkaMessage;
            }

            // 查询消息
            long start = System.currentTimeMillis();
            List<KafkaMessage> dataSets = kafkaConsumerAdapter.executor(kafkaSql);
            long end = System.currentTimeMillis();

            queryKafkaMessage.setError(false);
            queryKafkaMessage.setData(dataSets);
            queryKafkaMessage.setStatus("Finished by [" + (end - start) / 1000.0 + "s].");
            queryKafkaMessage.setSpent(end - start);
            log.info("KSQL:[{}] 查询数据量[{}],耗时:[{}]毫秒", kafkaSql.getSql(), dataSets.size(), queryKafkaMessage.getSpent());
            return queryKafkaMessage;
        } catch (Exception e) {
            queryKafkaMessage.setError(true);
            queryKafkaMessage.setStatus(e.getMessage());
            log.error("Execute sql to query kafka topic has error", e);
            return queryKafkaMessage;
        }
    }

    /**
     * Topic 校验是否存在
     **/
    private boolean hasTopic(String clusterAlias, KafkaSqlInfo kafkaSql) {
        boolean status = brokerService.findKafkaTopic(clusterAlias, kafkaSql.getTopic());
        if (status) {
            kafkaSql.setTopic(kafkaSql.getTopic());
        }
        return status;
    }
}
