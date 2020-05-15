package org.smartloli.kafka.eagle.web.util;

import kafka.zk.KafkaZkClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.utils.Time;

public class PooledZookeeperFactory extends BasePooledObjectFactory<KafkaZkClient> {

    private final String zookeeperList;
    private final String metricGroupName;
    public int connectTimeout;
    public int sessionTimeout;


    public PooledZookeeperFactory(String zookeeperList, String metricGroupName, int connectTimeout, int sessionTimeout) {
        this.zookeeperList = zookeeperList;
        this.metricGroupName = metricGroupName;
        this.connectTimeout = connectTimeout;
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * 创建一个对象实例
     */
    @Override
    public KafkaZkClient create() {
        return KafkaZkClient.apply(zookeeperList, JaasUtils.isZkSecurityEnabled(),
                sessionTimeout, connectTimeout, Integer.MAX_VALUE, Time.SYSTEM, metricGroupName, "SessionExpireListener");
    }

    @Override
    public PooledObject<KafkaZkClient> wrap(KafkaZkClient kafkaZkClient) {
        return new DefaultPooledObject<>(kafkaZkClient);
    }

    @Override
    public void destroyObject(PooledObject<KafkaZkClient> kafkaZkClientPooledObject) {
        kafkaZkClientPooledObject.getObject().close();
    }
}
