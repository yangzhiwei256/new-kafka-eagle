package org.smartloli.kafka.eagle.web.config;

import com.alibaba.fastjson.JSON;
import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.SaslConfigs;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.protocol.KafkaBrokerInfo;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.support.DatabaseTemplate;
import org.smartloli.kafka.eagle.web.support.KafkaAdminClientTemplate;
import org.smartloli.kafka.eagle.web.support.KafkaZkClientTemplate;
import org.smartloli.kafka.eagle.web.support.factory.PooledKafkaClientFactory;
import org.smartloli.kafka.eagle.web.support.factory.PooledZookeeperFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

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
}
