package de.chrgroth.jsonstore.store;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import de.chrgroth.jsonstore.store.exception.JsonStoreException;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.JSONTokener;
import flexjson.JsonNumber;
import flexjson.ObjectBinder;

/**
 * Represents a JSON store for a concrete class. Access is provided using delegate methods to Java built in stream API. You may use flexjson annotations to
 * control conversion from/to JSON.
 *
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 * @param <P>
 *            concrete type structure used for storage of instances of type T
 */
public abstract class AbstractJsonStore<T, P> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonStore.class);

    public static final String FILE_SEPARATOR = ".";
    public static final String FILE_PREFIX = "storage";
    public static final String FILE_SUFFIX = "json";

    private static final String JSON_FIELD_CLASS = "class";
    private static final String JSON_FIELD_PAYLOAD = "payload";
    private static final String JSON_FIELD_SINGLETON = "singleton";
    private static final String JSON_FIELD_PAYLOAD_TYPE_VERSION = "payloadTypeVersion";

    protected final FlexjsonHelper flexjsonHelper;
    protected JsonStoreMetadata<T, P> metadata;
    protected final File file;
    protected final Charset charset;
    protected final boolean prettyPrint;
    protected final boolean autoSave;
    protected final boolean deepSerialize;
    protected final Map<Integer, VersionMigrationHandler> migrationHandlers;

    protected AbstractJsonStore(String uid, Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, FlexjsonHelper flexjsonHelper, File storage, Charset charset,
            boolean prettyPrint, boolean autoSave, boolean deepSerialize, VersionMigrationHandler... migrationHandlers) {
        this(uid, payloadClass, payloadTypeVersion, singleton, flexjsonHelper, storage, charset, "", prettyPrint, autoSave, deepSerialize, migrationHandlers);
    }

    protected AbstractJsonStore(String uid, Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, FlexjsonHelper flexjsonHelper, File storage, Charset charset,
            String fileNameExtraPrefix, boolean prettyPrint, boolean autoSave, boolean deepSerialize, VersionMigrationHandler... migrationHandlers) {
        this.flexjsonHelper = flexjsonHelper;
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(uid);
        metadata.setPayloadType(payloadClass.getName());
        metadata.setPayloadTypeVersion(payloadTypeVersion);
        metadata.setSingleton(singleton);
        metadata.setCreated(new Date());
        this.file = storage != null ? new File(storage, FILE_PREFIX + FILE_SEPARATOR + fileNameExtraPrefix + uid + FILE_SEPARATOR + FILE_SUFFIX) : null;
        this.charset = charset;
        this.prettyPrint = prettyPrint;
        this.autoSave = autoSave;
        this.deepSerialize = deepSerialize;
        this.migrationHandlers = new HashMap<>();
        if (migrationHandlers != null) {
            for (VersionMigrationHandler migrationHandler : migrationHandlers) {
                this.migrationHandlers.put(migrationHandler.sourceVersion(), migrationHandler);
            }
        }
    }

    /**
     * Returns the number of contained items.
     *
     * @return number of items
     */
    public abstract long size();

    /**
     * Computes current metrics for this instance.
     *
     * @return metrics, never null
     */
    public JsonStoreMetrics computeMetrics() {

        // compute file size
        long fileSize = 0;
        if (file != null && file.exists()) {
            fileSize = FileUtils.sizeOf(file);
        }

        return new JsonStoreMetrics(metadata.getUid(), metadata.getPayloadType(), size(), metadata.getModified(), fileSize);
    }

    /**
     * Returns the stores uid.
     *
     * @return uid
     */
    public String getUid() {
        return metadata.getUid();
    }

    /**
     * Returns file used for storage.
     *
     * @return file, may be null
     */
    public final File getFile() {
        return file;
    }

    /**
     * Checks if store is persistent.
     *
     * @return true if store is persistent, false otherwise
     */
    public final boolean isPersistent() {
        return file != null;
    }

    /**
     * Saves all data contained in store to configured file. No action if store is not persistent.
     */
    public final void save() {

        // abort on transient stores
        if (!isPersistent()) {
            return;
        }

        // create JSON
        String json = toJson(prettyPrint);

        // update metadata
        metadata.setModified(new Date());

        // write to file
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

    /**
     * Returns store elements in JSON format.
     *
     * @return JSON data
     */
    public final String toJson() {
        return toJson(prettyPrint);
    }

    /**
     * Creates a copy of stored data in JSON format with given pretty-print mode.
     *
     * @param prettyPrint
     *            pretty-print mode
     * @return JSON data
     */
    public final String toJson(boolean prettyPrint) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {

            // get serializer
            final JSONSerializer serializer = flexjsonHelper.serializer(prettyPrint);

            // create json data
            return deepSerialize ? serializer.deepSerialize(metadata) : serializer.serialize(metadata);
        } finally {
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": converting to json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }
    }

    /**
     * Loads store elements from configure file. If any error occurs a {@link JsonStoreException} will be thrown cause otherwise data loss may occur on next
     * successful save.
     */
    public final void load() {

        // abort on transient stores
        if (!isPersistent()) {
            return;
        }

        // abort if not data file is present
        if (file == null || !file.exists()) {
            return;
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

        // recreate data
        fromJsonInternal(json, false);
    }

    /**
     * Creates store elements from given JSON data and replaces all store contents.Will invoke {@link #save()} if using auto-save mode. If any error occurs a
     * {@link JsonStoreException} will be thrown cause otherwise data loss may occur on next successful save.
     *
     * @param json
     *            JSON data
     */
    public final void fromJson(String json) {
        fromJsonInternal(json, true);
    }

    @SuppressWarnings("unchecked")
    private void fromJsonInternal(String json, boolean forceStore) {

        // null guard
        if (json == null || "".equals(json.trim())) {
            return;
        }

        // deserialize to raw generic structure
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object genericStructureRaw = new JSONTokener(json).nextValue();
        stopwatch.stop();
        LOG.info(metadata.getUid() + ": raw parsing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        if (genericStructureRaw == null) {
            return;
        }

        // determine current data situation
        boolean isMap = genericStructureRaw instanceof Map;
        boolean isMetadataAvailable = isMap && JsonStoreMetadata.class.getName().equals(((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_CLASS));
        boolean isSingleton = isMetadataAvailable ? (boolean) ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_SINGLETON) : isMap;

        // determine generic payload
        Object genericStructurePayload = isMetadataAvailable ? ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_PAYLOAD) : genericStructureRaw;
        if (genericStructurePayload == null) {
            return;
        }

        // determine version information
        Object topLevelTypeVersionRaw = isMetadataAvailable ? ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_PAYLOAD_TYPE_VERSION) : null;
        Integer topLevelTypeVersion = topLevelTypeVersionRaw != null ? ((JsonNumber) topLevelTypeVersionRaw).toInteger() : 0;
        Integer payloadTypeVersion = metadata.getPayloadTypeVersion();

        // migrate payload data
        boolean migrated = migrateVersions(isSingleton, topLevelTypeVersion, payloadTypeVersion, genericStructurePayload);

        // process deserialization to payload object instances
        jsonDeserialization(genericStructureRaw, isMetadataAvailable, genericStructurePayload);

        // save
        if (autoSave && (forceStore || migrated)) {
            save();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean migrateVersions(boolean isSingleton, Integer topLevelTypeVersion, Integer payloadTypeVersion, Object genericStructurePayload) {

        // compare version information
        if (topLevelTypeVersion == null || payloadTypeVersion == null) {
            return false;
        }

        // abort on newer version than available as code
        if (topLevelTypeVersion > payloadTypeVersion) {
            throw new JsonStoreException("loaded version is newer than specified version in code: " + topLevelTypeVersion + " > " + payloadTypeVersion + "!!");
        }

        // run all available version migrators
        boolean migrated = false;
        if (topLevelTypeVersion < payloadTypeVersion) {

            // update per version
            for (int i = topLevelTypeVersion; i <= payloadTypeVersion; i++) {

                // check for migration handler
                VersionMigrationHandler migrationHandler = migrationHandlers.get(i);
                if (migrationHandler == null) {
                    continue;
                }

                // invoke handler per instance, so you don't have to deal with wrapping outer list by yourself
                LOG.info(metadata.getUid() + ": migrating to version " + i + " using " + migrationHandler);
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    if (isSingleton) {
                        migrationHandler.migrate((Map<String, Object>) genericStructurePayload);
                    } else {
                        for (Object genericStructurePayloadItem : (List<Object>) genericStructurePayload) {
                            migrationHandler.migrate((Map<String, Object>) genericStructurePayloadItem);
                        }
                    }
                    stopwatch.stop();
                    migrated = true;
                    LOG.info(metadata.getUid() + ": migrating to version " + i + " took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                } catch (Exception e) {
                    throw new JsonStoreException("failed to migrate " + metadata.getUid() + " from version " + i + " to " + (i + 1) + ": " + e.getMessage() + "!!", e);
                }
            }
        }

        // done
        return migrated;
    }

    @SuppressWarnings("unchecked")
    private void jsonDeserialization(Object genericStructureRaw, boolean isMetadataAvailable, Object genericStructurePayload) {

        // proceed with deserialization
        try {

            // TODO for the moment this is a bad hack to get the binder instance!!
            Stopwatch stopwatch = Stopwatch.createStarted();
            JSONDeserializer<?> deserializer = flexjsonHelper.deserializer();
            Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
            method.setAccessible(true);
            ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);

            if (isMetadataAvailable) {

                // full metadata deserialization
                JsonStoreMetadata<T, P> oldMetadata = (JsonStoreMetadata<T, P>) binder.bind(genericStructureRaw);
                metadata.setPayload(oldMetadata.getPayload());
            } else {

                // proceed payload deserialization
                metadata.setPayload((P) binder.bind(genericStructurePayload));

                // update metadata timestamps
                Date now = new Date();
                metadata.setCreated(now);
                metadata.setModified(now);
            }
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": deserializing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

            // metadata refresh callback
            metadataRefreshed();
        } catch (Exception e) {
            throw new JsonStoreException("Unable to restore from JSON content: " + (file != null ? file.getAbsolutePath() : "") + "!!", e);
        }
    }

    /**
     * Gets called after metadata was refreshed on loading new JSON data.
     */
    protected abstract void metadataRefreshed();

    /**
     * Drops store file explicitly. Transient data in store remains unchanged.
     */
    public final void drop() {
        if (isPersistent()) {
            try {
                LOG.info(metadata.getUid() + ": dropping strage file");
                synchronized (file) {
                    Files.deleteIfExists(file.toPath());
                }
            } catch (IOException e) {
                LOG.error("Unable to delete persistent JSON store: " + file.getAbsolutePath() + "!!", e);
            }
        }
    }
}
