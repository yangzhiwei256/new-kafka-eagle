package org.smartloli.kafka.eagle.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhiwei_yang
 * @time 2020-5-14-14:59
 */
@Configuration
@ConfigurationProperties(prefix = "kafka.eagle")
@Data
public class KafkaClustersConfig {

    /** 集群公共配置 **/
    /**
     * zookeeper最大连接数
     **/
    private Integer zkPoolMaxSize = 20;
    /**
     * 最小空闲连接数
     **/
    private Integer zkPoolMinIdle = 5;
    /**
     * 最大空闲连接数
     **/
    private Integer zkPoolMaxIdle = 10;
    /**
     * zookeeper连接超时
     **/
    private Integer ZkConnectTimeoutMs = 30000;
    /**
     * zookeeper 会话超时
     **/
    private Integer zkSessionTimeoutMs = 30000;
    /**
     * Kafka Client 最大连接数
     **/
    private Integer kafkaClientPoolMaxSize = 20;
    /**
     * kafka 客户端最小空闲连接数
     **/
    private Integer kafkaClientPoolMinIdle = 5;
    /**
     * kafka 客户端最大连接数
     **/
    private Integer kafkaClientPoolMaxIdle = 10;
    /**
     * kafka消息发送错误重试次数
     **/
    private Integer kafkaSendErrorRetry = 1;
    /**
     * kafka 消息请求超时时间
     **/
    private Integer kafkaRequestTimeoutMs = 10000;

    /**
     * KSQL自动修复：保证读取具有offset
     * <p>
     * auto.offset.reset:
     * latest: 接收新数据，从新数据开始处理(默认)
     * earliest: 若已提交offset则从最新的offset读取，否则从0开始读取
     * none：主题分区Offset未提交offset则报错
     **/
    private Boolean sqlFixError = false;

    /**
     * KSQL最大常洵记录数
     **/
    private Long sqlTopicRecordsMax = 5000L;

    /**
     * 单个Kafka集群配置
     **/
    private List<SingleClusterConfig> clusters;

    /**
     * 获取Kafka集群名称列表
     *
     * @return
     */
    public List<String> getClusterAllAlias() {
        if (CollectionUtils.isEmpty(clusters)) {
            return Collections.emptyList();
        }
        return clusters.stream().map(SingleClusterConfig::getAlias).collect(Collectors.toList());
    }

    /**
     * 根据kafka集群名称获取配置
     *
     * @param clusterName
     * @return
     */
    public SingleClusterConfig getClusterConfigByName(String clusterName) {
        if (StringUtils.isEmpty(clusterName)) {
            return null;
        }
        for (SingleClusterConfig singleClusterConfig : clusters) {
            if (singleClusterConfig.getAlias().equalsIgnoreCase(clusterName)) {
                return singleClusterConfig;
            }
        }
        return null;
    }
}
