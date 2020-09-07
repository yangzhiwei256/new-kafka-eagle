package org.smartloli.kafka.eagle.web.entity;

import lombok.Data;

import java.util.List;

/**
 * KSQL 查询完全信息
 *
 * @author zhiwei_yang
 * @time 2020-9-7-9:21
 */
@Data
public class QueryKafkaMessage {

    /**
     * 是否查询失败
     **/
    private boolean error = false;

    /**
     * 查询消息数据
     **/
    private List<KafkaMessage> data;

    /**
     * 查询花费时间
     **/
    private long spent;

    /**
     * 状态说明信息
     **/
    private String status;
}
