/**
 * Licensed to the Apache Software Foundation (ASF) under one
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
package org.smartloli.kafka.eagle.sql;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartloli.kafka.eagle.web.KafkaEagleBootstrap;
import org.smartloli.kafka.eagle.web.entity.QueryKafkaMessage;
import org.smartloli.kafka.eagle.web.sql.execute.KafkaSqlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test Kafka Sql.
 *
 * @author smartloli.
 * Created by Feb 27, 2018
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaEagleBootstrap.class)
public class KSqlTest {

    @Autowired
    private KafkaSqlParser kafkaSqlParser;

    @Test
    public void executeKSqlTest() {
        String sql = "select * from MSG_EVENT_BROADCAST where `partition` in (0,1,2) limit 100";
        QueryKafkaMessage queryKafkaMessage = kafkaSqlParser.execute("local", sql);
        log.info("result: {}", queryKafkaMessage);
    }
}
