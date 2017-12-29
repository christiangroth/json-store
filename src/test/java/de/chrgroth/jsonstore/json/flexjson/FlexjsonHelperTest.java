package de.chrgroth.jsonstore.json.flexjson;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.json.flexjson.model.FlexjsonTestDataParent;

public class FlexjsonHelperTest {
    public static final String DATE_TIME_PATTERN = "HH:mm:ss.SSS dd.MM.yyyy";

    private FlexjsonHelper flexjsonHelper;
    private Date date;
    private LocalDateTime localDateTime;
    private FlexjsonTestDataParent entity1;
    private FlexjsonTestDataParent entity2;

    @Before
    public void init() {
        flexjsonHelper = FlexjsonHelper.builder().dateTimePattern(DATE_TIME_PATTERN).build();
        date = new Date();
        localDateTime = LocalDateTime.now();
        entity1 = new FlexjsonTestDataParent();
        entity1.setDate(date);
        entity1.setDateTime(localDateTime);
        entity2 = new FlexjsonTestDataParent();
        entity2.setDate(date);
        entity2.setDateTime(localDateTime);
    }

    @Test
    public void entityData() {

        // JSON roundtrip
        String json = flexjsonHelper.serializer(false).serialize(entity1);
        FlexjsonTestDataParent deserialized = (FlexjsonTestDataParent) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertNotNull(deserialized.getDate());
        Assert.assertEquals(date.getTime(), deserialized.getDate().getTime());
        Assert.assertNotNull(deserialized.getDateTime());
        Assert.assertEquals(localDateTime, deserialized.getDateTime());
    }

    @Test
    public void entityWithNullValues() {

        // edit to null
        entity1.setDate(null);
        entity1.setDateTime(null);

        // JSON roundtrip
        String json = flexjsonHelper.serializer(false).serialize(entity1);
        FlexjsonTestDataParent deserialized = (FlexjsonTestDataParent) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertNull(deserialized.getDate());
        Assert.assertNull(deserialized.getDateTime());
    }

    @Test
    public void booleanData() {

        // JSON roundtrip
        Boolean data = Boolean.TRUE;
        String json = flexjsonHelper.serializer(false).serialize(data);
        Boolean deserialized = (Boolean) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void characterData() {

        // JSON roundtrip
        Character data = new Character('-');
        String json = flexjsonHelper.serializer(false).serialize(data);
        // Character is handled as String internally
        String deserialized = (String) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data.toString(), deserialized);
    }

    @Test
    public void integerData() {

        // JSON roundtrip
        Integer data = new Integer(13);
        String json = flexjsonHelper.serializer(false).serialize(data);
        // integer will be handled as long internally
        Long deserialized = (Long) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(new Long(data), deserialized);
    }

    @Test
    public void floatData() {

        // JSON roundtrip
        Float data = new Float(1.23);
        String json = flexjsonHelper.serializer(false).serialize(data);
        // float will be handled as double internally
        Double deserialized = (Double) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(new Double(data), deserialized, 0.0000001);
    }

    @Test
    public void doubleData() {

        // JSON roundtrip
        Double data = new Double(1.23);
        String json = flexjsonHelper.serializer(false).serialize(data);
        Double deserialized = (Double) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void stringData() {

        // JSON roundtrip
        String data = "foo-bar";
        String json = flexjsonHelper.serializer(false).serialize(data);
        String deserialized = (String) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void doubleSet() {

        // JSON roundtrip
        Set<Double> data = new HashSet<>();
        data.add(new Double(1.23));
        data.add(new Double(2.733));
        data.add(new Double(-1.15));
        String json = flexjsonHelper.serializer(false).serialize(data);
        // always deserialized as list
        @SuppressWarnings("unchecked")
        List<Double> deserializedRaw = (List<Double>) flexjsonHelper.deserializer().deserialize(json);
        Set<Double> deserialized = new HashSet<>();
        deserialized.addAll(deserializedRaw);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void entitySet() {

        // JSON roundtrip
        Set<FlexjsonTestDataParent> data = new HashSet<>();
        data.add(entity1);
        data.add(entity2);
        String json = flexjsonHelper.serializer(false).serialize(data);
        // always deserialized as list
        @SuppressWarnings("unchecked")
        List<FlexjsonTestDataParent> deserializedRaw = (List<FlexjsonTestDataParent>) flexjsonHelper.deserializer().deserialize(json);
        Set<FlexjsonTestDataParent> deserialized = new HashSet<>();
        deserialized.addAll(deserializedRaw);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void stringSet() {

        // JSON roundtrip
        Set<String> data = new HashSet<>();
        data.add("foo");
        data.add("bar");
        data.add("...");
        String json = flexjsonHelper.serializer(false).serialize(data);
        // always deserialized as list
        @SuppressWarnings("unchecked")
        List<String> deserializedRaw = (List<String>) flexjsonHelper.deserializer().deserialize(json);
        Set<String> deserialized = new HashSet<>();
        deserialized.addAll(deserializedRaw);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void doubleList() {

        // JSON roundtrip
        List<Double> data = Arrays.asList(1.23d, 2.733d, -1.15d);
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        List<Double> deserialized = (List<Double>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void stringList() {

        // JSON roundtrip
        List<String> data = Arrays.asList("foo", "bar", "...");
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        List<String> deserialized = (List<String>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void entityList() {

        // JSON roundtrip
        List<FlexjsonTestDataParent> data = Arrays.asList(entity1, entity2);
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        List<FlexjsonTestDataParent> deserialized = (List<FlexjsonTestDataParent>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test(expected = ClassCastException.class)
    public void integerToStringMap() {

        // JSON roundtrip
        Map<Integer, String> data = new HashMap<>();
        data.put(1, "foo");
        data.put(2, "bar");
        data.put(3, "...");
        String json = flexjsonHelper.serializer(false).serialize(data);
        // map key is String always
        @SuppressWarnings("unchecked")
        Map<?, String> deserialized = (Map<?, String>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data.size(), deserialized.size());
        Assert.assertEquals(data.get(1), deserialized.get("1"));
        Assert.assertEquals(data.get(2), deserialized.get("2"));
        Assert.assertEquals(data.get(3), deserialized.get("3"));
        @SuppressWarnings("unused")
        Integer key = (Integer) deserialized.entrySet().iterator().next().getKey();
    }

    @Test
    public void stringToStringMap() {

        // JSON roundtrip
        Map<String, String> data = new HashMap<>();
        data.put("1", "foo");
        data.put("2", "bar");
        data.put("3", "...");
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        Map<String, String> deserialized = (Map<String, String>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test
    public void stringToEntityMap() {

        // JSON roundtrip
        Map<String, FlexjsonTestDataParent> data = new HashMap<>();
        data.put("1", entity1);
        data.put("2", entity2);
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        Map<String, FlexjsonTestDataParent> deserialized = (Map<String, FlexjsonTestDataParent>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data, deserialized);
    }

    @Test(expected = ClassCastException.class)
    public void entityToStringMap() {

        // JSON roundtrip
        Map<FlexjsonTestDataParent, String> data = new HashMap<>();
        data.put(entity1, "1");
        data.put(entity2, "2");
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        Map<?, String> deserialized = (Map<?, String>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data.size(), deserialized.size());
        Assert.assertEquals(data.get(entity1), deserialized.get(entity1.toString()));
        Assert.assertEquals(data.get(entity2), deserialized.get(entity2.toString()));
        @SuppressWarnings("unused")
        FlexjsonTestDataParent key = (FlexjsonTestDataParent) deserialized.entrySet().iterator().next().getKey();
    }

    @Test
    public void entityToEntityMap() {

        // JSON roundtrip
        Map<FlexjsonTestDataParent, FlexjsonTestDataParent> data = new HashMap<>();
        data.put(entity1, entity1);
        data.put(entity2, entity2);
        String json = flexjsonHelper.serializer(false).serialize(data);
        @SuppressWarnings("unchecked")
        Map<?, FlexjsonTestDataParent> deserialized = (Map<?, FlexjsonTestDataParent>) flexjsonHelper.deserializer().deserialize(json);

        // assert data
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(data.size(), deserialized.size());
        Assert.assertEquals(data.get(entity1), deserialized.get(entity1.toString()));
        Assert.assertEquals(data.get(entity2), deserialized.get(entity2.toString()));
    }
}
