package org.smartloli.kafka.eagle.web.support;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.kafka.clients.admin.AdminClient;

import java.util.Map;

/**
 * AdminClient 模板
 *
 * @author zhiwei_yang
 * @time 2020-6-30-14:30
 */
public class KafkaAdminClientTemplate implements ResourceManage<String, AdminClient> {

    private final Map<String, GenericObjectPool<AdminClient>> kafkaClusterPoolMap;

    public KafkaAdminClientTemplate(Map<String, GenericObjectPool<AdminClient>> kafkaClusterPoolMap) {
        this.kafkaClusterPoolMap = kafkaClusterPoolMap;
    }

    /**
     * 获取AdminClient
     *
     * @return
     */
    @Override
    public AdminClient acquire(String kafkaClusterName) {
        try {
            GenericObjectPool<AdminClient> adminClientGenericObjectPool = kafkaClusterPoolMap.get(kafkaClusterName);
            return null == adminClientGenericObjectPool ? null : adminClientGenericObjectPool.borrowObject();
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
    public <R> R doExecute(String kafkaClusterName, OperationCallback<AdminClient, R> operationCallback) {
        AdminClient adminClient = acquire(kafkaClusterName);
        try {
            return operationCallback.execute(adminClient);
        } finally {
            this.release(kafkaClusterName, adminClient);
        }
    }

    @Override
    public void release(String kafkaClusterName, AdminClient adminClient) {
        GenericObjectPool<AdminClient> adminClientGenericObjectPool = kafkaClusterPoolMap.get(kafkaClusterName);
        if (adminClientGenericObjectPool != null) {
            adminClientGenericObjectPool.returnObject(adminClient);
        }
    }
}
