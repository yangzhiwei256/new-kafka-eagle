package org.smartloli.kafka.eagle.web.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库操作模板
 *
 * @author zhiwei_yang
 * @time 2020-6-30-8:57
 */
@Slf4j
public class DatabaseTemplate implements ResourceManage<Object, Connection> {

    private final DataSourceProperties dataSourceProperties;

    public DatabaseTemplate(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    @Override
    public Connection acquire() {
        String address = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(address, username, password);
        } catch (Exception e) {
            log.error("Create mysql connection has error address[" + address + "],username[" + username + "],password[" + password + "],msg is " + e.getMessage());
        }
        return connection;
    }

    /**
     * 执行具体数据库操作
     *
     * @param operationCallback 数据库操作回调接口
     * @param <R>               返回值
     * @return
     */
    public <R> R doExecute(OperationCallback<Connection, R> operationCallback) {
        Connection connection = this.acquire();
        try {
            return operationCallback.execute(connection);
        } finally {
            this.release(null, connection);
        }
    }

    /**
     * 执行具体数据库操作
     *
     * @param operationCallbackWithoutResult 数据库操作回调接口
     * @return
     */
    public void doExecute(OperationCallbackWithoutResult<Connection> operationCallbackWithoutResult) {
        Connection connection = this.acquire();
        try {
            operationCallbackWithoutResult.executeWithoutResult(connection);
        } finally {
            this.release(null, connection);
        }
    }

    @Override
    public void release(Object o, Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
