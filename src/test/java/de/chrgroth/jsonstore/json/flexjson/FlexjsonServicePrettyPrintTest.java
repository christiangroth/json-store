package de.chrgroth.jsonstore.json.flexjson;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

public class FlexjsonServicePrettyPrintTest {

    private static final String COMMON_UID = "common";
    private static final String SPECIAL_UID = "special";

    private FlexjsonService flexjsonService;

    private JsonStoreMetadata<Set<FlexjsonTestDataParent>> commonMetadata;

    private JsonStoreMetadata<Set<FlexjsonTestDataParent>> specialMetadata;

    @Before
    public void init() {

        // build service
        flexjsonService = FlexjsonService.builder().setPrettyPrint(true).setPrettyPrint(SPECIAL_UID, false).build();

        // create test metadata
        commonMetadata = new JsonStoreMetadata<>();
        commonMetadata.setUid(COMMON_UID);
        commonMetadata.setPayload(Sets.newHashSet(new FlexjsonTestDataParent(1, "one")));
        specialMetadata = new JsonStoreMetadata<>();
        specialMetadata.setUid(SPECIAL_UID);
        specialMetadata.setPayload(Sets.newHashSet(new FlexjsonTestDataParent(2, "two")));
    }

    @Test
    public void prettyPrint() {
        String json = flexjsonService.toJson(commonMetadata);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("\n"));
    }

    @Test
    public void nonPrettyPrint() {
        String json = flexjsonService.toJson(specialMetadata);
        Assert.assertNotNull(json);
        Assert.assertFalse(json.contains("\n"));
    }
}
