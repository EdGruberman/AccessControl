package edgruberman.bukkit.accesscontrol.util;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SynchronousPluginLogger extends Logger {

    public static void log(final Plugin plugin, final LogRecord record) {
        if (!plugin.getLogger().isLoggable(record.getLevel())) return;
        Bukkit.getScheduler().runTask(plugin, new LogWriter(plugin.getLogger(), record));
    }

    public static void log(final Plugin plugin, final Level level, final String message, final Object... parameters) {
        if (!plugin.getLogger().isLoggable(level)) return;

        final LogRecord record = new LogRecord(level, message);
        record.setParameters(parameters);

        SynchronousPluginLogger.log(plugin, record);
    }



    private final Plugin plugin;
    private String pattern;

    public SynchronousPluginLogger(final Plugin plugin) {
        super(plugin.getLogger().getName() + "-" + SynchronousPluginLogger.class.getName(), null);
        this.plugin = plugin;
        this.pattern = "{0}";
        this.addHandler(new PassThroughHandler());
        this.setLevel(plugin.getLogger().getLevel());
    }

    /** @param pattern MessageFormat pattern to format message with, 0 = original message */
    public SynchronousPluginLogger setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }



    public class PassThroughHandler extends Handler {

        @Override
        public void publish(final LogRecord record) {
            record.setMessage(MessageFormat.format(SynchronousPluginLogger.this.pattern, record.getMessage()));
            SynchronousPluginLogger.log(SynchronousPluginLogger.this.plugin, record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

    }



    private static class LogWriter implements Runnable {

        private final Logger logger;
        private final LogRecord record;

        LogWriter(final Logger logger, final LogRecord record) {
            this.logger = logger;
            this.record = record;
        }

        @Override
        public void run() {
            this.logger.log(this.record);
        }

    }

}
