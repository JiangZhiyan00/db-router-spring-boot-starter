package com.jiangzhiyan.middleware.db.router.strategy;

import com.jiangzhiyan.middleware.db.router.RouterConfig;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
public abstract class AbstractRouterStrategy implements IRouterStrategy {

    protected final RouterConfig routerConfig;

    @Override
    public void dbRouter(Object dbKeyValue) {
        //获取分库路由结果,并存入上下文
        DBContextHolder.setDbIndex(this.doDbRouter(dbKeyValue));
    }

    @Override
    public void tableRouter(Object tableKeyValue) {
        //获取分表路由结果,并存入上下文
        DBContextHolder.setTableIndex(this.doTableRouter(tableKeyValue));
    }

    @Override
    public Integer getDbIndex() {
        return DBContextHolder.getDBIndex();
    }

    @Override
    public Integer getTableIndex() {
        return DBContextHolder.getTableIndex();
    }

    @Override
    public String getDefaultDb() {
        return this.routerConfig.getDefaultDb();
    }

    /**
     * 清除上下文
     */
    @Override
    public void clear() {
        DBContextHolder.clearDBIndex();
        DBContextHolder.clearTableIndex();
    }

    /**
     * 获取分库路由结果
     *
     * @param dbKeyValue 分库字段值
     * @return 分库库表索引
     */
    protected abstract Integer doDbRouter(Object dbKeyValue);

    /**
     * 获取分表路由结果
     *
     * @param tableKeyValue 分表字段值
     * @return 分表库表索引
     */
    protected abstract Integer doTableRouter(Object tableKeyValue);

    /**
     * 数据库上下文
     */
    @UtilityClass
    protected static class DBContextHolder {
        private static final ThreadLocal<Integer> dbIndexThreadLocal = new ThreadLocal<>();
        private static final ThreadLocal<Integer> tableIndexThreadLocal = new ThreadLocal<>();

        public static void setDbIndex(Integer dbIndex) {
            if (dbIndex != null) {
                dbIndexThreadLocal.set(dbIndex);
            }
        }

        public static void setTableIndex(Integer tableIndex) {
            if (tableIndex != null) {
                tableIndexThreadLocal.set(tableIndex);
            }
        }

        public static Integer getDBIndex() {
            return dbIndexThreadLocal.get();
        }

        public static Integer getTableIndex() {
            return tableIndexThreadLocal.get();
        }

        public static void clearDBIndex() {
            dbIndexThreadLocal.remove();
        }

        public void clearTableIndex() {
            tableIndexThreadLocal.remove();
        }
    }
}
