/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.plugin.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.plugin.util.JConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializing database scripts.
 *
 * @author smartloli.
 *
 *         Created by Aug 7, 2017
 */
@Slf4j
@Component
public class SqliteRecordSchema {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * Load database schema script.
     */
    public void schema() {
        String url = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();
        tables(url, username, password);
    }

    /**
     * 数据表数据初始化
     *
     * @param url      访问URL
     * @param username 用户名
     * @param password 密码
     */
    private void tables(String url, String username, String password) {
        Connection connection = SqliteStoragePlugin.getInstance(url, username, username);
        ResultSet rs = null;
        Statement stmt = null;
        List<String> tbls = new ArrayList<>();
        try {
            rs = connection.createStatement().executeQuery(JConstants.SQLITE_TABLES);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
					tbls.add(rs.getString(i));
				}
			}

			for (String tbl : JConstants.TBLS) {
                if (tbls.contains(tbl)) {
                    log.info("The [" + tbl + "] table already exists. Do not need to create it.");
				} else {
					String key = "CREATE_TABLE_SQLITE_" + tbl.toUpperCase();
					stmt = connection.createStatement();
					if (JConstants.KEYS.containsKey(key)) {
                        stmt.addBatch(JConstants.KEYS.get(key));
                    }
					if (JConstants.KEYS.containsKey(key + "_INSERT")) {
                        stmt.addBatch(JConstants.KEYS.get(key + "_INSERT"));
                    }
					int[] code = stmt.executeBatch();
                    if (code.length > 0) {
                        log.info("Create [" + tbl + "] has successed.");
                    } else {
                        log.error("Create [" + tbl + "] has failed.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		SqliteStoragePlugin.close(connection);
	}

}
