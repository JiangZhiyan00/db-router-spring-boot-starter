package com.jiangzhiyan.middleware.db.router.strategy;

/**
 * 手动指定分库/分表策略接口
 */
public interface IManualRouterStrategy extends IRouterStrategy {
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
}
