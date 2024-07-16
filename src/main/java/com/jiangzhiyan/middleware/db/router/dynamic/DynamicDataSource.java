package com.jiangzhiyan.middleware.db.router.dynamic;

import com.jiangzhiyan.middleware.db.router.strategy.IRouterStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 */
@RequiredArgsConstructor
public class DynamicDataSource extends AbstractRoutingDataSource {
    private static final String DB_PREFIX = "db";

    private final IRouterStrategy dbRouterStrategy;

    @Override
    protected Object determineCurrentLookupKey() {
        Integer dbKeyIndex = dbRouterStrategy.getDbIndex();
        return dbKeyIndex == null ? this.dbRouterStrategy.getDefaultDb() : DB_PREFIX + dbKeyIndex;
    }
}
