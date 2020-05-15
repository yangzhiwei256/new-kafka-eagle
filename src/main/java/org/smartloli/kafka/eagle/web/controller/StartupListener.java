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
package org.smartloli.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.config.SingleClusterConfig;
import org.smartloli.kafka.eagle.web.plugin.mysql.MySqlRecordSchema;
import org.smartloli.kafka.eagle.web.plugin.sqlite.SqliteRecordSchema;
import org.smartloli.kafka.eagle.web.plugin.util.JConstants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

/**
 * Load kafka consumer internal thread to get offset.
 * @author smartloli.
 * Created by May 22, 2017
 */
@Component
@Slf4j
public class StartupListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Value("spring.datasource.driver-class-name")
    private String driverClassName;

    @Autowired
    private SqliteRecordSchema sqliteRecordSchema;

    @Autowired
    private MySqlRecordSchema mySqlRecordSchema;

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {

        new Thread(() -> {
            if (JConstants.MYSQL_DRIVER.equals(driverClassName)) {
                mySqlRecordSchema.schema();
            } else if (JConstants.SQLITE_DRIVER.equals(driverClassName)) {
                sqliteRecordSchema.schema();
            }
        }).start();

        new Thread(() -> {
            for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
                if ("kafka".equals(singleClusterConfig.getOffsetStorage())) {
                    try {
//                        KafkaOffsetGetter.getInstance();
                    } catch (Exception ex) {
                        log.error("Initialize KafkaOffsetGetter thread has error", ex);
                    }
                }
            }
        }).start();
    }

	public static Object getBean(String beanName) {
		if (applicationContext == null) {
			applicationContext = ContextLoader.getCurrentWebApplicationContext();
		}
		return applicationContext.getBean(beanName);
	}

	public static <T> T getBean(String beanName, Class<T> clazz) {
		return clazz.cast(getBean(beanName));
	}
}
