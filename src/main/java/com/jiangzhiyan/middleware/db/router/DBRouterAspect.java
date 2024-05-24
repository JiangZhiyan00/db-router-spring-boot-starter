package com.jiangzhiyan.middleware.db.router;

import com.jiangzhiyan.middleware.db.router.annotation.DBRouter;
import com.jiangzhiyan.middleware.db.router.strategy.IDBRouterStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * DBRouter注解切面处理类
 */
@Aspect
@RequiredArgsConstructor
public class DBRouterAspect {

    private final IDBRouterStrategy dbRouterStrategy;
    private final DBRouterConfig dbRouterConfig;

    @Pointcut("@annotation(com.jiangzhiyan.middleware.db.router.annotation.DBRouter)")
    public void aopPoint() {
    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint pjp, DBRouter dbRouter) throws Throwable {
        String key = dbRouter.key() == null || dbRouter.key().isEmpty() ? this.dbRouterConfig.getDefaultRouterKey() : dbRouter.key();
        Class<?> keyClass = dbRouter.keyClass() == DBRouter.NullClass.class ? this.dbRouterConfig.getDefaultRouterKeyClass() : dbRouter.keyClass();
        //计算路由属性值
        Object dbKeyAttr = getAttrValue(key, keyClass, getMethod(pjp), pjp.getArgs());
        dbRouterStrategy.doRouter(dbKeyAttr);
        try {
            return pjp.proceed();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    public Object getAttrValue(String dbRouterKey, Class<?> dbRouterKeyClass, Method method, Object[] args) {
        if (args.length == 0) {
            return null;
        }
        if (dbRouterKey == null || dbRouterKey.isEmpty()) {
            throw new RuntimeException("dbRouter key can not be blank.");
        }
        if (dbRouterKeyClass == null || dbRouterKeyClass == DBRouter.NullClass.class) {
            throw new RuntimeException("dbRouter keyClass can not be null.");
        }
        Object attrValue = null;
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (attrValue != null) {
                return attrValue;
            }
            //先在方法参数中找
            if (dbRouterKey.equals(parameters[i].getName()) && dbRouterKeyClass.isAssignableFrom(parameters[i].getType())) {
                attrValue = args[i];
            } else {
                //再去参数的属性中找
                try {
                    attrValue = BeanUtils.getProperty(args[i], dbRouterKey);
                } catch (Exception ignored) {

                }
            }
        }
        throw new RuntimeException("can not find db router key: [" + dbRouterKey + "] in method's [" + method.getName() + "] parameters.");
    }
}
