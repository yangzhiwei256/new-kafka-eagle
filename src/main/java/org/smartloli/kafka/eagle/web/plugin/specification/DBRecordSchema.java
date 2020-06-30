package org.smartloli.kafka.eagle.web.plugin.specification;

/**
 * 数据库初始化策略接口
 *
 * @author zhiwei_yang
 * @time 2020-6-30-8:35
 */
public interface DBRecordSchema {

    /**
     * 数据库完全初始化
     */
    void initAll();

    /**
     * 初始化数据库（不存在则创建/存在则不处理）
     *
     * @param database 数据库名
     * @return
     */
    default boolean initDatabase(String database) {
        return true;
    }

    /**
     * 初始化数据库表
     */
    void initTables();
}
