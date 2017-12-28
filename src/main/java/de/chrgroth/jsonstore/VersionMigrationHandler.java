package de.chrgroth.jsonstore;

import java.util.Map;

import flexjson.JSONDeserializer;

/**
 * Migrates JSON data from source version to next version. This handler will be called during load process before data is deserialized into java instances
 * because this may fail e.g. if field types are changed.
 *
 * @author Christian Groth
 */
public interface VersionMigrationHandler {

    /**
     * The source version this handler will be called for. If migrating from x -&gt; y, set y.
     *
     * @return source version
     */
    int sourceVersion();

    /**
     * Callback to fix data for next version. In case of {@link JsonStore} this method will be called one per instance so you don't have to deal with wrapping
     * collection structures. Changes have to be done in given generic structure created by {@link JSONDeserializer}.
     *
     * @param genericPayload
     *            generic payload information to be adapted
     */
    void migrate(Map<String, Object> genericPayload);
}
