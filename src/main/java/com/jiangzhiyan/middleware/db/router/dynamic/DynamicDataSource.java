package com.jiangzhiyan.middleware.db.router.dynamic;

import com.jiangzhiyan.middleware.db.router.strategy.IDBRouterStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 */
@RequiredArgsConstructor
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final IDBRouterStrategy dbRouterStrategy;

    @Override
    protected Object determineCurrentLookupKey() {
        Integer dbKeyIndex = dbRouterStrategy.getDbKeyIndex();
        return dbKeyIndex == null ? this.dbRouterStrategy.getDefaultDb() : dbKeyIndex;
    }
}
