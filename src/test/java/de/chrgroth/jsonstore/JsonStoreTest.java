package de.chrgroth.jsonstore;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JsonStoreTest {

    private JsonStore<String> store;

    private String testDataOne;
    private String testDataTwo;
    private List<String> testData;

    @Mock
    private JsonService jsonService;

    @Mock
    private StorageService storageService;

    @Before
    public void init() {

        // configure mocks
        MockitoAnnotations.initMocks(this);

        store = new JsonStore<>(jsonService, storageService, "uid1", 0, true);
        testDataOne = "test data foo";
        testDataTwo = "test data bar";
        testData = Arrays.asList(testDataOne, testDataTwo);
    }

    @Test
    public void dataLifecycle() {
        assertDataLifecycle(true);
    }

    @Test
    public void dataLifecycleNoAutoSave() {
        store = new JsonStore<>(jsonService, storageService, "uid1", 0, false);
        assertDataLifecycle(false);
    }

    private void assertDataLifecycle(boolean autoSave) {

        // no interactions
        assertPersistenceInteractions(0);

        // no data
        Assert.assertTrue(store.isEmpty());
        Assert.assertTrue(store.copy().isEmpty());

        // add element
        store.add(testDataOne);
        assertPersistenceInteractions(autoSave ? 1 : 0);
        Assert.assertFalse(store.isEmpty());
        Assert.assertEquals(1, store.size());
        Assert.assertEquals(1, store.copy().size());
        Assert.assertTrue(store.contains(testDataOne));

        // add element two to copy only
        store.copy().add(testDataTwo);
        Assert.assertEquals(1, store.size());
        Assert.assertEquals(1, store.copy().size());

        // add element two
        store.add(testDataTwo);
        assertPersistenceInteractions(autoSave ? 2 : 0);
        Assert.assertEquals(2, store.size());
        Assert.assertTrue(store.containsAll(testData));

        // retain
        store.retainAll(Arrays.asList(testDataOne));
        assertPersistenceInteractions(autoSave ? 3 : 0);
        Assert.assertEquals(1, store.size());
        Assert.assertTrue(store.contains(testDataOne));

        // remove
        store.remove(testDataOne);
        assertPersistenceInteractions(autoSave ? 4 : 0);
        Assert.assertTrue(store.isEmpty());

        // remove all
        store.addAll(testData);
        assertPersistenceInteractions(autoSave ? 5 : 0);
        Assert.assertEquals(2, store.size());
        store.removeAll(testData);
        assertPersistenceInteractions(autoSave ? 6 : 0);
        Assert.assertTrue(store.isEmpty());

        // remove if
        store.addAll(testData);
        assertPersistenceInteractions(autoSave ? 7 : 0);
        Assert.assertEquals(2, store.size());
        store.removeIf(s -> testDataTwo.equals(s));
        assertPersistenceInteractions(autoSave ? 8 : 0);
        Assert.assertEquals(1, store.size());
        Assert.assertTrue(store.contains(testDataOne));

        // clear
        store.addAll(testData);
        assertPersistenceInteractions(autoSave ? 9 : 0);
        Assert.assertEquals(2, store.size());
        store.clear();
        assertPersistenceInteractions(autoSave ? 10 : 0);
        Assert.assertTrue(store.isEmpty());
    }

    private void assertPersistenceInteractions(int times) {
        Mockito.verify(jsonService, Mockito.times(times)).toJson(Mockito.any());
        Mockito.verify(storageService, Mockito.times(times)).write(Mockito.any(), Mockito.any());
    }
}
