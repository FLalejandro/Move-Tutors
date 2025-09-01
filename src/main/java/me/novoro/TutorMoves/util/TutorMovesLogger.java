package me.novoro.TutorMoves.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TutorMoves' Logger. It's not recommended to use this externally.
 */
public class TutorMovesLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("TutorMoves");

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s) {
        TutorMovesLogger.LOGGER.info("{}{}", "[TutorMoves]: ", s);
    }

    /**
     * Sends a warn log to console.
     * @param s The string to log.
     */
    public static void warn(String s) {
        TutorMovesLogger.LOGGER.warn("{}{}", "[TutorMoves]: ", s);
    }

    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s) {
        TutorMovesLogger.LOGGER.error("{}{}", "[TutorMoves]: ", s);
    }

    /**
     * Prints a stacktrace using TutorMoves's Logger.
     * @param throwable The exception to print.
     */
    public static void printStackTrace(Throwable throwable) {
        TutorMovesLogger.error(throwable.toString());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace) TutorMovesLogger.error("\tat " + traceElement);
    }
}