package com.jiangzhiyan.middleware.db.router.config;

import com.jiangzhiyan.middleware.db.router.DBRouterAspect;
import com.jiangzhiyan.middleware.db.router.RouterConfig;
import com.jiangzhiyan.middleware.db.router.TableRouterAspect;
import com.jiangzhiyan.middleware.db.router.dynamic.DynamicDataSource;
import com.jiangzhiyan.middleware.db.router.dynamic.DynamicMybatisPlugin;
import com.jiangzhiyan.middleware.db.router.strategy.IRouterStrategy;
import com.jiangzhiyan.middleware.db.router.strategy.impl.HashCodeRouterStrategy;
import com.jiangzhiyan.middleware.db.router.util.PropertyUtil;
import com.jiangzhiyan.middleware.db.router.util.StringUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源自动配置
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {
    private static final String PREFIX = "db-router.jdbc.datasource.";
    private static final String DB_COUNT_KEY = PREFIX.concat("dbCount");
    private static final String TABLE_COUNT_KEY = PREFIX.concat("tableCount");
    private static final String ROUTER_DB_LIST_KEY = PREFIX.concat("routerDbList");
    private static final String DEFAULT_DB_ROUTER_KEY = PREFIX.concat("defaultDbRouterKey");
    private static final String DEFAULT_TABLE_ROUTER_KEY = PREFIX.concat("defaultTableRouterKey");
    private static final String DEFAULT_DB_ROUTER_KEY_CLASS_KEY = PREFIX.concat("defaultDbRouterKeyClass");
    private static final String DEFAULT_TABLE_ROUTER_KEY_CLASS_KEY = PREFIX.concat("defaultTableRouterKeyClass");
    private static final String DEFAULT_DB_KEY = PREFIX.concat("defaultDb");
    private static final String GLOBAL_PROPS_KEY = PREFIX.concat("global");
    private static final String TAG_POOL = "pool";

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;
    /**
     * 分库的数据源配置集合
     */
    private final Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认库名
     */
    private String defaultDb;
    /**
     * 默认分库字段名
     */
    private String defaultDBRouterKey;
    /**
     * 默认分库字段类型
     */
    private Class<?> defaultDBRouterKeyClass;
    /**
     * 默认分表字段名
     */
    private String defaultTableRouterKey;
    /**
     * 默认分表字段类型
     */
    private Class<?> defaultTableRouterKeyClass;
    /**
     * 分库数
     */
    private int dbCount;
    /**
     * 分表数
     */
    private int tableCount;

    @Bean
    public RouterConfig routerConfig() {
        return new RouterConfig(dbCount, tableCount, defaultDb, defaultDBRouterKey, defaultDBRouterKeyClass, defaultTableRouterKey, defaultTableRouterKeyClass);
    }

    @Bean
    public IRouterStrategy routerStrategy(RouterConfig routerConfig) {
        return new HashCodeRouterStrategy(routerConfig);
    }

    @Bean("db-router-aspect")
    @ConditionalOnMissingBean
    public DBRouterAspect dbRouterAspect(IRouterStrategy dbRouterStrategy) {
        return new DBRouterAspect(dbRouterStrategy);
    }

    @Bean("table-router-aspect")
    @ConditionalOnMissingBean
    public TableRouterAspect tableRouterAspect(IRouterStrategy dbRouterStrategy) {
        return new TableRouterAspect(dbRouterStrategy);
    }

    @Bean("dynamicDataSource")
    public DataSource dataSource(IRouterStrategy dbRouterStrategy) {
        Map<Object, Object> targetDataSources = new HashMap<>(this.dataSourceMap.size());
        for (Map.Entry<String, Map<String, Object>> entry : this.dataSourceMap.entrySet()) {
            DataSource dataSource = this.createDataSource(entry.getValue());
            targetDataSources.put(entry.getKey(), dataSource);
        }
        DynamicDataSource dynamicDataSource = new DynamicDataSource(dbRouterStrategy);
        dynamicDataSource.setDefaultTargetDataSource(this.createDataSource(this.defaultDataSourceConfig));
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }

    @Bean
    public Interceptor plugin(IRouterStrategy dbRouterStrategy) {
        return new DynamicMybatisPlugin(dbRouterStrategy);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    /**
     * 配置示例
     * db-router:
     * jdbc:
     * datasource:
     * dbCount: 2
     * tableCount: 4
     * defaultDb: db0
     * routerDbList: db1,db2
     * defaultDbRouterKey: userId
     * defaultDbRouterKeyClass: java.lang.String
     * defaultTableRouterKey: userId
     * defaultTableRouterKeyClass: java.lang.String
     * db0:
     * driver-class-name: com.mysql.cj.jdbc.Driver
     * url: jdbc:mysql://127.0.0.1:3306/big_market?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=true
     * username: root
     * password: 123456
     * type-class-name: com.zaxxer.hikari.HikariDataSource
     * pool:
     * pool-name: Retail_HikariCP
     * minimum-idle: 15 #最小空闲连接数量
     * idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
     * maximum-pool-size: 25 #连接池最大连接数，默认是10
     * auto-commit: true  #此属性控制从池返回的连接的默认自动提交行为,默认值：true
     * max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
     * connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
     * connection-test-query: SELECT 1
     * db1:
     * driver-class-name: com.mysql.cj.jdbc.Driver
     * url: jdbc:mysql://127.0.0.1:3306/big_market_01?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=true
     * username: root
     * password: 123456
     * type-class-name: com.zaxxer.hikari.HikariDataSource
     * pool:
     * pool-name: Retail_HikariCP
     * minimum-idle: 15 #最小空闲连接数量
     * idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
     * maximum-pool-size: 25 #连接池最大连接数，默认是10
     * auto-commit: true  #此属性控制从池返回的连接的默认自动提交行为,默认值：true
     * max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
     * connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
     * connection-test-query: SELECT 1
     * db2:
     * driver-class-name: com.mysql.cj.jdbc.Driver
     * url: jdbc:mysql://127.0.0.1:3306/big_market_02?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=true
     * username: root
     * password: 123456
     * type-class-name: com.zaxxer.hikari.HikariDataSource
     * pool:
     * pool-name: Retail_HikariCP
     * minimum-idle: 15 #最小空闲连接数量
     * idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
     * maximum-pool-size: 25 #连接池最大连接数，默认是10
     * auto-commit: true  #此属性控制从池返回的连接的默认自动提交行为,默认值：true
     * max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
     * connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
     * connection-test-query: SELECT 1
     *
     * @param environment 系统环境
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setEnvironment(Environment environment) {
        String dbCountStr = environment.getRequiredProperty(DB_COUNT_KEY);
        String tableCountStr = environment.getRequiredProperty(TABLE_COUNT_KEY);
        String dbListStr = environment.getRequiredProperty(ROUTER_DB_LIST_KEY);
        String defaultDbRouterKeyClassStr = environment.getProperty(DEFAULT_DB_ROUTER_KEY_CLASS_KEY);
        String defaultTableRouterKeyClassStr = environment.getProperty(DEFAULT_TABLE_ROUTER_KEY_CLASS_KEY);

        try {
            this.defaultDBRouterKeyClass = Class.forName(defaultDbRouterKeyClassStr);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("can not find default db router key class by class name", e);
        }
        try {
            this.defaultTableRouterKeyClass = Class.forName(defaultTableRouterKeyClassStr);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("can not find default table router key class by class name", e);
        }

        this.defaultDb = environment.getRequiredProperty(DEFAULT_DB_KEY);
        this.defaultDBRouterKey = environment.getProperty(DEFAULT_DB_ROUTER_KEY);
        this.defaultTableRouterKey = environment.getProperty(DEFAULT_TABLE_ROUTER_KEY);

        int dbCount = parseIntOrThrow(dbCountStr, DB_COUNT_KEY);
        int tableCount = parseIntOrThrow(tableCountStr, TABLE_COUNT_KEY);
        if (dbCount <= 0) {
            throw new IllegalArgumentException("dbCount must be > 0,but now is " + dbCount);
        }
        if (tableCount <= 0) {
            throw new IllegalArgumentException("tableCount must be > 0,but now is " + tableCount);
        }
        this.dbCount = dbCount;
        this.tableCount = tableCount;

        //全局配置
        Map<String, Object> globalInfo = this.getGlobalProps(environment);

        for (String dbInfo : dbListStr.split(",")) {
            if (dbInfo != null && !dbInfo.isEmpty()) {
                Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, PREFIX.concat(dbInfo), Map.class);
                //将全局配置注入到每个数据源的配置中
                this.injectGlobal(dataSourceProps, globalInfo);
                dataSourceMap.put(dbInfo, dataSourceProps);
            }
        }

        //默认数据源
        this.defaultDataSourceConfig = (Map<String, Object>) PropertyUtil.handle(environment, PREFIX.concat(defaultDb), Map.class);
        this.injectGlobal(this.defaultDataSourceConfig, globalInfo);
    }

    @SuppressWarnings("unchecked")
    private DataSource createDataSource(Map<String, Object> attributes) {
        try {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl(attributes.get("url").toString());
            dataSourceProperties.setUsername(attributes.get("username").toString());
            dataSourceProperties.setPassword(attributes.get("password").toString());
            //驱动
            String driverClassName = attributes.getOrDefault("driver-class-name", "com.mysql.cj.jdbc.Driver").toString();
            dataSourceProperties.setDriverClassName(driverClassName);
            //连接池类型
            String typeClassName = attributes.getOrDefault("type-class-name", "com.zaxxer.hikari.HikariDataSource").toString();
            Class<?> clazz = Class.forName(typeClassName);
            Class<? extends DataSource> dataSourceClass = DataSource.class.isAssignableFrom(clazz) ? clazz.asSubclass(DataSource.class) : HikariDataSource.class;
            DataSource ds = dataSourceProperties.initializeDataSourceBuilder().type(dataSourceClass).build();
            //连接池配置信息
            MetaObject dsMeta = SystemMetaObject.forObject(ds);
            Map<String, Object> poolProps = (Map<String, Object>) attributes.getOrDefault(TAG_POOL, Collections.emptyMap());
            for (Map.Entry<String, Object> entry : poolProps.entrySet()) {
                String key = StringUtil.middleScoreToCamelCase(entry.getKey());
                if (dsMeta.hasSetter(key)) {
                    dsMeta.setValue(key, entry.getValue());
                }
            }
            return ds;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("can not find datasource type class by class name", e);
        }
    }

    /**
     * 获取全局配置
     *
     * @param environment 系统环境
     * @return 全局配置信息Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getGlobalProps(Environment environment) {
        try {
            return environment.containsProperty(GLOBAL_PROPS_KEY) ? PropertyUtil.handle(environment, GLOBAL_PROPS_KEY, Map.class) : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 将global配置中的属性注入到下级配置中
     *
     * @param origin 下级配置
     * @param global global配置
     */
    @SuppressWarnings("unchecked")
    private void injectGlobal(Map<String, Object> origin, Map<String, Object> global) {
        for (Map.Entry<String, Object> entry : global.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (origin.containsKey(key)) {
                if (origin.get(key) instanceof Map && value instanceof Map) {
                    this.injectGlobal((Map<String, Object>) origin.get(key), (Map<String, Object>) value);
                }
            } else {
                origin.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 解析字符串为数字
     *
     * @param numStr      字符串
     * @param propertyKey 配置key
     * @return 解析后的数字
     */
    private static int parseIntOrThrow(String numStr, String propertyKey) {
        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid format for property '" + propertyKey + "': expected an integer but found '" + numStr + "'", e);
        }
    }
}
