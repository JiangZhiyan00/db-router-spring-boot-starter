package com.jiangzhiyan.middleware.db.router;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分库分表配置
 */
@Getter
@AllArgsConstructor
public class DBRouterConfig {
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
     * 默认的分库分表字段名称
     */
    private String defaultRouterKey;
    /**
     * 默认的分库分表字段类型
     */
    private Class<?> defaultRouterKeyClass;
}
