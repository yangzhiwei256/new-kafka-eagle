package org.smartloli.kafka.eagle.web.support;

import kafka.zk.KafkaZkClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Map;

/**
 * Kafka Zookeeper 操作模板
 *
 * @author zhiwei_yang
 * @time 2020-6-30-10:18
 */
@Slf4j
public class KafkaZkClientTemplate implements ResourceManage<String, KafkaZkClient> {

    Map<String, GenericObjectPool<KafkaZkClient>> kafkaZookeeperPoolMap;

    public KafkaZkClientTemplate(Map<String, GenericObjectPool<KafkaZkClient>> kafkaZookeeperPoolMap) {
        this.kafkaZookeeperPoolMap = kafkaZookeeperPoolMap;
    }

    /**
     * 获取KafkaZkClient
     *
     * @return
     */
    @Override
    public KafkaZkClient acquire(String kafkaClusterName) {
        try {
            GenericObjectPool<KafkaZkClient> kafkaZkClientGenericObjectPool = kafkaZookeeperPoolMap.get(kafkaClusterName);
            return null == kafkaZkClientGenericObjectPool ? null : kafkaZkClientGenericObjectPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行业务逻辑
     *
     * @param kafkaClusterName  集群名称
     * @param operationCallback 回调接口
     * @param <R>               返回值
     * @return
     */
    public <R> R doExecute(String kafkaClusterName, OperationCallback<KafkaZkClient, R> operationCallback) {
        KafkaZkClient kafkaZkClient = acquire(kafkaClusterName);
        try {
            return operationCallback.execute(kafkaZkClient);
        } finally {
            this.release(kafkaClusterName, kafkaZkClient);
        }
    }

    @Override
    public void release(String kafkaClusterName, KafkaZkClient kafkaZkClient) {
        if (null == kafkaZookeeperPoolMap.get(kafkaClusterName) || null == kafkaZkClient) {
            return;
        }
        kafkaZookeeperPoolMap.get(kafkaClusterName).returnObject(kafkaZkClient);
    }
}
