package de.chrgroth.jsonstore;

import org.junit.Assert;
import org.junit.Test;

public class JsonStoreUtilsTest {

    @Test(expected = JsonStoreException.class)
    public void uidNullPayloadClass() {
        JsonStoreUtils.buildStoreUid(null, null);
    }

    @Test
    public void uidNullQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildStoreUid(JsonStoreUtilsTest.class, null));
    }

    @Test
    public void uidEmptyQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildStoreUid(JsonStoreUtilsTest.class, ""));
    }

    @Test
    public void uidWhitespaceQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildStoreUid(JsonStoreUtilsTest.class, "  "));
    }

    @Test
    public void uidQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName() + ".qualifier", JsonStoreUtils.buildStoreUid(JsonStoreUtilsTest.class, "qualifier"));
    }

    @Test
    public void uidQualifierContainsWhitespace() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName() + ".qualifier", JsonStoreUtils.buildStoreUid(JsonStoreUtilsTest.class, "quali fier"));
    }
}
