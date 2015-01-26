package de.herrlock.log;

import java.util.HashMap;
import java.util.Map;

public class Logger {
    static final int NONE = 1;
    static final int ERROR = NONE * 10;
    static final int WARNING = ERROR * 10;
    static final int INFO = WARNING * 10;
    static final int DEBUG = INFO * 10;
    static final int TRACE = DEBUG * 10;

    private static final Map<Integer, Logger> cache = new HashMap<>();
    static {
        cache.put(NONE, new Logger(NONE));
        cache.put(ERROR, new Logger(ERROR));
        cache.put(WARNING, new Logger(WARNING));
        cache.put(INFO, new Logger(INFO));
        cache.put(ERROR, new Logger(ERROR));
        cache.put(DEBUG, new Logger(DEBUG));
    }

    public static Logger getLogger() {
        return cache.get(NONE);
    }

    public static Logger getLogger(String _level) {
        int level;
        switch ((_level != null && !_level.isEmpty() ? _level : "E").charAt(0)) {
            case 'n':
            case 'N':
                level = NONE;
                break;
            case 'e':
            case 'E':
                level = ERROR;
                break;
            case 'w':
            case 'W':
                level = WARNING;
                break;
            case 'i':
            case 'I':
                level = INFO;
                break;
            case 'd':
            case 'D':
                level = DEBUG;
                break;
            case 't':
            case 'T':
                level = TRACE;
                break;
            default:
                level = ERROR;
                break;
        }
        return cache.get(level);
    }

    private int level;

    private Logger(int _level) {
        this.level = _level;
    }

    /**
     * logs with level {@link Logger#NONE}<br>
     * these are essential messages
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger none(Object message) {
        log(message, NONE);
        return this;
    }

    /**
     * logs with level {@link Logger#ERROR}<br>
     * there are messages that indicate a thrown error
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger error(Object message) {
        log(">>>>> " + message, ERROR);
        return this;
    }

    /**
     * logs with level {@link Logger#WARNING}<br>
     * these are messages that indicate a caught error
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger warn(Object message) {
        log(">>>> " + message, WARNING);
        return this;
    }

    /**
     * logs with level {@link Logger#INFO}<br>
     * these are not important messages that are 'nice to read'
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger info(Object message) {
        log(">>> " + message, INFO);
        return this;
    }

    /**
     * logs with level {@link Logger#DEBUG}<br>
     * these are messages for debugging that do not have the purpose of tracing
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger debug(Object message) {
        log(">> " + message, DEBUG);
        return this;
    }

    /**
     * logs with level {@link Logger#TRACE}<br>
     * these are for tracing the flow of methods
     * 
     * @param message
     *            the message to log
     * @return {@code this}, to allow methodChaining
     */
    public Logger trace(Object message) {
        log("> " + message, TRACE);
        return this;
    }

    /**
     * traces the method-name that invoked this method
     * 
     * @return {@code this}, to allow methodChaining
     */
    public Logger trace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            log(stackTrace[2].getClassName() + '.' + stackTrace[2].getMethodName(), TRACE);
        }
        return this;
    }

    private void log(Object message, int l) {
        if (this.level >= l) {
            System.out.println(message);
        }
    }

}