package de.chrgroth.jsonstore.json.flexjson;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

// TODO type handler
// TODO path handler
// TODO consumer

public class FlexjsonServiceTest {

    private static final String UID = "common";

    private FlexjsonService flexjsonService;

    private JsonStoreMetadata<List<FlexjsonTestDataParent>> metadata;

    @Before
    public void init() {

        // build service
        flexjsonService = FlexjsonService.builder().build();

        // create test metadata
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(UID);
    }

    @Test
    public void fromJsonNull() {
        flexjsonService.fromJson(metadata, null, null, null);
        Assert.assertNull(metadata.getPayload());
    }

    @Test
    public void fromJsonEmpty() {
        flexjsonService.fromJson(metadata, null, "", null);
        Assert.assertNull(metadata.getPayload());
    }

    @Test
    public void fromJsonWhitespace() {
        flexjsonService.fromJson(metadata, null, "  ", null);
        Assert.assertNull(metadata.getPayload());
    }
}
