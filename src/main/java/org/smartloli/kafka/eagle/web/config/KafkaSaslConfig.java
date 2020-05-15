package org.smartloli.kafka.eagle.web.config;

import lombok.Data;

/**
 * kafka sasl属性
 *
 * @author zhiwei_yang
 * @time 2020-5-14-15:04
 */
@Data
public class KafkaSaslConfig {

    /**
     * Kafka是否开启SASL认证
     */
    private Boolean enable;

    /**
     * sasl协议
     */
    private String protocol;

    /**
     * sasl 机制
     */
    private String mechanism;

    /**
     * jaas配置
     */
    private String jaasConfig;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * kafka cgroup 是否开启
     */
    private Boolean cgroupEnable = false;

    /**
     * kafka cgroup主题列表
     */
    private String cgroupTopics;
}
