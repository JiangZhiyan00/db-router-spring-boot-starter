package com.jiangzhiyan.middleware.db.router.strategy.impl;

import com.jiangzhiyan.middleware.db.router.RouterConfig;
import com.jiangzhiyan.middleware.db.router.strategy.AbstractRouterStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouterHashCodeStrategy extends AbstractRouterStrategy {

    public RouterHashCodeStrategy(RouterConfig routerConfig) {
        super(routerConfig);
    }

    @Override
    protected Integer doDbRouter(Object dbKeyValue) {
        if (dbKeyValue == null) {
            return null;
        }
        //扰动函数值
        int hash = hash(dbKeyValue);
        //库索引
        int dbIndex;
        if (isPowerOfTwo(this.routerConfig.getDbCount())) {
            dbIndex = (hash & (this.routerConfig.getDbCount() - 1)) + 1;
        } else {
            log.warn("The dbCount [{}] is not 2 to the nth power,and data hashing may be uneven.", this.routerConfig.getDbCount());
            dbIndex = (hash % this.routerConfig.getDbCount()) + 1;
        }

        log.info("db-router result: {}", dbIndex);
        return dbIndex;
    }

    @Override
    public Integer doTableRouter(Object tableKeyValue) {
        if (tableKeyValue == null) {
            return null;
        }
        int dbCount = this.routerConfig.getDbCount();
        int tableCount = this.routerConfig.getTableCount();

        //扰动函数值
        int hash = hash(tableKeyValue);
        boolean hasDbRouterBefore = this.getDbIndex() != null;
        int size = hasDbRouterBefore ? dbCount * tableCount : tableCount;

        int idx;
        if (isPowerOfTwo(size)) {
            idx = hash & (size - 1);
        } else {
            idx = hash % (size);
            if (hasDbRouterBefore) {
                log.warn("The dbCount * tableCount [{} * {} = {}] is not 2 to the nth power,and data hashing may be uneven.", dbCount, tableCount, size);
            } else {
                log.warn("The tableCount [{}] is not 2 to the nth power,and data hashing may be uneven.", tableCount);
            }
        }

        //表索引
        int tableIndex;
        if (hasDbRouterBefore) {
            //重新分库,再分表
            int dbIndex = (idx / this.routerConfig.getTableCount()) + 1;
            tableIndex = tableCount * dbIndex - idx;
            DBContextHolder.setDbIndex(dbIndex);
            log.info("db-router recalculate result: {}", dbIndex);
        } else {
            //仅分表
            tableIndex = (tableCount / idx) + 1;
        }

        log.info("table-router result: {}", tableIndex);
        return tableIndex;
    }

    /**
     * 扰动函数,二次hash,可以使hash尽量均匀(参考HashMap)
     *
     * @param key 值
     * @return 扰动函数值
     */
    private static int hash(Object key) {
        int h;
        return (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 检查数字是否是2的n次方
     *
     * @param n 数字
     * @return 是否是2的n次方
     */
    public static boolean isPowerOfTwo(int n) {
        if (n <= 0) {
            return false;
        }
        return (n & (n - 1)) == 0;
    }
}