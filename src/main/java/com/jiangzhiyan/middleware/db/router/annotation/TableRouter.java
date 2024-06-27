package com.jiangzhiyan.middleware.db.router.annotation;

import com.jiangzhiyan.middleware.db.router.annotation.tag.NullClazz;

import java.lang.annotation.*;

/**
 * 分表注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TableRouter {
    String key() default "";

    int tableCount() default -1;

    Class<?> keyClass() default NullClazz.class;
}