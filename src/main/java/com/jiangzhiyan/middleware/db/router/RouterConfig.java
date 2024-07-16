package com.jiangzhiyan.middleware.db.router;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分库分表配置
 */
@Getter
@AllArgsConstructor
public class RouterConfig {
    /**
     * 分库数
     */
    private int dbCount;
    /**
     * 分表数
     */
    private int tableCount;
    /**
     * 默认库名称
     */
    private String defaultDb;
    /**
     * 默认的分库字段名称
     */
    private String defaultDBRouterKey;
    /**
     * 默认的分库字段类型
     */
    private Class<?> defaultDBRouterKeyClass;
    /**
     * 默认的分表字段名称
     */
    private String defaultTableRouterKey;
    /**
     * 默认的分表字段类型
     */
    private Class<?> defaultTableRouterKeyClass;
}
