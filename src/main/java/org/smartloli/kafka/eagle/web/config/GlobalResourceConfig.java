package org.smartloli.kafka.eagle.web.config;

import com.alibaba.fastjson.JSON;
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SaslConfigs;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.support.*;
import org.smartloli.kafka.eagle.web.support.factory.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 全局资源配置
 *
 * @author zhiwei_yang
 * @time 2020-6-30-10:29
 */
@Configuration
@Slf4j
public class GlobalResourceConfig {

    /**
     * 数据库模板
     *
     * @param dataSourceProperties 数据源配置
     * @return
     */
    @Bean
    public DatabaseTemplate databaseTemplate(DataSourceProperties dataSourceProperties) {
        return new DatabaseTemplate(dataSourceProperties);
    }

    /**
     * KafkaZkClient 资源池配置
     *
     * @return
     */
    @Bean
    public Map<String, GenericObjectPool<KafkaZkClient>> kafkaZookeeperPoolMap(KafkaClustersConfig kafkaClustersConfig) {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        List<String> allZkList = kafkaClustersConfig.getClusters().stream().map(SingleClusterConfig::getZkList).collect(Collectors.toList());
        log.info("项目启动初始配置kafka集群Zookeeper信息：{}", JSON.toJSONString(allZkList));

        //不同Kafka集群对应多个资源池
        Map<String, GenericObjectPool<KafkaZkClient>> kafkaZookeeperPoolMap = new ConcurrentHashMap<>();
        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(Optional.ofNullable(singleClusterConfig.getZkPoolMinIdle()).orElse(kafkaClustersConfig.getZkPoolMinIdle()));
            poolConfig.setMaxIdle(Optional.ofNullable(singleClusterConfig.getZkPoolMaxIdle()).orElse(kafkaClustersConfig.getZkPoolMaxIdle()));
            poolConfig.setMaxTotal(Optional.ofNullable(singleClusterConfig.getZkPoolMaxSize()).orElse(kafkaClustersConfig.getZkPoolMaxSize()));
            poolConfig.setMaxWaitMillis(Optional.ofNullable(singleClusterConfig.getZkMaxWaitMs()).orElse(kafkaClustersConfig.getZkMaxWaitMs()));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);
            PooledZookeeperFactory factory = new PooledZookeeperFactory(singleClusterConfig.getZkList(),
                    KafkaConstants.METRIC_GROUP_NAME,
                    Optional.ofNullable(singleClusterConfig.getZkConnectTimeoutMs()).orElse(kafkaClustersConfig.getZkConnectTimeoutMs()),
                    Optional.ofNullable(singleClusterConfig.getZkSessionTimeoutMs()).orElse(kafkaClustersConfig.getZkSessionTimeoutMs()));
            GenericObjectPool<KafkaZkClient> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            kafkaZookeeperPoolMap.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
        return kafkaZookeeperPoolMap;
    }

    /**
     * kafka Zookeeper客户端模板
     *
     * @param kafkaZkClientGenericObjectPool 数据源配置
     * @return
     */
    @Bean
    public KafkaZkClientTemplate kafkaZkClientTemplate(Map<String, GenericObjectPool<KafkaZkClient>> kafkaZkClientGenericObjectPool) {
        return new KafkaZkClientTemplate(kafkaZkClientGenericObjectPool);
    }

    /**
     * AdminClient 资源池
     *
     * @param kafkaClustersConfig
     * @return
     */
    @Bean
    public Map<String, GenericObjectPool<AdminClient>> adminClientGenericObjectPool(KafkaClustersConfig kafkaClustersConfig,
                                                                                    KafkaService kafkaService) {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));

        Map<String, GenericObjectPool<AdminClient>> adminClientGenericObjectPool = new ConcurrentHashMap<>();
        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMinIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMinIdle()));
            poolConfig.setMaxIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxIdle()));
            poolConfig.setMaxTotal(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxSize()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxSize()));
            poolConfig.setMaxWaitMillis(Optional.ofNullable(singleClusterConfig.getKafkaClientMaxWaitMs()).orElse(kafkaClustersConfig.getKafkaClientMaxWaitMs()));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);

            String bootstrapServers = clusterBrokerInfoMap.get(singleClusterConfig.getAlias()).stream()
                    .map(kafkaBrokerInfo -> kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getPort()).collect(Collectors.joining(","));
            Properties properties = new Properties();
            if (singleClusterConfig.getSasl().getEnable()) {
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, singleClusterConfig.getSasl().getProtocol());
                if (!StringUtils.isEmpty(singleClusterConfig.getSasl().getClientId())) {
                    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, singleClusterConfig.getSasl().getClientId());
                }
                properties.put(SaslConfigs.SASL_MECHANISM, singleClusterConfig.getSasl().getMechanism());
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, singleClusterConfig.getSasl().getJaasConfig());
            }

            PooledKafkaClientFactory factory = new PooledKafkaClientFactory(bootstrapServers,
                    Optional.ofNullable(singleClusterConfig.getKafkaSendErrorRetry()).orElse(kafkaClustersConfig.getKafkaSendErrorRetry()),
                    Optional.ofNullable(singleClusterConfig.getKafkaRequestTimeoutMs()).orElse(kafkaClustersConfig.getKafkaRequestTimeoutMs()),
                    singleClusterConfig.getSasl().getEnable(), properties);
            GenericObjectPool<AdminClient> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            adminClientGenericObjectPool.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
        return adminClientGenericObjectPool;
    }

    /**
     * kafka admin Client 模板配置
     *
     * @param adminClientGenericObjectPool 数据源配置
     * @return
     */
    @Bean
    public KafkaAdminClientTemplate adminClientTemplate(Map<String, GenericObjectPool<AdminClient>> adminClientGenericObjectPool) {
        return new KafkaAdminClientTemplate(adminClientGenericObjectPool);
    }

    /**
     * Kafka消费者资源池
     */
    @Bean
    public Map<String, GenericObjectPool<KafkaConsumer<String, String>>> kafkaConsumerGenericObjectPoolMap(KafkaClustersConfig kafkaClustersConfig,
                                                                                                           KafkaService kafkaService) {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));

        Map<String, GenericObjectPool<KafkaConsumer<String, String>>> kafkaConsumerPoolMap = new ConcurrentHashMap<>();
        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMinIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMinIdle()));
            poolConfig.setMaxIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxIdle()));
            poolConfig.setMaxTotal(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxSize()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxSize()));
            poolConfig.setMaxWaitMillis(Optional.ofNullable(singleClusterConfig.getKafkaClientMaxWaitMs()).orElse(kafkaClustersConfig.getKafkaClientMaxWaitMs()));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);

            String bootstrapServers = clusterBrokerInfoMap.get(singleClusterConfig.getAlias()).stream()
                    .map(kafkaBrokerInfo -> kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getPort()).collect(Collectors.joining(","));
            Properties properties = new Properties();
            if (singleClusterConfig.getSasl().getEnable()) {
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, singleClusterConfig.getSasl().getProtocol());
                if (!StringUtils.isEmpty(singleClusterConfig.getSasl().getClientId())) {
                    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, singleClusterConfig.getSasl().getClientId());
                }
                properties.put(SaslConfigs.SASL_MECHANISM, singleClusterConfig.getSasl().getMechanism());
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, singleClusterConfig.getSasl().getJaasConfig());
            }

            if (Optional.ofNullable(singleClusterConfig.getSqlFixError()).orElse(kafkaClustersConfig.getSqlFixError())) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST);
            }

            PooledKafkaConsumerFactory factory = new PooledKafkaConsumerFactory(bootstrapServers,
                    Optional.ofNullable(singleClusterConfig.getKafkaSendErrorRetry()).orElse(kafkaClustersConfig.getKafkaSendErrorRetry()),
                    Optional.ofNullable(singleClusterConfig.getKafkaRequestTimeoutMs()).orElse(kafkaClustersConfig.getKafkaRequestTimeoutMs()),
                    singleClusterConfig.getSasl().getEnable(), properties);
            GenericObjectPool<KafkaConsumer<String, String>> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            kafkaConsumerPoolMap.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
        return kafkaConsumerPoolMap;
    }

    /**
     * Kafka Consumer 模板
     *
     * @param kafkaConsumerGenericObjectPoolMap
     * @return
     */
    @Bean
    public KafkaConsumerTemplate kafkaConsumerTemplate(Map<String, GenericObjectPool<KafkaConsumer<String, String>>> kafkaConsumerGenericObjectPoolMap) {
        return new KafkaConsumerTemplate(kafkaConsumerGenericObjectPoolMap);
    }

    /**
     * Kafak 生产者资源池
     *
     * @return
     */
    @Bean
    public Map<String, GenericObjectPool<KafkaProducer<String, String>>> kafkaProducerPoolMap(KafkaClustersConfig kafkaClustersConfig,
                                                                                              KafkaService kafkaService) {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));
        Map<String, GenericObjectPool<KafkaProducer<String, String>>> kafkaProducerPoolMap = new ConcurrentHashMap<>();

        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMinIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMinIdle()));
            poolConfig.setMaxIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxIdle()));
            poolConfig.setMaxTotal(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxSize()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxSize()));
            poolConfig.setMaxWaitMillis(Optional.ofNullable(singleClusterConfig.getKafkaClientMaxWaitMs()).orElse(kafkaClustersConfig.getKafkaClientMaxWaitMs()));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setLifo(true);
            poolConfig.setBlockWhenExhausted(true);

            String bootstrapServers = clusterBrokerInfoMap.get(singleClusterConfig.getAlias()).stream()
                    .map(kafkaBrokerInfo -> kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getPort()).collect(Collectors.joining(","));

            Properties properties = new Properties();
            if (singleClusterConfig.getSasl().getEnable()) {
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, singleClusterConfig.getSasl().getProtocol());
                if (!StringUtils.isEmpty(singleClusterConfig.getSasl().getClientId())) {
                    properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, singleClusterConfig.getSasl().getClientId());
                }
                properties.put(SaslConfigs.SASL_MECHANISM, singleClusterConfig.getSasl().getMechanism());
                properties.put(SaslConfigs.SASL_JAAS_CONFIG, singleClusterConfig.getSasl().getJaasConfig());
            }

            if (Optional.ofNullable(singleClusterConfig.getSqlFixError()).orElse(kafkaClustersConfig.getSqlFixError())) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaConstants.EARLIEST);
            }

            PooledKafkaProducerFactory factory = new PooledKafkaProducerFactory(bootstrapServers,
                    Optional.ofNullable(singleClusterConfig.getKafkaSendErrorRetry()).orElse(kafkaClustersConfig.getKafkaSendErrorRetry()),
                    Optional.ofNullable(singleClusterConfig.getKafkaRequestTimeoutMs()).orElse(kafkaClustersConfig.getKafkaRequestTimeoutMs()),
                    singleClusterConfig.getSasl().getEnable(), properties);
            GenericObjectPool<KafkaProducer<String, String>> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
            kafkaProducerPoolMap.put(singleClusterConfig.getAlias(), genericObjectPool);
        }
        return kafkaProducerPoolMap;
    }

    /***
     * kafka 生产者模板配置
     * @return
     */
    @Bean
    public KafkaProducerTemplate kafkaProducerTemplate(Map<String, GenericObjectPool<KafkaProducer<String, String>>> kafkaProducerPoolMap) {
        return new KafkaProducerTemplate(kafkaProducerPoolMap);
    }

    /**
     * Kafak JMX资源池
     */
    @Bean
    public Map<String, GenericObjectPool<JMXConnector>> brokerInfoGenericObjectPoolMap(KafkaClustersConfig kafkaClustersConfig,
                                                                                       KafkaService kafkaService) {
        if (CollectionUtils.isEmpty(kafkaClustersConfig.getClusters())) {
            throw new RuntimeException("Kafka集群配置为空,项目无法启动");
        }
        Map<String, List<KafkaBrokerInfo>> clusterBrokerInfoMap = kafkaService.getBrokerInfos(kafkaClustersConfig.getClusterAllAlias());
        log.info("项目启动初始配置kafka集群Broker节点信息：{}", JSON.toJSONString(clusterBrokerInfoMap));
        Map<String, GenericObjectPool<JMXConnector>> brokerInfoGenericObjectPoolMap = new ConcurrentHashMap<>();

        for (SingleClusterConfig singleClusterConfig : kafkaClustersConfig.getClusters()) {
            List<KafkaBrokerInfo> kafkaBrokerInfos = clusterBrokerInfoMap.get(singleClusterConfig.getAlias());
            for (KafkaBrokerInfo kafkaBrokerInfo : kafkaBrokerInfos) {

                // 开启KAFKA JMX 监控
                if (kafkaBrokerInfo.getJmxEnabled()) {
                    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                    poolConfig.setMinIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMinIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMinIdle()));
                    poolConfig.setMaxIdle(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxIdle()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxIdle()));
                    poolConfig.setMaxTotal(Optional.ofNullable(singleClusterConfig.getKafkaClientPoolMaxSize()).orElse(kafkaClustersConfig.getKafkaClientPoolMaxSize()));
                    poolConfig.setMaxWaitMillis(Optional.ofNullable(singleClusterConfig.getKafkaClientMaxWaitMs()).orElse(kafkaClustersConfig.getKafkaClientMaxWaitMs()));
                    poolConfig.setTestOnBorrow(true);
                    poolConfig.setTestOnReturn(true);
                    poolConfig.setTestWhileIdle(true);
                    poolConfig.setLifo(true);
                    poolConfig.setBlockWhenExhausted(true);

                    try {
                        JMXServiceURL jmxServiceUrl = new JMXServiceURL(String.format(KafkaConstants.JMX_URL_FORMAT, kafkaBrokerInfo.getHost() + ":" + kafkaBrokerInfo.getJmxPort()));
                        PooledJMXConnectorFactory factory = new PooledJMXConnectorFactory(jmxServiceUrl);
                        GenericObjectPool<JMXConnector> genericObjectPool = new GenericObjectPool<>(factory, poolConfig);
                        brokerInfoGenericObjectPoolMap.put(kafkaBrokerInfo.getJmxCacheKey(), genericObjectPool);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return brokerInfoGenericObjectPoolMap;
    }

    /***
     * kafka JMX 模板
     */
    @Bean
    public JMXConnectorTemplate jmxConnectorTemplate(Map<String, GenericObjectPool<JMXConnector>> brokerInfoGenericObjectPoolMap) {
        return new JMXConnectorTemplate(brokerInfoGenericObjectPoolMap);
    }
}
