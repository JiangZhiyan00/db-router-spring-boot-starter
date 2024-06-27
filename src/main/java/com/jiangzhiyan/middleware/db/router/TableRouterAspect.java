package com.jiangzhiyan.middleware.db.router;

import com.jiangzhiyan.middleware.db.router.annotation.TableRouter;
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

@Aspect
@Order(2)
@RequiredArgsConstructor
public class TableRouterAspect {
    private final IRouterStrategy routerStrategy;
    private final RouterConfig routerConfig;

    @Pointcut("@annotation(com.jiangzhiyan.middleware.db.router.annotation.TableRouter)")
    public void methodWithTableRouter() {
    }

    @Pointcut("within(@com.jiangzhiyan.middleware.db.router.annotation.TableRouter *)")
    public void classWithTableRouter() {
    }

    @Around("methodWithTableRouter() || classWithTableRouter()")
    public Object doRouter(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Method method = getMethod(pjp);
            //获取方法上的注解,如果存在
            TableRouter tableRouterAnnotation = AnnotationUtils.findAnnotation(method, TableRouter.class);
            //获取类或接口上的注解,如果存在(优先方法上的注解)
            if (tableRouterAnnotation == null) {
                Class<?> declaringClass = method.getDeclaringClass();
                tableRouterAnnotation = declaringClass.getAnnotation(TableRouter.class);
                if (tableRouterAnnotation == null) {
                    for (Class<?> declaringInterface : declaringClass.getInterfaces()) {
                        tableRouterAnnotation = declaringInterface.getAnnotation(TableRouter.class);
                    }
                    if (tableRouterAnnotation == null) {
                        while (declaringClass != null && declaringClass != Object.class && tableRouterAnnotation == null) {
                            declaringClass = declaringClass.getSuperclass();
                            tableRouterAnnotation = declaringClass.getAnnotation(TableRouter.class);
                        }
                    }
                }
            }
            if (tableRouterAnnotation != null) {
                int tableCount = tableRouterAnnotation.tableCount() < 1 ? this.routerConfig.getTableCount() : tableRouterAnnotation.tableCount();
                //计算路由属性值
                routerStrategy.tableRouter(getAttrValue(tableRouterAnnotation, method, pjp.getArgs()), tableCount);
            }

            return pjp.proceed();
        } finally {
            routerStrategy.clear();
        }
    }

    private static Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    private Object getAttrValue(TableRouter tableRouter, Method method, Object[] args) {
        if (args.length == 0 || tableRouter == null) {
            return null;
        }
        String tableRouterKey = tableRouter.key() == null || tableRouter.key().isEmpty() ? this.routerConfig.getDefaultTableRouterKey() : tableRouter.key();
        Class<?> tableRouterKeyClass = tableRouter.keyClass() == NullClazz.class ? this.routerConfig.getDefaultTableRouterKeyClass() : tableRouter.keyClass();
        if (tableRouterKey == null || tableRouterKey.isEmpty()) {
            throw new RuntimeException("tableRouter key can not be blank.");
        }
        if (tableRouterKeyClass == null || tableRouterKeyClass == NullClazz.class) {
            throw new RuntimeException("tableRouter keyClass can not be null.");
        }
        if (args.length == 1 && tableRouterKeyClass.isAssignableFrom(method.getParameters()[0].getType())) {
            return args[0];
        }

        try {
            for (Object arg : args) {
                Object attrValue = BeanUtils.getProperty(arg, tableRouterKey);
                if (attrValue != null) {
                    return attrValue;
                }
            }
        } catch (Exception ignored) {

        }

        throw new RuntimeException("can not find table router key: [" + tableRouterKey + "] in method's [" + method.getName() + "] parameters.");
    }
}
