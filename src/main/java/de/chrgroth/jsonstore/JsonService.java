package de.chrgroth.jsonstore;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Common interface for all JSON operations used in stores to be able to switch JSON libraries and implemetations as needed.
 *
 * @author Christian Groth
 */
public interface JsonService {

    /**
     * Converts the given store metadata to JSON data.
     *
     * @param metadata
     *            store metadata
     * @return serialized JSON data
     */
    String toJson(JsonStoreMetadata<?, ?> metadata);

    /**
     * Converts the given JSON and updates the payload in given store metadata.
     *
     * @param metadata
     *            store metadata
     * @param migrationHandlers
     *            version migrations handlers to convert given JSON data to correct version defined in store metadata
     * @param json
     *            JSON data to be deserialized
     * @param successConsumer
     *            success callback
     */
    void fromJson(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json, Consumer<Boolean> successConsumer);
}
