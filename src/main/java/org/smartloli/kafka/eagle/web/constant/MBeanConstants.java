package org.smartloli.kafka.eagle.web.constant;

/**
 * Kafka jmx mbean.
 */
public final class MBeanConstants {

    private MBeanConstants() {
    }

    public static final String COUNT = "Count";
    public static final String EVENT_TYPE = "EventType";
    public static final String FIFTEEN_MINUTE_RATE = "FifteenMinuteRate";
    public static final String FIVE_MINUTE_RATE = "FiveMinuteRate";
    public static final String MEAN_RATE = "MeanRate";
    public static final String ONE_MINUTE_RATE = "OneMinuteRate";
    public static final String RATE_UNIT = "RateUnit";
    public static final String VALUE = "Value";
    /**
     * Messages in /sec.
     */
    public static final String MESSAGES_IN = "msg";
    /**
     * Bytes in /sec.
     */
    public static final String BYTES_IN = "ins";
    /**
     * Bytes out /sec.
     */
    public static final String BYTES_OUT = "out";
    /**
     * Bytes rejected /sec.
     */
    public static final String BYTES_REJECTED = "rejected";
    /**
     * Failed fetch request /sec.
     */
    public static final String FAILED_FETCH_REQUEST = "fetch";
    /**
     * Failed produce request /sec.
     */
    public static final String FAILED_PRODUCE_REQUEST = "produce";
    /**
     * MBean keys.
     */
    public static final String MESSAGEIN = "message_in";
    public static final String BYTEIN = "byte_in";
    public static final String BYTEOUT = "byte_out";
    public static final String BYTESREJECTED = "byte_rejected";
    public static final String FAILEDFETCHREQUEST = "failed_fetch_request";
    public static final String FAILEDPRODUCEREQUEST = "failed_produce_request";
    public static final String PRODUCEMESSAGECONVERSIONS = "produce_message_conversions";
    public static final String TOTALFETCHREQUESTSPERSEC = "total_fetch_requests";
    public static final String TOTALPRODUCEREQUESTSPERSEC = "total_produce_requests";
    public static final String REPLICATIONBYTESINPERSEC = "replication_bytes_out";
    public static final String REPLICATIONBYTESOUTPERSEC = "replication_bytes_in";
    public static final String OSTOTALMEMORY = "os_total_memory";
    public static final String OSFREEMEMORY = "os_free_memory";

}