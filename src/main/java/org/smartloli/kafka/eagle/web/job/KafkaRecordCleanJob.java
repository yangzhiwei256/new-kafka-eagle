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
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.smartloli.kafka.eagle.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kafka数据清除作业
 *
 * @author smartloli.
 * Created by Jul 19, 2017
 */
@Slf4j
@Component
public class KafkaRecordCleanJob {

    @Value("${" + KafkaConstants.KAFKA_EAGLE_METRICS_CHARTS + ":false}")
    private Boolean kafkaEagleMetricsCharts;

    @Value("${" + KafkaConstants.KAFKA_EAGLE_METRICS_RETAIN + ":30}")
    private Integer kafkaEagleMetricsRetain;

    @Autowired
    private MetricsService metricsService;

//    @Scheduled(cron = "0 0/5 * * * ?")
    protected void execute() {
        log.info("JOB IS RUNNING ===> {}", getClass().getSimpleName());
        if (kafkaEagleMetricsCharts) {
            metricsService.remove(Integer.parseInt(DateUtils.getCustomLastDay(kafkaEagleMetricsRetain)));
            metricsService.cleanTopicLogSize(Integer.parseInt(DateUtils.getCustomLastDay(kafkaEagleMetricsRetain)));
            metricsService.cleanBScreenConsumerTopic(Integer.parseInt(DateUtils.getCustomLastDay(kafkaEagleMetricsRetain)));
            metricsService.cleanTopicSqlHistory(Integer.parseInt(DateUtils.getCustomLastDay(kafkaEagleMetricsRetain)));
        }
        log.info("JOB IS FINISHED ===> {}", getClass().getSimpleName());
    }
}
