package org.smartloli.kafka.eagle.web.kafka;

import kafka.zk.KafkaZkClient;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Kafka Zookeeper客户端操作模板
 *
 * @author zhiwei_yang
 * @time 2020-6-30-9:41
 */
public class KafkaZkClientTemplate {

    GenericObjectPool<KafkaZkClient> genericObjectPool;
}
