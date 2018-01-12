package de.chrgroth.jsonstore.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.chrgroth.jsonstore.JsonService;
import de.chrgroth.jsonstore.JsonStoreMetadata;
import de.chrgroth.jsonstore.json.model.AllFieldsTestEntity;
import de.chrgroth.jsonstore.json.model.ReferencedChildEntity;

public abstract class AbstractJsonServiceTest {

    protected static final String MAP_KEY_ONE = "foo";
    protected static final String MAP_KEY_TWO = "bar";

    protected static final String ENTITY_ONE_VALUE = "one";
    protected static final String ENTITY_TWO_VALUE = "two";

    protected static final float DELTA_FLOAT = 0.0f;
    protected static final double DELTA_DOUBLE = 0.0;

    protected Date dateOne;
    protected Date dateTwo;

    protected LocalDateTime ldtOne;
    protected LocalDateTime ldtTwo;

    protected ReferencedChildEntity refOne;
    protected ReferencedChildEntity refTwo;

    private AtomicInteger consumerCalled;
    protected JsonStoreMetadata<AllFieldsTestEntity> metadata;

    protected JsonService jsonService;

    @Before
    public void init() throws ParseException {

        // prepare consumer
        consumerCalled = new AtomicInteger(0);

        // create target metadata
        metadata = new JsonStoreMetadata<>();
        metadata.setUid(AllFieldsTestEntity.class.getName());
        metadata.setPayloadTypeVersion(1);
        metadata.setSingleton(false);

        // prepare test data values
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        dateOne = dateFormat.parse("13.07.2013 07:13:07");
        dateTwo = dateFormat.parse("17.03.2017 13:07:13");
        ldtOne = LocalDateTime.of(2013, 7, 13, 7, 13, 7);
        ldtTwo = LocalDateTime.of(2017, 3, 17, 13, 7, 13);
        refOne = new ReferencedChildEntity(ENTITY_ONE_VALUE);
        refTwo = new ReferencedChildEntity(ENTITY_TWO_VALUE);

        // create a test instance with all data
        metadata.setPayload(createTestEntity());

        // create json service
        jsonService = createJsonService();
    }

    protected AllFieldsTestEntity createTestEntity() {
        AllFieldsTestEntity entity = new AllFieldsTestEntity();

        entity.setByt((byte) 13);
        entity.setByts(new byte[] { 7, 13 });
        entity.setBytWrapper(new Byte((byte) 13));
        entity.setBytWrappers(new Byte[] { new Byte((byte) 7), new Byte((byte) 13) });
        entity.setBytWrappersSet(asSet(new Byte((byte) 7), new Byte((byte) 13)));
        entity.setBytWrappersList(asList(new Byte((byte) 7), new Byte((byte) 13)));
        entity.setBytWrappersMap(asMap(new Byte((byte) 7), new Byte((byte) 13)));

        entity.setShrt((short) 13);
        entity.setShrts(new short[] { 7, 13 });
        entity.setShrtWrapper(new Short((byte) 13));
        entity.setShrtWrappers(new Short[] { new Short((short) 7), new Short((short) 13) });
        entity.setShrtWrappersSet(asSet(new Short((short) 7), new Short((short) 13)));
        entity.setShrtWrappersList(asList(new Short((short) 7), new Short((short) 13)));
        entity.setShrtWrappersMap(asMap(new Short((short) 7), new Short((short) 13)));

        entity.setInteger(13);
        entity.setIntegers(new int[] { 7, 13 });
        entity.setIntegerWrapper(new Integer(13));
        entity.setIntegerWrappers(new Integer[] { new Integer(7), new Integer(13) });
        entity.setIntegerWrappersSet(asSet(new Integer(7), new Integer(13)));
        entity.setIntegerWrappersList(asList(new Integer(7), new Integer(13)));
        entity.setIntegerWrappersMap(asMap(new Integer(7), new Integer(13)));

        entity.setLng(13l);
        entity.setLngs(new long[] { 7l, 13l });
        entity.setLngWrapper(new Long(13l));
        entity.setLngWrappers(new Long[] { new Long(7l), new Long(13l) });
        entity.setLngWrappersSet(asSet(new Long(7l), new Long(13l)));
        entity.setLngWrappersList(asList(new Long(7l), new Long(13l)));
        entity.setLngWrappersMap(asMap(new Long(7l), new Long(13l)));

        entity.setFlt(13.13f);
        entity.setFlts(new float[] { 7.7f, 13.13f });
        entity.setFltWrapper(new Float(13.13f));
        entity.setFltWrappers(new Float[] { new Float(7.7f), new Float(13.13f) });
        entity.setFltWrappersSet(asSet(new Float(7.7f), new Float(13.13f)));
        entity.setFltWrappersList(asList(new Float(7.7f), new Float(13.13f)));
        entity.setFltWrappersMap(asMap(new Float(7.7f), new Float(13.13f)));

        entity.setDbl(13.13);
        entity.setDbls(new double[] { 7.7, 13.13 });
        entity.setDblWrapper(new Double(13.13));
        entity.setDblWrappers(new Double[] { new Double(7.7), new Double(13.13) });
        entity.setDblWrappersSet(asSet(new Double(7.7), new Double(13.13)));
        entity.setDblWrappersList(asList(new Double(7.7), new Double(13.13)));
        entity.setDblWrappersMap(asMap(new Double(7.7), new Double(13.13)));

        entity.setBool(true);
        entity.setBools(new boolean[] { true, false });
        entity.setBoolWrapper(Boolean.TRUE);
        entity.setBoolWrappers(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        entity.setBoolWrappersSet(asSet(Boolean.TRUE, Boolean.FALSE));
        entity.setBoolWrappersList(asList(Boolean.TRUE, Boolean.FALSE));
        entity.setBoolWrappersMap(asMap(Boolean.TRUE, Boolean.FALSE));

        entity.setChr('x');
        entity.setChrs(new char[] { 'x', 'y' });
        entity.setChrWrapper(new Character('x'));
        entity.setChrWrappers(new Character[] { new Character('x'), new Character('y') });
        entity.setChrWrappersSet(asSet(new Character('x'), new Character('y')));
        entity.setChrWrappersList(asList(new Character('x'), new Character('y')));
        entity.setChrWrappersMap(asMap(new Character('x'), new Character('y')));

        entity.setStr("foo");
        entity.setStrs(new String[] { "foo", "bar" });
        entity.setStrsSet(asSet("foo", "bar"));
        entity.setStrsList(asList("foo", "bar"));
        entity.setStrsMap(asMap("foo", "bar"));

        entity.setDate(dateOne);
        entity.setDates(new Date[] { dateOne, dateTwo });
        entity.setDatesSet(asSet(dateOne, dateTwo));
        entity.setDatesList(asList(dateOne, dateTwo));
        entity.setDatesMap(asMap(dateOne, dateTwo));

        entity.setLdt(ldtOne);
        entity.setLdts(new LocalDateTime[] { ldtOne, ldtTwo });
        entity.setLdtsSet(asSet(ldtOne, ldtTwo));
        entity.setLdtsList(asList(ldtOne, ldtTwo));
        entity.setLdtsMap(asMap(ldtOne, ldtTwo));

        entity.setRef(refOne);
        entity.setRefs(new ReferencedChildEntity[] { refOne, refTwo });
        entity.setRefsSet(asSet(refOne, refTwo));
        entity.setRefsList(asList(refOne, refTwo));
        entity.setRefsMap(asMap(refOne, refTwo));

        return entity;
    }

    protected abstract JsonService createJsonService();

    @Test
    public void fromJsonNull() {
        metadata.setPayload(null);
        jsonService.fromJson(metadata, null, null, migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void fromJsonEmpty() {
        metadata.setPayload(null);
        jsonService.fromJson(metadata, null, "", migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void fromJsonWhitespace() {
        metadata.setPayload(null);
        jsonService.fromJson(metadata, null, "  ", migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertNull(metadata.getPayload());
        Assert.assertEquals(0, consumerCalled.intValue());
    }

    @Test
    public void toJsonFromJsonRoundtrip() {
        String json = jsonService.toJson(metadata);
        jsonService.fromJson(metadata, null, json, migrated -> {
            consumerCalled.incrementAndGet();
        });
        Assert.assertEquals(1, consumerCalled.intValue());
        assertTestEntity(metadata.getPayload());
    }

    protected void assertTestEntity(AllFieldsTestEntity entity) {

        Assert.assertEquals((byte) 13, entity.getByt());
        Assert.assertArrayEquals(new byte[] { 7, 13 }, entity.getByts());
        Assert.assertEquals(new Byte((byte) 13), entity.getBytWrapper());
        Assert.assertArrayEquals(new Byte[] { new Byte((byte) 7), new Byte((byte) 13) }, entity.getBytWrappers());
        Assert.assertEquals(asSet(new Byte((byte) 7), new Byte((byte) 13)), entity.getBytWrappersSet());
        Assert.assertEquals(asList(new Byte((byte) 7), new Byte((byte) 13)), entity.getBytWrappersList());
        Assert.assertEquals(asMap(new Byte((byte) 7), new Byte((byte) 13)), entity.getBytWrappersMap());

        Assert.assertEquals((short) 13, entity.getShrt());
        Assert.assertArrayEquals(new short[] { 7, 13 }, entity.getShrts());
        Assert.assertEquals(new Short((short) 13), entity.getShrtWrapper());
        Assert.assertArrayEquals(new Short[] { new Short((short) 7), new Short((short) 13) }, entity.getShrtWrappers());
        Assert.assertEquals(asSet(new Short((short) 7), new Short((short) 13)), entity.getShrtWrappersSet());
        Assert.assertEquals(asList(new Short((short) 7), new Short((short) 13)), entity.getShrtWrappersList());
        Assert.assertEquals(asMap(new Short((short) 7), new Short((short) 13)), entity.getShrtWrappersMap());

        Assert.assertEquals(13, entity.getInteger());
        Assert.assertArrayEquals(new int[] { 7, 13 }, entity.getIntegers());
        Assert.assertEquals(new Integer(13), entity.getIntegerWrapper());
        Assert.assertArrayEquals(new Integer[] { new Integer(7), new Integer(13) }, entity.getIntegerWrappers());
        Assert.assertEquals(asSet(new Integer(7), new Integer(13)), entity.getIntegerWrappersSet());
        Assert.assertEquals(asList(new Integer(7), new Integer(13)), entity.getIntegerWrappersList());
        Assert.assertEquals(asMap(new Integer(7), new Integer(13)), entity.getIntegerWrappersMap());

        Assert.assertEquals(13l, entity.getLng());
        Assert.assertArrayEquals(new long[] { 7l, 13l }, entity.getLngs());
        Assert.assertEquals(new Long(13), entity.getLngWrapper());
        Assert.assertArrayEquals(new Long[] { new Long(7), new Long(13) }, entity.getLngWrappers());
        Assert.assertEquals(asSet(new Long(7), new Long(13)), entity.getLngWrappersSet());
        Assert.assertEquals(asList(new Long(7), new Long(13)), entity.getLngWrappersList());
        Assert.assertEquals(asMap(new Long(7), new Long(13)), entity.getLngWrappersMap());

        Assert.assertEquals(13.13f, entity.getFlt(), DELTA_FLOAT);
        Assert.assertArrayEquals(new float[] { 7.7f, 13.13f }, entity.getFlts(), DELTA_FLOAT);
        Assert.assertEquals(new Float(13.13f), entity.getFltWrapper());
        Assert.assertArrayEquals(new Float[] { new Float(7.7f), new Float(13.13f) }, entity.getFltWrappers());
        Assert.assertEquals(asSet(new Float(7.7f), new Float(13.13f)), entity.getFltWrappersSet());
        Assert.assertEquals(asList(new Float(7.7f), new Float(13.13f)), entity.getFltWrappersList());
        Assert.assertEquals(asMap(new Float(7.7f), new Float(13.13f)), entity.getFltWrappersMap());

        Assert.assertEquals(13.13, entity.getDbl(), DELTA_DOUBLE);
        Assert.assertArrayEquals(new double[] { 7.7, 13.13 }, entity.getDbls(), DELTA_DOUBLE);
        Assert.assertEquals(new Double(13.13), entity.getDblWrapper());
        Assert.assertArrayEquals(new Double[] { new Double(7.7), new Double(13.13) }, entity.getDblWrappers());
        Assert.assertEquals(asSet(new Double(7.7), new Double(13.13)), entity.getDblWrappersSet());
        Assert.assertEquals(asList(new Double(7.7), new Double(13.13)), entity.getDblWrappersList());
        Assert.assertEquals(asMap(new Double(7.7), new Double(13.13)), entity.getDblWrappersMap());

        Assert.assertEquals(true, entity.isBool());
        Assert.assertArrayEquals(new boolean[] { true, false }, entity.getBools());
        Assert.assertEquals(Boolean.TRUE, entity.getBoolWrapper());
        Assert.assertArrayEquals(new Boolean[] { Boolean.TRUE, Boolean.FALSE }, entity.getBoolWrappers());
        Assert.assertEquals(asSet(Boolean.TRUE, Boolean.FALSE), entity.getBoolWrappersSet());
        Assert.assertEquals(asList(Boolean.TRUE, Boolean.FALSE), entity.getBoolWrappersList());
        Assert.assertEquals(asMap(Boolean.TRUE, Boolean.FALSE), entity.getBoolWrappersMap());

        Assert.assertEquals('x', entity.getChr());
        Assert.assertArrayEquals(new char[] { 'x', 'y' }, entity.getChrs());
        Assert.assertEquals(new Character('x'), entity.getChrWrapper());
        Assert.assertArrayEquals(new Character[] { new Character('x'), new Character('y') }, entity.getChrWrappers());
        Assert.assertEquals(asSet(new Character('x'), new Character('y')), entity.getChrWrappersSet());
        Assert.assertEquals(asList(new Character('x'), new Character('y')), entity.getChrWrappersList());
        Assert.assertEquals(asMap(new Character('x'), new Character('y')), entity.getChrWrappersMap());

        Assert.assertEquals("foo", entity.getStr());
        Assert.assertArrayEquals(new String[] { "foo", "bar" }, entity.getStrs());
        Assert.assertEquals(asSet("foo", "bar"), entity.getStrsSet());
        Assert.assertEquals(asList("foo", "bar"), entity.getStrsList());
        Assert.assertEquals(asMap("foo", "bar"), entity.getStrsMap());

        Assert.assertEquals(dateOne, entity.getDate());
        Assert.assertArrayEquals(new Date[] { dateOne, dateTwo }, entity.getDates());
        Assert.assertEquals(asSet(dateOne, dateTwo), entity.getDatesSet());
        Assert.assertEquals(asList(dateOne, dateTwo), entity.getDatesList());
        Assert.assertEquals(asMap(dateOne, dateTwo), entity.getDatesMap());

        Assert.assertEquals(ldtOne, entity.getLdt());
        Assert.assertArrayEquals(new LocalDateTime[] { ldtOne, ldtTwo }, entity.getLdts());
        Assert.assertEquals(asSet(ldtOne, ldtTwo), entity.getLdtsSet());
        Assert.assertEquals(asList(ldtOne, ldtTwo), entity.getLdtsList());
        Assert.assertEquals(asMap(ldtOne, ldtTwo), entity.getLdtsMap());

        Assert.assertEquals(refOne, entity.getRef());
        Assert.assertArrayEquals(new ReferencedChildEntity[] { refOne, refTwo }, entity.getRefs());
        Assert.assertEquals(asSet(refOne, refTwo), entity.getRefsSet());
        Assert.assertEquals(asList(refOne, refTwo), entity.getRefsList());
        Assert.assertEquals(asMap(refOne, refTwo), entity.getRefsMap());
    }

    private <T> Set<T> asSet(T valueOne, T valueTwo) {
        Set<T> set = new HashSet<>();
        set.add(valueOne);
        set.add(valueTwo);
        return set;
    }

    private <T> List<T> asList(T valueOne, T valueTwo) {
        List<T> list = new ArrayList<>();
        list.add(valueOne);
        list.add(valueTwo);
        return list;
    }

    private <V> Map<String, V> asMap(V valueOne, V valueTwo) {
        return asMap(MAP_KEY_ONE, valueOne, MAP_KEY_TWO, valueTwo);
    }

    private <K, V> Map<K, V> asMap(K keyOne, V valueOne, K keyTwo, V valueTwo) {
        Map<K, V> map = new HashMap<>();
        map.put(keyOne, valueOne);
        map.put(keyTwo, valueTwo);
        return map;
    }
}
