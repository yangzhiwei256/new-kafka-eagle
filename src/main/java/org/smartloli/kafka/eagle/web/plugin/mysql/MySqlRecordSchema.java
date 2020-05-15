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
package org.smartloli.kafka.eagle.web.plugin.mysql;

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
@Component
@Slf4j
public class MySqlRecordSchema {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    private static void tables(String username, String password, String host, String port, String db) {
        Connection connection = MySqlStoragePlugin.getInstance(host + ":" + port + "/" + db, username, password);
        ResultSet rs = null;
        Statement stmt = null;
        List<String> tbls = new ArrayList<>();
        try {
            rs = connection.createStatement().executeQuery(JConstants.SHOW_TABLES);
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
					String key = "CREATE_TABLE_" + tbl.toUpperCase();
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
		MySqlStoragePlugin.close(connection);
	}

	private static boolean database(String username, String password, String host, String port, String db) {
		Connection connection = MySqlStoragePlugin.getInstance(host + ":" + port, username, password);
		ResultSet rs = null;
		List<String> dbs = new ArrayList<>();
		try {
			rs = connection.createStatement().executeQuery(JConstants.SHOW_DATABASES);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					dbs.add(rs.getString(i));
				}
			}
			if (dbs.contains(db)) {
                log.info("The [" + db + "] database already exists. Do not need to create it.");
            } else {
				boolean status = connection.createStatement().execute(String.format(JConstants.CREATE_DB_SQL, db));
				if (!status) {
                    log.info("SQL statement affect the 0 records. Create [" + db + "] has successed.");
                } else {
                    log.error("Create [" + db + "] has failed.");
                }
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        MySqlStoragePlugin.close(connection);
        return false;
    }

    /**
     * Load database schema script.
     */
    public void schema() {
        String url = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();
        String host = url.split("//")[1].split("/")[0].split(":")[0];
        String port = url.split("//")[1].split("/")[0].split(":")[1];
        String db = url.split("//")[1].split("/")[1].split("\\?")[0];

        if (database(username, password, host, port, db)) {
            tables(username, password, host, port, db);
        }
    }
}
