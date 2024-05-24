package com.jiangzhiyan.middleware.db.router.strategy.impl;

import com.jiangzhiyan.middleware.db.router.DBRouterConfig;
import com.jiangzhiyan.middleware.db.router.strategy.AbstractDbRouterStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBRouterHashCodeStrategy extends AbstractDbRouterStrategy {

    public DBRouterHashCodeStrategy(DBRouterConfig dbRouterConfig) {
        super(dbRouterConfig);
    }

    @Override
    protected AbstractDbRouterStrategy.RouterResult getRouterResult(Object dbKeyValue) {
        if (dbKeyValue == null) {
            return null;
        }
        int size = this.dbRouterConfig.getDbCount() * this.dbRouterConfig.getTableCount();
        //扰动函数
        int idx = (size - 1) & (dbKeyValue.hashCode() ^ (dbKeyValue.hashCode() >>> 16));
        //库表索引
        int dbKeyIndex = idx / this.dbRouterConfig.getTableCount() + 1;
        int tableKeyIndex = idx - this.dbRouterConfig.getTableCount() * (dbKeyIndex - 1);

        log.info("db-router dbIndex: {} tableIndex：{}", dbKeyIndex, tableKeyIndex);
        return new RouterResult(dbKeyIndex, tableKeyIndex);
    }
}