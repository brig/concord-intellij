package brig.concord.log;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private final com.intellij.openapi.diagnostic.Logger delegate;

    public Logger(com.intellij.openapi.diagnostic.Logger delegate) {
        this.delegate = delegate;
    }

    public static void out(String format, Object... args) {
        String msg = formatMessage(LogLevel.INFO, format, args);
        System.out.println(msg);
    }

    public static Logger getInstance(Class<?> clazz) {
        return new Logger(com.intellij.openapi.diagnostic.Logger.getInstance(clazz));
    }

    private static String formatMessage(LogLevel level, String log, Object... args) {
        String timestamp = ZonedDateTime.now().format(DT_FORMAT);
        FormattingTuple m = MessageFormatter.arrayFormat(log, args);
        if (m.getThrowable() != null) {
            return String.format("%s [%-5s] %s%n%s%n", timestamp, level.name(), m.getMessage(), formatException(m.getThrowable()));
        }

        return String.format("%s [%-5s] %s%n", timestamp, level.name(), m.getMessage());
    }

    private static String formatException(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public void info(String format, Object... args) {
        String msg = formatMessage(LogLevel.INFO, format, args);
        delegate.info(msg);
    }

    public void warn(String format, Object... args) {
        String msg = formatMessage(LogLevel.WARN, format, args);
        delegate.warn(msg);
    }

    private enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}
