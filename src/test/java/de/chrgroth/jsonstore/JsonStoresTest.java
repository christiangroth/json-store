package de.chrgroth.jsonstore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.json.flexjson.FlexjsonService;
import de.chrgroth.jsonstore.metrics.JsonStoreMetrics;
import de.chrgroth.jsonstore.storage.FileStorageService;

public class JsonStoresTest {

    private File tempDir;

    private JsonStores persistentStores;
    private JsonStores persistentStoresCopy;

    private JsonStores persistentStoresNoAutoSave;
    private JsonStores persistentStoresNoAutoSaveCopy;

    private JsonStores persistentStoresIsoCharset;
    private JsonStores persistentStoresIsoCharsetCopy;

    private JsonStores transientStores;
    private JsonStores transientStoresCopy;

    private String testData;

    @Before
    public void init() {
        tempDir = Files.createTempDir();

        FlexjsonService jsonService = FlexjsonService.builder().build();
        StorageService storageService = FileStorageService.builder().storage(tempDir).build();
        StorageService isoStorageService = FileStorageService.builder().storage(tempDir).charset(StandardCharsets.ISO_8859_1).build();

        persistentStores = JsonStores.builder(jsonService, storageService).autoSave(true).build();
        persistentStoresCopy = JsonStores.builder(jsonService, storageService).autoSave(true).build();

        persistentStoresNoAutoSave = JsonStores.builder(jsonService, storageService).autoSave(false).build();
        persistentStoresNoAutoSaveCopy = JsonStores.builder(jsonService, storageService).autoSave(false).build();

        persistentStoresIsoCharset = JsonStores.builder(jsonService, isoStorageService).autoSave(true).build();
        persistentStoresIsoCharsetCopy = JsonStores.builder(jsonService, isoStorageService).autoSave(true).build();

        transientStores = JsonStores.builder(jsonService, null).build();
        transientStoresCopy = JsonStores.builder(jsonService, null).build();

        testData = "test data";
    }

    @Test
    public void storeLifecycle() {
        assertStoreLifecycle(persistentStores, persistentStoresCopy, false, true, true);
    }

    @Test
    public void storeNoAutoSaveLifecycle() {
        assertStoreLifecycle(persistentStoresNoAutoSave, persistentStoresNoAutoSaveCopy, false, true, false);
    }

    @Test
    public void storeIsoCharsetLifecycle() {
        assertStoreLifecycle(persistentStoresIsoCharset, persistentStoresIsoCharsetCopy, false, true, true);
    }

    @Test
    public void transientStoreLifecycle() {
        assertStoreLifecycle(transientStores, transientStoresCopy, false, false, false);
    }

    @Test
    public void singletonStoreLifecycle() {
        assertStoreLifecycle(persistentStores, persistentStoresCopy, true, true, true);
    }

    @Test
    public void singletonStoreNoAutoSaveLifecycle() {
        assertStoreLifecycle(persistentStoresNoAutoSave, persistentStoresNoAutoSaveCopy, true, true, false);
    }

    @Test
    public void singletonStoreIsoCharsetLifecycle() {
        assertStoreLifecycle(persistentStoresIsoCharset, persistentStoresIsoCharsetCopy, true, true, true);
    }

    @Test
    public void transientSingletonStoreLifecycle() {
        assertStoreLifecycle(transientStores, transientStoresCopy, true, false, false);
    }

    private void assertStoreLifecycle(JsonStores stores, JsonStores storesCopy, boolean isSingleton, boolean isPersistent, boolean isAutoSave) {

        // nothing there
        Class<String> dataClass = String.class;
        AbstractJsonStore<?, ?> store = resolve(stores, isSingleton, dataClass);
        Assert.assertNull(store);
        Assert.assertTrue(tempDir.exists());
        Assert.assertTrue(tempDir.isDirectory());
        Assert.assertTrue(tempDir.canRead());
        Assert.assertEquals(0, tempDir.listFiles().length);

        // ensure
        store = ensure(stores, isSingleton, dataClass);
        Assert.assertNotNull(store);
        Assert.assertEquals(0, tempDir.listFiles().length);
        store = resolve(stores, isSingleton, dataClass);
        Assert.assertNotNull(store);
        JsonStoreMetrics storeMetrics = store.computeMetrics();
        Assert.assertNotNull(storeMetrics);
        Assert.assertEquals(dataClass.getName(), storeMetrics.getType());
        Assert.assertEquals(0, storeMetrics.getItemCount());
        Assert.assertEquals(null, storeMetrics.getLastModified());
        Assert.assertEquals(0, storeMetrics.getFileSize());

        // explicit save
        stores.save();
        if (isPersistent) {
            Assert.assertEquals(1, tempDir.listFiles().length);
        } else {
            Assert.assertEquals(0, tempDir.listFiles().length);
        }
        storeMetrics = store.computeMetrics();
        Assert.assertNotNull(storeMetrics);
        Assert.assertEquals(dataClass.getName(), storeMetrics.getType());
        Assert.assertEquals(0, storeMetrics.getItemCount());
        long fileSize = storeMetrics.getFileSize();
        if (isPersistent) {
            Assert.assertNotNull(storeMetrics.getLastModified());
            Assert.assertTrue(fileSize > 0);
        } else {
            Assert.assertNull(storeMetrics.getLastModified());
            Assert.assertEquals(0, fileSize);
        }

        // drop
        AbstractJsonStore<?, ?> droppedStore = drop(stores, isSingleton, dataClass);
        Assert.assertEquals(store, droppedStore);
        Assert.assertEquals(0, tempDir.listFiles().length);
        store = resolve(stores, isSingleton, dataClass);
        Assert.assertNull(store);
        storeMetrics = droppedStore.computeMetrics();
        Assert.assertNotNull(storeMetrics);
        Assert.assertEquals(dataClass.getName(), storeMetrics.getType());
        Assert.assertEquals(0, storeMetrics.getItemCount());
        if (isPersistent) {
            Assert.assertNotNull(storeMetrics.getLastModified());
        } else {
            Assert.assertNull(storeMetrics.getLastModified());
        }
        Assert.assertEquals(0, storeMetrics.getFileSize());

        // create again with data
        store = ensure(stores, isSingleton, dataClass);
        setOrAddData(store, isSingleton);
        if (isPersistent) {
            if (isAutoSave) {
                Assert.assertEquals(1, tempDir.listFiles().length);
            } else {
                Assert.assertEquals(0, tempDir.listFiles().length);
                save(store, isSingleton);
                Assert.assertEquals(1, tempDir.listFiles().length);
            }
        } else {
            Assert.assertEquals(0, tempDir.listFiles().length);
            save(store, isSingleton);
            Assert.assertEquals(0, tempDir.listFiles().length);
        }
        storeMetrics = store.computeMetrics();
        Assert.assertNotNull(storeMetrics);
        Assert.assertEquals(dataClass.getName(), storeMetrics.getType());
        Assert.assertEquals(1, storeMetrics.getItemCount());
        if (isPersistent) {
            Assert.assertNotNull(storeMetrics.getLastModified());
            Assert.assertTrue(fileSize < storeMetrics.getFileSize());
        } else {
            Assert.assertNull(storeMetrics.getLastModified());
            Assert.assertEquals(0, storeMetrics.getFileSize());
        }

        // copy still empty
        AbstractJsonStore<?, ?> storeCopy = resolve(storesCopy, isSingleton, dataClass);
        Assert.assertNull(storeCopy);

        // load into stores copy
        storeCopy = ensure(storesCopy, isSingleton, dataClass);
        if (isPersistent) {
            Assert.assertNotNull(storeCopy);
            if (isAutoSave) {
                Assert.assertEquals(testData, getData(storeCopy, isSingleton));
            } else {
                Assert.assertNull(getData(storeCopy, isSingleton));
            }
            Assert.assertEquals(1, tempDir.listFiles().length);
        } else {
            Assert.assertNotNull(storeCopy);
            Assert.assertNull(getData(storeCopy, isSingleton));
        }
    }

    private AbstractJsonStore<?, ?> resolve(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.resolveSingleton(dataClass, null) : stores.resolve(dataClass, null);
    }

    private AbstractJsonStore<?, ?> ensure(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.ensureSingleton(dataClass, null, 1) : stores.ensure(dataClass, null, 1);
    }

    private AbstractJsonStore<?, ?> drop(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.dropSingleton(dataClass, null) : stores.drop(dataClass, null);
    }

    @SuppressWarnings("unchecked")
    private void setOrAddData(Object store, boolean isSingleton) {
        if (isSingleton) {
            ((JsonSingletonStore<String>) store).set(testData);
        } else {
            ((JsonStore<String>) store).add(testData);
        }
    }

    @SuppressWarnings("unchecked")
    private String getData(Object store, boolean isSingleton) {
        if (isSingleton) {
            return ((JsonSingletonStore<String>) store).get();
        }

        return ((JsonStore) store).isEmpty() ? null : ((JsonStore<String>) store).copy().iterator().next();
    }

    @SuppressWarnings("unchecked")
    private void save(Object store, boolean isSingleton) {
        if (isSingleton) {
            ((JsonSingletonStore<String>) store).save();
        } else {
            ((JsonStore<String>) store).save();
        }
    }

    @Test
    public void nonExistentStorage() {

        // create stores with non existent storage directory
        String subdir = UUID.randomUUID().toString();
        File storage = new File(tempDir, subdir);
        Assert.assertFalse(storage.exists());

        // check empty storage created on startup
        FlexjsonService jsonService = FlexjsonService.builder().build();
        StorageService storageService = FileStorageService.builder().storage(tempDir).build();
        JsonStores.builder(jsonService, storageService).build();
        Assert.assertTrue(storage.exists());
        Assert.assertTrue(storage.isDirectory());
        Assert.assertTrue(storage.canRead());
        Assert.assertEquals(0, storage.listFiles().length);
    }
}
