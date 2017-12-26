package de.chrgroth.jsonstore.storage;

import de.chrgroth.jsonstore.store.JsonStoreMetadata;

public interface StorageService {

    void prepare();

    long storageSize(JsonStoreMetadata<?, ?> metadata);

    void write(JsonStoreMetadata<?, ?> metadata, String json);

    String read(JsonStoreMetadata<?, ?> metadata);

    void delete(JsonStoreMetadata<?, ?> metadata);
}
