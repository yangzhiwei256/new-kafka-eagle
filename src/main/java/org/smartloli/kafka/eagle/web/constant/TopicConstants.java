package org.smartloli.kafka.eagle.web.constant;

import java.util.Arrays;
import java.util.List;

public final class TopicConstants {
    public static final int PARTITION_LENGTH = 10;
    public static final String ADD = "ADD";
    public static final String DELETE = "DELETE";
    public static final String DESCRIBE = "DESCRIBE";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String LOGSIZE = "logsize";
    public static final String CAPACITY = "capacity";
    public static final String BROKER_SPREAD = "spread";
    public static final String BROKER_SKEWED = "skewed";
    public static final String BROKER_LEADER_SKEWED = "leader_skewed";
    public static final String[] BROKER_PERFORMANCE_LIST = new String[]{BROKER_SPREAD, BROKER_SKEWED, BROKER_LEADER_SKEWED};
    public static final String TRUNCATE = "truncate";// 0:truncating,1:truncated
    public static final String CLEANUP_POLICY_KEY = "cleanup.policy";
    public static final String CLEANUP_POLICY_VALUE = "delete";
    public static final String RETENTION_MS_KEY = "retention.ms";
    public static final String RETENTION_MS_VALUE = "1000";
    public static final int BATCH_SIZE = 500;
    public static final long TOPIC_BROKER_SPREAD_ERROR = 60;
    public static final long TOPIC_BROKER_SPREAD_NORMAL = 80;
    public static final long TOPIC_BROKER_SKEW_ERROR = 80;
    public static final long TOPIC_BROKER_SKEW_NORMAL = 30;
    public static final long TOPIC_BROKER_LEADER_SKEW_ERROR = 80;
    public static final long TOPIC_BROKER_LEADER_SKEW_NORMAL = 30;
    public static final int RUNNING = 0;
    public static final int SHUTDOWN = 1;
    public static final int PENDING = 2;
    public static final String RUNNING_STRING = "Running";
    public static final String SHUTDOWN_STRING = "Shutdown";
    public static final String PENDING_STRING = "Pending";
    public static final String PRODUCERS = "producers";
    public static final String CONSUMERS = "consumers";
    public static final String LAG = "lag";
    private TopicConstants() {
    }

    public static List<String> getTopicConfigKeys() {
        return Arrays.asList("cleanup.policy", "compression.type", "delete.retention.ms", "file.delete.delay.ms", "flush.messages", "flush.ms", "follower.replication.throttled", "index.interval.bytes", "leader.replication.throttled.replicas", "max.message.bytes", "message.downconversion.enable", "message.format.version",
                "message.timestamp.difference.max.ms", "message.timestamp.type", "min.cleanable.dirty.ratio", "min.compaction.lag.ms", "min.insync.replicas", "preallocate", "retention.bytes", "retention.ms", "segment.bytes", "segment.index.bytes", "segment.jitter.ms", "segment.ms", "unclean.leader.election.enable");
    }
}