package com.jiangzhiyan.middleware.db.router.util;

import lombok.experimental.UtilityClass;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 属性解析工具类
 */
@UtilityClass
public class PropertyUtil {

    private static final Resolver RESOLVER;

    static {
        String version = SpringApplication.class.getPackage().getImplementationVersion();
        if (version.startsWith("1.")) {
            RESOLVER = new SpringBoot1Resolver();
        } else if (version.startsWith("2.")) {
            RESOLVER = new SpringBoot2Resolver();
        } else if (version.startsWith("3.")) {
            RESOLVER = new SpringBoot3Resolver();
        } else {
            throw new IllegalStateException("Unsupported Spring Boot version: " + version);
        }
    }

    /**
     * Spring Boot 1.x is compatible with Spring Boot 2.x by Using Java Reflect.
     *
     * @param environment : the environment context
     * @param prefix      : the prefix part of property key
     * @param targetClass : the target class type of result
     * @param <T>         : refer to @param targetClass
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T handle(final Environment environment, final String prefix, final Class<T> targetClass) {
        return (T) RESOLVER.resolve(environment, prefix, targetClass);
    }


    private interface Resolver {
        Object resolve(Environment environment, String prefix, Class<?> targetClass);
    }

    private static class SpringBoot1Resolver implements Resolver {
        @Override
        public Object resolve(Environment environment, String prefix, Class<?> targetClass) {
            try {
                Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
                Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
                Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
                Object resolverObject = resolverConstructor.newInstance(environment);
                String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
                return getSubPropertiesMethod.invoke(resolverObject, prefixParam);
            } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                           | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }

    private static class SpringBoot2Resolver implements Resolver {
        @Override
        public Object resolve(Environment environment, String prefix, Class<?> targetClass) {
            try {
                Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
                Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
                Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
                Object binderObject = getMethod.invoke(null, environment);
                String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
                Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
                Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
                return resultGetMethod.invoke(bindResultObject);
            } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                           | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }

    private static class SpringBoot3Resolver implements Resolver {

        @Override
        public Object resolve(Environment environment, String prefix, Class<?> targetClass) {
            try {
                Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
                Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
                Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
                Object binderObject = getMethod.invoke(null, environment);
                String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
                Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
                Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
                return resultGetMethod.invoke(bindResultObject);
            } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                           | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }

}
