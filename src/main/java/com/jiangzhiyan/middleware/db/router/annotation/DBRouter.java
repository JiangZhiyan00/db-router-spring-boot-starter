package com.jiangzhiyan.middleware.db.router.annotation;

import com.jiangzhiyan.middleware.db.router.annotation.tag.NullClazz;

import java.lang.annotation.*;

/**
 * 分库注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {
    String key() default "";

    int dbCount() default -1;

    Class<?> keyClass() default NullClazz.class;
}
