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
    private static final String JSON_FIELD_PAYLOAD_TYPE_VERSION = "payloadTypeVersion";

    private final FlexjsonHelper flexjsonHelper;
    private final Map<String, FlexjsonHelper> flexjsonHelperPerStore;

    private final boolean deepSerialize;
    private final Map<String, Boolean> deepSerializePerStore;

    private final boolean prettyPrint;
    private final Map<String, Boolean> prettyPrintPerStore;

    /**
     * Builder to configure a new instance of {@link FlexjsonService}.
     *
     * @author Christian Groth
     */
    public static class FlexjsonServiceBuilder {

        private final FlexjsonHelperBuilder flexjsonHelperBuilder;
        private final Map<String, FlexjsonHelperBuilder> flexjsonHelperBuilderPerStore;

        private boolean deepSerialize;
        private final Map<String, Boolean> deepSerializePerStore;

        private boolean prettyPrint;
        private final Map<String, Boolean> prettyPrintPerStore;

        private FlexjsonServiceBuilder() {
            flexjsonHelperBuilder = FlexjsonHelper.builder();
            flexjsonHelperBuilderPerStore = new HashMap<>();
            deepSerializePerStore = new HashMap<>();
            prettyPrintPerStore = new HashMap<>();
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
         * @param uid
         *            used for store matching
         * @param dateTimePattern
         *            date time pattern to be used.
         * @return builder
         */
        public FlexjsonServiceBuilder dateTimePattern(String uid, String dateTimePattern) {
            ensureFlexjsonHelperBuilderPerStore(uid).dateTimePattern(dateTimePattern);
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
         * @param uid
         *            used for store matching
         * @return builder
         */
        public FlexjsonServiceBuilder useStringInterning(String uid) {
            ensureFlexjsonHelperBuilderPerStore(uid).useStringInterning();
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
         * @param uid
         *            used for store matching
         * @param type
         *            type the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public FlexjsonServiceBuilder factory(String uid, Class<?> type, AbstractFlexjsonTypeHandler handler) {
            ensureFlexjsonHelperBuilderPerStore(uid).handler(type, handler);
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
         * @param uid
         *            used for store matching
         * @param path
         *            path the handler applies on
         * @param handler
         *            handler to be applied
         * @return builder
         */
        public FlexjsonServiceBuilder factory(String uid, String path, AbstractFlexjsonTypeHandler handler) {
            ensureFlexjsonHelperBuilderPerStore(uid).handler(path, handler);
            return this;
        }

        private FlexjsonHelperBuilder ensureFlexjsonHelperBuilderPerStore(String uid) {

            // create
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
         * Sets the deep serialize mode for matching store with given uid.
         *
         * @param uid
         *            used for store matching
         * @param deepSerialize
         *            true for deep serialization, false to use explicit annotations
         * @return builder
         */
        public FlexjsonServiceBuilder setDeepSerialize(String uid, boolean deepSerialize) {
            deepSerializePerStore.put(uid, deepSerialize);
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
         * Sets the pretty print serialization mode for matching store with given uid.
         *
         * @param uid
         *            used for store matching
         * @param prettyPrint
         *            true for pretty print mode, false otherwise
         * @return builder
         */
        public FlexjsonServiceBuilder setPrettyPrint(String uid, boolean prettyPrint) {
            prettyPrintPerStore.put(uid, prettyPrint);
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
            return new FlexjsonService(flexjsonHelperBuilder.build(), flexjsonHelperPerStore, deepSerialize, deepSerializePerStore, prettyPrint, prettyPrintPerStore);
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

    protected FlexjsonService(FlexjsonHelper flexjsonHelper, Map<String, FlexjsonHelper> flexjsonHelperPerStore, boolean deepSerialize, Map<String, Boolean> deepSerializePerStore,
            boolean prettyPrint, Map<String, Boolean> prettyPrintPerStore) {

        this.flexjsonHelper = flexjsonHelper;
        this.flexjsonHelperPerStore = new HashMap<>();
        if (flexjsonHelperPerStore != null) {
            this.flexjsonHelperPerStore.putAll(flexjsonHelperPerStore);
        }

        this.deepSerialize = deepSerialize;
        this.deepSerializePerStore = new HashMap<>();
        if (deepSerializePerStore != null) {
            this.deepSerializePerStore.putAll(deepSerializePerStore);
        }

        this.prettyPrint = prettyPrint;
        this.prettyPrintPerStore = new HashMap<>();
        if (prettyPrintPerStore != null) {
            this.prettyPrintPerStore.putAll(prettyPrintPerStore);
        }
    }

    @Override
    public String toJson(JsonStoreMetadata<?> metadata) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            final String uid = metadata.getUid();

            final boolean prettyPrintForStore = prettyPrintPerStore.getOrDefault(metadata.getUid(), prettyPrint);
            final JSONSerializer serializer = resolveFlexjsonHelper(uid).serializer(prettyPrintForStore);

            final boolean deepSerializeForStore = deepSerializePerStore.getOrDefault(metadata.getUid(), deepSerialize);
            return deepSerializeForStore ? serializer.deepSerialize(metadata) : serializer.serialize(metadata);
        } finally {
            stopwatch.stop();
            LOG.info(metadata.getUid() + ": converting to json took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        }
    }

    @Override
    public void fromJson(JsonStoreMetadata<?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json, Consumer<Boolean> successConsumer) {

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
        if (successConsumer != null) {
            successConsumer.accept(migrated);
        }
    }

    protected boolean migrateVersions(JsonStoreMetadata<?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, Map<String, Object> oldMetadataRaw) {

        // determine source information
        Object rawPayload = oldMetadataRaw.get(JSON_FIELD_PAYLOAD);
        Object sourceTypeVersionRaw = oldMetadataRaw.get(JSON_FIELD_PAYLOAD_TYPE_VERSION);
        Integer sourceTypeVersion = sourceTypeVersionRaw != null ? ((JsonNumber) sourceTypeVersionRaw).toInteger() : 0;

        // do migration
        return migrateVersions(metadata, migrationHandlers, rawPayload, sourceTypeVersion);
    }

    @SuppressWarnings("unchecked")
    protected <T> void jsonDeserialization(JsonStoreMetadata<T> metadata, Map<String, Object> oldMetadataRaw) {

        // proceed with deserialization
        try {

            // TODO for the moment this is a bad hack to get the binder instance!!
            Stopwatch stopwatch = Stopwatch.createStarted();
            JSONDeserializer<?> deserializer = resolveFlexjsonHelper(metadata.getUid()).deserializer();
            Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
            method.setAccessible(true);
            ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);

            // metadata deserialization
            JsonStoreMetadata<T> oldMetadata = (JsonStoreMetadata<T>) binder.bind(oldMetadataRaw);
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
