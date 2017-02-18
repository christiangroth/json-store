package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.Charset;

import de.chrgroth.jsonstore.json.FlexjsonHelper;

/**
 * Represents a JSON store for a concrete class holding none or one instance. You may use flexjson annotations to control conversion from/to JSON.
 *
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonSingletonStore<T> extends AbstractJsonStore<T, T> {

    private static final String FILE_SINGLETON = "singleton";

    /**
     * Creates a new JSON store.
     *
     * @param payloadClass
     *            type of objects to be stored
     * @param payloadTypeVersion
     *            version of payload type class
     * @param flexjsonHelper
     *            helper for JSON serialization and deserialization
     * @param storage
     *            global storage path
     * @param charset
     *            storage charset
     * @param prettyPrint
     *            pretty-print mode
     * @param autoSave
     *            auto-save mode
     * @param deepSerialize
     *            deep serialization mode
     * @param migrationHandlers
     *            all migration handlers to be applied
     */
    public JsonSingletonStore(Class<T> payloadClass, Integer payloadTypeVersion, FlexjsonHelper flexjsonHelper, File storage, Charset charset, boolean prettyPrint,
            boolean autoSave, boolean deepSerialize, VersionMigrationHandler... migrationHandlers) {
        super(payloadClass, payloadTypeVersion, true, flexjsonHelper, storage, charset, FILE_SINGLETON + FILE_SEPARATOR, prettyPrint, autoSave, deepSerialize, migrationHandlers);
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
