package org.smartloli.kafka.eagle.web.support;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Map;

/**
 * @author zhiwei_yang
 * @time 2020-6-30-16:09
 */
public class KafkaProducerTemplate implements ResourceManage<String, KafkaProducer<String, String>> {

    private final Map<String, GenericObjectPool<KafkaProducer<String, String>>> kafkaProducerPoolMap;

    public KafkaProducerTemplate(Map<String, GenericObjectPool<KafkaProducer<String, String>>> kafkaProducerPoolMap) {
        this.kafkaProducerPoolMap = kafkaProducerPoolMap;
    }

    @Override
    public KafkaProducer<String, String> acquire(String kafkaClusterName) {
        try {
            GenericObjectPool<KafkaProducer<String, String>> kafkaConsumerGenericObjectPool = kafkaProducerPoolMap.get(kafkaClusterName);
            return null == kafkaConsumerGenericObjectPool ? null : kafkaConsumerGenericObjectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行业务
     *
     * @param kafkaClusterName  kafka集群名称
     * @param operationCallback 接口回调
     * @param <R>               返回值
     * @return
     */
    public <R> R doExecute(String kafkaClusterName, OperationCallback<KafkaProducer<String, String>, R> operationCallback) {
        KafkaProducer<String, String> kafkaProducer = acquire(kafkaClusterName);
        try {
            return operationCallback.execute(kafkaProducer);
        } finally {
            this.release(kafkaClusterName, kafkaProducer);
        }
    }

    @Override
    public void release(String kafkaClusterName, KafkaProducer<String, String> kafkaProducer) {
        if (null == kafkaProducerPoolMap.get(kafkaClusterName) || null == kafkaProducer) {
            return;
        }
        kafkaProducerPoolMap.get(kafkaClusterName).returnObject(kafkaProducer);
    }
}
