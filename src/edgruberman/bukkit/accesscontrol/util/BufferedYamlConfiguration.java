package edgruberman.bukkit.accesscontrol.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.io.Files;

/**
 * queues save requests to prevent occurring more than a maximum rate
 *
 * @author EdGruberman (ed@rjump.com)
 * @version 3.1.0
 */
public class BufferedYamlConfiguration extends YamlConfiguration implements Runnable {

    protected static final String NEWLINE_PLATFORM = System.getProperty("line.separator");
    protected static final Pattern NEWLINE_ANY = Pattern.compile("\\r?\\n");
    protected static final int TICKS_PER_SECOND = 20;

    protected final Plugin owner;
    protected File file;
    protected long rate;
    protected Logger logger;
    protected long lastSerialization = -1;
    protected int taskSave = -1;
    protected Object lock = new Object();
    protected String serialized = null;

    /** @param rate minimum time between saves (milliseconds) */
    public BufferedYamlConfiguration(final Plugin owner, final File file, final long rate) {
        this.owner = owner;
        this.file = file;
        this.rate = rate;
        this.logger = new SynchronousPluginLogger(owner);
    }

    public Plugin getOwner() {
        return this.owner;
    }

    public File getFile() {
        return this.file;
    }

    public long getRate() {
        return this.rate;
    }

    public void setRate(final int rate) {
        this.rate = rate;
        if (!this.isQueued()) return;
        Bukkit.getScheduler().cancelTask(this.taskSave);
        this.queueSave();
    }

    public long getLastSaveAttempt() {
        return this.lastSerialization;
    }

    public void clear() {
        this.map.clear();
    }

    public BufferedYamlConfiguration load() throws IOException, InvalidConfigurationException {
        try {
            super.load(this.file);
        } catch (final FileNotFoundException e) {
            this.loadFromString("");
        } catch (final IOException e) {
            throw e;
        } catch (final InvalidConfigurationException e) {
            throw e;
        }
        return this;
    }

    @Override
    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        this.file = file;
        this.load();
    }

    @Override
    public void save(final File file) throws IOException {
        this.file = file;
        super.save(file);
    }

    /** force immediate save */
    public boolean save() {
        this.lastSerialization = System.currentTimeMillis();
        try {
            super.save(this.file);

        } catch (final IOException e) {
            this.logger.log(Level.SEVERE, "Unable to save configuration file: {0}; {1}", new Object[] { this.file, e });
            return false;
        }

        this.logger.log(Level.FINEST, "Saved configuration file: {0}", this.file);
        return true;
    }

    public void queueSave() {
        synchronized (this.lock) {
            final boolean pending = (this.serialized != null);
            if (pending) {
                this.serialize();
                this.logger.log(Level.FINEST, "Reserialized current configuration for pending write operation for file: {0}", this.file);
                return;
            }
        }

        final long elapsed = System.currentTimeMillis() - this.lastSerialization;
        if (elapsed < this.rate) {
            final long delay = this.rate - elapsed;
            if (this.isQueued()) {
                this.logger.log(Level.FINEST
                        , "Save request already queued to run in {0,number,0.0} seconds for file: {1} (Last attempted {2,number,0.0} seconds ago)"
                        , new Object[] { delay / 1000D, this.getFile(), elapsed / 1000D });
                return;
            }

            // schedule task to flush cache to file system
            this.taskSave = Bukkit.getScheduler().scheduleSyncDelayedTask(this.owner, this, delay * BufferedYamlConfiguration.TICKS_PER_SECOND / 1000);
            this.logger.log(Level.FINEST
                    , "Queued save request to run in {0,number,0.0} seconds for configuration file: {1} (Last attempted {2,number,0.0} seconds ago)"
                    , new Object[] { delay / 1000D, this.getFile(), elapsed / 1000D });
            return;
        }

        this.run();
    }

    /** prepare for async write by serializing */
    @Override
    public void run() {
        // update serialized data and schedule if no async write is pending
        synchronized (this.lock) {
            final boolean pending = (this.serialized != null);
            this.serialize();
            if (!pending) Bukkit.getScheduler().runTaskAsynchronously(this.owner, new AsynchronousWriter());
        }

        this.taskSave = -1;
    }

    private void serialize() {
        this.lastSerialization = System.currentTimeMillis();
        this.serialized = BufferedYamlConfiguration.NEWLINE_ANY.matcher(this.saveToString()).replaceAll(BufferedYamlConfiguration.NEWLINE_PLATFORM);
    }

    public boolean isQueued() {
        return Bukkit.getScheduler().isQueued(this.taskSave);
    }

    public void cancelSave() {
        Bukkit.getScheduler().cancelTask(this.taskSave);
    }



    protected class AsynchronousWriter implements Runnable {

        @Override
        public synchronized void run() {
            final File file = BufferedYamlConfiguration.this.file;
            try {
                // ensure only a single async write occurs at a time
                synchronized (BufferedYamlConfiguration.this.lock) {
                    if (!file.getParentFile().exists()) Files.createParentDirs(file);

                    final Writer writer = new BufferedWriter(new FileWriter(file));
                    try {
                        writer.write(BufferedYamlConfiguration.this.serialized);

                    } finally {
                        try { writer.close(); } catch (final Throwable t) {}

                        // mark serialized data as flushed to file
                        BufferedYamlConfiguration.this.serialized = null;
                    }
                }

            } catch (final IOException e) {
                BufferedYamlConfiguration.this.logger.log(Level.SEVERE, "Unable to save configuration file: {0}; {1}", new Object[] { file, e });
                return;
            }

            BufferedYamlConfiguration.this.logger.log(Level.FINEST, "Saved configuration file: {0}", file);
        }

    }

}
