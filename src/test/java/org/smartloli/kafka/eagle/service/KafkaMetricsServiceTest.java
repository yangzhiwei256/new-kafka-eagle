package org.smartloli.kafka.eagle.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartloli.kafka.eagle.web.KafkaEagleBootstrap;
import org.smartloli.kafka.eagle.web.service.KafkaMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhiwei_yang
 * @time 2020-6-30-18:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaEagleBootstrap.class)
@Slf4j
public class KafkaMetricsServiceTest {

    @Autowired
    private KafkaMetricsService kafkaMetricsService;
    private final String clusterName = "cluster1";
    private final String topic = "MSG_EVENT_BROADCAST";

    /**
     * 获取主题日志大小
     */
    @Test
    public void topicSizeTest() {
        kafkaMetricsService.topicSize(clusterName, topic);
    }
}
