package com.jiangzhiyan.middleware.db.router.config;

import org.springframework.boot.ResourceBanner;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * banner图打印器
 */
public class BannerPrinter implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String BANNER_ENABLE = "db-router.banner.enabled";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment environment = event.getEnvironment();
        String bannerEnable = environment.getProperty(BANNER_ENABLE);

        //判断 bannerEnable 是否为空或者不为 "false"
        if (!"false".equalsIgnoreCase(bannerEnable)) {
            Resource resource = new ClassPathResource("banner.txt");
            ResourceBanner banner = new ResourceBanner(resource);
            banner.printBanner(environment, this.getClass(), System.out);
        }
    }
}