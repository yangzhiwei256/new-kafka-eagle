package org.smartloli.kafka.eagle.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartloli.kafka.eagle.web.KafkaEagleBootstrap;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhiwei_yang
 * @time 2020-5-16-9:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaEagleBootstrap.class)
@Slf4j
public class MetricsServiceTest {

    @Autowired
    private MetricsService metricsService;

    private final String kafkaClusterName = "cluster1";

    /**
     * 获取所有节点监控信息
     */
    @Test
    public void getAllBrokersMBeanTest(){
        String result= metricsService.getAllBrokersMBean(kafkaClusterName);
        Assert.assertNotNull(result);
    }
}
