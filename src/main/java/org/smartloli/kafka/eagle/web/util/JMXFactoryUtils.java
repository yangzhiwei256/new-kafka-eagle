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
package org.smartloli.kafka.eagle.web.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manager jmx connector object && release.
 *
 * @author smartloli.
 *
 *         Created by Feb 25, 2019
 */
@Slf4j
public class JMXFactoryUtils {

    private JMXFactoryUtils() {

    }

    /**
     * 获取JMXConnector
     *
     * @param kafkaBrokerInfo kafka代理节点信息
     * @return
     */
    public static JMXConnector connectWithTimeout(KafkaBrokerInfo kafkaBrokerInfo) {
        return connectWithTimeout(kafkaBrokerInfo, 30, TimeUnit.SECONDS);
    }

    /**
     * 获取JMXConnector
     *
     * @param kafkaBrokerInfo kakka代理节点信息
     * @param timeout
     * @param unit
     * @return
     */
    public static JMXConnector connectWithTimeout(KafkaBrokerInfo kafkaBrokerInfo, long timeout, TimeUnit unit) {
        if (!kafkaBrokerInfo.getJmxEnabled()) {
            return null;
        }
        try {
            JMXServiceURL jmxServiceUrl = new JMXServiceURL(String.format(KafkaConstants.JMX_URL_FORMAT, kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getJmxPort()));
            return connectWithTimeout(jmxServiceUrl, timeout, unit);
        } catch (MalformedURLException e) {
            log.error("创建JMXServiceURL出错 kafkaBrokerInfo ==> {}", JSON.toJSONString(kafkaBrokerInfo));
            return null;
        }
    }

    /**
     * 获取JMXConnector 利用阻塞队列实现超时处理
     *
     * @param url     服务地址
     * @param timeout 超时
     * @param unit    超时时间单位
     * @return
     */
    public static JMXConnector connectWithTimeout(final JMXServiceURL url, long timeout, TimeUnit unit) {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        try {
            JMXConnector connector = JMXConnectorFactory.connect(url);
            if (!blockQueue.offer(connector)) {
                connector.close();
            }
        } catch (Exception e) {
            log.error("获取JMXConnector失败,JMXServiceURL:{},timeout:{},unit:{}", url, timeout, unit.name(), e);
            return null;
        }
        Object result = null;
        try {
            result = blockQueue.poll(timeout, unit);
            if (result == null && !blockQueue.offer("")) {
                result = blockQueue.take();
            }
        } catch (Exception e) {
            log.error("Take block queue has error", e);
        }
        return (JMXConnector) result;
    }
}
