/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.constant;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.util.JdbcConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * The client requests the t operation to parse the SQL and obtain the fields
 * and conditions.
 *
 * @author smartloli.
 * Created by May 19, 2019
 */
@Slf4j
public class OdpsSqlParser {

    private OdpsSqlParser() {
    }

    /**
     * 解析SQL获取表名即Topic名称
     */
    public static String parserTopic(String sql) {
        try {
            String dbType = JdbcConstants.MYSQL;
            List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
            SQLStatement stmt = stmtList.get(0);
            OdpsSchemaStatVisitor visitor = new OdpsSchemaStatVisitor();
            stmt.accept(visitor);
            Map<Name, TableStat> tables = visitor.getTables();
            String tableName = "";
            for (Name name : tables.keySet()) {
                tableName = name.toString();
            }
            return tableName.trim();
        } catch (Exception e) {
            log.error("Parser kafka sql has error", e);
            return "";
        }
    }
}
