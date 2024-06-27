package com.jiangzhiyan.middleware.db.router.strategy.impl;

import com.jiangzhiyan.middleware.db.router.RouterConfig;
import com.jiangzhiyan.middleware.db.router.strategy.AbstractRouterStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashCodeRouterStrategy extends AbstractRouterStrategy {

    public HashCodeRouterStrategy(RouterConfig routerConfig) {
        super(routerConfig);
    }

    @Override
    protected Integer doDbRouter(Object dbKeyValue, int dbCount) {
        if (dbKeyValue == null) {
            return null;
        }
        //扰动函数值
        int hash = hash(dbKeyValue);
        //库索引
        int dbIndex;
        if (isPowerOfTwo(dbCount)) {
            dbIndex = (hash & (dbCount - 1)) + 1;
        } else {
            log.warn("The dbCount [{}] is not 2 to the nth power,and data hashing may be uneven.", dbCount);
            dbIndex = (hash % dbCount) + 1;
        }

        log.info("db-router result: {}", dbIndex);
        return dbIndex;
    }

    @Override
    public Integer doTableRouter(Object tableKeyValue, int tableCount) {
        if (tableKeyValue == null) {
            return null;
        }
        //扰动函数值
        int hash = hash(tableKeyValue);
        //表索引
        int tableIndex;
        if (isPowerOfTwo(tableCount)) {
            tableIndex = (hash & (tableCount - 1)) + 1;
        } else {
            tableIndex = (hash % (tableCount)) + 1;
            log.warn("The tableCount [{}] is not 2 to the nth power,and data hashing may be uneven.", tableCount);
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