package com.jiangzhiyan.middleware.db.router.strategy;

/**
 * 分库分表策略接口
 */
public interface IRouterStrategy {

    /**
     * 执行分库路由
     *
     * @param dbKeyValue 分库字段值
     */
    void dbRouter(Object dbKeyValue);

    /**
     * 执行分表路由
     *
     * @param dbKeyValue 分表字段值
     */
    void tableRouter(Object dbKeyValue);

    /**
     * 获取分库索引
     *
     * @return 分库索引
     */
    Integer getDbIndex();

    /**
     * 获取分表索引
     *
     * @return 分表索引
     */
    Integer getTableIndex();

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
