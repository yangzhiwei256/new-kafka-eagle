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

    /** kafka集群元数据存储位置 **/
    private String offsetStorage;

    /**
     * 是否开启sasl认证
     */
    private KafkaSaslConfig sasl;
}
