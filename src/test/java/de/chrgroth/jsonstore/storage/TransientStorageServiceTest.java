package de.chrgroth.jsonstore.storage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonStoreMetadata;

public class TransientStorageServiceTest {

    private TransientStorageService storageService;

    private JsonStoreMetadata<?> metadata;

    @Before
    public void setup() {

        // prepare test data
        metadata = new JsonStoreMetadata<>();
        metadata.setUid("test-uid");

        // build service
        storageService = new TransientStorageService();
    }

    @Test
    public void prepareNoAction() {
        storageService.prepare();
    }

    @Test
    public void dataCycle() {
        Assert.assertEquals(0, storageService.size(metadata));
        Assert.assertNull(storageService.read(metadata));
        Assert.assertEquals(0, storageService.size(metadata));
        String data = "some test content: äöü+#*'ß?`?-:-.,;_";
        storageService.write(metadata, data);
        Assert.assertEquals(0, storageService.size(metadata));
        Assert.assertNull(storageService.read(metadata));
        storageService.delete(metadata);
        Assert.assertEquals(0, storageService.size(metadata));
    }
}
