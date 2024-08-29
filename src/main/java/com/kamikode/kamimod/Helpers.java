package com.kamikode.kamimod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Helpers {
    private static final Logger LOGGER = LogManager.getLogger(Helpers.class);
    private static final String MOD_ID = "Kami Votes";
    static void logInfo(String message) {
        LOGGER.info("[{}] {}", MOD_ID, message);
    }

    static void logWarn(String message, String throwable) {
        LOGGER.warn("[{}] {}", MOD_ID, message, throwable);
    }
    static void logError(String message, Throwable throwable) {
        LOGGER.error("[{}] {}", MOD_ID, message, throwable);
    }
}
