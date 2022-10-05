package dev.marcinromanowski.communicationservice.base

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import groovy.transform.CompileStatic
import org.slf4j.LoggerFactory

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Appender for Sl4j logs capture with thread safe collector.
 *
 * Base implementation source: https://stackoverflow.com/a/64428034
 */
@CompileStatic
class LogAppender {

    static ThreadSafeListAppender<ILoggingEvent> getListAppenderForClass(Class clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz)
        ThreadSafeListAppender<ILoggingEvent> loggingEventListAppender = new ThreadSafeListAppender<>()
        loggingEventListAppender.start()
        logger.addAppender(loggingEventListAppender)
        return loggingEventListAppender
    }

    static class ThreadSafeListAppender<T> extends AppenderBase<T> implements Closeable {

        List<T> eventsContainer = new CopyOnWriteArrayList<>()

        @Override
        protected void append(T event) {
            eventsContainer.add(event)
        }

        @Override
        void close() throws IOException {
            this.stop()
        }

    }

}
