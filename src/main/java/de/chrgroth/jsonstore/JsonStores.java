package de.chrgroth.jsonstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import de.chrgroth.jsonstore.json.AbstractFlexjsonTypeHandler;
import de.chrgroth.jsonstore.json.FlexjsonHelper;
import de.chrgroth.jsonstore.json.FlexjsonHelper.FlexjsonHelperBuilder;
import de.chrgroth.jsonstore.json.custom.StringInterningHandler;
import de.chrgroth.jsonstore.store.AbstractJsonStore;
import de.chrgroth.jsonstore.store.JsonSingletonStore;
import de.chrgroth.jsonstore.store.JsonStore;
import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import de.chrgroth.jsonstore.store.exception.JsonStoreException;

/**
 * Central API class to create JSON stores. Stores are maintained per class using {@link #resolve(Class, String)},
 * {@link #ensure(Class, String, Integer, VersionMigrationHandler...)} and {@link #drop(Class, String)} and similar methods for singleton stores. Each JSON
 * store will create a separate file. The {@link #save()} method acts as shortcut to save all stores. If an instance is created with auto save mode enabled (see
 * {@link #builder()}) then {@link #ensure(Class, String, Integer, VersionMigrationHandler...)} and
 * {@link #ensureSingleton(Class, String, Integer, VersionMigrationHandler...)} will automatically load possibly existing data from configured storage path.
 *
 * @author Christian Groth
 */
public final class JsonStores {

    private static final Logger LOG = LoggerFactory.getLogger(JsonStores.class);

    private final FlexjsonHelper flexjsonHelper;
    private final Map<String, FlexjsonHelper> flexjsonHelperPerStore;
    private final Map<String, JsonStore<?>> stores;
    private final Map<String, JsonSingletonStore<?>> singletonStores;
    private final File storage;
    private final Charset charset;
    private final boolean prettyPrint;
    private final boolean autoSave;
    private final boolean deepSerialize;

    private static String buildStoreUid(Class<?> payloadClass, String optionalQualifier) {
        return payloadClass.getName() + (Strings.isNullOrEmpty(optionalQualifier) ? "" : "." + optionalQualifier);
    }

    /**
     * Builder class to control creation of {@link JsonStores}.
     *
     * @author Christian Groth
     */
    public static class JsonStoresBuilder {
        private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

        private File storage;
        private Charset charset;
        private boolean prettyPrint;
        private boolean autoSave;
        private boolean deepSerialize;

        private final FlexjsonHelperBuilder flexjsonHelperBuilder;
        private final Map<String, FlexjsonHelperBuilder> flexjsonHelperBuilderPerStore;

        public JsonStoresBuilder() {
            flexjsonHelperBuilder = FlexjsonHelper.builder();
            flexjsonHelperBuilderPerStore = new HashMap<>();
        }

        /**
         * {@link FlexjsonHelperBuilder#dateTimePattern(String)}
         *
         * @param dateTimePattern
         *            date time pattern to be used.
         * @return builder
         */
        public JsonStoresBuilder dateTimePattern(String dateTimePattern) {
            flexjsonHelperBuilder.dateTimePattern(dateTimePattern);
            return this;
        }

        /**
         * {@link FlexjsonHelperBuilder#dateTimePattern(String)}
         *
         * @param payloadClass
         *            use for store matching
         * @param optionalQualifier
         *            use for store matching
         * @param dateTimePattern
         *            date time pattern to be used.
         * @return builder
         */
        public JsonStoresBuilder dateTimePattern(Class<?> payloadClass, String optionalQualifier, String dateTimePattern) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).dateTimePattern(dateTimePattern);
            return this;
        }

        /**
         * Adds {@link StringInterningHandler} as custom handler for {@link String} class.
         *
         * @return builder
         */
        public JsonStoresBuilder useStringInterning() {
            flexjsonHelperBuilder.useStringInterning();
            return this;
        }

        /**
         * Adds {@link StringInterningHandler} as custom handler for {@link String} class.
         *
         * @param payloadClass
         *            use for store matching
         * @param optionalQualifier
         *            use for store matching
         * @return builder
         */
        public JsonStoresBuilder useStringInterning(Class<?> payloadClass, String optionalQualifier) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).useStringInterning();
            return this;
        }

        /**
         * {@link FlexjsonHelperBuilder#handler(Class, AbstractFlexjsonTypeHandler)}
         *
         * @param type
         *            type the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public JsonStoresBuilder factory(Class<?> type, AbstractFlexjsonTypeHandler handler) {
            flexjsonHelperBuilder.handler(type, handler);
            return this;
        }

        /**
         * {@link FlexjsonHelperBuilder#handler(Class, AbstractFlexjsonTypeHandler)}
         *
         * @param payloadClass
         *            use for store matching
         * @param optionalQualifier
         *            use for store matching
         * @param type
         *            type the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public JsonStoresBuilder factory(Class<?> payloadClass, String optionalQualifier, Class<?> type, AbstractFlexjsonTypeHandler handler) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).handler(type, handler);
            return this;
        }

        /**
         * {@link FlexjsonHelperBuilder#handler(String, AbstractFlexjsonTypeHandler)}
         *
         * @param path
         *            path the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public JsonStoresBuilder factory(String path, AbstractFlexjsonTypeHandler handler) {
            flexjsonHelperBuilder.handler(path, handler);
            return this;
        }

        /**
         * {@link FlexjsonHelperBuilder#handler(String, AbstractFlexjsonTypeHandler)}
         *
         * @param payloadClass
         *            use for store matching
         * @param optionalQualifier
         *            use for store matching
         * @param path
         *            path the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public JsonStoresBuilder factory(Class<?> payloadClass, String optionalQualifier, String path, AbstractFlexjsonTypeHandler handler) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).handler(path, handler);
            return this;
        }

        private FlexjsonHelperBuilder ensureFlexjsonHelperBuilderPerStore(Class<?> payloadClass, String optionalQualifier) {

            // create
            final String uid = buildStoreUid(payloadClass, optionalQualifier);
            if (!flexjsonHelperBuilderPerStore.containsKey(uid)) {
                flexjsonHelperBuilderPerStore.put(uid, FlexjsonHelper.builder());
            }

            // done
            return flexjsonHelperBuilderPerStore.get(uid);
        }

        /**
         * Configures persistent JSON storage with given base directory, default UTF-8 charset, pretty-print mode and deep serialization disabled and auto-save
         * mode enabled.
         *
         * @param storage
         *            base storage directory
         * @return builder
         */
        public JsonStoresBuilder storage(File storage) {
            this.storage = storage;
            charset = DEFAULT_CHARSET;
            prettyPrint = false;
            autoSave = true;
            deepSerialize = false;
            return this;
        }

        /**
         * Configures persistent JSON storage with given base directory, charset, pretty-print mode and auto-save mode.
         *
         * @param storage
         *            base storage directory
         * @param charset
         *            storage charset
         * @param prettyPrint
         *            pretty-print mode
         * @param autoSave
         *            auto-save mode
         * @param deepSerialize
         *            deep serialization mode
         * @return builder
         */
        public JsonStoresBuilder storage(File storage, Charset charset, boolean prettyPrint, boolean autoSave, boolean deepSerialize) {
            this.storage = storage;
            this.charset = charset;
            this.prettyPrint = prettyPrint;
            this.autoSave = autoSave;
            this.deepSerialize = deepSerialize;
            return this;
        }

        /**
         * Creates the {@link JsonStores} instance.
         *
         * @return stores instance
         */
        public JsonStores build() {

            // create builders per store
            Map<String, FlexjsonHelper> flexjsonHelperPerStore = new HashMap<>();
            for (Entry<String, FlexjsonHelperBuilder> entry : flexjsonHelperBuilderPerStore.entrySet()) {
                flexjsonHelperPerStore.put(entry.getKey(), entry.getValue().build());
            }

            // create json stores
            return new JsonStores(flexjsonHelperBuilder.build(), flexjsonHelperPerStore, storage, charset, prettyPrint, autoSave, deepSerialize);
        }
    }

    /**
     * Creates a new builder instance.
     *
     * @return stores builder
     */
    public static JsonStoresBuilder builder() {
        return new JsonStoresBuilder();
    }

    private JsonStores(FlexjsonHelper flexjsonHelper, Map<String, FlexjsonHelper> flexjsonHelperPerStore, File storage, Charset charset, boolean prettyPrint, boolean autoSave,
            boolean deepSerialize) {

        // init state
        this.flexjsonHelper = flexjsonHelper;
        this.flexjsonHelperPerStore = new HashMap<>();
        if (flexjsonHelperPerStore != null) {
            this.flexjsonHelperPerStore.putAll(flexjsonHelperPerStore);
        }
        stores = new HashMap<>();
        singletonStores = new HashMap<>();
        this.storage = storage == null ? null : storage.getAbsoluteFile();
        this.charset = charset;
        this.prettyPrint = prettyPrint;
        this.autoSave = autoSave;
        this.deepSerialize = deepSerialize;

        // prepare storage if not exists
        if (isPersistent() && !Files.exists(storage.toPath())) {
            try {
                LOG.info("creating storage path " + storage.getAbsolutePath());
                Files.createDirectories(storage.toPath());
            } catch (IOException e) {
                LOG.error("Unable to initialize storage path: " + storage.getAbsolutePath() + "!!", e);
            }
        }
    }

    /**
     * Computes current metrics for all contained store instance.
     *
     * @return metrics, never null
     */
    public JsonStoresMetrics computeMetrics() {
        Stream<AbstractJsonStore<?, ? extends Object>> allStores = Stream.concat(stores.values().stream(), singletonStores.values().stream());
        return new JsonStoresMetrics(allStores.map(s -> s.computeMetrics()).collect(Collectors.toList()));
    }

    /**
     * Ensures existence of JSON store for given class. If auto save mode is enabled store will automatically load possibly existing data from configured
     * storage path. If any error occurs during load of eisting data a {@link JsonStoreException} will be thrown cause otherwise data loss may occur on next
     * successful save.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @param payloadClassVersion
     *            version of payload class, next version is always supposed to be increased by one
     * @param versionMigrationHandlers
     *            all migration handlers
     * @return existing or created JSON store
     * @param <T>
     *            concrete type of data
     * @see VersionMigrationHandler
     */
    public <T> JsonStore<T> ensure(Class<T> payloadClass, String optionalQualifier, Integer payloadClassVersion, VersionMigrationHandler... versionMigrationHandlers) {

        // build uid
        String uid = buildStoreUid(payloadClass, optionalQualifier);

        // ensure store
        boolean initialDataLod = false;
        if (!stores.containsKey(uid)) {
            initialDataLod = true;
            create(uid, payloadClass, payloadClassVersion, versionMigrationHandlers);
        }

        // load data
        JsonStore<T> store = resolve(uid);
        if (isPersistent() && initialDataLod && autoSave) {
            try {
                store.load();
            } catch (Exception e) {
                throw new JsonStoreException("Unable to delegate data load for " + payloadClass + ": " + store.getFile().getAbsolutePath() + "!!", e);
            }
        }

        // done
        return store;
    }

    private void create(String uid, Class<?> payloadClass, Integer payloadClassVersion, VersionMigrationHandler... versionMigrationHandlers) {
        stores.put(uid, new JsonStore<>(uid, payloadClass, payloadClassVersion, resolveFlexjsonHelper(uid), storage, charset, prettyPrint, autoSave, deepSerialize,
                versionMigrationHandlers));
    }

    /**
     * Resolves JSON store for given payload class and qualifier.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @return existing JSON store, may be null
     * @param <T>
     *            concrete type of data
     */
    public <T> JsonStore<T> resolve(Class<?> payloadClass, String optionalQualifier) {
        return resolve(buildStoreUid(payloadClass, optionalQualifier));
    }

    /**
     * Resolves JSON store for given uid.
     *
     * @param uid
     *            store uid
     * @return existing JSON store, may be null
     * @param <T>
     *            concrete type of data
     */
    @SuppressWarnings("unchecked")
    public <T> JsonStore<T> resolve(String uid) {
        return (JsonStore<T>) stores.get(uid);
    }

    /**
     * Ensures existence of JSON singleton store for given class. If auto save mode is enabled store will automatically load possibly existing data from
     * configured storage path. If any error occurs during load of eisting data a {@link JsonStoreException} will be thrown cause otherwise data loss may occur
     * on next successful save.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @param payloadClassVersion
     *            version of payload class, next version is always supposed to be increased by one
     * @param versionMigrationHandlers
     *            all migration handlers
     * @return existing or created JSON singleton store
     * @param <T>
     *            concrete type of data
     * @see VersionMigrationHandler
     */
    public <T> JsonSingletonStore<T> ensureSingleton(Class<T> payloadClass, String optionalQualifier, Integer payloadClassVersion,
            VersionMigrationHandler... versionMigrationHandlers) {

        // build uid
        String uid = buildStoreUid(payloadClass, optionalQualifier);

        // ensure store
        if (!singletonStores.containsKey(uid)) {
            createSingleton(uid, payloadClass, payloadClassVersion, versionMigrationHandlers);
        }

        // load data
        JsonSingletonStore<T> store = resolveSingleton(uid);
        if (isPersistent() && autoSave) {
            try {
                store.load();
            } catch (Exception e) {
                throw new JsonStoreException("Unable to delegate data load for " + payloadClass + ": " + store.getFile().getAbsolutePath() + "!!", e);
            }
        }

        // done
        return store;
    }

    private void createSingleton(String uid, Class<?> payloadClass, Integer payloadClassVersion, VersionMigrationHandler... versionMigrationHandlers) {
        singletonStores.put(uid, new JsonSingletonStore<>(uid, payloadClass, payloadClassVersion, resolveFlexjsonHelper(uid), storage, charset, prettyPrint, autoSave,
                deepSerialize, versionMigrationHandlers));
    }

    public FlexjsonHelper resolveFlexjsonHelper(String uid) {
        return flexjsonHelperPerStore.getOrDefault(uid, flexjsonHelper);
    }

    /**
     * Resolves JSON singleton store for given class and qualifier.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @return existing JSON singleton store, may be null
     * @param <T>
     *            concrete type of data
     */
    public <T> JsonSingletonStore<T> resolveSingleton(Class<T> payloadClass, String optionalQualifier) {
        return resolveSingleton(buildStoreUid(payloadClass, optionalQualifier));
    }

    /**
     * Resolves JSON singleton store for given uid.
     *
     * @param uid
     *            store uid
     * @return existing JSON singleton store, may be null
     * @param <T>
     *            concrete type of data
     */
    @SuppressWarnings("unchecked")
    public <T> JsonSingletonStore<T> resolveSingleton(String uid) {
        return (JsonSingletonStore<T>) singletonStores.get(uid);
    }

    /**
     * Drops JSON store for given class and qualifier, is existent. Results in calling {@link JsonStore#drop()} if using auto-save mode and store exists.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @return dropped JSON store
     * @param <T>
     *            concrete type of data
     */
    public <T> JsonStore<T> drop(Class<T> payloadClass, String optionalQualifier) {
        return drop(buildStoreUid(payloadClass, optionalQualifier));
    }

    /**
     * Drops JSON store for given uid, is existent. Results in calling {@link JsonStore#drop()} if using auto-save mode and store exists.
     *
     * @param uid
     *            store uid
     * @return dropped JSON store
     * @param <T>
     *            concrete type of data
     */
    @SuppressWarnings("unchecked")
    public <T> JsonStore<T> drop(String uid) {

        // drop in memory
        JsonStore<T> store = (JsonStore<T>) stores.remove(uid);
        if (store != null && isPersistent()) {

            // remove file
            store.drop();
        }

        // done
        return store;
    }

    /**
     * Drops JSON singleton store for given class and qualifier, is existent. Results in calling {@link JsonSingletonStore#drop()} if using auto-save mode and
     * store exists.
     *
     * @param payloadClass
     *            class for JSON store
     * @param optionalQualifier
     *            optional payload class qualifier
     * @return dropped JSON singleton store
     * @param <T>
     *            concrete type of data
     */
    public <T> JsonSingletonStore<T> dropSingleton(Class<T> payloadClass, String optionalQualifier) {
        return dropSingleton(buildStoreUid(payloadClass, optionalQualifier));
    }

    /**
     * Drops JSON singleton store for given uid, is existent. Results in calling {@link JsonSingletonStore#drop()} if using auto-save mode and store exists.
     *
     * @param uid
     *            store uid
     * @return dropped JSON singleton store
     * @param <T>
     *            concrete type of data
     */
    @SuppressWarnings("unchecked")
    public <T> JsonSingletonStore<T> dropSingleton(String uid) {

        // drop in memory
        JsonSingletonStore<T> store = (JsonSingletonStore<T>) singletonStores.remove(uid);
        if (store != null && isPersistent()) {

            // remove file
            store.drop();
        }

        // done
        return store;
    }

    /**
     * If stores are persistent and auto save mode is disabled, this method invoked {@link JsonStore#load()} on all existing stores. In case auto save is
     * enabled stores are loaded automatically.
     */
    public void load() {

        // abort on transient stores or auto save mode
        if (!isPersistent() || autoSave) {
            return;
        }

        stores.entrySet().parallelStream().forEach(entry -> {
            try {
                entry.getValue().load();
            } catch (Exception e) {
                throw new JsonStoreException("Unable to delegate data load for " + entry.getKey() + ": " + entry.getValue().getFile().getAbsolutePath() + "!!", e);
            }
        });
        singletonStores.entrySet().parallelStream().forEach(entry -> {
            try {
                entry.getValue().load();
            } catch (Exception e) {
                throw new JsonStoreException("Unable to delegate data load for " + entry.getKey() + ": " + entry.getValue().getFile().getAbsolutePath() + "!!", e);
            }
        });
    }

    /**
     * If stores are persistent {@link JsonStore#save()} will be invoked using parallel stream on all existing stores.
     */
    public void save() {

        // abort on transient stores
        if (!isPersistent()) {
            return;
        }

        // delegate to all stores
        stores.values().parallelStream().forEach(store -> store.save());
        singletonStores.values().parallelStream().forEach(store -> store.save());
    }

    public boolean isPersistent() {
        return storage != null;
    }
}
