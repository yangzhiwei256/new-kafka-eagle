package org.smartloli.kafka.eagle.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsOptions;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartloli.kafka.eagle.web.KafkaEagleBootstrap;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.KafkaService;
import org.smartloli.kafka.eagle.web.util.KafkaResourcePoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author zhiwei_yang
 * @time 2020-5-15-11:15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaEagleBootstrap.class)
@Slf4j
public class KafkaServiceTest {

    @Autowired
    private KafkaService kafkaService;

    @Test
    public void getKafkaProducerLogSizeTest() {
        String cluster = "cluster1";
        String topic = "MSG_OUTBOUND_APP3331";
        Set<Integer> partitionIds = new HashSet<>();
        partitionIds.add(1);
        partitionIds.add(2);
        Assert.assertNotEquals(0, kafkaService.getKafkaProducerLogSize(cluster, topic, partitionIds));
    }


    /**
     * 获取当前消费组主题消费偏移量
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void getKafkaProducerOffsetTest() throws ExecutionException, InterruptedException {
        String cluster = "cluster1";
        String topic = "MSG_OUTBOUND_APP3331";

        //查询分区
        List<TopicPartition> topicPartitions = new ArrayList<>();
        topicPartitions.add(new TopicPartition(topic, 1));
        topicPartitions.add(new TopicPartition(topic, 2));

        AdminClient adminClient = KafkaResourcePoolUtils.getKafkaClient(cluster);
        ListConsumerGroupOffsetsOptions listConsumerGroupOffsetsOptions = new ListConsumerGroupOffsetsOptions();
        listConsumerGroupOffsetsOptions.topicPartitions(topicPartitions);
        listConsumerGroupOffsetsOptions.timeoutMs(30000);

        Map<TopicPartition, OffsetAndMetadata> offsetMetadataMap =
                adminClient.listConsumerGroupOffsets(KafkaConstants.KAFKA_EAGLE_SYSTEM_GROUP, listConsumerGroupOffsetsOptions).partitionsToOffsetAndMetadata().get();
        Assert.assertNotNull(offsetMetadataMap);

        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsetMetadataMap.entrySet()) {
            TopicPartition topicPartition = entry.getKey();
            OffsetAndMetadata offsetAndMetadata = entry.getValue();
            log.info("主题：{},分区:{},当前偏移量:{}", topicPartition.topic(), topicPartition.partition(), offsetAndMetadata.offset());
        }
        KafkaResourcePoolUtils.release(cluster, adminClient);
    }
}
