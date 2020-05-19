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
package org.smartloli.kafka.eagle.sql;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartloli.kafka.eagle.web.KafkaEagleBootstrap;
import org.smartloli.kafka.eagle.web.sql.execute.KafkaSqlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test kafka sql query.
 * @author smartloli.
 * Created by Feb 28, 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaEagleBootstrap.class)
@Slf4j
public class KafkaParserTest {

    @Autowired
    private KafkaSqlParser kafkaSqlParser;

    /**
     * KQL 语法单元测试
     */
    @Test
	public void executeTest() {
		// String sql = "SELECT \"partition\", \"offset\",\"msg\" from
		// \"kv-test2019\" where \"partition\" in (0) and \"offset\"=37445 group
		// by \"partition\" limit 10";
		String sql = "select * from \"kv-test2019\" where \"partition\" in (0) limit 10";
		String result = kafkaSqlParser.execute("cluster1", sql);
		log.info("result: {}", result);
	}
}
