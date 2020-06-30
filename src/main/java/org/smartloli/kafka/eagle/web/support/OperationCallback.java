package org.smartloli.kafka.eagle.web.support;

/**
 * 业务操作接口，需返回值
 *
 * @author zhiwei_yang
 * @time 2020-6-30-8:58
 */
public interface OperationCallback<A, R> {

    /**
     * 业务执行逻辑
     *
     * @param a 请求入参对想想
     * @return 返回值
     */
    R execute(A a);
}
