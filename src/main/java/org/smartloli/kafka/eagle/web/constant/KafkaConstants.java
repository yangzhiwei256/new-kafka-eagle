package org.smartloli.kafka.eagle.web.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kafka parameter setting.
 */
@Component
public final class KafkaConstants {


    //kakfa eagle全局配置参数
    public static final String KAFKA_EAGLE_SQL_TOPIC_RECORDS_MAX = "kafka.eagle.sql.topic.records.max";
    public static final String KAFKA_EAGLE_SQL_FIX_ERROR = "kafka.eagle.sql.fix.error";
    public static final String KAFKA_EAGLE_TOPIC_TOKEN = "kafka.eagle.topic.token";
    public static final String KAFKA_EAGLE_METRICS_CHARTS = "kafka.eagle.metrics.charts";
    public static final String KAFKA_EAGLE_METRICS_RETAIN = "kafka.eagle.metrics.retain";

    public static final String KAFKA_CONFIG_FILE_NAME = "system-config.properties";
    public static final String CONSUMER_OFFSET_TOPIC = "__consumer_offsets";
    public static final String KAFKA_EAGLE_SYSTEM_GROUP = "kafka.eagle.system.group";
    public static final String AUTO_COMMIT = "true";
    public static final String AUTO_COMMIT_MS = "1000";
    public static final String EARLIEST = "earliest";
    public static final String JAVA_SECURITY = "java.security.auth.login.config";
    public static final int TIME_OUT = 100;
    public static final String KAFKA_ZK_CONNECT_TIMEOUT_MS = "kafka.zk.connect.timeout.ms";

    public static final String PARTITION_CLASS = "partitioner.class";
    public static final String KEY_SERIALIZER = "key.serializer";
    public static final String VALUE_SERIALIZER = "value.serializer";
    public static final String UNKOWN = "Unknown";

    public static final String JOB_PARAMS = "job_params";

    //KafkaConstants
    public static String[] TYPE = new String[]{"DingDing", "KafkaConstants", "Email"};
    public static String[] CLUSTER = new String[]{"Kafka", "KafkaConstants", "Topic", "Producer"};
    public static String[] LEVEL = new String[]{"P0", "P1", "P2", "P3"};
    public static int[] MAXTIMES = new int[]{-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    public static String EMAIL = "Email";
    public static String DingDing = "DingDing";
    public static String KafkaConstants = "KafkaConstants";
    public static String HTTP_GET = "get";
    public static String HTTP_POST = "post";
    public static String DISABLE = "N";
    public static String TOPIC = "Topic";
    public static String PRODUCER = "Producer";

    //KafkaConstants
    public static final String ZOOKEEPER = "zookeeper";
    public static final String KAFKA = "kafka";

    //Compoment
    public static final String UNKNOW = "unknow-host";

    //D3
    public static final int SIZE = 40;
    public static final int CHILD_SIZE = 20;

    //KafkaConstants
    public static final String TITLE = "Kafka Eagle Alert";
    public static final String LOGIN_USER_SESSION = "LOGIN_USER_SESSION";
    public static final String UNKNOW_USER = "__unknow__";
    public static final String ERROR_LOGIN = "error_msg";
    public static final String ADMIN = "admin";
    public static final int ADMINISTRATOR = 1; //管理员权限
    public static final int ANONYMOUS = 0; //匿名权限
    public static final String IF_SYSTEM_ADMIN = "IF_SYSTEM_ADMIN"; //是否系统管理员权限
    public static final String SYSTEM = "System"; //是否系统管理员权限

    public static final String KAFKA_EAGLE_ZK_CLUSTER_ALIAS = "kafka.eagle.zk.cluster.alias";
    public static final String CLUSTER_ALIAS = "clusterAlias";
    public static final String CLUSTER_ALIAS_LIST = "clusterAliasList";
    public static final int CLUSTER_ALIAS_LIST_LIMIT = 5;

    //Kafka Client 资源池配置
    public static final String KAFKA_CLIENT_MAX_TOTAL = "kafka.client.max.total";
    public static final String KAFKA_CLIENT_MIN_IDLE = "kafka.client.min.idle";
    public static final String KAFKA_CLIENT_MAX_IDLE = "kafka.client.max.idle";
    public static final String KAFKA_SEND_ERROR_RETRY = "kafka.send.error.retry";
    public static final String KAFKA_REQUEST_TIMEOUT_MS = "kafka.request.timeout.ms";


    /**
     * kafka zookeeper资源池最大数量
     **/
    public static final String KAFKA_ZK_MAX_TOTAL = "kafka.zk.max.total";
    public static final String KAFKA_ZK_MIN_IDLE = "kafka.zk.min.idle";
    public static final String KAFKA_ZK_MAX_IDLE = "kafka.zk.max.idle";
    public static final String KAFKA_ZK_SESSION_TIMEOUT_MS = "kafka.zk.session.timeout.ms";
    @Value("${" + KAFKA_EAGLE_SQL_TOPIC_RECORDS_MAX + ":5000}")
    public static Long POSITION;

    //Wechat
    public static final String TOUSER = "@all";
    public static final String TOPARTY = "PartyID1|PartyID2";
    public static final String TOTAG = "TagID1 | TagID2";
    public static final long AGENTID = 1;

    //KafkaConstants
    public static final String ZK_SEND_PACKETS = "zk_packets_sent";
    public static final String ZK_RECEIVEDPACKETS = "zk_packets_received";
    public static final String ZK_NUM_ALIVECONNRCTIONS = "zk_num_alive_connections";
    public static final String ZK_OUTSTANDING_REQUESTS = "zk_outstanding_requests";
    public static final String LEADER = "leader";

}