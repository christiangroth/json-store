package de.chrgroth.jsonstore.json;

import java.util.Map;

import de.chrgroth.jsonstore.store.JsonStoreMetadata;
import de.chrgroth.jsonstore.store.VersionMigrationHandler;

/**
 * General abstraction regarding all JSON operations used in stores to be able to switch JSON libraries and implemetations as needed.
 *
 * @author Christian Groth
 */
public interface JsonService {

    String toJson(JsonStoreMetadata<?, ?> metadata);

    boolean fromJson(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json);

}
