package de.chrgroth.jsonstore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AbstractJsonStoreTest {

    private AbstractJsonStore<?, ?> store;

    @Mock
    private JsonService jsonService;

    @Mock
    private StorageService storageService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        store = new JsonStore<>(jsonService, storageService, "uid", 0, false);
    }

    @Test(expected = JsonStoreException.class)
    public void nullJsonService() {
        store = new JsonStore<>(null, storageService, "uid", 0, false);
    }

    @Test(expected = JsonStoreException.class)
    public void nullStrageService() {
        store = new JsonStore<>(jsonService, null, "uid", 0, false);
    }

    @Test
    public void uid() {
        Assert.assertEquals("uid", store.getUid());
    }
}
