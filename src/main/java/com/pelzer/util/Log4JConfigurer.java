package com.pelzer.util;

import com.pelzer.util.Logging.Logger;
import com.pelzer.util.Logging.Priority;

public class Log4JConfigurer {

  /**
   * Called during Logging init, potentially resets log4j to follow the settings
   * configured by pelzer.util.
   */
   static void configureLog4j() {
    org.apache.log4j.LogManager.resetConfiguration();
    org.apache.log4j.Logger.getRootLogger().addAppender(new org.apache.log4j.Appender() {
      private String name;

      @Override
      public void setName(String name) {
        this.name = name;
      }

      @Override
      public void setLayout(org.apache.log4j.Layout layout) {
      }

      @Override
      public void setErrorHandler(org.apache.log4j.spi.ErrorHandler errorHandler) {
      }

      @Override
      public boolean requiresLayout() {
        return false;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public org.apache.log4j.Layout getLayout() {
        return null;
      }

      @Override
      public org.apache.log4j.spi.Filter getFilter() {
        return null;
      }

      @Override
      public org.apache.log4j.spi.ErrorHandler getErrorHandler() {
        return null;
      }

      @Override
      public void doAppend(org.apache.log4j.spi.LoggingEvent event) {
        Logger logger = Logging.getLogger(event.getLoggerName());
        logger.genericLog(event.getMessage() + "", event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable(), convertPriority(event.getLevel()));
      }

      private Priority convertPriority(org.apache.log4j.Level level) {
        switch (level.toInt()) {
          case org.apache.log4j.Level.ALL_INT:
            return Priority.ALL;
          case org.apache.log4j.Level.TRACE_INT:
            return Priority.VERBOSE;
          case org.apache.log4j.Level.DEBUG_INT:
            return Priority.DEBUG;
          case org.apache.log4j.Level.WARN_INT:
            return Priority.WARN;
          case org.apache.log4j.Level.INFO_INT:
            return Priority.INFO;
          case org.apache.log4j.Level.ERROR_INT:
            return Priority.ERROR;
          case org.apache.log4j.Level.FATAL_INT:
            return Priority.FATAL;
        }
        return Priority.OFF;
      }

      @Override
      public void close() {
      }

      @Override
      public void clearFilters() {
      }

      @Override
      public void addFilter(org.apache.log4j.spi.Filter newFilter) {
      }
    });
  }

}
