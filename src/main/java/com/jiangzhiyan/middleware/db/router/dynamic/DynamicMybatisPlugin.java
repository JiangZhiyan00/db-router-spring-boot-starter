package com.jiangzhiyan.middleware.db.router.dynamic;

import com.jiangzhiyan.middleware.db.router.strategy.IRouterStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {
    private static final Pattern PATTERN = Pattern.compile("(from|into|update)\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    private final IRouterStrategy routerStrategy;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Integer tableKeyIndex = routerStrategy.getTableIndex();
        if (tableKeyIndex == null) {
            return invocation.proceed();
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        Matcher matcher = PATTERN.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }

        assert tableName != null;

        String replaceSql = matcher.replaceAll(tableName + "_" + routerStrategy.getTableIndex());
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }
}