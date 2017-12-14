package de.chrgroth.jsonstore.store;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.json.FlexjsonHelper;

public class JsonSingletonStoreTest {
    public static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";

    private File tempDir;
    private JsonSingletonStore<String> persistentStore;
    private JsonSingletonStore<String> persistentStoreCopy;
    private JsonSingletonStore<String> transientStore;
    private JsonSingletonStore<String> transientStoreCopy;

    private String testDataOne;

    @Before
    public void init() {
        tempDir = Files.createTempDir();
        FlexjsonHelper flexjsonHelper = FlexjsonHelper.builder().dateTimePattern(DATE_TIME_PATTERN).build();
        persistentStore = new JsonSingletonStore<>("uid1", String.class, null, flexjsonHelper, tempDir, StandardCharsets.UTF_8, true, true, false);
        persistentStoreCopy = new JsonSingletonStore<>("uid2", String.class, null, flexjsonHelper, tempDir, StandardCharsets.UTF_8, true, true, false);
        transientStore = new JsonSingletonStore<>("uid3", String.class, null, flexjsonHelper, null, null, false, false, false);
        transientStoreCopy = new JsonSingletonStore<>("uid4", String.class, null, flexjsonHelper, null, null, false, false, false);
        testDataOne = "test data";
    }

    @Test
    public void dataLifecycle() {
        dataLifecycle(persistentStore);
    }

    @Test
    public void transientDataLifecycle() {
        dataLifecycle(transientStore);
    }

    public void dataLifecycle(JsonSingletonStore<String> store) {

        // no data
        Assert.assertTrue(store.isEmpty());
        Assert.assertNull(store.get());

        // set element
        store.set(testDataOne);
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(testDataOne, store.get());

        // clear data
        store.clear();
        Assert.assertTrue(store.isEmpty());
        Assert.assertNull(store.get());

        // clear implicitly
        store.set(testDataOne);
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(testDataOne, store.get());
        store.set(null);
        Assert.assertTrue(store.isEmpty());
        Assert.assertNull(store.get());
    }

    @Test
    public void jsonLifecycle() {
        jsonLifecycle(persistentStore, persistentStoreCopy);
    }

    @Test
    public void transientJsonLifecycle() {
        jsonLifecycle(transientStore, transientStoreCopy);
    }

    private void jsonLifecycle(JsonSingletonStore<String> store, JsonSingletonStore<String> storeCopy) {

        // export data
        store.set(testDataOne);
        String json = store.toJson();
        Assert.assertNotNull(json);

        // copy still empty
        Assert.assertTrue(storeCopy.isEmpty());

        // import into copy
        storeCopy.fromJson(json);
        Assert.assertFalse(storeCopy.isEmpty());
        Assert.assertEquals(store.get(), storeCopy.get());
    }
}
