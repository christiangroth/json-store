package de.chrgroth.jsonstore;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.store.AbstractJsonStore;
import de.chrgroth.jsonstore.store.model.TestDataVersion1;

public class JsonStoresFilenameTest {

    private File tempDir;

    private JsonStores stores;

    @Before
    public void init() {
        tempDir = Files.createTempDir();
        stores = JsonStores.builder().storage(tempDir, StandardCharsets.UTF_8, true, true, false).build();
    }

    @Test
    public void storeNoQualifier() {
        AbstractJsonStore<?, ?> store = ensure(false, null);
        Assert.assertEquals("storage." + TestDataVersion1.class.getName() + ".json", store.getFile().getName());
    }

    @Test
    public void storeWithQualifier() {
        AbstractJsonStore<?, ?> store = ensure(false, "archive");
        Assert.assertEquals("storage." + TestDataVersion1.class.getName() + ".archive.json", store.getFile().getName());
    }

    @Test
    public void storeBoth() {
        AbstractJsonStore<?, ?> store = ensure(false, null);
        AbstractJsonStore<?, ?> storeArchive = ensure(false, "archive");
        Assert.assertEquals(2, stores.computeMetrics().getMetrics().size());
        Assert.assertEquals("storage." + TestDataVersion1.class.getName() + ".json", store.getFile().getName());
        Assert.assertEquals("storage." + TestDataVersion1.class.getName() + ".archive.json", storeArchive.getFile().getName());
    }

    @Test
    public void singletonNoQualifier() {
        AbstractJsonStore<?, ?> store = ensure(true, null);
        Assert.assertEquals("storage.singleton." + TestDataVersion1.class.getName() + ".json", store.getFile().getName());
    }

    @Test
    public void singletonWithQualifier() {
        AbstractJsonStore<?, ?> store = ensure(true, "archive");
        Assert.assertEquals("storage.singleton." + TestDataVersion1.class.getName() + ".archive.json", store.getFile().getName());
    }

    @Test
    public void singletonBoth() {
        AbstractJsonStore<?, ?> store = ensure(true, null);
        AbstractJsonStore<?, ?> storeArchive = ensure(true, "archive");
        Assert.assertEquals(2, stores.computeMetrics().getMetrics().size());
        Assert.assertEquals("storage.singleton." + TestDataVersion1.class.getName() + ".json", store.getFile().getName());
        Assert.assertEquals("storage.singleton." + TestDataVersion1.class.getName() + ".archive.json", storeArchive.getFile().getName());
    }

    private AbstractJsonStore<?, ?> ensure(boolean isSingleton, String optionalQualifier) {
        return isSingleton ? stores.ensureSingleton(TestDataVersion1.class, optionalQualifier, 1) : stores.ensure(TestDataVersion1.class, optionalQualifier, 1);
    }
}
