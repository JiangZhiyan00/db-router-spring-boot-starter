package com.jiangzhiyan.middleware.db.router.annotation;

import lombok.experimental.UtilityClass;

import java.lang.annotation.*;

/**
 * 动态数据库路由注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {
    String key() default "";

    Class<?> keyClass() default NullClass.class;

    @UtilityClass
    class NullClass {
        // 这个类用来表示keyClass的默认值，即null
    }
}
