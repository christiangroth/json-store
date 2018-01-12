package de.chrgroth.jsonstore.json;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonStoreException;
import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.VersionMigrationHandler;

public class AbstractJsonServiceMigrationTest {

    // generic migration handler just signaling if it was called
    private class TestMigrationHandler implements VersionMigrationHandler {

        private final int targetVersion;

        private TestMigrationHandler(int targetVersion) {
            this.targetVersion = targetVersion;
        }

        @Override
        public int sourceVersion() {
            return targetVersion;
        }

        @Override
        public void migrate(Map<String, Object> genericPayload) {
            versionsMigrated.put(targetVersion, versionsMigrated.getOrDefault(targetVersion, 0) + 1);
        }
    }

    // mocked service to test migration stuff only
    private AbstractJsonService jsonService = new AbstractJsonService() {

        @Override
        public String toJson(JsonStoreMetadata<?> metadata) {
            return null;
        }

        @Override
        public void fromJson(JsonStoreMetadata<?> metadata, Map<Integer, VersionMigrationHandler> migrationHandlers, String json, Consumer<Boolean> successConsumer) {

        }
    };

    private Map<Integer, Integer> versionsMigrated;

    private JsonStoreMetadata<?> metadata;
    private Map<Integer, VersionMigrationHandler> versionMigrationHandlers;

    private Map<String, Object> sourceDataOne = new HashMap<>();
    private Map<String, Object> sourceDataTwo = new HashMap<>();
    private List<Map<String, Object>> sourcePayload = Arrays.asList(sourceDataOne, sourceDataTwo);

    @Before
    public void init() {

        // create target metadata
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(AbstractJsonServiceMigrationTest.class.getName());
        metadata.setPayloadTypeVersion(3);
        metadata.setSingleton(false);

        // create migration handlers
        versionsMigrated = new HashMap<>();
        versionMigrationHandlers = new HashMap<>();
        versionMigrationHandlers.put(0, new TestMigrationHandler(0));
        versionMigrationHandlers.put(1, new TestMigrationHandler(1));
        versionMigrationHandlers.put(3, new TestMigrationHandler(3));
    }

    @Test
    public void migrateNullPayload() {
        Assert.assertFalse(migrate(null, 0));
    }

    @Test
    public void migrateNullSourceVersion() {
        Assert.assertFalse(migrate(sourcePayload, null));
    }

    @Test(expected = JsonStoreException.class)
    public void migrateSourceVersionGreaterTargetVersion() {
        Assert.assertFalse(migrate(sourcePayload, 5));
    }

    @Test
    public void migrateSourceVersionEqualsTargetVersion() {
        Assert.assertFalse(migrate(sourcePayload, metadata.getPayloadTypeVersion()));
    }

    @Test
    public void migrateFromZero() {
        Assert.assertTrue(migrate(sourcePayload, 0));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertEquals(Integer.valueOf(2), versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(2), versionsMigrated.get(3));
    }

    @Test
    public void migrateFromOne() {
        Assert.assertTrue(migrate(sourcePayload, 1));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertNull(versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(2), versionsMigrated.get(3));
    }

    @Test
    public void migrateFromTwo() {
        Assert.assertTrue(migrate(sourcePayload, 1));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertNull(versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(2), versionsMigrated.get(3));
    }

    @Test
    public void migrateSingletonFromZero() {
        metadata.setSingleton(true);
        Assert.assertTrue(migrate(sourceDataOne, 0));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertEquals(Integer.valueOf(1), versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(1), versionsMigrated.get(3));
    }

    @Test
    public void migrateSingletonFromOne() {
        metadata.setSingleton(true);
        Assert.assertTrue(migrate(sourceDataOne, 1));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertNull(versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(1), versionsMigrated.get(3));
    }

    @Test
    public void migrateSingletonFromTwo() {
        metadata.setSingleton(true);
        Assert.assertTrue(migrate(sourceDataOne, 1));
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertNull(versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertEquals(Integer.valueOf(1), versionsMigrated.get(3));
    }

    @Test
    public void exceptionCausedByHandler() {
        versionMigrationHandlers.put(2, new VersionMigrationHandler() {

            @Override
            public int sourceVersion() {
                return 2;
            }

            @Override
            public void migrate(Map<String, Object> genericPayload) {
                throw new RuntimeException("test exception to abort migration.");
            }
        });

        boolean caughtException = false;
        try {
            migrate(sourcePayload, 0);
        } catch (JsonStoreException e) {
            caughtException = true;
        }
        Assert.assertTrue(caughtException);
        Assert.assertNull(versionsMigrated.get(0));
        Assert.assertEquals(Integer.valueOf(2), versionsMigrated.get(1));
        Assert.assertNull(versionsMigrated.get(2));
        Assert.assertNull(versionsMigrated.get(3));
    }

    private boolean migrate(Object sourcePayload, Integer sourceVersion) {
        return jsonService.migrateVersions(metadata, versionMigrationHandlers, sourcePayload, sourceVersion);
    }
}
