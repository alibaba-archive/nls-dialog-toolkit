package com.alibaba.idst.nls.sdm.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.Charset;

/**
 * @author jianghaitao
 * @date 2018/11/12
 */
public class DialogLoggerUtil {
    private static final String LOG_PATH = "logs/";

    /**
     * 获取Logger
     * @param filename
     * @return
     */
    public static Logger createLogger(String filename) {
        final LoggerContext context = (LoggerContext)LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        Layout layout = PatternLayout.newBuilder()
            .withPattern(PatternLayout.DEFAULT_CONVERSION_PATTERN)
            .withConfiguration(config)
            .withCharset(Charset.forName("UTF-8"))
            .build();

        String fileName = LOG_PATH + filename + ".log";
        String filePattern = fileName + "-%d{yyyy-MM-dd}";
        Appender appender = RollingFileAppender.newBuilder()
            .setConfiguration(config)
            .withName("FILE_APPENDER")
            .withFileName(fileName)
            .withFilePattern(filePattern)
            .withPolicy(TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build())
            .withAppend(true)
            .withLocking(false)
            .withIgnoreExceptions(true)
            .withBufferedIo(true)
            .withLayout(layout)
            .build();
        appender.start();

        config.addAppender(appender);

        AppenderRef ref = AppenderRef.createAppenderRef("FILE_APPENDER", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, filename,
            "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);

        config.addLogger(filename, loggerConfig);
        context.updateLoggers();

        return context.getLogger(filename);
    }
}
