package de.chrgroth.jsonstore;

/**
 * Represents a JSON store for a concrete class holding none or one instance. You may use flexjson annotations to control conversion from/to JSON.
 *
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonSingletonStore<T> extends AbstractJsonStore<T, T> {

    /**
     * Creates a new JSON store.
     *
     * @param jsonService
     *            JSON service implementation
     * @param storageService
     *            storage service implementation
     * @param uid
     *            store uid
     * @param payloadClass
     *            type of objects to be stored
     * @param payloadTypeVersion
     *            version of payload type class
     * @param autoSave
     *            auto-save mode
     * @param migrationHandlers
     *            all migration handlers to be applied
     */
    public JsonSingletonStore(JsonService jsonService, StorageService storageService, String uid, Class<T> payloadClass, Integer payloadTypeVersion, boolean autoSave,
            VersionMigrationHandler... migrationHandlers) {
        super(jsonService, storageService, uid, payloadClass, payloadTypeVersion, true, autoSave, migrationHandlers);
    }

    @Override
    public long size() {
        return isEmpty() ? 0 : 1;
    }

    @Override
    protected void metadataRefreshed() {
        // nothing to do
    }

    /**
     * Returns stored data.
     *
     * @return data, may be null
     */
    public T get() {
        return metadata.getPayload();
    }

    /**
     * Checks if store is empty
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return get() == null;
    }

    /**
     * Stores the given object. Will invoke {@link #save()} if using auto-save mode and store was changed.
     *
     * @param payload
     *            object to store
     * @return previous stored object or null
     */
    public T set(T payload) {

        // switch data
        T old = payload;
        metadata.setPayload(payload);

        // save
        if (autoSave) {
            save();
        }

        // done
        return old;
    }

    /**
     * Clears the store. Will invoke {@link #save()} if using auto-save mode.
     */
    public void clear() {
        set(null);
    }
}
