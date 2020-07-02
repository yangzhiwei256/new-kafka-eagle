package org.smartloli.kafka.eagle.web.support.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;

/**
 * JMXConnector资源池
 */
public class PooledJMXConnectorFactory extends BasePooledObjectFactory<JMXConnector> {

    /**
     * kafka集群节点列表
     **/
    private final JMXServiceURL jmxServiceURL;

    private final Map<String, ?> environment;

    /**
     * 构造器
     *
     * @param jmxServiceURL JMX连接地址
     */
    public PooledJMXConnectorFactory(JMXServiceURL jmxServiceURL) {
        this(jmxServiceURL, null);
    }

    /**
     * 构造器
     *
     * @param jmxServiceURL JMX连接地址
     */
    public PooledJMXConnectorFactory(JMXServiceURL jmxServiceURL, Map<String, ?> environment) {
        this.jmxServiceURL = jmxServiceURL;
        this.environment = environment;
    }

    /**
     * 创建一个对象实例
     */
    @Override
    public JMXConnector create() {
        try {
            return JMXConnectorFactory.connect(jmxServiceURL, environment);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PooledObject<JMXConnector> wrap(JMXConnector jmxConnector) {
        return new DefaultPooledObject<>(jmxConnector);
    }

    @Override
    public void destroyObject(PooledObject<JMXConnector> jmxConnectorPooledObject) {
        try {
            if (null != jmxConnectorPooledObject)
                jmxConnectorPooledObject.getObject().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
