package com.jiangzhiyan.middleware.db.router;

import com.jiangzhiyan.middleware.db.router.annotation.DBRouter;
import com.jiangzhiyan.middleware.db.router.annotation.tag.NullClazz;
import com.jiangzhiyan.middleware.db.router.strategy.IRouterStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * DBRouter注解切面处理类
 */
@Aspect
@Order(1)
@RequiredArgsConstructor
public class DBRouterAspect {

    private final IRouterStrategy routerStrategy;
    private final RouterConfig routerConfig;

    @Pointcut("@annotation(com.jiangzhiyan.middleware.db.router.annotation.DBRouter)")
    public void methodWithDBRouter() {
    }

    @Pointcut("within(@com.jiangzhiyan.middleware.db.router.annotation.DBRouter *)")
    public void classWithDBRouter() {
    }

    @Around("methodWithDBRouter() || classWithDBRouter()")
    public Object doRouter(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Method method = this.getMethod(pjp);
            //获取方法上的注解,如果存在
            DBRouter dbRouterAnnotation = AnnotationUtils.findAnnotation(method, DBRouter.class);
            //获取类或接口上的注解,如果存在(优先方法上的注解)
            if (dbRouterAnnotation == null) {
                Class<?> declaringClass = method.getDeclaringClass();
                dbRouterAnnotation = declaringClass.getAnnotation(DBRouter.class);
                if (dbRouterAnnotation == null) {
                    for (Class<?> declaringInterface : declaringClass.getInterfaces()) {
                        dbRouterAnnotation = declaringInterface.getAnnotation(DBRouter.class);
                    }
                    if (dbRouterAnnotation == null) {
                        while (declaringClass != null && declaringClass != Object.class && dbRouterAnnotation == null) {
                            declaringClass = declaringClass.getSuperclass();
                            dbRouterAnnotation = declaringClass.getAnnotation(DBRouter.class);
                        }
                    }
                }
            }

            if (dbRouterAnnotation != null) {
                int dbCount = dbRouterAnnotation.dbCount() < 1 ? this.routerConfig.getDbCount() : dbRouterAnnotation.dbCount();
                //计算路由属性值
                routerStrategy.dbRouter(getAttrValue(dbRouterAnnotation, method, pjp.getArgs()), dbCount);
            }
            return pjp.proceed();
        } finally {
            routerStrategy.clear();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    private Object getAttrValue(DBRouter dbRouter, Method method, Object[] args) {
        if (args.length == 0) {
            return null;
        }
        String dbRouterKey = dbRouter.key() == null || dbRouter.key().isEmpty() ? this.routerConfig.getDefaultDBRouterKey() : dbRouter.key();
        Class<?> dbRouterKeyClass = dbRouter.keyClass() == NullClazz.class ? this.routerConfig.getDefaultDBRouterKeyClass() : dbRouter.keyClass();
        if (dbRouterKey == null || dbRouterKey.isEmpty()) {
            throw new RuntimeException("dbRouter key can not be blank.");
        }
        if (dbRouterKeyClass == null || dbRouterKeyClass == NullClazz.class) {
            throw new RuntimeException("dbRouter keyClass can not be null.");
        }

        Parameter[] parameters = method.getParameters();
        if (args.length == 1 && dbRouterKeyClass.isAssignableFrom(parameters[0].getType())) {
            return args[0];
        }

        try {
            for (Object arg : args) {
                Object attrValue = BeanUtils.getProperty(arg, dbRouterKey);
                if (attrValue != null) {
                    return attrValue;
                }
            }
        } catch (Exception ignored) {

        }

        throw new RuntimeException("can not find db router key: [" + dbRouterKey + "] in method's [" + method.getName() + "] parameters.");
    }
}
