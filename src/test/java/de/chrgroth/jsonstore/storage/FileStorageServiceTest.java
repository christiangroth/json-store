package de.chrgroth.jsonstore.storage;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import de.chrgroth.jsonstore.JsonStoreException;
import de.chrgroth.jsonstore.JsonStoreMetadata;

public class FileStorageServiceTest {

    private FileStorageService storageService;

    private File baseDir;
    private JsonStoreMetadata<?> metadata;

    @Before
    public void setup() {

        // prepare test data
        baseDir = Files.createTempDir();
        metadata = new JsonStoreMetadata<>();
        metadata.setUid("test-uid");

        // build service
        storageService = FileStorageService.builder().storage(baseDir).build();
    }

    @Test(expected = JsonStoreException.class)
    public void buildServiceNoStorage() {
        FileStorageService.builder().build();
    }

    @Test(expected = JsonStoreException.class)
    public void buildServiceNullStorage() {
        FileStorageService.builder().storage(null).build();
    }

    @Test(expected = JsonStoreException.class)
    public void buildServiceNullCharset() {
        FileStorageService.builder().storage(baseDir).charset(null).build();
    }

    @Test
    public void prepareExistentStorage() {
        Assert.assertTrue(baseDir.exists());
        storageService.prepare();
        Assert.assertTrue(baseDir.exists());
    }

    @Test
    public void prepareNonExistentStorage() {
        File baseStorage = new File(baseDir, "storageDir");
        Assert.assertFalse(baseStorage.exists());
        storageService = FileStorageService.builder().storage(baseStorage).build();
        storageService.prepare();
        Assert.assertTrue(baseStorage.exists());
    }

    @Test
    public void dataCycle() {
        assertDataCycle("some test content: äöü+#*'ß?`?-:-.,;_", false);
    }

    @Test
    public void dataCycleDifferentCharset() {
        storageService = FileStorageService.builder().storage(baseDir).charset(StandardCharsets.ISO_8859_1).build();
        assertDataCycle("some test content: äöü+#*'ß?`?-:-.,;_", true);
    }

    private void assertDataCycle(String data, boolean isIsoCharset) {
        Assert.assertEquals(0, storageService.size(metadata));
        Assert.assertNull(storageService.read(metadata));
        Assert.assertEquals(0, storageService.size(metadata));
        storageService.write(metadata, data);
        Assert.assertEquals(data.length() + 1 + (isIsoCharset ? 0 : 4), storageService.size(metadata));
        Assert.assertEquals(data, storageService.read(metadata));
        storageService.delete(metadata);
        Assert.assertEquals(0, storageService.size(metadata));
    }

    @Test
    public void resolveFile() {
        final File file = storageService.resolveFile(metadata);
        Assert.assertNotNull(file);
        Assert.assertEquals(baseDir, file.getParentFile());
        Assert.assertEquals("storage.test-uid.json", file.getName());
    }

    @Test
    public void resolveFileSingleton() {
        metadata.setSingleton(true);
        final File file = storageService.resolveFile(metadata);
        Assert.assertNotNull(file);
        Assert.assertEquals(baseDir, file.getParentFile());
        Assert.assertEquals("storage.singleton.test-uid.json", file.getName());
    }
}
