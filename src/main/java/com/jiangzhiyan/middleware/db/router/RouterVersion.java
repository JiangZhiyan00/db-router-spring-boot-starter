package com.jiangzhiyan.middleware.db.router;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RouterVersion {

    /**
     * 版本号
     *
     * @return 版本号
     */
    public static String getVersion() {
        return RouterVersion.class.getPackage().getImplementationVersion();
    }
}
