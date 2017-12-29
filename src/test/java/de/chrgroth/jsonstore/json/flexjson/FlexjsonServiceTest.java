package de.chrgroth.jsonstore.json.flexjson;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

public class FlexjsonServiceTest {

    private static final String UID = "common";

    private FlexjsonService flexjsonService;

    private AtomicInteger consumerCalled;
    private JsonStoreMetadata<List<FlexjsonTestDataParent>> metadata;

    @Before
    public void init() {

        // build service
        flexjsonService = FlexjsonService.builder().build();

        // create test metadata
        consumerCalled = new AtomicInteger(0);
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(UID);
        metadata.setPayload(Arrays.asList(new FlexjsonTestDataParent(1, "one")));
    }

    @Test
    public void fromJsonNull() {
        metadata.setPayload(null);
        flexjsonService.fromJson(metadata, null, null, migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void fromJsonEmpty() {
        metadata.setPayload(null);
        flexjsonService.fromJson(metadata, null, "", migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void fromJsonWhitespace() {
        metadata.setPayload(null);
        flexjsonService.fromJson(metadata, null, "  ", migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void successCallback() {
        flexjsonService.fromJson(metadata, null, flexjsonService.toJson(metadata), migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertEquals(1, consumerCalled.intValue());
    }
}
