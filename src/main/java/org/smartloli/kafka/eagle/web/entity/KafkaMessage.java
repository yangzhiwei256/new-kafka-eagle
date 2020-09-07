package org.smartloli.kafka.eagle.web.entity;

import lombok.Data;

/**
 * Kafka消息封装体
 *
 * @author zhiwei_yang
 * @time 2020-9-7-9:10
 */
@Data
public class KafkaMessage {

    /**
     * 消息ID
     **/
    private String messageId;

    /**
     * 偏移量
     **/
    private Long offset;

    /**
     * 分区号
     **/
    private Integer partition;

    /**
     * 消息详情
     **/
    private String msg;
}
