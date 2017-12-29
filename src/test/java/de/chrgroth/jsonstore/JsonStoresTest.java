package de.chrgroth.jsonstore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JsonStoresTest {

    private static final String UID = "test-uid";
    private static final String UID_SINGLETON = "test-uid-singleton";

    private JsonStores stores;

    private String testData;

    @Mock
    private JsonService jsonService;

    @Mock
    private StorageService storageService;

    @Before
    public void init() {

        // configure mocks
        MockitoAnnotations.initMocks(this);

        stores = JsonStores.builder(jsonService, storageService).autoSave(true).build();
        testData = "test data";
    }

    // TODO metrics for all (mixed)

    @Test
    public void prepareCalled() {
        Mockito.verify(storageService, Mockito.times(1)).prepare();
    }

    @Test
    public void storeLifecycle() {
        assertStoreLifecycle(false);
    }

    @Test
    public void storeLifecycleSingleton() {
        assertStoreLifecycle(true);
    }

    @Test
    public void storeLifecycleNoAutoSave() {
        stores = JsonStores.builder(jsonService, storageService).build();
        assertStoreLifecycle(false);
    }

    @Test
    public void storeLifecycleSingletonNoAutoSave() {
        stores = JsonStores.builder(jsonService, storageService).build();
        assertStoreLifecycle(true);
    }

    @SuppressWarnings("unchecked")
    private void assertStoreLifecycle(boolean isSingleton) {

        // nothing there
        AbstractJsonStore<?, ?> store = isSingleton ? stores.resolveSingleton(UID_SINGLETON) : stores.resolve(UID);
        Assert.assertNull(store);
        Assert.assertEquals(0, stores.computeMetrics().getMetrics().size());
        assertLoadInteractions(0);
        assertSaveInteractions(0);

        // ensure
        store = isSingleton ? stores.ensureSingleton(UID_SINGLETON, 0) : stores.ensure(UID, 0);
        Assert.assertNotNull(store);
        Assert.assertEquals(1, stores.computeMetrics().getMetrics().size());
        stores.load();
        assertLoadInteractions(1);
        assertSaveInteractions(0);

        // duplicate ensure
        store = isSingleton ? stores.ensureSingleton(UID_SINGLETON, 0) : stores.ensure(UID, 0);
        Assert.assertNotNull(store);
        Assert.assertEquals(1, stores.computeMetrics().getMetrics().size());
        assertLoadInteractions(1);
        assertSaveInteractions(0);

        // resolve
        store = isSingleton ? stores.resolveSingleton(UID_SINGLETON) : stores.resolve(UID);
        Assert.assertNotNull(store);
        assertLoadInteractions(1);
        assertSaveInteractions(0);

        // set data
        if (isSingleton) {
            ((JsonSingletonStore<String>) store).set(testData);
        } else {
            ((JsonStore<String>) store).add(testData);
        }
        stores.save();
        assertLoadInteractions(1);
        assertSaveInteractions(1);

        // drop
        AbstractJsonStore<?, ?> droppedStore = isSingleton ? stores.dropSingleton(UID_SINGLETON) : stores.drop(UID);
        Assert.assertEquals(store, droppedStore);
        store = isSingleton ? stores.resolveSingleton(UID_SINGLETON) : stores.resolve(UID);
        Assert.assertNull(store);
        Assert.assertEquals(0, stores.computeMetrics().getMetrics().size());
        assertLoadInteractions(1);
        assertSaveInteractions(1);
    }

    private void assertLoadInteractions(int times) {
        Mockito.verify(jsonService, Mockito.times(times)).fromJson(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(storageService, Mockito.times(times)).read(Mockito.any());
    }

    private void assertSaveInteractions(int times) {
        Mockito.verify(jsonService, Mockito.times(times)).toJson(Mockito.any());
        Mockito.verify(storageService, Mockito.times(times)).write(Mockito.any(), Mockito.any());
    }

    @Test
    public void mixedMetrics() {
        stores.ensure(UID, 0);
        stores.ensureSingleton(UID_SINGLETON, 0);
        Assert.assertEquals(2, stores.computeMetrics().getMetrics().size());
    }
}
