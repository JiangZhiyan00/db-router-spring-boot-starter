package com.jiangzhiyan.middleware.db.router.strategy;

import com.jiangzhiyan.middleware.db.router.DBRouterConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
public abstract class AbstractDbRouterStrategy implements IDBRouterStrategy {

    protected final DBRouterConfig dbRouterConfig;

    @Override
    public void doRouter(Object dbKeyValue) {
        //获取路由结果
        RouterResult routerResult = this.getRouterResult(dbKeyValue);
        //存入上下文
        DBContextHolder.setRouterResult(routerResult);
    }

    @Override
    public Integer getDbKeyIndex() {
        return DBContextHolder.getDBKeyIndex();
    }

    @Override
    public Integer getTableKeyIndex() {
        return DBContextHolder.getTableKeyIndex();
    }

    @Override
    public String getDefaultDb() {
        return this.dbRouterConfig.getDefaultDb();
    }

    /**
     * 清除上下文
     */
    @Override
    public void clear() {
        DBContextHolder.clear();
    }

    /**
     * 获取路由结果
     *
     * @param dbKeyValue 分库分表字段值
     */
    protected abstract RouterResult getRouterResult(Object dbKeyValue);

    /**
     * 路由结果对象
     */
    @Getter
    @AllArgsConstructor
    public static class RouterResult {
        private final int dbKeyIndex;
        private final int tableKeyIndex;
    }

    /**
     * 数据库上下文
     */
    @UtilityClass
    static class DBContextHolder {
        private static final ThreadLocal<RouterResult> routerResultThreadLocal = new ThreadLocal<>();

        public static void setRouterResult(RouterResult routerResult) {
            routerResultThreadLocal.set(routerResult);
        }

        public static Integer getDBKeyIndex() {
            RouterResult routerResult = routerResultThreadLocal.get();
            return routerResult == null ? null : routerResult.getDbKeyIndex();
        }

        public static Integer getTableKeyIndex() {
            RouterResult routerResult = routerResultThreadLocal.get();
            return routerResult == null ? null : routerResult.getTableKeyIndex();
        }

        public static void clear() {
            routerResultThreadLocal.remove();
        }
    }
}
