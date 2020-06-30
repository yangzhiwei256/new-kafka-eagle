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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SaslConfigs;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.config.SingleClusterConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.support.factory.PooledKafkaConsumerFactory;
import org.smartloli.kafka.eagle.web.support.factory.PooledKafkaProducerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * kafka zookeeper/kafka 客户端资源池工具类
 *
 * @author smartloli.
 * Created by Jan 11, 2019.
 */
@Slf4j
@Component
public final class KafkaResourcePoolUtils implements InitializingBean {

    /**
     * kafka集群zookeeper资源池
     */
    private static final Map<String, GenericObjectPool<KafkaConsumer>> kafkaConsumerPoolMap = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<KafkaProducer>> kafkaProducerPoolMap = new ConcurrentHashMap<>();
    private static final Map<String, String> kafkaClusterBootstrapServerMap = new ConcurrentHashMap<>();

    @Value("${" + KafkaConstants.KAFKA_CLIENT_MAX_TOTAL + ":20}")
    private int kafkaClientPoolMaxSize;

    @Value("${" + KafkaConstants.KAFKA_CLIENT_MIN_IDLE + ":5}")
    private int kafkaClientPoolMinIdle;

    @Value("${" + KafkaConstants.KAFKA_CLIENT_MAX_IDLE + ":10}")
    private int kafkaClientPoolMaxIdle;

    @Value("${" + KafkaConstants.KAFKA_SEND_ERROR_RETRY + ":1}")
    private int kafkaSendErrorRetry;

    @Value("${" + KafkaConstants.KAFKA_REQUEST_TIMEOUT_MS + ":3000}")
    private int kafkaRequestTimeoutMs;

    @Value("${" + KafkaConstants.KAFKA_EAGLE_SQL_FIX_ERROR + ":false}")
    private Boolean kafkaEagleSqlFixError;

    @Value("${pool.max.wait.ms:10000}")
    private Integer poolMaxWaitMs;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @Autowired
    private KafkaService kafkaService;

    /**
     * 获取kafka集群消费者
     *
     * @param clusterAlias kafka集群别名
     * @return
     */
    public static KafkaConsumer<String, String> getKafkaConsumer(String clusterAlias) {
        GenericObjectPool<KafkaConsumer> genericObjectPool = kafkaConsumerPoolMap.get(clusterAlias);
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取kafka集群生产者
     *
     * @param clusterAlias kafka集群别名
     * @return
     */
    public static KafkaProducer<String, String> getKafkaProducer(String clusterAlias) {
        GenericObjectPool<KafkaProducer> genericObjectPool = kafkaProducerPoolMap.get(clusterAlias);
        try {
            return genericObjectPool.borrowObject();
        } catch (Exception e) {
            log.error("资源池获取kafka生产者失败", e);
            return null;
        }
    }

    /**
     * 获取kafka集群Broker客户端
     *
     * @param clusterAlias kafka集群别名
     * @return
     */
    public static String getBootstrapServer(String clusterAlias) {
        return kafkaClusterBootstrapServerMap.get(clusterAlias);
    }

    /**
     * 释放kafka集群 zookeeper连接池资源
     *
     * @param clusterAlias kakfa集群别名
     * @param resource     kafka/zookeeper客户端
     */
    public static void release(String clusterAlias, Object resource) {
        if (resource instanceof KafkaConsumer) {
            kafkaConsumerPoolMap.get(clusterAlias).returnObject((KafkaConsumer) resource);
        }
        if (resource instanceof KafkaProducer) {
            kafkaProducerPoolMap.get(clusterAlias).returnObject((KafkaProducer) resource);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initKafkaConsumerPool();
        initKafkaProducerPool();
    }

    /**
     * kafka生产者资源池
     */
    private void initKafkaProducerPool() {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));

        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(kafkaClientPoolMinIdle);
            poolConfig.setMaxIdle(kafkaClientPoolMaxIdle);
            poolConfig.setMaxTotal(kafkaClientPoolMaxSize);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(false);
            poolConfig.setMaxWaitMillis(poolMaxWaitMs);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);

            String bootstrapServers = clusterBrokerInfoMap.get(singleClusterConfig.getAlias()).stream()
                    .map(kafkaBrokerInfo -> kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getPort()).collect(Collectors.joining(","));
            kafkaClusterBootstrapServerMap.put(singleClusterConfig.getAlias(), bootstrapServers);

            Properties properties = new Properties();
            if (singleClusterConfig.getSasl().getEnable()) {
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, singleClusterConfig.getSasl().getProtocol());
                if (!StringUtils.isEmpty(singleClusterConfig.getSasl().getClientId())) {
                    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, singleClusterConfig.getSasl().getClientId());
                }
                properties.put(SaslConfigs.SASL_MECHANISM, singleClusterConfig.getSasl().getMechanism());
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, singleClusterConfig.getSasl().getJaasConfig());
            }

            if (kafkaEagleSqlFixError) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST);
            }

            PooledKafkaProducerFactory factory = new PooledKafkaProducerFactory(bootstrapServers,
                    kafkaSendErrorRetry, kafkaRequestTimeoutMs, singleClusterConfig.getSasl().getEnable(), properties);
            GenericObjectPool<KafkaProducer> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            kafkaProducerPoolMap.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
    }

    /**
     * Kafka消费者初始化
     */
    private void initKafkaConsumerPool() {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));

        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(kafkaClientPoolMinIdle);
            poolConfig.setMaxIdle(kafkaClientPoolMaxIdle);
            poolConfig.setMaxTotal(kafkaClientPoolMaxSize);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(false);
            poolConfig.setTestWhileIdle(false);
            poolConfig.setMaxWaitMillis(poolMaxWaitMs);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);

            String bootstrapServers = clusterBrokerInfoMap.get(singleClusterConfig.getAlias()).stream()
                    .map(kafkaBrokerInfo -> kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getPort()).collect(Collectors.joining(","));
            kafkaClusterBootstrapServerMap.put(singleClusterConfig.getAlias(), bootstrapServers);

            Properties properties = new Properties();
            if (singleClusterConfig.getSasl().getEnable()) {
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, singleClusterConfig.getSasl().getProtocol());
                if (!StringUtils.isEmpty(singleClusterConfig.getSasl().getClientId())) {
                    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, singleClusterConfig.getSasl().getClientId());
                }
                properties.put(SaslConfigs.SASL_MECHANISM, singleClusterConfig.getSasl().getMechanism());
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, singleClusterConfig.getSasl().getJaasConfig());
            }

            if (kafkaEagleSqlFixError) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST);
            }

            PooledKafkaConsumerFactory factory = new PooledKafkaConsumerFactory(bootstrapServers,
                    kafkaSendErrorRetry, kafkaRequestTimeoutMs, singleClusterConfig.getSasl().getEnable(), properties);
            GenericObjectPool<KafkaConsumer> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            kafkaConsumerPoolMap.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
    }
}
