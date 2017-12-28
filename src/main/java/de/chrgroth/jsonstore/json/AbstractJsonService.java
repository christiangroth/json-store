package de.chrgroth.jsonstore.json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import de.chrgroth.jsonstore.JsonService;
import de.chrgroth.jsonstore.JsonStoreException;
import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.VersionMigrationHandler;

/**
 * Abstract JSON service implementation to offer logic for data migration using {@link VersionMigrationHandler} for all future {@link JsonService}
 * implementations.
 *
 * @author Christian Groth
 */
public abstract class AbstractJsonService implements JsonService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonService.class);

    /**
     * Migrates the given raw payload to target version contained in given metadata with given migration handlers. If no migration is needed or payload is null,
     * nothing will be done.
     *
     * @param metadata
     *            store metadata
     * @param migrationHandlers
     *            migrations handlers to be used
     * @param rawPayload
     *            the raw payload to be migrated
     * @param isSingleton
     *            true if given metadata belonds to a singleton store
     * @param sourceTypeVersion
     *            version of given raw metadata
     * @return true if payload was migrated, false otherwise
     */
    @SuppressWarnings("unchecked")
    protected boolean migrateVersions(JsonStoreMetadata<?, ?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, Object rawPayload, boolean isSingleton,
            Integer sourceTypeVersion) {

        // ensure payload
        if (rawPayload == null) {
            return false;
        }

        // compare version information
        Integer targetTypeVersion = metadata.getPayloadTypeVersion();
        if (sourceTypeVersion == null || targetTypeVersion == null) {
            return false;
        }

        // abort on newer version than available as code
        if (sourceTypeVersion > targetTypeVersion) {
            throw new JsonStoreException("loaded version is newer than specified version in code: " + sourceTypeVersion + " > " + targetTypeVersion + "!!");
        }

        // run all available version migrators
        boolean migrated = false;
        if (sourceTypeVersion < targetTypeVersion) {

            // update per version
            for (int i = sourceTypeVersion; i <= targetTypeVersion; i++) {

                // check for migration handler
                VersionMigrationHandler migrationHandler = migrationHandlers.get(i);
                if (migrationHandler == null) {
                    continue;
                }

                // invoke handler per instance, so you don't have to deal with wrapping outer list by yourself
                LOG.info(metadata.getUid() + ": migrating to version " + i + " using " + migrationHandler);
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    if (isSingleton) {
                        migrationHandler.migrate((Map<String, Object>) rawPayload);
                    } else {
                        for (Object genericStructurePayloadItem : (List<Object>) rawPayload) {
                            migrationHandler.migrate((Map<String, Object>) genericStructurePayloadItem);
                        }
                    }
                    stopwatch.stop();
                    migrated = true;
                    LOG.info(metadata.getUid() + ": migrating to version " + i + " took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                } catch (Exception e) {
                    throw new JsonStoreException("failed to migrate " + metadata.getUid() + " from version " + i + " to " + (i + 1) + ": " + e.getMessage() + "!!", e);
                }
            }
        }

        // done
        return migrated;
    }
}
