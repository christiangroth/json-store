package de.chrgroth.jsonstore.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.chrgroth.jsonstore.JsonStoreException;
import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.StorageService;

/**
 * Implementation storing each store in a separate file using metadata uid.
 *
 * @author Christian Groth
 */
public class FileStorageService implements StorageService {
    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    public static final String FILE_SEPARATOR = ".";
    public static final String FILE_PREFIX = "storage";
    public static final String FILE_SUFFIX = "json";

    private static final String FILE_SINGLETON = "singleton";

    private final File storage;
    private final Charset charset;

    /**
     * Builder to configure a new instance of {@link FileStorageService}. Be sure to set the base path calling {@link #storage(File)}.
     *
     * @author Christian Groth
     */
    public static class FileStorageServiceBuilder {
        private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

        private File storage;
        private Charset charset = DEFAULT_CHARSET;

        private FileStorageServiceBuilder() {

        }

        /**
         * Sets the given storage directory as base path for all files.
         *
         * @param storage
         *            base path for all files
         * @return builder
         */
        public FileStorageServiceBuilder storage(File storage) {
            this.storage = storage;
            return this;
        }

        /**
         * Sets the given charset to be used.
         *
         * @param charset
         *            charset to be used
         * @return builder
         */
        public FileStorageServiceBuilder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Creates the service instance.
         *
         * @return create service
         */
        public FileStorageService build() {
            return new FileStorageService(storage != null ? storage.getAbsoluteFile() : null, charset);
        }
    }

    /**
     * Creates a new builder instance.
     *
     * @return builder
     */
    public static FileStorageServiceBuilder builder() {
        return new FileStorageServiceBuilder();
    }

    protected FileStorageService(File storage, Charset charset) {

        // storage base path
        if (storage == null) {
            throw new JsonStoreException("storage base path must not be null!!");
        }
        this.storage = storage;

        // storage charset
        if (charset == null) {
            throw new JsonStoreException("storage charset must not be null!!");
        }
        this.charset = charset;
    }

    @Override
    public void prepare() {

        // prepare storage if not exists
        if (!Files.exists(storage.toPath())) {
            try {
                LOG.info("creating storage path " + storage.getAbsolutePath());
                Files.createDirectories(storage.toPath());
            } catch (IOException e) {
                LOG.error("Unable to initialize storage path: " + storage.getAbsolutePath() + "!!", e);
            }
        }
    }

    @Override
    public long size(JsonStoreMetadata<?> metadata) {

        long fileSize = 0;
        File file = resolveFile(metadata);
        if (file != null && file.exists()) {
            fileSize = FileUtils.sizeOf(file);
        }
        return fileSize;
    }

    @Override
    public void write(JsonStoreMetadata<?> metadata, String json) {

        // write to file
        File file = resolveFile(metadata);
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            synchronized (file) {
                Files.write(file.toPath(), Arrays.asList(json), charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": saving json to file took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        } catch (IOException e) {
            LOG.error("Unable to write file content, skipping file during store: " + file.getAbsolutePath() + "!!", e);
        }
    }

    @Override
    public String read(JsonStoreMetadata<?> metadata) {

        // abort if not data file is present
        File file = resolveFile(metadata);
        if (file == null || !file.exists()) {
            return null;
        }

        // load JSON
        String json = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            synchronized (file) {
                json = Files.lines(file.toPath(), charset).parallel().filter(line -> line != null && !"".equals(line.trim())).map(String::trim).collect(Collectors.joining());
            }
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": loading json from file took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        } catch (Exception e) {
            throw new JsonStoreException("Unable to read file content: " + file.getAbsolutePath() + "!!", e);
        }

        // done
        return json;
    }

    @Override
    public void delete(JsonStoreMetadata<?> metadata) {
        try {
            LOG.info(metadata.getUid() + ": dropping strage file");
            File file = resolveFile(metadata);
            synchronized (file) {
                Files.deleteIfExists(file.toPath());
            }
        } catch (IOException e) {
            LOG.error("Unable to delete persistent JSON store: " + metadata.getUid() + "!!", e);
        }
    }

    /**
     * Resolves the persistent file for given store metadata.
     *
     * @param metadata
     *            store metadata
     * @return storage file
     */
    public File resolveFile(JsonStoreMetadata<?> metadata) {
        return new File(storage, FILE_PREFIX + FILE_SEPARATOR + (metadata.isSingleton() ? FILE_SINGLETON + FILE_SEPARATOR : "") + metadata.getUid() + FILE_SEPARATOR + FILE_SUFFIX);
    }
}
