package org.smartloli.kafka.eagle.web.support;

import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.management.remote.JMXConnector;
import java.util.Map;

/**
 * @author zhiwei_yang
 * @time 2020-7-2-10:27
 */
public class JMXConnectorTemplate implements ResourceManage<String, JMXConnector> {

    private final Map<String, GenericObjectPool<JMXConnector>> kafkaBrokerInfoGenericObjectPoolMap;

    public JMXConnectorTemplate(Map<String, GenericObjectPool<JMXConnector>> kafkaBrokerInfoGenericObjectPoolMap) {
        this.kafkaBrokerInfoGenericObjectPoolMap = kafkaBrokerInfoGenericObjectPoolMap;
    }

    @Override
    public JMXConnector acquire(String brokerJmxCacheKey) {
        try {
            GenericObjectPool<JMXConnector> jmxConnectorGenericObjectPool = kafkaBrokerInfoGenericObjectPoolMap.get(brokerJmxCacheKey);
            return null != jmxConnectorGenericObjectPool ? kafkaBrokerInfoGenericObjectPoolMap.get(brokerJmxCacheKey).borrowObject() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行业务
     *
     * @param brokerJmxCacheKey JMX代理节点缓存键
     * @param operationCallback 接口回调
     * @param <R>               返回值
     * @return
     */
    public <R> R doExecute(String brokerJmxCacheKey, OperationCallback<JMXConnector, R> operationCallback) {
        JMXConnector jmxConnector = acquire(brokerJmxCacheKey);
        try {
            return operationCallback.execute(jmxConnector);
        } finally {
            this.release(brokerJmxCacheKey, jmxConnector);
        }
    }

    @Override
    public void release(String brokerJmxCacheKey, JMXConnector jmxConnector) {
        if (null == kafkaBrokerInfoGenericObjectPoolMap.get(brokerJmxCacheKey) || null == jmxConnector) {
            return;
        }
        kafkaBrokerInfoGenericObjectPoolMap.get(brokerJmxCacheKey).returnObject(jmxConnector);
    }
}
