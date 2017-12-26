package de.chrgroth.jsonstore.json.flexjson;

import java.lang.reflect.Method;
import java.util.Date;
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

// TODO move some fromJson migration logic to abstract class?

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

    // TODO drop scenario of backward compatibility without metadata being used

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
        if (genericStructureRaw == null) {
            return false;
        }

        // determine current data situation
        boolean isMap = genericStructureRaw instanceof Map;
        @SuppressWarnings("unchecked")
        boolean isMetadataAvailable = isMap && JsonStoreMetadata.class.getName().equals(((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_CLASS));
        @SuppressWarnings("unchecked")
        boolean isSingleton = isMetadataAvailable ? (boolean) ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_SINGLETON) : isMap;

        // determine generic payload
        @SuppressWarnings("unchecked")
        Object genericStructurePayload = isMetadataAvailable ? ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_PAYLOAD) : genericStructureRaw;
        if (genericStructurePayload == null) {
            return false;
        }

        // determine version information
        @SuppressWarnings("unchecked")
        Object topLevelTypeVersionRaw = isMetadataAvailable ? ((Map<String, Object>) genericStructureRaw).get(JSON_FIELD_PAYLOAD_TYPE_VERSION) : null;
        Integer topLevelTypeVersion = topLevelTypeVersionRaw != null ? ((JsonNumber) topLevelTypeVersionRaw).toInteger() : 0;
        Integer payloadTypeVersion = metadata.getPayloadTypeVersion();

        // migrate payload data
        boolean migrated = migrateVersions(metadata, migrationHandlers, isSingleton, topLevelTypeVersion, payloadTypeVersion, genericStructurePayload);

        // process deserialization to payload object instances
        jsonDeserialization(metadata, genericStructureRaw, isMetadataAvailable, genericStructurePayload);

        return migrated;
    }

    @SuppressWarnings("unchecked")
    private boolean migrateVersions(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, boolean isSingleton, Integer topLevelTypeVersion,
            Integer payloadTypeVersion, Object genericStructurePayload) {

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
    private <T, P> void jsonDeserialization(JsonStoreMetadata<T, P> metadata, Object genericStructureRaw, boolean isMetadataAvailable, Object genericStructurePayload) {

        // proceed with deserialization
        try {

            // TODO for the moment this is a bad hack to get the binder instance!!
            Stopwatch stopwatch = Stopwatch.createStarted();
            JSONDeserializer<?> deserializer = resolveFlexjsonHelper(metadata.getUid()).deserializer();
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
        } catch (Exception e) {
            throw new JsonStoreException("Unable to restore from JSON content: " + metadata.getUid() + "!!", e);
        }
    }

    private FlexjsonHelper resolveFlexjsonHelper(String uid) {
        return flexjsonHelperPerStore.getOrDefault(uid, flexjsonHelper);
    }
}
