package de.chrgroth.jsonstore;

import org.junit.Assert;
import org.junit.Test;

public class JsonStoreUtilsTest {

    @Test(expected = JsonStoreException.class)
    public void uidNullPayloadClass() {
        JsonStoreUtils.buildUid(null, null);
    }

    @Test
    public void uidNullQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildUid(JsonStoreUtilsTest.class, null));
    }

    @Test
    public void uidEmptyQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildUid(JsonStoreUtilsTest.class, ""));
    }

    @Test
    public void uidWhitespaceQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName(), JsonStoreUtils.buildUid(JsonStoreUtilsTest.class, "  "));
    }

    @Test
    public void uidQualifier() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName() + ".qualifier", JsonStoreUtils.buildUid(JsonStoreUtilsTest.class, "qualifier"));
    }

    @Test
    public void uidQualifierContainsWhitespace() {
        Assert.assertEquals(JsonStoreUtilsTest.class.getName() + ".qualifier", JsonStoreUtils.buildUid(JsonStoreUtilsTest.class, "quali fier"));
    }
}
