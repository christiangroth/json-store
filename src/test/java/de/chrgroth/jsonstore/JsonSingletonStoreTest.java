package de.chrgroth.jsonstore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JsonSingletonStoreTest {
    public static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";

    private JsonSingletonStore<String> store;
    private String testDataOne;

    @Mock
    private JsonService jsonService;

    @Mock
    private StorageService storageService;

    @Before
    public void init() {

        // configure mocks
        MockitoAnnotations.initMocks(this);

        store = new JsonSingletonStore<>(jsonService, storageService, "uid1", 0, true);
        testDataOne = "test data";
    }

    @Test
    public void dataLifecycle() {
        assertDataLifecycle(true);
    }

    @Test
    public void dataLifecycleNoAutoSave() {
        store = new JsonSingletonStore<>(jsonService, storageService, "uid1", 0, false);
        assertDataLifecycle(false);
    }

    private void assertDataLifecycle(boolean autoSave) {

        // verfiy no interactions yet
        assertPersistenceInteractions(0);

        // no data
        Assert.assertNull(store.get());
        Assert.assertTrue(store.isEmpty());

        // set element
        store.set(testDataOne);
        assertPersistenceInteractions(autoSave ? 1 : 0);
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

    private void assertPersistenceInteractions(int times) {
        Mockito.verify(jsonService, Mockito.times(times)).toJson(Mockito.any());
        Mockito.verify(storageService, Mockito.times(times)).write(Mockito.any(), Mockito.any());
    }
}
