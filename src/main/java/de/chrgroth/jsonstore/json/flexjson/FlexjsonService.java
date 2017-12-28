package de.chrgroth.jsonstore.json.flexjson;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.chrgroth.jsonstore.JsonStoreException;
import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.JsonStoreUtils;
import de.chrgroth.jsonstore.VersionMigrationHandler;
import de.chrgroth.jsonstore.json.AbstractJsonService;
import de.chrgroth.jsonstore.json.flexjson.FlexjsonHelper.FlexjsonHelperBuilder;
import de.chrgroth.jsonstore.json.flexjson.custom.AbstractFlexjsonTypeHandler;
import de.chrgroth.jsonstore.json.flexjson.custom.StringInterningHandler;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.JSONTokener;
import flexjson.JsonNumber;
import flexjson.ObjectBinder;

/**
 * Service to handle JSON serialization and deserialization using Flexjson library.
 *
 * @author Christian Groth
 */
public class FlexjsonService extends AbstractJsonService {
    private static final Logger LOG = LoggerFactory.getLogger(FlexjsonService.class);

    private static final String JSON_FIELD_CLASS = "class";
    private static final String JSON_FIELD_PAYLOAD = "payload";
    private static final String JSON_FIELD_SINGLETON = "singleton";
    private static final String JSON_FIELD_PAYLOAD_TYPE_VERSION = "payloadTypeVersion";

    private final FlexjsonHelper flexjsonHelper;
    private final Map<String, FlexjsonHelper> flexjsonHelperPerStore;

    private final boolean deepSerialize;
    private final boolean prettyPrint;

    /**
     * Builder to configure a new instance of {@link FlexjsonService}.
     *
     * @author Christian Groth
     */
    public static class FlexjsonServiceBuilder {

        private final FlexjsonHelperBuilder flexjsonHelperBuilder;
        private final Map<String, FlexjsonHelperBuilder> flexjsonHelperBuilderPerStore;

        private boolean deepSerialize;
        private boolean prettyPrint;

        private FlexjsonServiceBuilder() {
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

        /**
         * Sets the deep serialize mode.
         *
         * @param deepSerialize
         *            true for deep serialization, false to use explicit annotations
         * @return builder
         */
        public FlexjsonServiceBuilder setDeepSerialize(boolean deepSerialize) {
            this.deepSerialize = deepSerialize;
            return this;
        }

        /**
         * Sets the pretty print serialization mode.
         *
         * @param prettyPrint
         *            true for pretty print mode, false otherwise
         * @return builder
         */
        public FlexjsonServiceBuilder setPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        /**
         * Creates the service instance.
         *
         * @return created service
         */
        public FlexjsonService build() {
            Map<String, FlexjsonHelper> flexjsonHelperPerStore = flexjsonHelperBuilderPerStore.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().build()));
            return new FlexjsonService(flexjsonHelperBuilder.build(), flexjsonHelperPerStore, deepSerialize, prettyPrint);
        }
    }

    /**
     * Creates a new builder instance.
     *
     * @return builder
     */
    public static FlexjsonServiceBuilder builder() {
        return new FlexjsonServiceBuilder();
    }

    protected FlexjsonService(FlexjsonHelper flexjsonHelper, Map<String, FlexjsonHelper> flexjsonHelperPerStore, boolean deepSerialize, boolean prettyPrint) {
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
            final JSONSerializer serializer = resolveFlexjsonHelper(metadata.getUid()).serializer(prettyPrint);
            return deepSerialize ? serializer.deepSerialize(metadata) : serializer.serialize(metadata);
        } finally {
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": converting to json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }
    }

    @Override
    public void fromJson(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json, Consumer<Boolean> successConsumer) {

        // null guard
        if (json == null || "".equals(json.trim())) {
            return;
        }

        // deserialize to raw generic structure
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object genericStructureRaw = new JSONTokener(json).nextValue();
        stopwatch.stop();
        LOG.info(metadata.getUid() + ": raw parsing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        if (!(genericStructureRaw instanceof Map)) {
            return;
        }

        // ensure metadata wrapper
        @SuppressWarnings("unchecked")
        Map<String, Object> oldMetadataRaw = (Map<String, Object>) genericStructureRaw;
        boolean isOfMetadataType = JsonStoreMetadata.class.getName().equals(oldMetadataRaw.get(JSON_FIELD_CLASS));
        if (!isOfMetadataType) {
            LOG.error(metadata.getUid() + ": json invalid, no/invalid metadata wrapper detected.");
            return;
        }

        // migrate payload data
        boolean migrated = migrateVersions(metadata, migrationHandlers, oldMetadataRaw);

        // process deserialization to payload object instances
        jsonDeserialization(metadata, oldMetadataRaw);

        // callback after work is done
        successConsumer.accept(migrated);
    }

    protected boolean migrateVersions(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, Map<String, Object> oldMetadataRaw) {

        // determine source information
        Object rawPayload = oldMetadataRaw.get(JSON_FIELD_PAYLOAD);
        boolean isSingleton = (boolean) oldMetadataRaw.get(JSON_FIELD_SINGLETON);
        Object sourceTypeVersionRaw = oldMetadataRaw.get(JSON_FIELD_PAYLOAD_TYPE_VERSION);
        Integer sourceTypeVersion = sourceTypeVersionRaw != null ? ((JsonNumber) sourceTypeVersionRaw).toInteger() : 0;

        // do migration
        return migrateVersions(metadata, migrationHandlers, rawPayload, isSingleton, sourceTypeVersion);
    }

    @SuppressWarnings("unchecked")
    protected <T, P> void jsonDeserialization(JsonStoreMetadata<T, P> metadata, Map<String, Object> oldMetadataRaw) {

        // proceed with deserialization
        try {

            // TODO for the moment this is a bad hack to get the binder instance!!
            Stopwatch stopwatch = Stopwatch.createStarted();
            JSONDeserializer<?> deserializer = resolveFlexjsonHelper(metadata.getUid()).deserializer();
            Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
            method.setAccessible(true);
            ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);

            // metadata deserialization
            JsonStoreMetadata<T, P> oldMetadata = (JsonStoreMetadata<T, P>) binder.bind(oldMetadataRaw);
            metadata.setPayload(oldMetadata.getPayload());

            stopwatch.stop();
            LOG.info(metadata.getUid() + ": deserializing from json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        } catch (Exception e) {
            throw new JsonStoreException("Unable to restore from JSON content: " + metadata.getUid() + "!!", e);
        }
    }

    protected FlexjsonHelper resolveFlexjsonHelper(String uid) {
        return flexjsonHelperPerStore.getOrDefault(uid, flexjsonHelper);
    }
}
