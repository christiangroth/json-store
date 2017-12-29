package de.chrgroth.jsonstore.json.flexjson;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataChild;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

public class FlexjsonServiceDeepSerializeTest {

    private static final String COMMON_UID = "common";
    private static final String SPECIAL_UID = "special";

    private FlexjsonService flexjsonService;

    private JsonStoreMetadata<List<FlexjsonTestDataParent>> commonMetadata;

    private JsonStoreMetadata<List<FlexjsonTestDataParent>> specialMetadata;

    @Before
    public void init() {

        // build service
        flexjsonService = FlexjsonService.builder().setDeepSerialize(true).setDeepSerialize(SPECIAL_UID, false).build();

        // create test data
        FlexjsonTestDataParent parent = new FlexjsonTestDataParent(1, "parent");
        parent.add(new FlexjsonTestDataChild("child"));

        // create test metadata
        commonMetadata = new JsonStoreMetadata<>();
        commonMetadata.setUid(COMMON_UID);
        commonMetadata.setPayload(Arrays.asList(parent));
        specialMetadata = new JsonStoreMetadata<>();
        specialMetadata.setUid(SPECIAL_UID);
        specialMetadata.setPayload(Arrays.asList(parent));
    }

    @Test
    public void deepSerialize() {
        flexjsonService.fromJson(commonMetadata, null, flexjsonService.toJson(commonMetadata), null);
        final List<FlexjsonTestDataParent> payload = commonMetadata.getPayload();
        Assert.assertNotNull(payload);
        Assert.assertEquals(1, payload.size());
        FlexjsonTestDataParent entity = payload.iterator().next();
        Assert.assertNotNull(entity);
        Assert.assertNotNull(entity.getChildren());
        Assert.assertEquals(1, entity.getChildren().size());
    }

    @Test
    public void nonDeepSerialize() {
        flexjsonService.fromJson(specialMetadata, null, flexjsonService.toJson(specialMetadata), null);
        final List<FlexjsonTestDataParent> payload = specialMetadata.getPayload();
        Assert.assertNotNull(payload);
        Assert.assertEquals(1, payload.size());
        FlexjsonTestDataParent entity = payload.iterator().next();
        Assert.assertNotNull(entity);
        Assert.assertNull(entity.getChildren());
    }
}
