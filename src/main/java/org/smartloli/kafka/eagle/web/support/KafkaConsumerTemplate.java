package org.smartloli.kafka.eagle.web.support;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Map;

/**
 * @author zhiwei_yang
 * @time 2020-6-30-15:49
 */
public class KafkaConsumerTemplate implements ResourceManage<String, KafkaConsumer<String, String>> {

    private final Map<String, GenericObjectPool<KafkaConsumer<String, String>>> kafkaConsumerGenericObjectPoolMap;

    public KafkaConsumerTemplate(Map<String, GenericObjectPool<KafkaConsumer<String, String>>> kafkaConsumerGenericObjectPoolMap) {
        this.kafkaConsumerGenericObjectPoolMap = kafkaConsumerGenericObjectPoolMap;
    }

    /**
     * 获取KafkaConsumer
     *
     * @return
     */
    @Override
    public KafkaConsumer<String, String> acquire(String kafkaClusterName) {
        try {
            GenericObjectPool<KafkaConsumer<String, String>> kafkaConsumerGenericObjectPool = kafkaConsumerGenericObjectPoolMap.get(kafkaClusterName);
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
    public <R> R doExecute(String kafkaClusterName, OperationCallback<KafkaConsumer<String, String>, R> operationCallback) {
        KafkaConsumer<String, String> kafkaConsumer = acquire(kafkaClusterName);
        try {
            return operationCallback.execute(kafkaConsumer);
        } finally {
            this.release(kafkaClusterName, kafkaConsumer);
        }
    }

    @Override
    public void release(String kafkaClusterName, KafkaConsumer<String, String> kafkaConsumer) {
        if (null == kafkaConsumerGenericObjectPoolMap.get(kafkaClusterName) || null == kafkaConsumer) {
            return;
        }
        kafkaConsumerGenericObjectPoolMap.get(kafkaClusterName).returnObject(kafkaConsumer);
    }
}
