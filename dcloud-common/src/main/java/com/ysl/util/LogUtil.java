package com.ysl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static void info(String tip, String content) {
        logger.info("{}{}", tip, content);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void error(String tip, String content) {
        logger.error("{}{}", tip, content);

    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}

