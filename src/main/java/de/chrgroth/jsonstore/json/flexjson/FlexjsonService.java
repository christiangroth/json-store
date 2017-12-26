package de.chrgroth.jsonstore.json.flexjson;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.chrgroth.jsonstore.json.JsonService;
import de.chrgroth.jsonstore.json.flexjson.FlexjsonHelper.FlexjsonHelperBuilder;
import de.chrgroth.jsonstore.json.flexjson.custom.StringInterningHandler;
import de.chrgroth.jsonstore.store.JsonStoreMetadata;
import de.chrgroth.jsonstore.store.JsonStoreUtils;
import de.chrgroth.jsonstore.store.VersionMigrationHandler;
import de.chrgroth.jsonstore.store.exception.JsonStoreException;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.JSONTokener;
import flexjson.JsonNumber;
import flexjson.ObjectBinder;

public final class FlexjsonService implements JsonService {
    private static final Logger LOG = LoggerFactory.getLogger(FlexjsonService.class);

    private static final String JSON_FIELD_CLASS = "class";
    private static final String JSON_FIELD_PAYLOAD = "payload";
    private static final String JSON_FIELD_SINGLETON = "singleton";
    private static final String JSON_FIELD_PAYLOAD_TYPE_VERSION = "payloadTypeVersion";

    private final FlexjsonHelper flexjsonHelper;
    private final Map<String, FlexjsonHelper> flexjsonHelperPerStore;

    private final boolean deepSerialize;
    private final boolean prettyPrint;

    public static class FlexjsonServiceBuilder {

        private final FlexjsonHelperBuilder flexjsonHelperBuilder;
        private final Map<String, FlexjsonHelperBuilder> flexjsonHelperBuilderPerStore;

        private boolean deepSerialize;
        private boolean prettyPrint;

        public FlexjsonServiceBuilder() {
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
        public FlexjsonServiceBuilder dateTimePattern(String dateTimePattern) {
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
        public FlexjsonServiceBuilder dateTimePattern(Class<?> payloadClass, String optionalQualifier, String dateTimePattern) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).dateTimePattern(dateTimePattern);
            return this;
        }

        /**
         * Adds {@link StringInterningHandler} as custom handler for {@link String} class.
         *
         * @return builder
         */
        public FlexjsonServiceBuilder useStringInterning() {
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
        public FlexjsonServiceBuilder useStringInterning(Class<?> payloadClass, String optionalQualifier) {
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
        public FlexjsonServiceBuilder factory(Class<?> type, AbstractFlexjsonTypeHandler handler) {
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
        public FlexjsonServiceBuilder factory(Class<?> payloadClass, String optionalQualifier, Class<?> type, AbstractFlexjsonTypeHandler handler) {
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
        public FlexjsonServiceBuilder factory(String path, AbstractFlexjsonTypeHandler handler) {
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
        public FlexjsonServiceBuilder factory(Class<?> payloadClass, String optionalQualifier, String path, AbstractFlexjsonTypeHandler handler) {
            ensureFlexjsonHelperBuilderPerStore(payloadClass, optionalQualifier).handler(path, handler);
            return this;
        }

        private FlexjsonHelperBuilder ensureFlexjsonHelperBuilderPerStore(Class<?> payloadClass, String optionalQualifier) {

            // create
            final String uid = JsonStoreUtils.buildStoreUid(payloadClass, optionalQualifier);
            if (!flexjsonHelperBuilderPerStore.containsKey(uid)) {
                flexjsonHelperBuilderPerStore.put(uid, FlexjsonHelper.builder());
            }

            // done
            return flexjsonHelperBuilderPerStore.get(uid);
        }

        public FlexjsonService build() {
            Map<String, FlexjsonHelper> flexjsonHelperPerStore = flexjsonHelperBuilderPerStore.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().build()));
            return new FlexjsonService(flexjsonHelperBuilder.build(), flexjsonHelperPerStore, deepSerialize, prettyPrint);
        }
    }

    public static FlexjsonServiceBuilder builder() {
        return new FlexjsonServiceBuilder();
    }

    private FlexjsonService(FlexjsonHelper flexjsonHelper, Map<String, FlexjsonHelper> flexjsonHelperPerStore, boolean deepSerialize, boolean prettyPrint) {
        this.flexjsonHelper = flexjsonHelper;
        this.flexjsonHelperPerStore = new HashMap<>();
        if (flexjsonHelperPerStore != null) {
            this.flexjsonHelperPerStore.putAll(flexjsonHelperPerStore);
        }
        this.deepSerialize = deepSerialize;
        this.prettyPrint = prettyPrint;
    }

    @Override
    public String toJson(JsonStoreMetadata<?, ?> metadata) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {

            // get serializer
            final JSONSerializer serializer = resolveFlexjsonHelper(metadata.getUid()).serializer(prettyPrint);

            // create json data
            return deepSerialize ? serializer.deepSerialize(metadata) : serializer.serialize(metadata);
        } finally {
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": converting to json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }
    }

    @Override
    public boolean fromJson(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json) {

        // null guard
        if (json == null || "".equals(json.trim())) {
            return false;
        }

        // deserialize to raw generic structure
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object genericStructureRaw = new JSONTokener(json).nextValue();
        stopwatch.stop();
        LOG.info(metadata.getUid() + ": raw parsing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        if (!(genericStructureRaw instanceof Map)) {
            return false;
        }

        // ensure metadata wrapper
        @SuppressWarnings("unchecked")
        Map<String, Object> metadataRaw = (Map<String, Object>) genericStructureRaw;
        boolean isMetadataAvailable = JsonStoreMetadata.class.getName().equals(metadataRaw.get(JSON_FIELD_CLASS));
        if (!isMetadataAvailable) {
            LOG.error(metadata.getUid() + ": json invalid, no metadata wrapper detected.");
            return false;
        }

        // migrate payload data
        boolean migrated = migrateVersions(metadata, migrationHandlers, metadataRaw);

        // process deserialization to payload object instances
        jsonDeserialization(metadata, metadataRaw);

        return migrated;
    }

    @SuppressWarnings("unchecked")
    private boolean migrateVersions(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, Map<String, Object> metadataRaw) {

        // determine payload
        Object rawPayload = metadataRaw.get(JSON_FIELD_PAYLOAD);
        if (rawPayload == null) {
            return false;
        }

        // determine source information
        boolean isSingleton = (boolean) metadataRaw.get(JSON_FIELD_SINGLETON);
        Object sourceTypeVersionRaw = metadataRaw.get(JSON_FIELD_PAYLOAD_TYPE_VERSION);
        Integer sourceTypeVersion = sourceTypeVersionRaw != null ? ((JsonNumber) sourceTypeVersionRaw).toInteger() : 0;

        // compare version information
        Integer targetTypeVersion = metadata.getPayloadTypeVersion();
        if (sourceTypeVersion == null || targetTypeVersion == null) {
            return false;
        }

        // abort on newer version than available as code
        if (sourceTypeVersion > targetTypeVersion) {
            throw new JsonStoreException("loaded version is newer than specified version in code: " + sourceTypeVersion + " > " + targetTypeVersion + "!!");
        }

        // run all available version migrators
        boolean migrated = false;
        if (sourceTypeVersion < targetTypeVersion) {

            // update per version
            for (int i = sourceTypeVersion; i <= targetTypeVersion; i++) {

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
                        migrationHandler.migrate((Map<String, Object>) rawPayload);
                    } else {
                        for (Object genericStructurePayloadItem : (List<Object>) rawPayload) {
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
    private <T, P> void jsonDeserialization(JsonStoreMetadata<T, P> metadata, Map<String, Object> metadataRaw) {

        // proceed with deserialization
        try {

            // TODO for the moment this is a bad hack to get the binder instance!!
            Stopwatch stopwatch = Stopwatch.createStarted();
            JSONDeserializer<?> deserializer = resolveFlexjsonHelper(metadata.getUid()).deserializer();
            Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
            method.setAccessible(true);
            ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);

            // metadata deserialization
            JsonStoreMetadata<T, P> oldMetadata = (JsonStoreMetadata<T, P>) binder.bind(metadataRaw);
            metadata.setPayload(oldMetadata.getPayload());

            stopwatch.stop();
            LOG.info(metadata.getUid() + ": deserializing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        } catch (Exception e) {
            throw new JsonStoreException("Unable to restore from JSON content: " + metadata.getUid() + "!!", e);
        }
    }

    private FlexjsonHelper resolveFlexjsonHelper(String uid) {
        return flexjsonHelperPerStore.getOrDefault(uid, flexjsonHelper);
    }
}
