/*
 * Copyright 2015 ArtisTech, Inc.
 */
package org.tros.utils.logging;

/**
 *
 * @author matta
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class ShortenedNameFormatter extends Formatter {

    protected String FORMAT;
    protected Date DATE;
    private static final String DEFAULT_FORMAT = "[%1$tc] %4$s: %2$s - %5$s %6$s%n";

    public ShortenedNameFormatter() {
        this(DEFAULT_FORMAT);
    }

    public ShortenedNameFormatter(String defaultFormat) {
        DATE = new Date();
        String property = LogManager.getLogManager().getProperty(this.getClass().getName() + ".format");
        FORMAT = property == null ? defaultFormat : property;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder nameBuilder = new StringBuilder();

        String[] names = record.getLoggerName().split("\\.");
        for (int ii = 0; ii < names.length - 1; ii++) {
            nameBuilder.append(names[ii].charAt(0));
        }
        nameBuilder.append(".").append(names[names.length - 1]);

        DATE.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return String.format(FORMAT,
                DATE,
                source,
                nameBuilder.toString(),
                record.getLevel().getLocalizedName(),
                message,
                throwable);
    }
}
