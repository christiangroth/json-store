package de.chrgroth.jsonstore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.chrgroth.jsonstore.metrics.JsonStoreMetrics;

/**
 * Represents a JSON store for a concrete class. Access is provided using delegate methods to Java built in stream API. You may use flexjson annotations to
 * control conversion from/to JSON.
 *
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 * @param <P>
 *            concrete type structure used for storage of instances of type T
 */
public abstract class AbstractJsonStore<T, P> {

    protected final JsonService jsonService;
    protected final StorageService storageService;

    protected final JsonStoreMetadata<T, P> metadata;
    protected final boolean autoSave;
    protected final Map<Integer, VersionMigrationHandler> migrationHandlers;

    protected AbstractJsonStore(JsonService jsonService, StorageService storageService, String uid, Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton,
            boolean autoSave, VersionMigrationHandler... migrationHandlers) {

        // set json service
        if (jsonService == null) {
            throw new JsonStoreException("jsonService must be provided!!");
        }
        this.jsonService = jsonService;

        // set storage service
        if (storageService == null) {
            throw new JsonStoreException("storageService must be provided!!");
        }
        this.storageService = storageService;

        // create empty metadata
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(uid);
        metadata.setPayloadType(payloadClass.getName());
        metadata.setPayloadTypeVersion(payloadTypeVersion);
        metadata.setSingleton(singleton);
        metadata.setCreated(new Date());

        // set auto-save mode
        this.autoSave = autoSave;

        // set migration handlers
        this.migrationHandlers = new HashMap<>();
        if (migrationHandlers != null) {
            for (VersionMigrationHandler migrationHandler : migrationHandlers) {
                this.migrationHandlers.put(migrationHandler.sourceVersion(), migrationHandler);
            }
        }
    }

    /**
     * Returns the stores uid.
     *
     * @return uid
     */
    public String getUid() {
        return metadata.getUid();
    }

    /**
     * Returns the number of contained items.
     *
     * @return number of items
     */
    public abstract long size();

    /**
     * Computes current metrics for this instance.
     *
     * @return metrics, never null
     */
    public JsonStoreMetrics computeMetrics() {
        return new JsonStoreMetrics(metadata.getUid(), metadata.getPayloadType(), size(), metadata.getModified(), storageService.size(metadata));
    }

    /**
     * Saves all data contained in store to configured file. No action if store is not persistent.
     */
    public final void save() {

        // create JSON
        String json = toJson();

        // update metadata
        metadata.setModified(new Date());

        // write
        storageService.write(metadata, json);
    }

    /**
     * Returns store elements in JSON format.
     *
     * @return JSON data
     */
    public final String toJson() {
        return jsonService.toJson(metadata);
    }

    /**
     * Loads store elements from configure file. If any error occurs a {@link JsonStoreException} will be thrown cause otherwise data loss may occur on next
     * successful save.
     */
    public final void load() {

        // load
        String json = storageService.read(metadata);

        // recreate data
        fromJsonInternal(json, false);
    }

    /**
     * Creates store elements from given JSON data and replaces all store contents. Will invoke {@link #save()} if using auto-save mode. If any error occurs a
     * {@link JsonStoreException} will be thrown cause otherwise data loss may occur on next successful save.
     *
     * @param json
     *            JSON data
     */
    public final void fromJson(String json) {
        fromJsonInternal(json, true);
    }

    protected void fromJsonInternal(String json, boolean forceStore) {
        jsonService.fromJson(metadata, migrationHandlers, json, migrated -> {
            metadataRefreshed();
            if (autoSave && (forceStore || migrated)) {
                save();
            }
        });
    }

    /**
     * Gets called after metadata was refreshed on loading new JSON data.
     */
    protected abstract void metadataRefreshed();

    /**
     * Drops store file explicitly. Transient data in store remains unchanged.
     */
    public final void drop() {
        storageService.delete(metadata);
    }
}
