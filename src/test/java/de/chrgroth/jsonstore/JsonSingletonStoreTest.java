package de.chrgroth.jsonstore;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.json.flexjson.FlexjsonService;
import de.chrgroth.jsonstore.storage.FileStorageService;

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
        FlexjsonService flexjsonService = FlexjsonService.builder().dateTimePattern(DATE_TIME_PATTERN).build();
        FileStorageService storageService = FileStorageService.builder().storage(tempDir).build();
        persistentStore = new JsonSingletonStore<>(flexjsonService, storageService, "uid1", String.class, null, true);
        persistentStoreCopy = new JsonSingletonStore<>(flexjsonService, storageService, "uid2", String.class, null, true);
        transientStore = new JsonSingletonStore<>(flexjsonService, storageService, "uid3", String.class, null, false);
        transientStoreCopy = new JsonSingletonStore<>(flexjsonService, storageService, "uid4", String.class, null, false);
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
