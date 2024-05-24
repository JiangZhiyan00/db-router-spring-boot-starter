package com.jiangzhiyan.middleware.db.router.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    /**
     * 横杠转驼峰 pool-name转为poolName
     *
     * @param input 字符串
     * @return 驼峰格式字符串
     */
    public static String middleScoreToCamelCase(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '-') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                result.append(Character.toUpperCase(currentChar));
                nextUpperCase = false;
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }
}