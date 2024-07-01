package com.jiangzhiyan.middleware.db.router.strategy;

/**
 * 分库分表策略接口
 */
public interface IRouterStrategy {

    /**
     * 执行分库路由,使用默认分库数量
     *
     * @param dbKeyValue 分库字段值
     */
    void dbRouter(Object dbKeyValue);

    /**
     * 执行分库路由,使用指定的分库数量
     *
     * @param dbKeyValue 分库字段值
     * @param dbCount    分库数量
     */
    void dbRouter(Object dbKeyValue, int dbCount);

    /**
     * 执行分表路由,使用默认的分表数量
     *
     * @param tableKeyValue 分表字段值
     */
    void tableRouter(Object tableKeyValue);

    /**
     * 执行分表路由,使用指定的分表数量
     *
     * @param tableKeyValue 分表字段值
     * @param tableCount    分表数量
     */
    void tableRouter(Object tableKeyValue, int tableCount);

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
     * 获取默认分库数量
     *
     * @return 默认分库数量
     */
    int getDefaultDbCount();

    /**
     * 获取默认分表数量
     *
     * @return 默认分表数量
     */
    int getDefaultTableCount();

    /**
     * 获取默认分库字段
     *
     * @return 默认分库字段
     */
    String getDefaultDbRouterKey();

    /**
     * 获取默认分库字段类型
     *
     * @return 默认分库字段类型
     */
    Class<?> getDefaultDbRouterKeyClass();

    /**
     * 获取默认分表字段
     *
     * @return 默认分表字段
     */
    String getDefaultTableRouterKey();

    /**
     * 获取默认分表字段类型
     *
     * @return 默认分表字段类型
     */
    Class<?> getDefaultTableRouterKeyClass();

    /**
     * 清除上下文
     */
    void clear();
}
