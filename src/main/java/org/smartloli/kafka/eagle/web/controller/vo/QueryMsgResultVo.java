package org.smartloli.kafka.eagle.web.controller.vo;

import lombok.Data;
import org.smartloli.kafka.eagle.web.entity.QueryKafkaMessage;
import org.smartloli.kafka.eagle.web.protocol.topic.TopicSqlHistory;

/**
 * KSQl 查询结果
 *
 * @author zhiwei_yang
 * @time 2020-9-7-9:36
 */
@Data
public class QueryMsgResultVo {

    /**
     * 查询消息结果
     **/
    private QueryKafkaMessage queryKafkaMessage;

    /**
     * 主题SQL查询历史
     **/
    private TopicSqlHistory topicSqlHistory;
}
