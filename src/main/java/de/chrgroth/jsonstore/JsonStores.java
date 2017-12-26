package de.chrgroth.jsonstore;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.chrgroth.jsonstore.json.JsonService;
import de.chrgroth.jsonstore.storage.StorageService;
import de.chrgroth.jsonstore.store.AbstractJsonStore;
import de.chrgroth.jsonstore.store.JsonSingletonStore;
import de.chrgroth.jsonstore.store.JsonStore;
import de.chrgroth.jsonstore.store.JsonStoreUtils;
import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import de.chrgroth.jsonstore.store.exception.JsonStoreException;

/**
 * Central API class to create JSON stores. Stores are maintained per class using {@link #resolve(Class, String)},
 * {@link #ensure(Class, String, Integer, VersionMigrationHandler...)} and {@link #drop(Class, String)} and similar methods for singleton stores. Each JSON
 * store will create a separate file. The {@link #save()} method acts as shortcut to save all stores. If an instance is created with auto save mode enabled (see
 * {@link #builder(JsonService)}) then {@link #ensure(Class, String, Integer, VersionMigrationHandler...)} and
 * {@link #ensureSingleton(Class, String, Integer, VersionMigrationHandler...)} will automatically load possibly existing data from configured storage path.
 *
 * @author Christian Groth
 */
public final class JsonStores {

    // TODO ensure services not null or handle null safely

    private final JsonService jsonService;
    private final StorageService storageService;

    private final Map<String, JsonStore<?>> stores;
    private final Map<String, JsonSingletonStore<?>> singletonStores;
    private final boolean autoSave;

    /**
     * Builder class to control creation of {@link JsonStores}.
     *
     * @author Christian Groth
     */
    public static final class JsonStoresBuilder {

        private final JsonService jsonService;
        private final StorageService storageService;

        private boolean autoSave;

        private JsonStoresBuilder(JsonService jsonService, StorageService storageService) {
            this.jsonService = jsonService;
            this.storageService = storageService;
        }

        public JsonStoresBuilder autoSave(boolean autoSave) {
            this.autoSave = autoSave;
            return this;
        }

        /**
         * Creates the {@link JsonStores} instance.
         *
         * @return stores instance
         */
        public JsonStores build() {
            return new JsonStores(jsonService, storageService, autoSave);
        }
    }

    /**
     * Creates a new builder instance.
     *
     * @param jsonService
     *            JSON service to be used
     * @param storageService
     *            storage service to be used
     * @return stores builder
     */
    public static JsonStoresBuilder builder(JsonService jsonService, StorageService storageService) {
        return new JsonStoresBuilder(jsonService, storageService);
    }

    private JsonStores(JsonService jsonService, StorageService storageService, boolean autoSave) {

        // init state
        this.jsonService = jsonService;
        this.storageService = storageService;
        stores = new HashMap<>();
        singletonStores = new HashMap<>();
        this.autoSave = autoSave;

        // prepare storage if not exists
        if (isPersistent()) {
            storageService.prepare();
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
        String uid = JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier);

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
                throw new JsonStoreException("Unable to delegate data load for " + store.getUid() + "!!", e);
            }
        }

        // done
        return store;
    }

    private void create(String uid, Class<?> payloadClass, Integer payloadClassVersion, VersionMigrationHandler... versionMigrationHandlers) {
        stores.put(uid, new JsonStore<>(jsonService, storageService, uid, payloadClass, payloadClassVersion, autoSave, versionMigrationHandlers));
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
        return resolve(JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier));
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
        String uid = JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier);

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
                throw new JsonStoreException("Unable to delegate data load for " + store.getUid() + "!!", e);
            }
        }

        // done
        return store;
    }

    private void createSingleton(String uid, Class<?> payloadClass, Integer payloadClassVersion, VersionMigrationHandler... versionMigrationHandlers) {
        singletonStores.put(uid, new JsonSingletonStore<>(jsonService, storageService, uid, payloadClass, payloadClassVersion, autoSave, versionMigrationHandlers));
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
        return resolveSingleton(JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier));
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
        return drop(JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier));
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
        return dropSingleton(JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier));
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
                throw new JsonStoreException("Unable to delegate data load for " + entry.getKey() + "!!", e);
            }
        });
        singletonStores.entrySet().parallelStream().forEach(entry -> {
            try {
                entry.getValue().load();
            } catch (Exception e) {
                throw new JsonStoreException("Unable to delegate data load for " + entry.getKey() + "!!", e);
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
        return storageService != null;
    }
}
