package org.smartloli.kafka.eagle.web.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;

import java.util.Properties;

public class PooledKafkaConsumerFactory extends BasePooledObjectFactory<KafkaConsumer> {

    /**
     * kafka集群节点列表
     **/
    private final String bootstrapServers;

    /**
     * 失败重试发送次数
     */
    private final Integer retries;

    /**
     * 请求超时设置
     */
    private final Integer requestTimeoutMs;

    /**
     * 是否开启sask认证
     **/
    private final Boolean saslEnable;

    /**
     * 负载配置参数
     **/
    public Properties properties;

    /**
     * 构造器
     *
     * @param bootstrapServers kafka集群列表
     * @param saslEnable       是否开启sasl认证
     * @param properties       附件参数
     */
    public PooledKafkaConsumerFactory(String bootstrapServers, Integer retries, Integer requestTimeoutMs, Boolean saslEnable, Properties properties) {
        this.bootstrapServers = bootstrapServers;
        this.retries = retries;
        this.requestTimeoutMs = requestTimeoutMs;
        this.saslEnable = saslEnable;
        this.properties = properties;
    }

    /**
     * 创建一个对象实例
     */
    @Override
    public KafkaConsumer<String, String> create() {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConstants.KAFKA_EAGLE_SYSTEM_GROUP);
        kafkaProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaProperties.setProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, Integer.toString(requestTimeoutMs));
        kafkaProperties.setProperty(CommonClientConfigs.RETRIES_CONFIG, Integer.toString(retries));
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        if (saslEnable) {
            this.properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
            this.properties.put(SaslConfigs.SASL_MECHANISM, properties.getProperty(SaslConfigs.SASL_MECHANISM));
            this.properties.put(SaslConfigs.SASL_JAAS_CONFIG, properties.getProperty(SaslConfigs.SASL_JAAS_CONFIG));

            if (!StringUtils.isEmpty(kafkaProperties.getProperty(CommonClientConfigs.CLIENT_ID_CONFIG))) {
                this.properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, properties.getProperty(CommonClientConfigs.CLIENT_ID_CONFIG));
            }
        }
        return new KafkaConsumer<>(kafkaProperties);
    }

    @Override
    public PooledObject<KafkaConsumer> wrap(KafkaConsumer kafkaConsumer) {
        return new DefaultPooledObject<>(kafkaConsumer);
    }

    @Override
    public void destroyObject(PooledObject<KafkaConsumer> kafkaConsumerPooledObject) {
        kafkaConsumerPooledObject.getObject().close();
    }
}
