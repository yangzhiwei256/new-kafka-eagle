package org.smartloli.kafka.eagle.web.config;

/**
 * kafka集群属性
 *
 * @author zhiwei_yang
 * @time 2020-5-14-15:01
 */

import lombok.Data;

@Data
public class SingleClusterConfig {

    /**
     * 集群名
     */
    private String alias;

    /**
     * zookeeper集群
     **/
    private String zkList;

    /**
     * kafka集群代理服务器数量
     */
    private Integer brokerSize;

    /**
     * kafka集群元数据存储位置
     **/
    private String offsetStorage;

    /**
     * 是否开启sasl认证
     */
    private KafkaSaslConfig sasl;

    /**
     * zookeeper最大连接数
     **/
    private Integer zkPoolMaxSize;
    /**
     * 最小空闲连接数
     **/
    private Integer zkPoolMinIdle;
    /**
     * 最大空闲连接数
     **/
    private Integer zkPoolMaxIdle;
    /**
     * zookeeper连接超时
     **/
    private Integer ZkConnectTimeoutMs;
    /**
     * zookeeper 会话超时
     **/
    private Integer zkSessionTimeoutMs;
    /**
     * Kafka Client 最大连接数
     **/
    private Integer kafkaClientPoolMaxSize;
    /**
     * kafka 客户端最小空闲连接数
     **/
    private Integer kafkaClientPoolMinIdle;
    /**
     * kafka 客户端最大连接数
     **/
    private Integer kafkaClientPoolMaxIdle;
    /**
     * kafka消息发送错误重试次数
     **/
    private Integer kafkaSendErrorRetry;
    /**
     * kafka 消息请求超时时间
     **/
    private Integer kafkaRequestTimeoutMs;
}
