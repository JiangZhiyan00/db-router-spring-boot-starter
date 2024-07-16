package com.jiangzhiyan.middleware.db.router.strategy;

/**
 * 手动指定分库/分表策略接口
 */
public interface IManualRouterStrategy {
    /**
     * 手动指定分库
     *
     * @param dbIndex 库索引
     */
    void setDbRouter(int dbIndex);

    /**
     * 手动指定分表
     *
     * @param tableIndex 表索引
     */
    void setTableRouter(int tableIndex);

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
     * 清除上下文
     */
    void clear();
}
