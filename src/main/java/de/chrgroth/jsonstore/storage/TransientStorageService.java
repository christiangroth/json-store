package de.chrgroth.jsonstore.storage;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.StorageService;

/**
 * Implementation for transient in memory storage only.
 *
 * @author Christian Groth
 */
public class TransientStorageService implements StorageService {

    @Override
    public void prepare() {
    }

    @Override
    public long size(JsonStoreMetadata<?, ?> metadata) {
        return 0;
    }

    @Override
    public void write(JsonStoreMetadata<?, ?> metadata, String json) {
    }

    @Override
    public String read(JsonStoreMetadata<?, ?> metadata) {
        return null;
    }

    @Override
    public void delete(JsonStoreMetadata<?, ?> metadata) {
    }
}
