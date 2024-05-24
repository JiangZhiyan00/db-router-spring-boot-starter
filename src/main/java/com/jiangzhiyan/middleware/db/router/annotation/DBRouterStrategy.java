package com.jiangzhiyan.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * 分表注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {
    boolean splitTable() default false;
}