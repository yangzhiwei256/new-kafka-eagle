package org.smartloli.kafka.eagle.pool;

import kafka.zk.KafkaZkClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.smartloli.kafka.eagle.web.support.factory.PooledZookeeperFactory;
import org.springframework.util.Assert;

public class PoolObjectPoolTest {

    public static void main(String[] args) throws Exception {
        // 创建池对象工厂
        PooledZookeeperFactory factory = new PooledZookeeperFactory("10.101.72.43:2181,10.101.72.43:2182,10.101.72.43:2183",
                "topic-management-service", 30000, 30000);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        // 最大空闲数
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxTotal(20);
        poolConfig.setMinEvictableIdleTimeMillis(1800000);
        poolConfig.setTimeBetweenEvictionRunsMillis(1800000 * 2L);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(false);
        poolConfig.setMaxWaitMillis(5000);
        poolConfig.setLifo(true);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setNumTestsPerEvictionRun(3);

        // 创建对象池
        final GenericObjectPool<KafkaZkClient> pool = new GenericObjectPool<>(factory, poolConfig);

        KafkaZkClient zkClient = pool.borrowObject();
        boolean flag = zkClient.pathExists("/brokers");
        Assert.isTrue(flag, "节点不存在");
        pool.returnObject(zkClient);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                KafkaZkClient zookeeperClient = null;
                try {
                    zookeeperClient = pool.borrowObject();
                    Assert.isTrue(zkClient.pathExists("/brokers"), "节点不存在");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    pool.returnObject(zookeeperClient);
                }
            }).start();
        }
    }
}