package com.jiangzhiyan.middleware.db.router.strategy;

/**
 * 分库分表策略接口
 */
public interface IDBRouterStrategy {

    /**
     * 执行分库分表路由
     *
     * @param dbKeyValue 分库分表字段值
     */
    void doRouter(Object dbKeyValue);

    /**
     * 获取分库索引
     *
     * @return 分库索引
     */
    Integer getDbKeyIndex();

    /**
     * 获取分表索引
     *
     * @return 分表索引
     */
    Integer getTableKeyIndex();

    /**
     * 获取默认库名
     *
     * @return 默认库名
     */
    String getDefaultDb();

    /**
     * 清除上下文
     */
    void clear();
}
