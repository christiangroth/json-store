package de.chrgroth.jsonstore;

/**
 * Common interface for all storage relevant operations.
 *
 * @author Christian Groth
 */
public interface StorageService {

    /**
     * Prepares the storeage and is called once on initialization of {@link JsonStores}.
     */
    void prepare();

    /**
     * Returns the storage size for given store metadata in bytes.
     *
     * @param metadata
     *            store metadata
     * @return storge size in bytes
     */
    long size(JsonStoreMetadata<?, ?> metadata);

    /**
     * Writes the given JSON data for given store metadata.
     *
     * @param metadata
     *            store metadata
     * @param json
     *            JSON data to be written
     */
    void write(JsonStoreMetadata<?, ?> metadata, String json);

    /**
     * Reads the JSON data for given store metadata.
     *
     * @param metadata
     *            store metadata
     * @return persistent JSON data
     */
    String read(JsonStoreMetadata<?, ?> metadata);

    /**
     * Deletes the persistent storage for given store metadata.
     *
     * @param metadata
     *            store metadata
     */
    void delete(JsonStoreMetadata<?, ?> metadata);
}
