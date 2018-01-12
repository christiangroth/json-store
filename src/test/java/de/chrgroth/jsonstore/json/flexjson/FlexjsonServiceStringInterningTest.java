package de.chrgroth.jsonstore.json.flexjson;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

// just testing if nothing fails at the moment cause we can't check the string pool size
public class FlexjsonServiceStringInterningTest {

    private static final String COMMON_UID = "common";
    private static final String SPECIAL_UID = "special";

    private FlexjsonService flexjsonService;

    private JsonStoreMetadata<Set<FlexjsonTestDataParent>> commonMetadata;

    private JsonStoreMetadata<Set<FlexjsonTestDataParent>> specialMetadata;

    @Before
    public void init() {

        // build service
        flexjsonService = FlexjsonService.builder().useStringInterning(SPECIAL_UID).build();

        // create test metadata
        commonMetadata = new JsonStoreMetadata<>();
        commonMetadata.setUid(COMMON_UID);
        commonMetadata.setPayload(Sets.newHashSet(new FlexjsonTestDataParent(1, "one")));
        specialMetadata = new JsonStoreMetadata<>();
        specialMetadata.setUid(SPECIAL_UID);
        specialMetadata.setPayload(Sets.newHashSet(new FlexjsonTestDataParent(2, "two")));
    }

    @Test
    public void noStringInterning() {
        String json = flexjsonService.toJson(commonMetadata);
        Assert.assertNotNull(json);
    }

    @Test
    public void stringInterning() {
        String json = flexjsonService.toJson(specialMetadata);
        Assert.assertNotNull(json);
    }
}
