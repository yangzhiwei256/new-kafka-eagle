package org.smartloli.kafka.eagle.web.support;

/**
 * 业务操作接口，无需返回值
 *
 * @author zhiwei_yang
 * @time 2020-6-30-9:10
 */
public interface OperationCallbackWithoutResult<A> {

    /**
     * 业务执行无需返回值
     *
     * @param a 请求入参
     */
    void executeWithoutResult(A a);
}
