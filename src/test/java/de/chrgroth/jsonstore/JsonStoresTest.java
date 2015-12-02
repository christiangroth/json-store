package de.chrgroth.jsonstore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.store.JsonSingletonStore;
import de.chrgroth.jsonstore.store.JsonStore;

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

        persistentStores = JsonStores.builder().storage(tempDir, StandardCharsets.UTF_8, true, true).build();
        persistentStoresCopy = JsonStores.builder().storage(tempDir, StandardCharsets.UTF_8, false, true).build();

        persistentStoresNoAutoSave = JsonStores.builder().storage(tempDir, StandardCharsets.UTF_8, true, false).build();
        persistentStoresNoAutoSaveCopy = JsonStores.builder().storage(tempDir, StandardCharsets.UTF_8, false, false).build();

        persistentStoresIsoCharset = JsonStores.builder().storage(tempDir, StandardCharsets.ISO_8859_1, true, true).build();
        persistentStoresIsoCharsetCopy = JsonStores.builder().storage(tempDir, StandardCharsets.ISO_8859_1, false, true).build();

        transientStores = JsonStores.builder().build();
        transientStoresCopy = JsonStores.builder().build();

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
        Object store = resolve(stores, isSingleton, dataClass);
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
        
        // explicit save
        stores.save();
        if (isPersistent) {
            Assert.assertEquals(1, tempDir.listFiles().length);
            Assert.assertEquals(file(store, isSingleton), tempDir.listFiles()[0]);
        } else {
            Assert.assertEquals(0, tempDir.listFiles().length);
        }
        
        // drop
        Object droppedStore = drop(stores, isSingleton, dataClass);
        Assert.assertEquals(store, droppedStore);
        Assert.assertEquals(0, tempDir.listFiles().length);
        store = resolve(stores, isSingleton, dataClass);
        Assert.assertNull(store);
        
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
        
        // copy still empty
        Object storeCopy = resolve(storesCopy, isSingleton, dataClass);
        Assert.assertNull(storeCopy);
        
        // load into stores copy
        storeCopy = ensure(storesCopy, isSingleton, dataClass);
        if (isPersistent) {
            Assert.assertNotNull(storeCopy);
            Assert.assertEquals(file(storeCopy, isSingleton), tempDir.listFiles()[0]);
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

    private Object resolve(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.resolveSingleton(dataClass) : stores.resolve(dataClass);
    }

    private Object ensure(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.ensureSingleton(dataClass, 1) : stores.ensure(dataClass, 1);
    }

    private Object drop(JsonStores stores, boolean isSingleton, Class<String> dataClass) {
        return isSingleton ? stores.dropSingleton(dataClass) : stores.drop(dataClass);
    }

    private File file(Object store, boolean isSingleton) {
        return isSingleton ? ((JsonSingletonStore<?>) store).getFile() : ((JsonStore<?>) store).getFile();
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
        
        return ((JsonStore<String>) store).isEmpty() ? null : ((JsonStore<String>) store).copy().iterator().next();
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
        JsonStores.builder().storage(storage).build();
        Assert.assertTrue(storage.exists());
        Assert.assertTrue(storage.isDirectory());
        Assert.assertTrue(storage.canRead());
        Assert.assertEquals(0, storage.listFiles().length);
    }
}
